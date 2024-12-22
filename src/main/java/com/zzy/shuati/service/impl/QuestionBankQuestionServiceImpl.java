package com.zzy.shuati.service.impl;

import cn.hutool.core.collection.CollUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.zzy.shuati.common.ErrorCode;
import com.zzy.shuati.constant.CommonConstant;
import com.zzy.shuati.exception.BusinessException;
import com.zzy.shuati.exception.ThrowUtils;
import com.zzy.shuati.mapper.QuestionBankQuestionMapper;
import com.zzy.shuati.model.dto.questionBankQuestion.QuestionBankQuestionQueryRequest;
import com.zzy.shuati.model.entity.Question;
import com.zzy.shuati.model.entity.QuestionBank;
import com.zzy.shuati.model.entity.QuestionBankQuestion;
import com.zzy.shuati.model.entity.User;
import com.zzy.shuati.model.vo.QuestionBankQuestionVO;
import com.zzy.shuati.model.vo.UserVO;
import com.zzy.shuati.service.QuestionBankQuestionService;
import com.zzy.shuati.service.QuestionBankService;
import com.zzy.shuati.service.QuestionService;
import com.zzy.shuati.service.UserService;
import com.zzy.shuati.utils.SqlUtils;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.aop.framework.AopContext;
import org.springframework.context.annotation.Lazy;
import org.springframework.dao.DataAccessException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * 题目题库关联服务实现
 *
 * 
 * @from <a href="https://www.code-nav.cn">编程导航学习圈</a>
 */
@Service
@Slf4j
public class QuestionBankQuestionServiceImpl extends ServiceImpl<QuestionBankQuestionMapper, QuestionBankQuestion> implements QuestionBankQuestionService {

    @Resource
    private UserService userService;

    @Resource
    @Lazy
    private QuestionService questionService;

    @Resource
    private QuestionBankService questionBankService;

    /**
     * 校验数据
     *
     * @param questionBankQuestion
     * @param add      对创建的数据进行校验
     */
    @Override
    public void validQuestionBankQuestion(QuestionBankQuestion questionBankQuestion, boolean add) {
        ThrowUtils.throwIf(questionBankQuestion == null, ErrorCode.PARAMS_ERROR);
        // 题目和题库必须存在
        Long questionId = questionBankQuestion.getQuestionId();
        if (questionId != null) {
            Question question = questionService.getById(questionId);
            ThrowUtils.throwIf(question == null, ErrorCode.NOT_FOUND_ERROR, "题目不存在");
        }
        Long questionBankId = questionBankQuestion.getQuestionBankId();
        if (questionBankId != null) {
            QuestionBank questionBank = questionBankService.getById(questionBankId);
            ThrowUtils.throwIf(questionBank == null, ErrorCode.NOT_FOUND_ERROR, "题库不存在");
        }
    }


    /**
     * 获取查询条件
     *
     * @param questionBankQuestionQueryRequest
     * @return
     */
    @Override
    public QueryWrapper<QuestionBankQuestion> getQueryWrapper(QuestionBankQuestionQueryRequest questionBankQuestionQueryRequest) {
        QueryWrapper<QuestionBankQuestion> queryWrapper = new QueryWrapper<>();
        if (questionBankQuestionQueryRequest == null) {
            return queryWrapper;
        }

        Long id = questionBankQuestionQueryRequest.getId();
        Long notId = questionBankQuestionQueryRequest.getNotId();
        String searchText = questionBankQuestionQueryRequest.getSearchText();
        Long questionId = questionBankQuestionQueryRequest.getQuestionId();
        Long questionBankId = questionBankQuestionQueryRequest.getQuestionBankId();
        String sortField = questionBankQuestionQueryRequest.getSortField();
        String sortOrder = questionBankQuestionQueryRequest.getSortOrder();
        Long userId = questionBankQuestionQueryRequest.getUserId();

        // 从多字段中搜索
        if (StringUtils.isNotBlank(searchText)) {
            // 需要拼接查询条件
            queryWrapper.and(qw -> qw.like("title", searchText).or().like("content", searchText));
        }
        // 模糊查询
        // JSON 数组查询
        // 精确查询
        queryWrapper.ne(ObjectUtils.isNotEmpty(notId), "id", notId);
        queryWrapper.eq(ObjectUtils.isNotEmpty(id), "id", id);
        queryWrapper.eq(ObjectUtils.isNotEmpty(userId), "questionId", questionId);
        queryWrapper.eq(ObjectUtils.isNotEmpty(userId), "questionBankId", questionBankId);
        queryWrapper.eq(ObjectUtils.isNotEmpty(userId), "userId", userId);
        // 排序规则
        queryWrapper.orderBy(SqlUtils.validSortField(sortField),
                sortOrder.equals(CommonConstant.SORT_ORDER_ASC),
                sortField);
        return queryWrapper;
    }

    /**
     * 获取题目题库关联封装
     *
     * @param questionBankQuestion
     * @param request
     * @return
     */
    @Override
    public QuestionBankQuestionVO getQuestionBankQuestionVO(QuestionBankQuestion questionBankQuestion, HttpServletRequest request) {
        // 对象转封装类
        QuestionBankQuestionVO questionBankQuestionVO = QuestionBankQuestionVO.objToVo(questionBankQuestion);

        // region 可选
        // 1. 关联查询用户信息
        Long userId = questionBankQuestion.getUserId();
        User user = null;
        if (userId != null && userId > 0) {
            user = userService.getById(userId);
        }
        UserVO userVO = userService.getUserVO(user);
        questionBankQuestionVO.setUser(userVO);

        // endregion

        return questionBankQuestionVO;
    }

    /**
     * 分页获取题目题库关联封装
     *
     * @param questionBankQuestionPage
     * @param request
     * @return
     */
    @Override
    public Page<QuestionBankQuestionVO> getQuestionBankQuestionVOPage(Page<QuestionBankQuestion> questionBankQuestionPage, HttpServletRequest request) {
        List<QuestionBankQuestion> questionBankQuestionList = questionBankQuestionPage.getRecords();
        Page<QuestionBankQuestionVO> questionBankQuestionVOPage = new Page<>(questionBankQuestionPage.getCurrent(), questionBankQuestionPage.getSize(), questionBankQuestionPage.getTotal());
        if (CollUtil.isEmpty(questionBankQuestionList)) {
            return questionBankQuestionVOPage;
        }
        // 对象列表 => 封装对象列表
        List<QuestionBankQuestionVO> questionBankQuestionVOList = questionBankQuestionList.stream().map(questionBankQuestion -> {
            return QuestionBankQuestionVO.objToVo(questionBankQuestion);
        }).collect(Collectors.toList());

        // region 可选
        // 1. 关联查询用户信息
        Set<Long> userIdSet = questionBankQuestionList.stream().map(QuestionBankQuestion::getUserId).collect(Collectors.toSet());
        Map<Long, List<User>> userIdUserListMap = userService.listByIds(userIdSet).stream()
                .collect(Collectors.groupingBy(User::getId));

        // 填充信息
        questionBankQuestionVOList.forEach(questionBankQuestionVO -> {
            Long userId = questionBankQuestionVO.getUserId();
            User user = null;
            if (userIdUserListMap.containsKey(userId)) {
                user = userIdUserListMap.get(userId).get(0);
            }
            questionBankQuestionVO.setUser(userService.getUserVO(user));
        });
        // endregion

        questionBankQuestionVOPage.setRecords(questionBankQuestionVOList);
        return questionBankQuestionVOPage;
    }

    /**
     * 向题库批量添加题目
     * @param questionIds
     * @param questionBankId
     * @param userId
     */
    @Override
    public void batchAddQuestionToBank(List<Long> questionIds, Long questionBankId, Long userId) {
        //参数不为null
        ThrowUtils.throwIf(questionIds==null,ErrorCode.PARAMS_ERROR,"题目为空");
        ThrowUtils.throwIf(questionBankId==null,ErrorCode.PARAMS_ERROR,"题库为空");
        ThrowUtils.throwIf(userId==null,ErrorCode.PARAMS_ERROR,"用户为空");

        //获取合法的题目id
        //questionIds里数据库查不到的数据会直接每滤过
        List<Question> validQuestions = questionService.listByIds(questionIds);
        ThrowUtils.throwIf(validQuestions==null,ErrorCode.NOT_FOUND_ERROR,"题目不合法");
        List<Long> validQuestionIds = validQuestions.stream()
                .map(Question::getId)
                .collect(Collectors.toList());
        //获取合法的题库id
        QuestionBank validQuestionBank = questionBankService.getById(questionBankId);
        ThrowUtils.throwIf(validQuestionBank==null,ErrorCode.NOT_FOUND_ERROR,"题库不合法");
        Long validQuestionBankId = validQuestionBank.getId();

        //提前排除已经存在题库的题目
        //获取已存在题目
        LambdaQueryWrapper<QuestionBankQuestion> questionBankQuestionLambdaQueryWrapper = new LambdaQueryWrapper<>();
        questionBankQuestionLambdaQueryWrapper
                .eq(QuestionBankQuestion::getQuestionBankId, validQuestionBankId)
                .in(QuestionBankQuestion::getQuestionId, validQuestionIds);
        List<QuestionBankQuestion> existedQuestionIds = this.list(questionBankQuestionLambdaQueryWrapper);
        Set<Long> existedIdSet = existedQuestionIds.stream()
                .map(QuestionBankQuestion::getQuestionId)
                .collect(Collectors.toSet());
        //排除
        validQuestionIds = validQuestionIds.stream().filter(questionId -> !existedIdSet.contains(questionId))
                .collect(Collectors.toList());

        //并发编程
        // 自定义线程池
        ThreadPoolExecutor customExecutor = new ThreadPoolExecutor(
                20,                         // 核心线程数
                50,                        // 最大线程数
                60L,                       // 线程空闲存活时间
                TimeUnit.SECONDS,           // 存活时间单位
                new LinkedBlockingQueue<>(10000),  // 阻塞队列容量
                new ThreadPoolExecutor.CallerRunsPolicy() // 拒绝策略：由调用线程处理任务
        );

        // 用于保存所有批次的 CompletableFuture
        List<CompletableFuture<Void>> futures = new ArrayList<>();

        //添加题目
        // 分批处理避免长事务，假设每次处理 1000 条数据
        int batchSize = 1000;
        int totalQuestionListSize = validQuestionIds.size();
        for(int i=0;i<totalQuestionListSize;i+=batchSize) {
            List<Long> subValidQuestionIds = validQuestionIds.subList(i, Math.min(i + batchSize, totalQuestionListSize));
            List<QuestionBankQuestion> questionBankQuestions=subValidQuestionIds.stream()
                    .map(questionId->{
                        QuestionBankQuestion questionBankQuestion = new QuestionBankQuestion();
                        questionBankQuestion.setQuestionId(questionId);
                        questionBankQuestion.setUserId(userId);
                        questionBankQuestion.setQuestionBankId(validQuestionBankId);
                        return questionBankQuestion;
                    }).collect(Collectors.toList());
            // 使用事务处理每批数据
            //获取当前代理对象
            QuestionBankQuestionService questionBankQuestionService = (QuestionBankQuestionServiceImpl) AopContext.currentProxy();
            // 异步处理每批数据并添加到 futures 列表
            CompletableFuture<Void> future = CompletableFuture.runAsync(() -> {
                questionBankQuestionService.batchAddQuestionsToBankInner(questionBankQuestions);
            }, customExecutor);
            futures.add(future);
        }
        // 等待所有批次操作完成
        CompletableFuture.allOf(futures.toArray(new CompletableFuture[0])).join();
        // 关闭线程池
        customExecutor.shutdown();
//        for(Long questionId : validQuestionIds) {
//            QuestionBankQuestion questionBankQuestion = new QuestionBankQuestion();
//            questionBankQuestion.setQuestionId(questionId);
//            questionBankQuestion.setUserId(userId);
//            questionBankQuestion.setQuestionBankId(validQuestionBankId);
//            boolean res = this.save(questionBankQuestion);
//            ThrowUtils.throwIf(!res,ErrorCode.OPERATION_ERROR,"向题库批量插入题目失败");
//        }
    }

    /**
     * 向题库批量增加题目,子事务
     * @param questionBankQuestions
     */
    @Transactional(rollbackFor = Exception.class)
    @Override
    public void batchAddQuestionsToBankInner(List<QuestionBankQuestion> questionBankQuestions) {
        for (QuestionBankQuestion questionBankQuestion : questionBankQuestions) {
            long questionId = questionBankQuestion.getQuestionId();
            long questionBankId = questionBankQuestion.getQuestionBankId();
            boolean result = this.save(questionBankQuestion);
            ThrowUtils.throwIf(!result, ErrorCode.OPERATION_ERROR, "向题库添加题目失败");
        }
    }


    /**
     * 向题库批量删除题目
     * @param questionIds
     * @param questionBankId
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void batchRemoveQuestionFromBank(List<Long> questionIds, Long questionBankId) {
        //参数不为null
        ThrowUtils.throwIf(questionIds==null,ErrorCode.PARAMS_ERROR,"题目为空");
        ThrowUtils.throwIf(questionBankId==null,ErrorCode.PARAMS_ERROR,"题库为空");
        //获取合法的题库id
        QuestionBank validQuestionBank = questionBankService.getById(questionBankId);
        ThrowUtils.throwIf(validQuestionBank==null,ErrorCode.NOT_FOUND_ERROR,"题库不合法");
        Long validQuestionBankId = validQuestionBank.getId();
        //题目id没有必要过滤
        //批量删除
        for(Long questionId : questionIds) {
            LambdaQueryWrapper<QuestionBankQuestion> lambdaQueryWrapper = new LambdaQueryWrapper<>();
            lambdaQueryWrapper
                    .eq(QuestionBankQuestion::getQuestionId,questionId)
                    .eq(QuestionBankQuestion::getQuestionBankId,validQuestionBankId);
            boolean res = this.remove(lambdaQueryWrapper);
            ThrowUtils.throwIf(!res,ErrorCode.OPERATION_ERROR,"向题库批量删除题目失败");
            //其他异常处理....
        }
    }

}
