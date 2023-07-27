分布式锁

# 1. 基础组件redis,zookeeper使用

## 1.1 redis

### 1.1.1 基本数据类型

- `string`字符串

`string` 是 `redis` 最基本的类型，你可以理解成与 `Memcached` 一模一样的类型，一个`key` 对应一个` value`。

`string` 类型是二进制安全的。意思是 `redis` 的 `string` 可以包含任何数据。比如`jpg`图片或者序列化的对象。

`string `类型是 `Redis `最基本的数据类型，`string `类型的值最大能存储 `512MB`。

```shell
redis localhost:6379> set woldier "菜鸟"
redis localhost:6379> get woldier 
 "菜鸟"
```

- `hash`哈希

`Redis` `hash` 是一个键值(`key` `->` `value`)对集合。

`Redis` `hash` 是一个 `string` 类型的 `field` 和 `value` 的映射表，`hash` 特别适合用于存储对象。

```shell
redis localhost:6379> del woldier # 删除之前的string类型
redis localhost:6379> HMSET woldier "name" "woldier" "sex" "男" "age" "25" 
redis localhost:6379> HGET woldier "name"  # HGET只能查询一个
 "woldier"
```

实例中我们使用了 `Redis` **HMSET, HGET** 命令，**HMSET** 设置了两个 **field=>value** 对, HGET 获取对应 **field** 对应的 **value**。

每个 hash 可以存储  $2 ^(32    - 1)$ 键值对（40多亿）。

- `list`列表

Redis 列表是简单的字符串列表，按照插入顺序排序。你可以添加一个元素到列表的头部（左边）或者尾部（右边）。

- `set` 集合

Redis 的 Set 是 string 类型的无序集合。

集合是通过哈希表实现的，所以添加，删除，查找的复杂度都是 O(1)。

```shell
redis localhost:6379> DEL woldier
redis localhost:6379> sadd woldier redis redis bingwang 1
redis localhost:6379> SMEMBERS woldier
1) "redis"
2) "bingwang"
3) "1"

```

以上实例中`redis`添加了两次，但根据集合内元素的唯一性，第二次插入的元素将被忽略。

集合中最大的成员数为 232 - 1(4294967295, 每个集合可存储40多亿个成员)。

- `zset`有序集合

`Redis` `zset` 和 `set`一样也是`string`类型元素的集合,且不允许重复的成员。

不同的是每个元素都会关联一个`double`类型的分数。`redis`正是通过分数来为集合中的成员进行从小到大的排序。

`zset`的成员是唯一的,但分数(score)却可以重复。

```shell
redis localhost:6379> DEL woldier
redis localhost:6379> ZADD woldier 0 redis
redis localhost:6379> ZADD woldier 1 woldier
redis localhost:6379> ZADD woldier 1 woldier
redis localhost:6379> ZADD woldier 2 mybatis

redis localhost:6379> ZRANGEBYSCORE woldier 0 10
1) "redis"
2) "woldier"
3) "mybatis"

```

### 1.1.2 Redis keys命令

| 命令                                        | 描述                                                         |
| ------------------------------------------- | ------------------------------------------------------------ |
| `DEL key`                                   | 删除key                                                      |
| `DUMP key`                                  | 序列化                                                       |
| `EXITST key`                                | 检查指定的key是否存在                                        |
| `EXPIRE key seconds`                        | 为key设置一个过期时间(单位是秒)                              |
| `EXPIRE key timestamp`                      | EXPIREAT 的作用和 EXPIRE 类似，都用于为 key 设置过期时间。不同在于 EXPIREAT 命令接受的时间参数是 UNIX 时间戳(unix timestamp)。 |
| `PEXPIRE key milliseconds`                  | 设置 key 的过期时间以毫秒计。                                |
| `PEXPIREAT key milliseconds-timestamp`      | 设置 key 过期时间的时间戳(unix timestamp) 以毫秒计           |
| `KEYS pattern`                              | 查找所有符合给定模式(pattern)的key                           |
| `MOVE key db`                               | 将当前数据库的key移动到给定的数据库db当中                    |
| `PERSISIT key`                              | 移除key的过期时间,key永久保存                                |
| `PTTL key`                                  | 以毫秒为单位返回 key 的剩余的过期时间。                      |
| `TTL key`                                   | 以秒为单位,返回给定key的剩余生存时间(TTL,time to live)       |
| `RANDOMKEY`                                 | 从当前数据库中随机返回一个key                                |
| `RENAME key newkey`                         | 修改key的名称                                                |
| `SCAN cursor [MATCH pattern] [COUNT count]` | 迭代数据库中的数据库键                                       |
| `TYPE key`                                  | 返回key所存储的值的类型                                      |

## 2.1 zookeepker

docker 安装

```shell
docker run --name some-zookeeper -p 2181:2181 -p 28888:2888 -p 3888:3888 -p 8080 --restart always -d zookeeper #创建镜像
 docker exec -it some-zookeeper "zkCli.sh" # 进入镜像的命令模式
```

```shell
This image includes EXPOSE 2181 2888 3888 8080 (the zookeeper client port, follower port, election port, AdminServer port respectively), so standard container linking will make it automatically available to the linked containers. Since the Zookeeper "fails fast" it's better to always restart it.
```

```shell
ls -R / # 列出节点及其子节点
/
/zookeeper
/zookeeper/config
/zookeeper/quota
#列出当前节点的子节点
ls / 
[zookeeper]
#添加节点
create /app1
create /app2
create /app
ls /
[app, app1, app2, zookeeper]

```

zookeeper实现分布式锁的原理

分布式锁要求如果锁的持有者宕了，锁可以被释放。ZooKeeper 的 ephemeral 节点恰好具备这样的特性。

接下来我们来演示下，需要在两个终端上分别启动 zkCli

在终端 1 上：

执行 `zkCli.sh`，再执行 `create -e /lock` 命令，来建立临时 znode，加锁的操作其实就是建立 znode 的过程，此时第一个客户端加锁成功。

接下来尝试在第二个客户端加锁，在终端 2 上：

执行 `zkCli.sh`，再执行 `create -e /lock` 命令，会发现提示 `Node already exists: /lock`，提示 znode 已存在，znode 建立失败，因此加锁失败，这时候我们来监控这个 znode，使用 `stat -w /lock` 来等待锁被释放。

```shell
WATCHER::
WatchedEvent state:SyncConnected type:NodeDeleted path:/lock
```

再收到这个事件后再次在客户端 2 上执行加锁，执行 `create -e /lock`，会显示创建 znode 成功，即加锁成功。

```java


```

# 2. Redission lock

Redis分布式锁问题：

1. 看门狗隔多久续期一次？ 怎么续期的？

首先如果lock的时候设置了过期时间,那么redssion认为调用者已经掌握了程序的大概执行时间,因此不会触发看门狗机制.

当没有设置过期时间时,那么会使用默认过期时间30s,此时会启动看门狗线程,该线程的作用是对key进行续期,续期的周期时默认过期时间的三分之一也就是10s.

续期流程大致是,得到当前redis链接+加锁线程id(需要注意的是,watchdog线程与加锁线程不是同一个,因此不能直接调用getCurrentId获取).获取到id后那么就开启一个超时任务,这个超时任务的超时时间设置为默认过期时间的1/3,这个超时任务做的事情是去redis续期,具体流程是,如果redis中对应的key的hash结构中有对应的hashkey那么续期并且返回1,否则返回0,如果是返回1那么递归调用watchDog线程续期,否则退出调用.

```java
private void renewExpiration() {
        ExpirationEntry ee = EXPIRATION_RENEWAL_MAP.get(getEntryName()); //获取entryName
        if (ee == null) { //如果为null 直接返回
            return;
        }
        
        Timeout task = getServiceManager().newTimeout(new TimerTask() { //开启一个超时任务
            @Override
            public void run(Timeout timeout) throws Exception {
                ExpirationEntry ent = EXPIRATION_RENEWAL_MAP.get(getEntryName());
                if (ent == null) {
                    return;
                }
                Long threadId = ent.getFirstThreadId();
                if (threadId == null) {
                    return;
                }
                
                CompletionStage<Boolean> future = renewExpirationAsync(threadId); //刷新过期时间
                future.whenComplete((res, e) -> {
                    if (e != null) { //如果这个过程抛出了异常,说明存在问题 
                        log.error("Can't update lock {} expiration", getRawName(), e);
                        EXPIRATION_RENEWAL_MAP.remove(getEntryName());
                        return;
                    }
                    
                    if (res) { //如果res不等于0表示续期成功 ,那么递归调用
                        // reschedule itself
                        renewExpiration();
                    } else {
                        cancelExpirationRenewal(null);//如果等于0表示续期失败,这时候就需要取消再续期
                    }
                });
            }
        }, internalLockLeaseTime / 3, TimeUnit.MILLISECONDS); // 设置超时时间为internalLockLeaseTime的3分之1,以及设置时间单位
        ee.setTimeout(task);
    }
```



2. Key和value存储的内容分别是什么？

lock哈希表的key存储的是redis连接id+jvm线程id的拼接,前部分用于区分不同的jvm,第二部分用于区分不同的线程

 其他线程自旋锁，多久自旋一次，多少次自旋锁会锁膨胀？

```java
    private int timeout = 3000;

    private int retryAttempts = 3;

    private int retryInterval = 1500; 
public void timeout(CompletableFuture<?> promise) {
        int timeout = config.getTimeout() + config.getRetryInterval() * config.getRetryAttempts();
        timeout(promise, timeout);
    }
```

1500ms重试一次 ,重试3次后会进入锁碰撞



3. CAP理论中，redis是什么锁？为什么？

redis属于AP锁,首先C是指一致性,A指的是高可用性,用于redis主从之间的同步存在一定的延迟,因此不能保证查询时不存在slave节点中的key一定不存在master节点中.redis牺牲了数据的一致性而允许redis在短时间内不同步因此属于是AP锁



4. TryLock和lock在redis中怎么实现的？ lua脚本怎么写？

首先tryLock与lock的区别在于tryLock只尝试获取一次锁,如果能够获取锁成功那么就执行获取成功的逻辑,否则执行获取失败的逻辑.而lock则是多次尝试获取锁直到获取锁成功.但是两者调用的lua脚本是一样的,只是失败是否继续重试的差别

```lua
if ((redis.call('exists', KEYS[1]) == 0)  
                            or (redis.call('hexists', KEYS[1], ARGV[2]) == 1)) then  
                        redis.call('hincrby', KEYS[1], ARGV[2], 1); 
                        redis.call('pexpire', KEYS[1], ARGV[1]); 
                        return nil; 
                    end; 
                    return redis.call('pttl', KEYS[1]);
```





5. Unlock方法具体做了啥？Lua脚本怎么写？

因为是可重入锁,因此需要考虑是冲入次数减一还是释放锁,除此之外,因为可能有wathdog机制还需要,删除map中对应的id,以便watchDog退出

还有就是要通知自旋加锁的进程现在锁可用了,因此还有一个消息发布机制存在

```lua
if (redis.call('hexists', KEYS[1], ARGV[3]) == 0) then  -- 检查key是否存在对应的hashkey
	return nil;
end; 
local counter = redis.call('hincrby', KEYS[1], ARGV[3], -1);  --重入次数减一
if (counter > 0) then  -- 如果解锁后重入次数任然大于0 说明不需要要删除key
 	redis.call('pexpire', KEYS[1], ARGV[2]);  -- 锁续期
    return 0;  -- 返回0
else
    redis.call('del', KEYS[1]);   -- 删除
    redis.call(ARGV[4], KEYS[2], ARGV[1]); --发布订阅
    return 1; 
end; 
return nil;
```

```java
   protected RFuture<Boolean> unlockInnerAsync(long threadId) {
        return evalWriteAsync(getRawName(), LongCodec.INSTANCE, RedisCommands.EVAL_BOOLEAN,
              "if (redis.call('hexists', KEYS[1], ARGV[3]) == 0) then " +
                        "return nil;" +
                    "end; " +
                    "local counter = redis.call('hincrby', KEYS[1], ARGV[3], -1); " +
                    "if (counter > 0) then " +
                        "redis.call('pexpire', KEYS[1], ARGV[2]); " +
                        "return 0; " +
                    "else " +
                        "redis.call('del', KEYS[1]); " +
                        "redis.call(ARGV[4], KEYS[2], ARGV[1]); " +
                        "return 1; " +
                    "end; " +
                    "return nil;",
                Arrays.asList(getRawName(), getChannelName()),
                LockPubSub.UNLOCK_MESSAGE, internalLockLeaseTime, getLockName(threadId), getSubscribeService().getPublishCommand());
    }
```



6. Redis分布式锁，是轻量级锁吗？

为了解释这个问题,我们需要明白几种锁的概念

- 偏向锁

这种情况是指只有一个线程进入了临界区

此时当Thread#1进入临界区时，JVM会将lockObject的对象头Mark Word的锁标志位设为“01”，同时会用CAS操作把Thread#1的线程ID记录到Mark Word中，此时进入偏向模式。所谓“偏向”，指的是这个锁会偏向于Thread#1，若接下来没有其他线程进入临界区，则Thread#1再出入临界区无需再执行任何同步操作。也就是说，若只有Thread#1会进入临界区，实际上只有Thread#1初次进入临界区时需要执行CAS操作，以后再出入临界区都不会有同步操作带来的开销。

- 轻量级锁

然而始终只有一个线程进入临界区是一个偏于理想的情况,更多的情况可能是Thread#1,Thread#2交替进入临界区

若Thread#2尝试进入时Thread#1已退出临界区，即此时lockObject处于未锁定状态，这时说明偏向锁上发生了竞争，此时会撤销偏向，Mark Word中不再存放偏向线程ID，而是存放hashCode和GC分代年龄，同时锁标识位变为“01”（表示未锁定），这时Thread#2会获取lockObject的轻量级锁。因为此时Thread#1和Thread#2交替进入临界区，所以偏向锁无法满足需求，需要膨胀到轻量级锁。

- 重量级锁

。若一直是Thread#1和Thread#2交替进入临界区，那么没有问题，轻量锁hold住。一旦在轻量级锁上发生竞争，即出现“Thread#1和Thread#2同时进入临界区”的情况，轻量级锁就hold不住了。 （根本原因是轻量级锁没有足够的空间存储额外状态，此时若不膨胀为重量级锁，则所有等待[轻量锁](https://www.zhihu.com/search?q=轻量锁&search_source=Entity&hybrid_search_source=Entity&hybrid_search_extra={"sourceType"%3A"answer"%2C"sourceId"%3A160222185})的线程只能自旋，可能会损失很多CPU时间）

由于分布式中,存在同时加锁的情况,也就是存在竞争,并且通过订阅模式和超时重试机制来阻塞等待,因此可以说redis分布式锁属于是重量级锁,



7. Redis分布式锁，有羊群效应（惊群效应）吗？

redis实现的分布式锁不存在惊群效应,因为其在自旋获取锁的时候有一个信号量机制,这个机制起到了限流的作用,因此不会存在惊群效应

8. Redis分布式锁，属于公平锁吗？

非公平的,因为我们只是通过key-value去控制加锁是否成功而没有设置一个队列或者list来存储请求的先后顺序

9. Redis分布式锁，可重入吗？具体怎么实现的可重入锁？

可以重入,分布式锁中保存的数据结构是hash结构,hashkey保存的是lock 的jvm+thread标识,而value则是保存的重入次数

10. AP锁为什么不保证强一致性？请描述细节。

因为redis的同步方式是主从拷贝,而主从的拷贝过程中存在数据的不一致,即不具备CP但是正是因为她不用保证CP因此具有较高的可用性,即AP



```java

 @Override
    public void lock() {
        try {
            lock(-1, null, false);
        } catch (InterruptedException e) {
            throw new IllegalStateException();
        }
    }
```



```java
private void lock(long leaseTime, TimeUnit unit, boolean interruptibly) throws InterruptedException {
        long threadId = Thread.currentThread().getId();
        Long ttl = tryAcquire(-1, leaseTime, unit, threadId);
		//......................
    }
```

等待时间为-1,过期时间为-1,时间单位为null,并且传入线程id

```java
    private Long tryAcquire(long waitTime, long leaseTime, TimeUnit unit, long threadId) {
        return get(tryAcquireAsync0(waitTime, leaseTime, unit, threadId));
    }
```

```java
private RFuture<Long> tryAcquireAsync0(long waitTime, long leaseTime, TimeUnit unit, long threadId) {
        return getServiceManager().execute(() -> tryAcquireAsync(waitTime, leaseTime, unit, threadId));
    }

```

`tryAcquireAsync0`中做的事情是获取`ServiceManager`并且执行`tryAcquireAsync`方法

调用`getServiceManager().execute(() -> tryAcquireAsync(waitTime, leaseTime, unit, threadId))`会返回一个RFuther对象

这个对象继承了`interface RFuture<V> extends java.util.concurrent.Future<V>, CompletionStage<V>`

继承了`Future`因此可以阻塞等待执行返回,继承了CompletionStage可以进行两阶段执行

```java
  private RFuture<Long> tryAcquireAsync(long waitTime, long leaseTime, TimeUnit unit, long threadId) {
        RFuture<Long> ttlRemainingFuture;  //声明一个RFuture变量
        if (leaseTime > 0) {
            ttlRemainingFuture = tryLockInnerAsync(waitTime, leaseTime, unit, threadId, RedisCommands.EVAL_LONG);
        } else {
            ttlRemainingFuture = tryLockInnerAsync(waitTime, internalLockLeaseTime,
                    TimeUnit.MILLISECONDS, threadId, RedisCommands.EVAL_LONG);  //Lock 调用的是
        }
        //.......
    }
```

因为传入的lesstime=-1 因此会进入到else方法执行调用`tryLockInnerAsync`

```java
    <T> RFuture<T> tryLockInnerAsync(long waitTime, long leaseTime, TimeUnit unit, long threadId, RedisStrictCommand<T> command) {
        return commandExecutor.syncedEval(getRawName(), LongCodec.INSTANCE, command,
                "if ((redis.call('exists', KEYS[1]) == 0) " +
                            "or (redis.call('hexists', KEYS[1], ARGV[2]) == 1)) then " +
                        "redis.call('hincrby', KEYS[1], ARGV[2], 1); " +
                        "redis.call('pexpire', KEYS[1], ARGV[1]); " +
                        "return nil; " +
                    "end; " +
                    "return redis.call('pttl', KEYS[1]);",
                Collections.singletonList(getRawName()), unit.toMillis(leaseTime), getLockName(threadId));
    }
```

`Collections.singletonList(getRawName())`中包裹的就是`KEYS`,剩余的参数就是`ARGV1,ARGV2...`

`KEYS[1]`就是写入到`redis`中的`key`(通过`getRawName()`得到)

```java
  public final String getRawName() { //抽象父类RedissionObject中的方法
        return name;
    }
```

这个属性是new RedissionLock时构造方法传入的

`Redisson.*create*().getLock("");`

`Redission.java`

```java
  @Override
    public RLock getLock(String name) {
        return new RedissonLock(commandExecutor, name);
    }
```

`RedissonLock.java`的构造方法中传入name又去调用了父类`RedissonBaseLock`

```java
 public RedissonLock(CommandAsyncExecutor commandExecutor, String name) {
        super(commandExecutor, name);
		//...
    }
```

调用父类`RedissonBaseLock`

```java
public RedissonBaseLock(CommandAsyncExecutor commandExecutor, String name) {
        super(commandExecutor, name);
       //....
    }
```

调用父类`RedissonExpirable`

```java
RedissonExpirable(CommandAsyncExecutor connectionManager, String name) {
        super(connectionManager, name);
    }
```

调用父类`RedissonObject`

```java
 public RedissonObject(CommandAsyncExecutor commandExecutor, String name) {
        this(commandExecutor.getServiceManager().getCfg().getCodec(), commandExecutor, name);
    }
```

```java
   public RedissonObject(Codec codec, CommandAsyncExecutor commandExecutor, String name) {
        this.codec = codec;
        this.commandExecutor = commandExecutor;
        if (name == null) {
            throw new NullPointerException("name can't be null");
        }

        setName(name);  //调用方法
    }
```

```java
 protected final void setName(String name) {
     //调用ServiceManager
        this.name = commandExecutor.getServiceManager().getConfig().getNameMapper().map(name);
    }
```

设置到name

回到`getLockName(threadId)`

```java
    <T> RFuture<T> tryLockInnerAsync(long waitTime, long leaseTime, TimeUnit unit, long threadId, RedisStrictCommand<T> command) {
        return commandExecutor.syncedEval(getRawName(), LongCodec.INSTANCE, command,
                "if ((redis.call('exists', KEYS[1]) == 0) " +
                            "or (redis.call('hexists', KEYS[1], ARGV[2]) == 1)) then " +
                        "redis.call('hincrby', KEYS[1], ARGV[2], 1); " +
                        "redis.call('pexpire', KEYS[1], ARGV[1]); " +
                        "return nil; " +
                    "end; " +
                    "return redis.call('pttl', KEYS[1]);",
                Collections.singletonList(getRawName()), unit.toMillis(leaseTime), getLockName(threadId));
    }
```

```java
protected String getLockName(long threadId) {
        return id + ":" + threadId;
    }
```

```java
public RedissonBaseLock(CommandAsyncExecutor commandExecutor, String name) {
        super(commandExecutor, name);
        this.id = getServiceManager().getId(); //set id value
        this.internalLockLeaseTime = getServiceManager().getCfg().getLockWatchdogTimeout();
        this.entryName = id + ":" + name;
    }
```

这个`id`来自于`RedissonBaseLock`的属性, 这个属性的值来自于`getServiceManager().getId();`

回到`tryAcquireAsync`

```java
private RFuture<Long> tryAcquireAsync(long waitTime, long leaseTime, TimeUnit unit, long threadId) {
        RFuture<Long> ttlRemainingFuture;
        if (leaseTime > 0) {
            ttlRemainingFuture = tryLockInnerAsync(waitTime, leaseTime, unit, threadId, RedisCommands.EVAL_LONG);
        } else {
            ttlRemainingFuture = tryLockInnerAsync(waitTime, internalLockLeaseTime,
                    TimeUnit.MILLISECONDS, threadId, RedisCommands.EVAL_LONG);
        }
        CompletionStage<Long> s = handleNoSync(threadId, ttlRemainingFuture);
    //...
}
```

`handleNoSync(threadId, ttlRemainingFuture);`

```java
protected final <T> CompletionStage<T> handleNoSync(long threadId, CompletionStage<T> ttlRemainingFuture) {
        return commandExecutor.handleNoSync(ttlRemainingFuture, () -> unlockInnerAsync(threadId));
    }
```



```java
public <T> CompletionStage<T> handleNoSync(CompletionStage<T> stage, Supplier<CompletionStage<?>> supplier) {
        CompletionStage<T> s = stage.handle((r, ex) -> {
            if (ex != null) {
                if (ex.getCause().getMessage().equals("None of slaves were synced")) {
                    return supplier.get().handle((r1, e) -> {
                        if (e != null) {
                            if (e.getCause().getMessage().equals("None of slaves were synced")) {
                                throw new CompletionException(ex.getCause());
                            }
                            e.getCause().addSuppressed(ex.getCause());
                        }
                        throw new CompletionException(ex.getCause());
                    });
                } else {
                    throw new CompletionException(ex.getCause());
                }
            }
            return CompletableFuture.completedFuture(r);
        }).thenCompose(f -> (CompletionStage<T>) f);
        return s;
    }
```



```java
 private RFuture<Long> tryAcquireAsync(long waitTime, long leaseTime, TimeUnit unit, long threadId) {
        RFuture<Long> ttlRemainingFuture;
        if (leaseTime > 0) {
            ttlRemainingFuture = tryLockInnerAsync(waitTime, leaseTime, unit, threadId, RedisCommands.EVAL_LONG);
        } else {
            ttlRemainingFuture = tryLockInnerAsync(waitTime, internalLockLeaseTime,
                    TimeUnit.MILLISECONDS, threadId, RedisCommands.EVAL_LONG);
        }
        CompletionStage<Long> s = handleNoSync(threadId, ttlRemainingFuture);
        ttlRemainingFuture = new CompletableFutureWrapper<>(s);

        CompletionStage<Long> f = ttlRemainingFuture.thenApply(ttlRemaining -> { //获取锁成功后做什么事情
            // lock acquired
            if (ttlRemaining == null) { //如果前面执行的结果返回的是null 说明加锁成功,那么我们就判断是否需要加入看门狗机制
                if (leaseTime > 0) { //leaseTime>0则不需要
                    internalLockLeaseTime = unit.toMillis(leaseTime);
                } else { //需要看门狗
                    scheduleExpirationRenewal(threadId);
                }
            }
            return ttlRemaining; //返回前面调用的结果,相当于是对
        });
        return new CompletableFutureWrapper<>(f);
    }

```





```java
protected void scheduleExpirationRenewal(long threadId) {
        ExpirationEntry entry = new ExpirationEntry();
        ExpirationEntry oldEntry = EXPIRATION_RENEWAL_MAP.putIfAbsent(getEntryName(), entry);
        if (oldEntry != null) {
            oldEntry.addThreadId(threadId); //如果旧的oldEntry不为空
        } else {
            entry.addThreadId(threadId); //为空 那么用新的,并且开启看门狗
            try {
                renewExpiration(); //开启看门狗
            } finally {
                if (Thread.currentThread().isInterrupted()) {
                    cancelExpirationRenewal(threadId);
                }
            }
        }
    }
```

`RedissonBaseLock.ExpirationEntry`

```java
 public static class ExpirationEntry {  //加了static

        private final Map<Long, Integer> threadIds = new LinkedHashMap<>();
        private volatile Timeout timeout;

        public ExpirationEntry() {
            super();
        }

        public synchronized void addThreadId(long threadId) {
            threadIds.compute(threadId, (t, counter) -> {
                counter = Optional.ofNullable(counter).orElse(0);
                counter++;
                return counter;
            });
        }
        public synchronized boolean hasNoThreads() {
            return threadIds.isEmpty();
        }
        public synchronized Long getFirstThreadId() {
            if (threadIds.isEmpty()) {
                return null;
            }
            return threadIds.keySet().iterator().next();
        }
        public synchronized void removeThreadId(long threadId) {
            threadIds.compute(threadId, (t, counter) -> {
                if (counter == null) {
                    return null;
                }
                counter--;
                if (counter == 0) {
                    return null;
                }
                return counter;
            });
        }

        public void setTimeout(Timeout timeout) {
            this.timeout = timeout;
        }
        public Timeout getTimeout() {
            return timeout;
        }

    }
```





```java
private void renewExpiration() {
        ExpirationEntry ee = EXPIRATION_RENEWAL_MAP.get(getEntryName()); //获取entryName
        if (ee == null) { //如果为null 直接返回
            return;
        }
        
        Timeout task = getServiceManager().newTimeout(new TimerTask() { //开启一个超时任务
            @Override
            public void run(Timeout timeout) throws Exception {
                ExpirationEntry ent = EXPIRATION_RENEWAL_MAP.get(getEntryName());
                if (ent == null) {
                    return;
                }
                Long threadId = ent.getFirstThreadId();
                if (threadId == null) {
                    return;
                }
                
                CompletionStage<Boolean> future = renewExpirationAsync(threadId); //刷新过期时间
                future.whenComplete((res, e) -> {
                    if (e != null) { //如果这个过程抛出了异常,说明存在问题 
                        log.error("Can't update lock {} expiration", getRawName(), e);
                        EXPIRATION_RENEWAL_MAP.remove(getEntryName());
                        return;
                    }
                    
                    if (res) { //如果res不等于0表示续期成功 ,那么递归调用
                        // reschedule itself
                        renewExpiration();
                    } else {
                        cancelExpirationRenewal(null);//如果等于0表示续期失败,这时候就需要取消再续期
                    }
                });
            }
        }, internalLockLeaseTime / 3, TimeUnit.MILLISECONDS); // 设置超时时间为internalLockLeaseTime的3分之1,以及设置时间单位
        ee.setTimeout(task);
    }
```



```java
 protected CompletionStage<Boolean> renewExpirationAsync(long threadId) {
        return evalWriteAsync(getRawName(), LongCodec.INSTANCE, RedisCommands.EVAL_BOOLEAN,
                "if (redis.call('hexists', KEYS[1], ARGV[2]) == 1) then " +
                        "redis.call('pexpire', KEYS[1], ARGV[1]); " +
                        "return 1; " +
                        "end; " +
                        "return 0;",
                Collections.singletonList(getRawName()),
                internalLockLeaseTime, getLockName(threadId));
    }
```

```java
protected void cancelExpirationRenewal(Long threadId) {
        ExpirationEntry task = EXPIRATION_RENEWAL_MAP.get(getEntryName());//
        if (task == null) { //如果已经被删除
            return;
        }
        
        if (threadId != null) { //如果threadId存在那么执行删除
            task.removeThreadId(threadId);
        }

        if (threadId == null || task.hasNoThreads()) { //如果传入的是null,并且task中也没有线程了 
            Timeout timeout = task.getTimeout();
            if (timeout != null) { //如果存在啊Timeout任务 那么要把它cancel了
                timeout.cancel();
            }
            EXPIRATION_RENEWAL_MAP.remove(getEntryName()); //移除
        }
    }
```





现在回到`lock`



```java
 private void lock(long leaseTime, TimeUnit unit, boolean interruptibly) throws InterruptedException {
        long threadId = Thread.currentThread().getId();
        Long ttl = tryAcquire(-1, leaseTime, unit, threadId);
        // lock acquired
        if (ttl == null) {  //加锁成功那么直接返回
            return;
        }

        CompletableFuture<RedissonLockEntry> future = subscribe(threadId); 
        pubSub.timeout(future);
        RedissonLockEntry entry;
        if (interruptibly) {
            entry = commandExecutor.getInterrupted(future);
        } else {
            entry = commandExecutor.get(future);
        }

        try {
            while (true) {
                ttl = tryAcquire(-1, leaseTime, unit, threadId);
                // lock acquired
                if (ttl == null) {
                    break;
                }

                // waiting for message
                if (ttl >= 0) {
                    try {
                        entry.getLatch().tryAcquire(ttl, TimeUnit.MILLISECONDS);
                    } catch (InterruptedException e) {
                        if (interruptibly) {
                            throw e;
                        }
                        entry.getLatch().tryAcquire(ttl, TimeUnit.MILLISECONDS);
                    }
                } else {
                    if (interruptibly) {
                        entry.getLatch().acquire();
                    } else {
                        entry.getLatch().acquireUninterruptibly();
                    }
                }
            }
        } finally {
            unsubscribe(entry, threadId);
        }
//        get(lockAsync(leaseTime, unit));
    }
```

