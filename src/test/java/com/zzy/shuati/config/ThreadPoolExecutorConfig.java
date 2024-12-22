package com.zzy.shuati.config;

import org.jetbrains.annotations.NotNull;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

@Configuration
public class ThreadPoolExecutorConfig {
    @Bean
    public ThreadPoolExecutor threadPoolExecutor() {
        //线程工程重写newThread方法
        ThreadFactory threadFactory = new ThreadFactory() {

            int count=0;

            @Override
            public Thread newThread(@NotNull Runnable r) {
                Thread thread = new Thread(r);
                thread.setName("线程 "+count++);
                return thread;
            }
        };

        //核心线程数
        int corePoolSize = 2;
        //最大线程数
        int maximumPoolSize = 4;
        //闲置线程空闲时间10s
        long keepAliveTime = 10;
        TimeUnit unit = TimeUnit.SECONDS;
        //工作队列
        ArrayBlockingQueue<Runnable> workQuery = new ArrayBlockingQueue<>(4);
        ThreadPoolExecutor threadPoolExecutor = new ThreadPoolExecutor(corePoolSize, maximumPoolSize, keepAliveTime, unit, workQuery, threadFactory);

        return threadPoolExecutor;
    }
}
