package com.zzy.shuati.job.cycle;

import cn.hutool.core.collection.CollUtil;
import com.zzy.shuati.esdao.QuestionEsDao;
import com.zzy.shuati.mapper.QuestionMapper;
import com.zzy.shuati.model.dto.question.QuestionEsDTO;
import com.zzy.shuati.model.entity.Question;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

@Component
@Slf4j
//用于在 Spring Boot 应用程序启动后执行一些特定的任务或逻辑。当应用程序启动时，`CommandLineRunner` 接口的实现类中的 `run` 方法会被调用
public class IncSyncQuestionToEs{

    @Resource
    private QuestionMapper questionMapper;

    @Resource
    private QuestionEsDao questionEsDao;

    /**
     * 增量同步，5分钟一次
     */
    //定时任务，每5分钟一次
    @Scheduled(fixedRate = 60*5*1000)
    public void run() {
        // 查找5分钟以内的题目，需要写一下mapper
        Long FIVE_MINUTES=60*5*1000L;
        Date fiveMinAgo=new Date(new Date().getTime()-FIVE_MINUTES);
        List<Question> questionList = questionMapper.listQuestionWithDelete(fiveMinAgo);
        //判空
        if(CollUtil.isEmpty(questionList)){
            log.info("no inc question");
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
