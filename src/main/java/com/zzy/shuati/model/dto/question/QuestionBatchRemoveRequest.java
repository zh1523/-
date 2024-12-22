package com.zzy.shuati.model.dto.question;

import lombok.Data;

import java.io.Serializable;
import java.util.List;

/**
 * 题目id列表
 */
@Data
public class QuestionBatchRemoveRequest implements Serializable {
    private List<Long> questionIdList;

    private static final long serialVersionUID = 1L;

}
