package com.zzy.shuati.constant;

public interface RedisConstant {

    /**
     * 用户签到记录的rediskey前缀
     */
    String USER_SIGN_IN_REDIS_PREFIX = "user:signins";

    /**
     * 用户签到记录的rediskey
     * @param year
     * @param userId
     * @return
     */
    static String getUserSignInRedisKey(int year, long userId){
        return USER_SIGN_IN_REDIS_PREFIX + ":" +year + ":" + userId;
    }
}
