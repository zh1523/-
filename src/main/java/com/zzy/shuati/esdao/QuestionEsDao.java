package com.zzy.shuati.esdao;

import com.zzy.shuati.model.dto.question.QuestionEsDTO;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;

import java.util.List;

/**
 * 题目es dao,类似于basemapper
 */
public interface QuestionEsDao extends ElasticsearchRepository<QuestionEsDTO,Long> {

}
