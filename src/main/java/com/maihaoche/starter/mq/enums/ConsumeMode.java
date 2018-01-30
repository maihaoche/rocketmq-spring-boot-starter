package com.maihaoche.starter.mq.enums;

public enum ConsumeMode {
    /**
     * CONCURRENTLY
     * 使用线程池并发消费
     */
    CONCURRENTLY("CONCURRENTLY"),
    /**
     * ORDERLY
     * 单线程消费
     */
    ORDERLY("ORDERLY");

    private String mode;

    ConsumeMode(String mode) {
        this.mode = mode;
    }

    public String getMode() {
        return mode;
    }
}
