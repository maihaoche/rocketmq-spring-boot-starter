package com.maihaoche.starter.mq.trace.dispatch;

import java.io.IOException;


/**
 * 异步传输数据模块
 */
public abstract class AsyncDispatcher {
    /**
     * 初始化异步传输数据模块
     * @param appender 实际的传输模块
     * @param workName 传输模块工作线程名称
     */
    public abstract void start(AsyncAppender appender, String workName);

    /**
     * 往数据传输模块中添加信息，
     * @param ctx 数据信息
     * @return  返回true代表添加成功，返回false代表数据被抛弃
     */
    public abstract boolean append(Object ctx);

    /**
     * 强制调用写操作
     * @throws IOException
     */
    public abstract void flush() throws IOException;
}
