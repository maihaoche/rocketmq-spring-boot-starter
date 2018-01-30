package com.maihaoche.starter.mq.trace.dispatch.impl;

import com.maihaoche.starter.mq.trace.common.OnsTraceConstants;
import com.maihaoche.starter.mq.trace.common.OnsTraceContext;
import com.maihaoche.starter.mq.trace.common.OnsTraceDataEncoder;
import com.maihaoche.starter.mq.trace.common.OnsTraceTransferBean;
import com.maihaoche.starter.mq.trace.dispatch.AsyncAppender;
import org.apache.rocketmq.client.exception.MQClientException;
import org.apache.rocketmq.client.log.ClientLogger;
import org.apache.rocketmq.client.producer.DefaultMQProducer;
import org.apache.rocketmq.client.producer.SendCallback;
import org.apache.rocketmq.client.producer.SendResult;
import org.apache.rocketmq.common.message.Message;
import org.apache.rocketmq.common.namesrv.TopAddressing;
import org.slf4j.Logger;

import java.util.*;


/**
 * Created by alvin on 16-3-7.
 */
public class AsyncTraceAppender extends AsyncAppender {
    private final static Logger clientlog = ClientLogger.getLog();
    /**
     * batch大小
     */
    private final int batchSize;
    /**
     * 临时存储batch的数据
     */
    private List<OnsTraceTransferBean> transDataList;
    /**
     * 消息轨迹数据的producer
     */
    private final DefaultMQProducer traceProducer;

    /**
     * 构造消息类型的轨迹数据发送器
     *
     * @param properties 参数属性
     * @throws MQClientException 消息异常
     */
    public AsyncTraceAppender(Properties properties) throws MQClientException {
        transDataList = new ArrayList<OnsTraceTransferBean>();
        traceProducer = new DefaultMQProducer();

        this.traceProducer.setProducerGroup(OnsTraceConstants.groupName);
        traceProducer.setSendMsgTimeout(5000);
        traceProducer.setInstanceName(properties.getProperty(OnsTraceConstants.InstanceName, String.valueOf(System.currentTimeMillis())));

        String nameSrv = properties.getProperty(OnsTraceConstants.NAMESRV_ADDR);
        if (nameSrv == null) {
            TopAddressing topAddressing = new TopAddressing(properties.getProperty(OnsTraceConstants.ADDRSRV_URL));
            nameSrv = topAddressing.fetchNSAddr();
        }
        traceProducer.setNamesrvAddr(nameSrv);
        traceProducer.setVipChannelEnabled(false);
        // 消息最大大小128K
        int maxSize = Integer.parseInt(properties.getProperty(OnsTraceConstants.MaxMsgSize, "128000"));
        batchSize = Integer.parseInt(properties.getProperty(OnsTraceConstants.MaxBatchNum, "1"));
        traceProducer.setMaxMessageSize(maxSize - 10 * 1000);
        traceProducer.start();
    }


    /**
     * 往消息缓冲区编码轨迹数据
     *
     * @param context 上下文
     */
    @Override
    public void append(Object context) {
        OnsTraceContext traceContext = (OnsTraceContext) context;
        if (traceContext == null) {
            return;
        }
        OnsTraceTransferBean traceData = OnsTraceDataEncoder.encoderFromContextBean(traceContext);
        transDataList.add(traceData);
    }


    /**
     * 实际批量发送数据
     */
    @Override
    public void flush() {
        if (transDataList.size() == 0) {
            return;
        }
        // 临时缓冲区
        StringBuilder sb = new StringBuilder(1024);
        int count = 0;
        Set<String> keySet = new HashSet<String>();

        for (OnsTraceTransferBean bean : transDataList) {
            keySet.addAll(bean.getTransKey());
            sb.append(bean.getTransData());
            count++;
            // 保证包的大小不要超过上限
            if (count >= this.batchSize || sb.length() >= traceProducer.getMaxMessageSize()) {
                sendTraceDataByMQ(keySet, sb.toString());
                // 发送完成，清除临时缓冲区
                sb.delete(0, sb.length());
                keySet.clear();
                count = 0;
            }
        }
        if (count > 0) {
            sendTraceDataByMQ(keySet, sb.toString());
        }
        this.transDataList.clear();
    }


    /**
     * 发送数据的接口
     *
     * @param keySet
     *            本批次包含的keyset
     * @param data
     *            本批次的轨迹数据
     */
    public void sendTraceDataByMQ(Set<String> keySet, String data) {
        String topic = OnsTraceConstants.traceTopic;
        final Message message = new Message(topic, data.getBytes());
        message.setKeys(keySet);
        try {
            traceProducer.send(message, new SendCallback() {
                @Override
                public void onSuccess(SendResult sendResult) {
                }


                @Override
                public void onException(Throwable e) {
                    //todo 对于发送失败的数据，如何保存，保证所有轨迹数据都记录下来
                    clientlog.info("send trace data failed ,the msgidSet is"+message.getKeys());
                }
            }, 5000);
        }
        catch (Exception e) {
            clientlog.info("send trace data failed ,the msgidSet is"+message.getKeys());
        }
    }

}
