package com.maihaoche.starter.mq.trace.common;


import org.apache.rocketmq.common.message.MessageType;

import java.util.ArrayList;
import java.util.List;


/**
 * Created by alvin on 16-3-10.
 */
public class OnsTraceDataEncoder {
    /**
     * 从轨迹数据字符串中解析出traceContext列表
     * 
     * @param traceData trace数据
     * @return trace列表
     */
    public static List<OnsTraceContext> decoderFromTraceDataString(String traceData) {
        List<OnsTraceContext> resList = new ArrayList<OnsTraceContext>();
        if (traceData == null || traceData.length() <= 0) {
            return resList;
        }
        String[] contextList = traceData.split(String.valueOf(OnsTraceConstants.FIELD_SPLITOR));
        for (String context : contextList) {
            String[] line = context.split(String.valueOf(OnsTraceConstants.CONTENT_SPLITOR));
            if (line[0].equals(OnsTraceType.Pub.name())) {
                OnsTraceContext pubContext = new OnsTraceContext();
                pubContext.setTraceType(OnsTraceType.Pub);
                pubContext.setTimeStamp(Long.parseLong(line[1]));
                pubContext.setRegionId(line[2]);
                pubContext.setGroupName(line[3]);
                OnsTraceBean bean = new OnsTraceBean();
                bean.setTopic(line[4]);
                bean.setMsgId(line[5]);
                bean.setTags(line[6]);
                bean.setKeys(line[7]);
                bean.setStoreHost(line[8]);
                bean.setBodyLength(Integer.parseInt(line[9]));
                pubContext.setCostTime(Integer.parseInt(line[10]));
                bean.setMsgType(MessageType.values()[Integer.parseInt(line[11])]);
                // 兼容某个版本的snapshot，此处没有offsetid的情况
                if (line.length == 13) {
                    pubContext.setSuccess(Boolean.parseBoolean(line[12]));
                }
                else if (line.length == 14) {
                    bean.setOffsetMsgId(line[12]);
                    pubContext.setSuccess(Boolean.parseBoolean(line[13]));
                }
                pubContext.setTraceBeans(new ArrayList<OnsTraceBean>(1));
                pubContext.getTraceBeans().add(bean);
                resList.add(pubContext);
            }
            else if (line[0].equals(OnsTraceType.SubBefore.name())) {
                OnsTraceContext subBeforeContext = new OnsTraceContext();
                subBeforeContext.setTraceType(OnsTraceType.SubBefore);
                subBeforeContext.setTimeStamp(Long.parseLong(line[1]));
                subBeforeContext.setRegionId(line[2]);
                subBeforeContext.setGroupName(line[3]);
                subBeforeContext.setRequestId(line[4]);
                OnsTraceBean bean = new OnsTraceBean();
                bean.setMsgId(line[5]);
                bean.setRetryTimes(Integer.parseInt(line[6]));
                bean.setKeys(line[7]);
                subBeforeContext.setTraceBeans(new ArrayList<OnsTraceBean>(1));
                subBeforeContext.getTraceBeans().add(bean);
                resList.add(subBeforeContext);
            }
            else if (line[0].equals(OnsTraceType.SubAfter.name())) {
                OnsTraceContext subAfterContext = new OnsTraceContext();
                subAfterContext.setTraceType(OnsTraceType.SubAfter);
                subAfterContext.setRequestId(line[1]);
                OnsTraceBean bean = new OnsTraceBean();
                bean.setMsgId(line[2]);
                bean.setKeys(line[5]);
                subAfterContext.setTraceBeans(new ArrayList<OnsTraceBean>(1));
                subAfterContext.getTraceBeans().add(bean);
                subAfterContext.setCostTime(Integer.parseInt(line[3]));
                subAfterContext.setSuccess(Boolean.parseBoolean(line[4]));
                resList.add(subAfterContext);
            }
        }
        return resList;
    }


    /**
     * 将轨迹上下文编码成轨迹数据字符串以及keyset集合
     * 
     * @param ctx 上下文
     * @return trace bean
     */
    public static OnsTraceTransferBean encoderFromContextBean(OnsTraceContext ctx) {
        if (ctx == null) {
            return null;
        }
        OnsTraceTransferBean transferBean = new OnsTraceTransferBean();
        StringBuilder sb = new StringBuilder(256);
        switch (ctx.getTraceType()) {
//        case Pub: {
//            OnsTraceBean bean = ctx.getTraceBeans().get(0);
//            sb.append(ctx.getTraceType()).append(OnsTraceConstants.CONTENT_SPLITOR)//
//                .append(ctx.getTimeStamp()).append(OnsTraceConstants.CONTENT_SPLITOR)//
//                .append(ctx.getRegionId()).append(OnsTraceConstants.CONTENT_SPLITOR)//
//                .append(ctx.getGroupName()).append(OnsTraceConstants.CONTENT_SPLITOR)//
//                .append(bean.getTopic()).append(OnsTraceConstants.CONTENT_SPLITOR)//
//                .append(bean.getMsgId()).append(OnsTraceConstants.CONTENT_SPLITOR)//
//                .append(bean.getTags()).append(OnsTraceConstants.CONTENT_SPLITOR)//
//                .append(bean.getKeys()).append(OnsTraceConstants.CONTENT_SPLITOR)//
//                .append(bean.getStoreHost()).append(OnsTraceConstants.CONTENT_SPLITOR)//
//                .append(bean.getBodyLength()).append(OnsTraceConstants.CONTENT_SPLITOR)//
//                .append(ctx.getCostTime()).append(OnsTraceConstants.CONTENT_SPLITOR)//
//                .append(bean.getMsgType().ordinal()).append(OnsTraceConstants.CONTENT_SPLITOR)//
//                .append(bean.getOffsetMsgId()).append(OnsTraceConstants.CONTENT_SPLITOR)//
//                .append(ctx.isSuccess()).append(OnsTraceConstants.FIELD_SPLITOR);
//        }
//            break;
        case SubBefore: {
            for (OnsTraceBean bean : ctx.getTraceBeans()) {
                sb.append(ctx.getTraceType()).append(OnsTraceConstants.CONTENT_SPLITOR)//
                    .append(ctx.getTimeStamp()).append(OnsTraceConstants.CONTENT_SPLITOR)//
                    .append(ctx.getRegionId()).append(OnsTraceConstants.CONTENT_SPLITOR)//
                    .append(ctx.getGroupName()).append(OnsTraceConstants.CONTENT_SPLITOR)//
                    .append(ctx.getRequestId()).append(OnsTraceConstants.CONTENT_SPLITOR)//
                    .append(bean.getMsgId()).append(OnsTraceConstants.CONTENT_SPLITOR)//
                    .append(bean.getRetryTimes()).append(OnsTraceConstants.CONTENT_SPLITOR)//
                    .append(bean.getKeys()).append(OnsTraceConstants.FIELD_SPLITOR);//
            }
        }
            break;
        case SubAfter: {
            for (OnsTraceBean bean : ctx.getTraceBeans()) {
                sb.append(ctx.getTraceType()).append(OnsTraceConstants.CONTENT_SPLITOR)//
                    // .append(ctx.getTimeStamp()).append(OnsTraceConstants.CONTENT_SPLITOR)//
                    .append(ctx.getRequestId()).append(OnsTraceConstants.CONTENT_SPLITOR)//
                    .append(bean.getMsgId()).append(OnsTraceConstants.CONTENT_SPLITOR)//
                    .append(ctx.getCostTime()).append(OnsTraceConstants.CONTENT_SPLITOR)//
                    .append(ctx.isSuccess()).append(OnsTraceConstants.CONTENT_SPLITOR)//
                    .append(bean.getKeys()).append(OnsTraceConstants.FIELD_SPLITOR);
            }
        }
            break;
        default:
        }
        transferBean.setTransData(sb.toString());
        for (OnsTraceBean bean : ctx.getTraceBeans()) {
            transferBean.getTransKey().add(bean.getMsgId());
            if (bean.getKeys() != null && bean.getKeys().length() > 0) {
                transferBean.getTransKey().add(bean.getKeys());
            }
        }
        return transferBean;
    }
}
