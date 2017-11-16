package com.maihaoche.starter.mq.trace.common;

import java.util.List;
import java.util.UUID;


/**
 * Created by alvin on 16-3-7.
 */
public class OnsTraceContext implements Comparable<OnsTraceContext> {
    /**
     * 轨迹数据的类型，Pub,SubBefore,SubAfter
     */
    private OnsTraceType traceType;
    /**
     * 记录时间
     */
    private long timeStamp = System.currentTimeMillis();
    /**
     * Region信息
     */
    private String regionId = "";
    /**
     * 发送组或者消费组名
     */
    private String groupName = "";
    /**
     * 耗时，单位ms
     */
    private int costTime = 0;
    /**
     * 消费状态，成功与否
     */
    private boolean isSuccess = true;
    /**
     * UUID,用于匹配消费前和消费后的数据
     */
    private String requestId = UUID.randomUUID().toString().replaceAll("-", "");
    /**
     * 针对每条消息的轨迹数据
     */
    private List<OnsTraceBean> traceBeans;


    public List<OnsTraceBean> getTraceBeans() {
        return traceBeans;
    }


    public void setTraceBeans(List<OnsTraceBean> traceBeans) {
        this.traceBeans = traceBeans;
    }


    public String getRegionId() {
        return regionId;
    }


    public void setRegionId(String regionId) {
        this.regionId = regionId;
    }


    public OnsTraceType getTraceType() {
        return traceType;
    }


    public void setTraceType(OnsTraceType traceType) {
        this.traceType = traceType;
    }


    public long getTimeStamp() {
        return timeStamp;
    }


    public void setTimeStamp(long timeStamp) {
        this.timeStamp = timeStamp;
    }


    public String getGroupName() {
        return groupName;
    }


    public void setGroupName(String groupName) {
        this.groupName = groupName;
    }


    public int getCostTime() {
        return costTime;
    }


    public void setCostTime(int costTime) {
        this.costTime = costTime;
    }


    public boolean isSuccess() {
        return isSuccess;
    }


    public void setSuccess(boolean success) {
        isSuccess = success;
    }


    public String getRequestId() {
        return requestId;
    }


    public void setRequestId(String requestId) {
        this.requestId = requestId;
    }


    @Override
    public int compareTo(OnsTraceContext o) {
        return (int) (this.timeStamp - o.getTimeStamp());
    }


    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder(1024);
        if (traceBeans != null && traceBeans.size() > 0) {
            for (OnsTraceBean bean : traceBeans) {
                sb.append(bean.getMsgId() + "_");
            }
        }
        return "OnsTraceContext{" + "traceBeans=" + sb.toString() + '}';
    }
}
