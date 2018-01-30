package com.maihaoche.starter.mq.enums;

public enum DelayTimeLevel {
    /**
     * 1s 5s 10s 30s 1m 2m 3m 4m 5m 6m 7m 8m 9m 10m 20m 30m 1h 2h
     */
    SECOND_1(1),
    SECOND_5(2),
    SECOND_10(3),
    SECOND_30(4),
    MINUTE_1(5),
    MINUTE_2(6),
    MINUTE_3(7),
    MINUTE_4(8),
    MINUTE_5(9),
    MINUTE_6(10),
    MINUTE_7(11),
    MINUTE_8(12),
    MINUTE_9(13),
    MINUTE_10(14),
    MINUTE_20(15),
    MINUTE_30(16),
    HOUR_1(17),
    HOUR_2(18),
    ;

    private int level;

    DelayTimeLevel(int level) {
        this.level = level;
    }

    public int getLevel() {
        return level;
    }
}
