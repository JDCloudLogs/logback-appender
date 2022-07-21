package com.jdcloud.logs.util;

/**
 * 纳秒时间生成器
 *
 * @author liubai
 * @date 2022/7/14
 */
public class NanoTimeGenerator {

    private static final int NANOS_OF_PER_MILLIS = 1000000;
    private static final long RELATIVE_START_NANOS = System.nanoTime();
    private static final long START_NANOS = System.currentTimeMillis() * NANOS_OF_PER_MILLIS;

    public static long currentTimeNanos() {
        long diffNanos = System.nanoTime() - RELATIVE_START_NANOS;
        return START_NANOS + diffNanos;
    }
}
