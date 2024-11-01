package com.zzy.shuati;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.zzy.shuati.constant.CommonConstant;
import com.zzy.shuati.esdao.QuestionEsDao;
import com.zzy.shuati.model.dto.question.QuestionQueryRequest;
import com.zzy.shuati.model.entity.Question;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.sort.SortBuilder;
import org.elasticsearch.search.sort.SortBuilders;
import org.elasticsearch.search.sort.SortOrder;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.elasticsearch.core.query.NativeSearchQuery;
import org.springframework.data.elasticsearch.core.query.NativeSearchQueryBuilder;

import javax.annotation.Resource;
import java.util.List;

/**
 * 主类测试
 *
 */
@SpringBootTest
class MainApplicationTests {
        @Resource
        private QuestionEsDao questionEsDao;

        @Test
        void contextLoads() {

        }

}
