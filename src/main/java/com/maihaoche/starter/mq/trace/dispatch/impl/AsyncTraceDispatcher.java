package com.maihaoche.starter.mq.trace.dispatch.impl;

import com.maihaoche.starter.mq.trace.common.OnsTraceConstants;
import com.maihaoche.starter.mq.trace.dispatch.AsyncAppender;
import com.maihaoche.starter.mq.trace.dispatch.AsyncDispatcher;
import org.apache.rocketmq.client.log.ClientLogger;
import org.slf4j.Logger;

import java.io.IOException;
import java.util.Properties;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;


/**
 * 异步提交消息轨迹等数据
 */
public class AsyncTraceDispatcher extends AsyncDispatcher {
    private final static Logger clientlog = ClientLogger.getLog();
    // RingBuffer 实现，size 必须为 2 的 n 次方
    private final Object[] entries;
    private final int queueSize;
    private final int indexMask;
    private final int notifyThreshold;
    // 唤醒最大等待时间，单位ms
    private final int maxDelayTime = 20;
    private final ReentrantLock lock;
    private final Condition notEmpty;

    // 下一个写的位置，一直递增
    private AtomicLong putIndex;
    // 最近丢弃的日志条数
    private AtomicLong discardCount;
    // 下一个读的位置，一直递增，不能大于 putIndex
    private AtomicLong takeIndex;

    private AsyncAppender appender;
    private String workerName;

    private Thread worker;
    private AtomicBoolean running;


    public AsyncTraceDispatcher(Properties properties) {
        int queueSize = Integer.parseInt(properties.getProperty(OnsTraceConstants.AsyncBufferSize, "2048"));
        // queueSize 取大于或等于 value 的 2 的 n 次方数
        queueSize = 1 << (32 - Integer.numberOfLeadingZeros(queueSize - 1));
        this.queueSize = queueSize;
        this.entries = new Object[queueSize];
        this.indexMask = queueSize - 1;
        /**
         * 默认的消费者唤醒阈值，这个值需要让消费者能较持续的有事情做， 这个值设置过小，会导致生产者频繁唤起消费者；
         * 设置过大，可能导致生产者速度过快导致队列满丢日志的问题。
         */
        this.notifyThreshold = Integer.parseInt(properties.getProperty(OnsTraceConstants.WakeUpNum, "1"));
        this.putIndex = new AtomicLong(0L);
        this.discardCount = new AtomicLong(0L);
        this.takeIndex = new AtomicLong(0L);

        this.running = new AtomicBoolean(false);

        this.lock = new ReentrantLock(false);
        this.notEmpty = lock.newCondition();
    }


    @Override
    public void start(AsyncAppender appender, String workerName) {
        this.appender = appender;
        this.workerName = workerName;

        this.worker = new Thread(new AsyncRunnable(), "MQ-AsyncDispatcher-Thread-" + workerName);
        this.worker.setDaemon(true);
        this.worker.start();
    }


    public int size() {
        return (int) (putIndex.get() - takeIndex.get());
    }


    /**
     * 队列满时直接丢弃数据，不阻塞业务线程，返回日志是否被接受
     */
    @Override
    public boolean append(Object ctx) {
        final long qsize = queueSize;

        for (;;) {
            final long put = putIndex.get();
            final long size = put - takeIndex.get();
            if (size >= qsize) {
                clientlog.info("msgtrace buffer is full,the loss count is" + discardCount.incrementAndGet() + "  " + ctx);
                return false;
            }
            if (putIndex.compareAndSet(put, put + 1)) {
                entries[(int) put & indexMask] = ctx;
                // 仅仅在队列的数据超过阈值，且消费者不在运行，且获得锁，才唤醒消费者
                // 这个做法能保证只有必要时才立即通知消费者，减少上下文切换的开销
                if (size >= notifyThreshold && !running.get() && lock.tryLock()) {
                    try {
                        notEmpty.signal();
                    }
                    catch (Exception e) {
                        clientlog.info("fail to signal notEmpty,maybe block!");
                    }
                    finally {
                        lock.unlock();
                    }
                }
                return true;
            }
        }
    }


    @Override
    public void flush() throws IOException {
        // 最多等待刷新的时间，避免数据一直在写导致无法返回
        long end = System.currentTimeMillis() + 500;
        while (size() > 0 && System.currentTimeMillis() <= end) {
            if (running.get()) {
                try {
                    Thread.sleep(1);
                }
                catch (InterruptedException e) {
                    break;
                }
            }
            else {
                if (lock.tryLock()) {
                    try {
                        notEmpty.signal();
                    }
                    catch (Exception e) {
                        clientlog.info("fail to signal notEmpty,maybe block!");
                    }
                    finally {
                        lock.unlock();
                    }
                }
            }
        }
    }

    class AsyncRunnable implements Runnable {
        public void run() {
            final AsyncTraceDispatcher parent = AsyncTraceDispatcher.this;
            final int indexMask = parent.indexMask;
            final int queueSize = parent.queueSize;
            final String workerName = parent.workerName;
            final Object[] entries = parent.entries;
            final AtomicLong putIndex = parent.putIndex;
            final AtomicLong takeIndex = parent.takeIndex;
            final AtomicLong discardCount = parent.discardCount;
            final AtomicBoolean running = parent.running;
            final ReentrantLock lock = parent.lock;
            final Condition notEmpty = parent.notEmpty;

            // 输出丢弃的数据量
            final long outputSpan = TimeUnit.MINUTES.toMillis(1);
            long lastOutputTime = System.currentTimeMillis();
            long now;

            for (;;) {
                try {
                    running.set(true);
                    long take = takeIndex.get();
                    long size = putIndex.get() - take;
                    if (size > 0) {
                        // 直接批量处理掉 size 个日志对象
                        do {
                            final int idx = (int) take & indexMask;
                            Object ctx = entries[idx];
                            // 从生产者 claim 到 putIndex 位置，到生产者把日志对象放入队列之间，有可能存在间隙
                            while (ctx == null) {
                                Thread.yield();
                                ctx = entries[idx];
                            }
                            entries[idx] = null;
                            takeIndex.set(++take); // 单个消费者，无需用 CAS
                            --size;
                            // 调用消费程序进行消费
                            parent.appender.append(ctx);
                        } while (size > 0);
                        // 集中flush
                        parent.appender.flush();
                        long discardNum = discardCount.get();
                        if (discardNum > 0 && (now = System.currentTimeMillis()) - lastOutputTime > outputSpan) {
                            discardNum = discardCount.get();
                            discardCount.lazySet(0); // 无需内存屏障，数量稍微丢失一点关系不大
                            lastOutputTime = now;
                        }
                    }
                    else {
                        if (lock.tryLock()) {
                            try {
                                running.set(false);
                                notEmpty.await(parent.maxDelayTime, TimeUnit.MILLISECONDS);
                            }
                            finally {
                                lock.unlock();
                            }
                        }
                    }
                }
                catch (InterruptedException e) {
                    clientlog.info("[WARN] " + workerName + " async thread is iterrupted");
                    break;
                }
                catch (Exception e) {
                    clientlog.info("[ERROR] Fail to async write log");
                }
            }
            running.set(false);
        }
    }
}
