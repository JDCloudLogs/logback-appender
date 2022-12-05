# JdCloud Logback Appender

## JdCloud Logback Appender

使用JdCloud Logback Appender，可以将日志写入到京东云日志服务，写到日志服务中的日志的样式如下：
```
level: ERROR
location: com.jdcloud.logs.JDCloudLogbackAppenderTest.smokeTest(JDCloudLogbackAppenderTest.java:29)
message: error log
throwable: java.lang.NullPointerException: xxx
thread: main
source: xxx
customKey : xxx
```
其中：
- level 日志级别。
- location 日志打印语句的代码位置。
- message 日志内容。
- throwable 日志异常信息（只有记录了异常信息，这个字段才会出现）。
- thread 线程名称。
- source 日志来源，可在配置文件中指定，默认为机器ip。
- customKey 自定义日志字段。

## 版本支持
* logback 1.2.3
* aliyun-log-producer 0.2.0
* protobuf-java 2.5.0


## 配置步骤

### 1. maven 工程中引入依赖

```
<dependency>
    <groupId>com.jdcloud.logs</groupId>
    <artifactId>logback-appender</artifactId>
    <version>0.2.0</version>
</dependency>
```

### 2. 修改配置文件

logbak.xml 配置JDCloudLogbackAppender，例如：
```
  <!--为了防止进程退出时，内存中的数据丢失，请加上此选项-->
  <shutdownHook class="ch.qos.logback.core.hook.DelayingShutdownHook"/>

  <appender name="JDCloudAppender" class="com.jdcloud.logs.JDCloudLogbackAppender">
        <!-- 必选配置项start -->
        <!-- 认证标识 -->
        <accessKeyId>${accessKeyId}</accessKeyId>
        <!-- 认证秘钥 -->
        <secretAccessKey>${secretAccessKey}</secretAccessKey>
        <!-- 地域标识 -->
        <regionId>${regionId}</regionId>
        <!-- 日志发送地址，可选值： -->
        <!-- 华北-北京：logs.internal.cn-north-1.jdcloud-api.com -->
        <!-- 华南-广州：logs.internal.cn-south-1.jdcloud-api.com -->
        <!-- 华东-宿迁：logs.internal.cn-east-1.jdcloud-api.com -->
        <!-- 华东-上海：logs.internal.cn-east-2.jdcloud-api.com -->
        <endpoint>${endpoint}</endpoint>
        <!-- 日志主题 -->
        <logTopic>${logTopic}</logTopic>
        <!-- 必选配置项end -->

        <!-- 可选配置项start -->
        <!-- 日志来源，默认为机器ip -->
        <source></source>
        <!-- 日志文件名 -->
        <fileName></fileName>
        <!-- 日志缓存的内存占用字节数上限，默认为100M -->
        <totalSizeInBytes>104857600</totalSizeInBytes>
        <!-- 日志缓存达到上限后，获取可用内存的最大阻塞等待时间，默认为0，表示不等待。等待超时后丢弃日志，由于会阻塞日志线程，建议设置为0 -->
        <maxBlockMillis>0</maxBlockMillis>
        <!-- 日志发送线程数，默认为可用处理器个数 -->
        <sendThreads>4</sendThreads>
        <!-- 每批次发送的日志数，默认为4096，最大不能超过32768 -->
        <batchSize>4096</batchSize>
        <!-- 每批次发送的日志字节数，默认2MB，最大不能超过4MB -->
        <batchSizeInBytes>2097152</batchSizeInBytes>
        <!-- 批次发送时间间隔毫秒数，默认为2秒，表示每隔2秒发送一个批次日志，最小不少于100毫秒 -->
        <batchMillis>2000</batchMillis>
        <!-- 发送失败后重试次数，0为不重试 -->
        <retries>10</retries>
        <!-- 发送失败后首次重试时间毫秒数 -->
        <initRetryBackoffMillis>100</initRetryBackoffMillis>
        <!-- 发送失败后最大退避时间毫秒数，默认为50秒 -->
        <maxRetryBackoffMillis>50000</maxRetryBackoffMillis>
        <!-- 自定义日志字段，格式为：key=value -->
        <customLogProperty>customKey1=value1</customLogProperty>
        <customLogProperty>customKey2=value2</customLogProperty>
        <!-- 可选配置项end -->
    </appender>
```
