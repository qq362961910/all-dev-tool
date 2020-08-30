package cn.t.tool.netproxytool.constants;

import java.util.concurrent.TimeUnit;

/**
 * @author <a href="mailto:jian.yang@liby.ltd">野生程序员-杨建</a>
 * @version V1.0
 * @since 2020-05-24 19:27
 **/
public class ThreadPoolConfig {
    public static final String PROXY_THREAD_POOL_NAME = "net-proxy";
    public static final int PROCESSOR_COUNT = Runtime.getRuntime().availableProcessors();
    public static final int PROXY_CORE_THREAD_COUNT = (PROCESSOR_COUNT < 4 ? 2 : PROCESSOR_COUNT) * 4;
    public static final int PROXY_BLOCKING_THREAD_COUNT = 0;
    public static final int PROXY_MAX_THREAD_COUNT = (PROXY_CORE_THREAD_COUNT + PROXY_BLOCKING_THREAD_COUNT) * 2;
    public static final int PROXY_THREAD_TT = 10;
    public static final TimeUnit PROXY_THREAD_TT_TIME_UNIT = TimeUnit.SECONDS;
}
