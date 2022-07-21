package com.jdcloud.logs.util;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * 纳秒时间测试
 *
 * @author liubai
 * @date 2022/7/14
 */
public class NanoTimeGeneratorTest {

    private static volatile long maxTimestampMs = 0L;
    private static final AtomicInteger nanoCounter = new AtomicInteger(0);
    private static final int NANOS_OF_PER_MILLIS = 1000000;

    private static final long RELATIVE_START_NANOS = System.nanoTime();
    private static final long START_MILLIS = System.currentTimeMillis();
    private static final long START_NANOS = START_MILLIS * NANOS_OF_PER_MILLIS;

    public static int timestampToNanos(long timestampMs) {
        if (timestampMs < maxTimestampMs) {
            long nextMs = timestampMs % 1000L;
            return (int) nextMs * 1000 + 999;
        } else {
            int nanos;
            if (maxTimestampMs == timestampMs) {
                nanos = nanoCounter.incrementAndGet();
            } else {
                nanos = 1;
                nanoCounter.set(nanos);
            }
            maxTimestampMs = timestampMs;
            return nanos;
        }
    }

    public static void main(String[] args) {
        // 第一种方式：使用自增数字
//        for (int i = 1; i < 1000; i++) {
//            long now = System.currentTimeMillis();
//            System.out.println(now * NANOS_OF_PER_MILLIS + timestampToNanos(now));
//        }

        // 第一种方式：使用nanoTime绝对值计算
        ExecutorService executorService = Executors.newFixedThreadPool(5);
        for (int i = 0; i < 1000; i++) {
            executorService.submit(new Runnable() {
                @Override
                public void run() {
//                    long diffNanos = System.nanoTime() - RELATIVE_START_NANOS;
//                    long nowNanos = START_NANOS + diffNanos;
//                    System.out.println(Thread.currentThread() + ":" + nowNanos);
                    System.out.println(Thread.currentThread() + ":" + System.nanoTime());
                    try {
                        Thread.sleep(200);
                    } catch (InterruptedException e) {
                        //
                    }
                }
            });
        }
        executorService.shutdown();
    }
}
