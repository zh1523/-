package com.zzy.shuati.model.dto.questionBank;

import lombok.Data;

import java.io.Serializable;
import java.util.List;

/**
 * 编辑题库请求
 *
 * 
 * @from <a href="https://www.code-nav.cn">编程导航学习圈</a>
 */
@Data
public class QuestionBankEditRequest implements Serializable {

    /**
     * id
     */
    private Long id;

    /**
     * 标题
     */
    private String title;

    /**
     * 描述
     */
    private String description;

    /**
     * 图片
     */
    private String picture;

    private static final long serialVersionUID = 1L;
}