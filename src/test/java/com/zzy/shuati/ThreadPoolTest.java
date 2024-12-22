package com.zzy.shuati;

import com.zzy.shuati.config.ThreadPoolExecutorConfig;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import javax.annotation.Resource;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ThreadPoolExecutor;

@SpringBootTest
@Slf4j
public class ThreadPoolTest {

    @Resource
    ThreadPoolExecutor threadPoolExecutor;

    public void add(String name) {
        CompletableFuture.runAsync(() -> {
            log.info("任务执行中 " + "任务名 " + name + "执行线程 " + Thread.currentThread().getName());
            //模拟执行10秒
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
            //加入到线程池
        }, threadPoolExecutor);
    }

    // 该方法返回线程池的状态信息
    public void get() {
        // 创建一个HashMap存储线程池的状态信息
        Map<String, Object> map = new HashMap<>();
        // 获取线程池的队列长度
        int size = threadPoolExecutor.getQueue().size();
        // 将队列长度放入map中
        map.put("队列长度", size);
        // 获取线程池已接收的任务总数
        long taskCount = threadPoolExecutor.getTaskCount();
        // 将任务总数放入map中
        map.put("任务总数", taskCount);
        // 获取线程池已完成的任务数
        long completedTaskCount = threadPoolExecutor.getCompletedTaskCount();
        // 将已完成的任务数放入map中
        map.put("已完成任务数", completedTaskCount);
        // 获取线程池中正在执行任务的线程数
        int activeCount = threadPoolExecutor.getActiveCount();
        // 将正在工作的线程数放入map中
        map.put("正在工作的线程数", activeCount);
        // 将map转换为JSON字符串并返回
        for (Map.Entry<String, Object> entry : map.entrySet()) {
            System.out.println(entry.getKey() + " " + entry.getValue());
        }
        System.out.println("_______");
    }

    @Test
    public void test(){
        add("任务1");
        add("任务2");
        //核心线程满
        get();
        add("任务3");
        add("任务4");
        get();
        add("任务5");
        add("任务6");
        //工作队列满
        get();
        add("任务7");
        add("任务8");
        //最大线程数满
        get();
        //报错
        add("任务9");
        get();
    }

}
