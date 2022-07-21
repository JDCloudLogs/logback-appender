package com.jdcloud.logs;

import ch.qos.logback.classic.spi.IThrowableProxy;
import ch.qos.logback.classic.spi.LoggingEvent;
import ch.qos.logback.classic.spi.StackTraceElementProxy;
import ch.qos.logback.classic.spi.ThrowableProxyUtil;
import ch.qos.logback.core.CoreConstants;
import ch.qos.logback.core.UnsynchronizedAppenderBase;
import ch.qos.logback.core.encoder.Encoder;
import com.jdcloud.logs.api.common.LogItem;
import com.jdcloud.logs.producer.LogProducer;
import com.jdcloud.logs.producer.Producer;
import com.jdcloud.logs.producer.config.ProducerConfig;
import com.jdcloud.logs.producer.config.RegionConfig;
import com.jdcloud.logs.util.NanoTimeGenerator;
import com.jdcloud.sdk.utils.StringUtils;

import java.util.HashMap;
import java.util.Map;

/**
 * jdcloud logback appender
 *
 * @author liubai
 * @date 2022/7/8
 */
public class JDCloudLogbackAppender<E> extends UnsynchronizedAppenderBase<E> {

    /**
     * 单条日志最大字符
     */
    private static final int MAX_MESSAGE_SIZE = 64 * 1024;

    private String accessKeyId;
    private String secretAccessKey;
    private String regionId;
    private String endpoint;
    private String logTopic;
    private String mdcFields;
    private final ProducerConfig producerConfig = new ProducerConfig();
    private Producer logProducer;
    private Encoder<E> encoder;
    private Map<String, String> customLogPropertyMap = new HashMap<String, String>();

    /**
     * 日志来源，默认为机器ip
     */
    private String source;

    /**
     * 日志文件名
     */
    private String fileName;

    @Override
    public void start() {
        try {
            createLogProducer();
            super.start();
        } catch (Exception e) {
            addError("Failed to start JdcloudLogbackAppender.", e);
        }
    }

    private void createLogProducer() {
        RegionConfig regionConfig = new RegionConfig(accessKeyId, secretAccessKey, regionId, endpoint);
        logProducer = new LogProducer(producerConfig);
        logProducer.putRegionConfig(regionConfig);
    }

    @Override
    protected void append(E e) {
        if (!(e instanceof LoggingEvent)) {
            return;
        }
        LoggingEvent logEvent = (LoggingEvent) e;
        LogItem logItem = new LogItem(NanoTimeGenerator.currentTimeNanos());

        logItem.addContent("level", logEvent.getLevel().levelStr);
        logItem.addContent("thread", logEvent.getThreadName());

        StackTraceElement[] caller = logEvent.getCallerData();
        if (caller != null && caller.length > 0) {
            logItem.addContent("location", caller[0].toString());
        }

        String formattedMessage = logEvent.getFormattedMessage();
        if (formattedMessage.length() > MAX_MESSAGE_SIZE) {
            formattedMessage = formattedMessage.substring(0, MAX_MESSAGE_SIZE) + "..";
        }
        logItem.addContent("message", formattedMessage);

        IThrowableProxy iThrowableProxy = logEvent.getThrowableProxy();
        if (iThrowableProxy != null) {
            String throwable = getExceptionInfo(iThrowableProxy);
            throwable += fullDump(logEvent.getThrowableProxy().getStackTraceElementProxyArray());
            logItem.addContent("throwable", throwable);
        }

        for (Map.Entry<String, String> mdcEntry : logEvent.getMDCPropertyMap().entrySet()) {
            logItem.addContent(mdcEntry.getKey(), mdcEntry.getValue());
        }

        for (Map.Entry<String, String> customLogProperty : customLogPropertyMap.entrySet()) {
            logItem.addContent(customLogProperty.getKey(), customLogProperty.getValue());
        }

        try {
            logProducer.send(regionId, logTopic, logItem, source, fileName);
        } catch (Exception ignore) {
        }
    }

    @Override
    public void stop() {
        try {
            if (!isStarted()) {
                return;
            }
            super.stop();
            logProducer.close();
        } catch (Exception e) {
            addError("Failed to stop JdcloudLogbackAppender.", e);
        }
    }

    private String getExceptionInfo(IThrowableProxy iThrowableProxy) {
        String s = iThrowableProxy.getClassName();
        String message = iThrowableProxy.getMessage();
        return (message != null) ? (s + ": " + message) : s;
    }

    private String fullDump(StackTraceElementProxy[] stackTraceElementProxyArray) {
        StringBuilder builder = new StringBuilder();
        for (StackTraceElementProxy step : stackTraceElementProxyArray) {
            builder.append(CoreConstants.LINE_SEPARATOR);
            String string = step.toString();
            builder.append(CoreConstants.TAB).append(string);
            ThrowableProxyUtil.subjoinPackagingData(builder, step);
        }
        return builder.toString();
    }

    public String getAccessKeyId() {
        return accessKeyId;
    }

    public void setAccessKeyId(String accessKeyId) {
        this.accessKeyId = accessKeyId;
    }

    public String getSecretAccessKey() {
        return secretAccessKey;
    }

    public void setSecretAccessKey(String secretAccessKey) {
        this.secretAccessKey = secretAccessKey;
    }

    public String getRegionId() {
        return regionId;
    }

    public void setRegionId(String regionId) {
        this.regionId = regionId;
    }

    public String getEndpoint() {
        return endpoint;
    }

    public void setEndpoint(String endpoint) {
        this.endpoint = endpoint;
    }

    public String getLogTopic() {
        return logTopic;
    }

    public void setLogTopic(String logTopic) {
        if (StringUtils.isBlank(logTopic)) {
            throw new NullPointerException("logTopic cannot be null");
        }
        this.logTopic = logTopic;
    }

    public String getMdcFields() {
        return mdcFields;
    }

    public void setMdcFields(String mdcFields) {
        this.mdcFields = mdcFields;
    }

    public int getTotalSizeInBytes() {
        return this.producerConfig.getTotalSizeInBytes();
    }

    public void setTotalSizeInBytes(int totalSizeInBytes) {
        this.producerConfig.setTotalSizeInBytes(totalSizeInBytes);
    }

    public long getMaxBlockMillis() {
        return this.producerConfig.getMaxBlockMillis();
    }

    public void setMaxBlockMillis(long maxBlockMillis) {
        this.producerConfig.setMaxBlockMillis(maxBlockMillis);
    }

    public int getSendThreads() {
        return this.producerConfig.getSendThreads();
    }

    public void setSendThreads(int sendThreads) {
        this.producerConfig.setSendThreads(sendThreads);
    }

    public int getBatchSize() {
        return this.producerConfig.getBatchSize();
    }

    public void setBatchSize(int batchSize) {
        this.producerConfig.setBatchSize(batchSize);
    }

    public int getBatchSizeInBytes() {
        return this.producerConfig.getBatchSizeInBytes();
    }

    public void setBatchSizeInBytes(int batchSizeInBytes) {
        this.producerConfig.setBatchSizeInBytes(batchSizeInBytes);
    }

    public int getBatchMillis() {
        return this.producerConfig.getBatchMillis();
    }

    public void setBatchMillis(int batchMillis) {
        this.producerConfig.setBatchMillis(batchMillis);
    }

    public int getRetries() {
        return this.producerConfig.getRetries();
    }

    public void setRetries(int retries) {
        this.producerConfig.setRetries(retries);
    }

    public long getInitRetryBackoffMillis() {
        return this.producerConfig.getInitRetryBackoffMillis();
    }

    public void setInitRetryBackoffMillis(long initRetryBackoffMillis) {
        this.producerConfig.setInitRetryBackoffMillis(initRetryBackoffMillis);
    }

    public long getMaxRetryBackoffMillis() {
        return this.producerConfig.getMaxRetryBackoffMillis();
    }

    public void setMaxRetryBackoffMillis(long maxRetryBackoffMillis) {
        this.producerConfig.setMaxRetryBackoffMillis(maxRetryBackoffMillis);
    }

    public String getSource() {
        return source;
    }

    public void setSource(String source) {
        this.source = source;
    }

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public Encoder<E> getEncoder() {
        return encoder;
    }

    public void setEncoder(Encoder<E> encoder) {
        this.encoder = encoder;
    }

    public Map<String, String> getCustomLogPropertyMap() {
        return customLogPropertyMap;
    }

    public void setCustomLogPropertyMap(Map<String, String> customLogPropertyMap) {
        this.customLogPropertyMap = customLogPropertyMap;
    }

    public void addCustomLogProperty(String keyValue) {
        String[] split = keyValue.split("=", 2);
        if (split.length == 2) {
            addCustomLogProperty(split[0], split[1]);
        }
    }

    public void addCustomLogProperty(String key, String value) {
        this.customLogPropertyMap.put(key, value);
    }
}
