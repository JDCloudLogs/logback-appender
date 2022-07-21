package com.jdcloud.logs;

import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.core.status.Status;
import ch.qos.logback.core.status.StatusManager;
import com.jdcloud.logs.producer.config.ProducerConfig;
import org.junit.AfterClass;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;

import java.util.List;

import static org.junit.Assert.assertNotEquals;

/**
 * test
 *
 * @author liubai
 * @date 2022/7/12
 */
public class JDCloudLogbackAppenderTest {

    private static final Logger LOGGER = LoggerFactory.getLogger(JDCloudLogbackAppenderTest.class);

    @Test
    public void smokeTest() {
        LOGGER.warn("This is a test message");
    }

    @Test
    public void mdcTest() {
        MDC.put("mdcKey1", "value1");
        MDC.put("mdcKey2", "value2");
        LOGGER.warn("This is a test message");
        MDC.clear();
    }

    @Test
    public void throwableTest() {
        LOGGER.error("This is a throwable test message", new NullPointerException("Nothing is null"));
    }

    @Test
    public void appendLoop() throws InterruptedException {
        while (true) {
            LOGGER.warn("This is a test message");
            Thread.sleep(500);
        }
    }

    private static void sleep() {
        ProducerConfig producerConfig = new ProducerConfig();
        try {
            Thread.sleep(2L * producerConfig.getBatchMillis());
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    @AfterClass
    public static void checkStatusList() {
        sleep();
        LoggerContext lc = (LoggerContext) LoggerFactory.getILoggerFactory();
        StatusManager statusManager = lc.getStatusManager();
        List<Status> statusList = statusManager.getCopyOfStatusList();
        for (Status status : statusList) {
            int level = status.getLevel();
            assertNotEquals(status.getMessage(), Status.ERROR, level);
            assertNotEquals(status.getMessage(), Status.WARN, level);
        }
    }
}
