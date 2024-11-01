package com.zzy.shuati.job.once;

import cn.hutool.core.collection.CollUtil;
import com.zzy.shuati.esdao.QuestionEsDao;
import com.zzy.shuati.model.dto.question.QuestionEsDTO;
import com.zzy.shuati.model.entity.Question;
import com.zzy.shuati.service.QuestionService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.List;
import java.util.stream.Collectors;

//@Component
@Slf4j
//用于在 Spring Boot 应用程序启动后执行一些特定的任务或逻辑。当应用程序启动时，`CommandLineRunner` 接口的实现类中的 `run` 方法会被调用
public class FullSyncQuestionToEs implements CommandLineRunner {

    @Resource
    private QuestionService questionService;

    @Resource
    private QuestionEsDao questionEsDao;

    /**
     * Mysql的question表和es所以内容一次，不使用时把注入组件@component去掉
     * @param args
     */
    @Override
    public void run(String... args) {
        // 全量获取题目
        List<Question> questionList = questionService.list();
        if (CollUtil.isEmpty(questionList)) {
            return;
        }
        // 转为 ES 实体类
        List<QuestionEsDTO> questionEsDTOList = questionList.stream()
                .map(QuestionEsDTO::objToDto)
                .collect(Collectors.toList());
        // 分页批量插入到 ES
        final int pageSize = 500;
        int total = questionEsDTOList.size();
        log.info("FullSyncQuestionToEs start, total {}", total);
        for (int i = 0; i < total; i += pageSize) {
            // 注意同步的数据下标不能超过总数据量
            int end = Math.min(i + pageSize, total);
            log.info("sync from {} to {}", i, end);
            questionEsDao.saveAll(questionEsDTOList.subList(i, end));//sublist取出list集合指定index之间的子集
        }
        log.info("FullSyncQuestionToEs end, total {}", total);
    }
}

