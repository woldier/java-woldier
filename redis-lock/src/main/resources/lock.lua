if (redis.call('EXISTS',KEYS[1])==0) then  -- 查询到不存在
    redis.call('HSET',KEYS[1],ARGV[2],1); -- 不存在就添加一个hashkey 设置冲入次数1
    redis.call('PEXPIRE',KEYS[1],ARGV[1]); -- 设置过去过期
    return nil;
end;
if(redis.call('HEXISTS',KEYS[1],ARGV[2])==1) then--存在锁
    redis.call('HINCRBY',KEYS[1],ARGV[2],1); --重入次数加1
    redis.call('PEXPIRE',KEYS[1],ARGV[1]); --设置过期时间
    return nil;
end;

return redis.call('PTTL',KEYS[1]) --上面两种情况都不满足,说明现在有线程在使用lock,那么返回锁过期s

