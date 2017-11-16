package com.maihaoche.starter.mq.trace.common;


/**
 * Created by alvin on 16-3-8.
 */
public class OnsTraceConstants {
    // 外部直传Nameserver的地址
    public static String NAMESRV_ADDR = "NAMESRV_ADDR";
    // 外部传入地址服务器的Url，获取NameServer地址
    public static String ADDRSRV_URL = "ADDRSRV_URL";
    // 实例名称
    public static final String InstanceName = "InstanceName";
    // 缓冲区队列大小
    public static final String AsyncBufferSize = "AsyncBufferSize";
    // 最大Batch
    public static final String MaxBatchNum = "MaxBatchNum";

    public static final String WakeUpNum ="WakeUpNum";
    // Batch消息最大大小
    public static final String MaxMsgSize = "MaxMsgSize";

    // producer名称
    public static final String groupName = "_INNER_TRACE_PRODUCER";
    // topic
    public static final String traceTopic = "MQ_TRACE_DATA";

    public static char CONTENT_SPLITOR = (char) 1;
    public static char FIELD_SPLITOR = (char) 2;
}
