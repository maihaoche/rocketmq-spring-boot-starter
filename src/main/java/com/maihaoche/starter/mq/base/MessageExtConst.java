package com.maihaoche.starter.mq.base;

/**
 * Created by suclogger on 2017/11/8.
 */
public class MessageExtConst {

    /**
     * 消息模式 集群或者广播
     */
    public static final String MESSAGE_MODE_CLUSTERING = "CLUSTERING";
    public static final String MESSAGE_MODE_BROADCASTING = "BROADCASTING";

    /**
     * 消费模式 有序（单线程）或者无序（多线程）
     */
    public static final String CONSUME_MODE_CONCURRENTLY = "CONCURRENTLY";
    public static final String CONSUME_MODE_ORDERLY = "ORDERLY";

    public static final String PROPERTY_TOPIC = "TOPIC";

    /**
     * 来自 MessageExt
     */
    public static final String PROPERTY_EXT_QUEUE_ID = "QUEUE_ID";
    public static final String PROPERTY_EXT_STORE_SIZE = "STORE_SIZE";
    public static final String PROPERTY_EXT_QUEUE_OFFSET = "QUEUE_OFFSET";
    public static final String PROPERTY_EXT_SYS_FLAG = "SYS_FLAG";
    public static final String PROPERTY_EXT_BORN_TIMESTAMP = "BORN_TIMESTAMP";
    public static final String PROPERTY_EXT_BORN_HOST = "BORN_HOST";
    public static final String PROPERTY_EXT_STORE_TIMESTAMP = "STORE_TIMESTAMP";
    public static final String PROPERTY_EXT_STORE_HOST = "STORE_HOST";
    public static final String PROPERTY_EXT_MSG_ID = "MSG_ID";
    public static final String PROPERTY_EXT_COMMIT_LOG_OFFSET = "COMMIT_LOG_OFFSET";
    public static final String PROPERTY_EXT_RECONSUME_TIMES = "RECONSUME_TIMES";
    public static final String PROPERTY_EXT_PREPARED_TRANSACTION_OFFSET = "PREPARED_TRANSACTION_OFFSET";
    public static final String PROPERTY_EXT_BODY_CRC = "BODY_CRC";

    /**
     *
     * 以下属性来自 Message.property
     *
     */
    public static final String PROPERTY_KEYS = "KEYS";
    public static final String PROPERTY_TAGS = "TAGS";
    public static final String PROPERTY_WAIT_STORE_MSG_OK = "WAIT";
    public static final String PROPERTY_DELAY_TIME_LEVEL = "DELAY";
    public static final String PROPERTY_RETRY_TOPIC = "RETRY_TOPIC";
    public static final String PROPERTY_REAL_TOPIC = "REAL_TOPIC";
    public static final String PROPERTY_REAL_QUEUE_ID = "REAL_QID";
    public static final String PROPERTY_TRANSACTION_PREPARED = "TRAN_MSG";
    public static final String PROPERTY_PRODUCER_GROUP = "PGROUP";
    public static final String PROPERTY_MIN_OFFSET = "MIN_OFFSET";
    public static final String PROPERTY_MAX_OFFSET = "MAX_OFFSET";
    public static final String PROPERTY_BUYER_ID = "BUYER_ID";
    public static final String PROPERTY_ORIGIN_MESSAGE_ID = "ORIGIN_MESSAGE_ID";
    public static final String PROPERTY_TRANSFER_FLAG = "TRANSFER_FLAG";
    public static final String PROPERTY_CORRECTION_FLAG = "CORRECTION_FLAG";
    public static final String PROPERTY_MQ2_FLAG = "MQ2_FLAG";
    public static final String PROPERTY_RECONSUME_TIME = "RECONSUME_TIME";
    public static final String PROPERTY_MSG_REGION = "MSG_REGION";
    public static final String PROPERTY_TRACE_SWITCH = "TRACE_ON";
    public static final String PROPERTY_UNIQ_CLIENT_MESSAGE_ID_KEYIDX = "UNIQ_KEY";
    public static final String PROPERTY_MAX_RECONSUME_TIMES = "MAX_RECONSUME_TIMES";
    public static final String PROPERTY_CONSUME_START_TIMESTAMP = "CONSUME_START_TIME";

}
