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

1500ms重试一次 ,重试3次



3. CAP理论中，redis是什么锁？为什么？

redis属于AP锁,首先C是指一致性,A指的是高可用性,用于redis主从之间的同步存在一定的延迟,因此不能保证查询时不存在slave节点中的key一定不存在master节点中.redis牺牲了数据的一致性而允许redis在短时间内不同步因此属于是AP锁

C , P  为什么 ,为什么不能共存. redis同步细节,zk同步细节

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

和公平的.

9. Redis分布式锁，可重入吗？具体怎么实现的可重入锁？

可以重入,分布式锁中保存的数据结构是hash结构,hashkey保存的是lock 的jvm+thread标识,而value则是保存的重入次数

10. AP锁为什么不保证强一致性？请描述细节。

因为redis的同步方式是主从拷贝,而主从的拷贝过程中存在数据的不一致,即不具备CP但是正是因为她不用保证CP因此具有较高的可用性,即AP



公平锁自测

jvm 2-3 , 两到三个线程启动 ,观察锁的获取情况.

trylock , 

可重入

看门狗自测



公平自测

























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

# 3.Redission fair lock

```lua
-- KEY[1] lockName
-- KEY[2] threadsQueueName -> redisson_lock_queue:{lockName}
-- KEY[3] timeoutSetName -> redisson_lock_timeout:{lockName}
-- ARGV[1] unit.toMillis(leaseTime)
-- ARGV[2] uuid:threadId 
-- ARGV[3] waitTime
-- ARGV[4] currentTime当前时间

				-- 删除超过等待时间的key
                    while true do 
                        local firstThreadId2 = redis.call('lindex', KEYS[2], 0);  --获取queue中头节点其值为 UUID:threadId
                        if firstThreadId2 == false then --如果为null 说明当前没有等待加锁线程,直接跳出循环
                            break;
                        end;
						-- 获取timeout zset集合中对应元素的值(过期时间)
                        local timeout = tonumber(redis.call('zscore', KEYS[3], firstThreadId2));  
    					-- 如果timeout时间小于当前时间(ARGV[4) 那么就将对应的线程 UUID:threadId 从队列和过期时间zset中移除
                        if timeout <= tonumber(ARGV[4]) then
                            -- remove the item from the queue and timeout set
                            -- NOTE we do not alter any other timeout
                            redis.call('zrem', KEYS[3], firstThreadId2);
                            redis.call('lpop', KEYS[2]);
                        else 
                            break;
                        end;
                    end;
                    -- check if the lock can be acquired now	
                    if (redis.call('exists', KEYS[1]) == 0) -- 是否存在对应的业务key
                        and ((redis.call('exists', KEYS[2]) == 0) -- 当前业务key的等待队列不存在
                            or (redis.call('lindex', KEYS[2], 0) == ARGV[2])) then  -- 当前业务key等待队列的队头元素是要加锁的线程表示
						
                       -- remove this thread from the queue and timeout set
                       redis.call('lpop', KEYS[2]); -- 从队列中移除到当前队头元素
                       redis.call('zrem', KEYS[3], ARGV[2]); -- 从超时时间zset中移除对应的线程

                        -- decrease timeouts for all waiting in the queue
                        local keys = redis.call('zrange', KEYS[3], 0, -1); -- 就获取zset集合中的所有元素，赋值给keys
   						-- 而zscore的设置是: 上一个锁的score+waitTime+currentTime
    					-- 让整个set集合中的元素都减掉waitTime 
                        for i = 1, #keys, 1 do  -- 有点不知道在干嘛
                            redis.call('zincrby', KEYS[3], -tonumber(ARGV[3]), keys[i]);
                        end;

                        -- acquire the lock and set the TTL for the lease
                        redis.call('hset', KEYS[1], ARGV[2], 1);  --上锁
                        redis.call('pexpire', KEYS[1], ARGV[1]);  --刷新过期时间
                        return nil;
                    end;

                    -- check if the lock is already held, and this is a re-entry
                    if redis.call('hexists', KEYS[1], ARGV[2]) == 1 then  -- 判断是重入
                        redis.call('hincrby', KEYS[1], ARGV[2],1);
                        redis.call('pexpire', KEYS[1], ARGV[1]);
                        return nil;
                    end;

                    -- the lock cannot be acquired
                    -- check if the thread is already in the queue
                    local timeout = redis.call('zscore', KEYS[3], ARGV[2]); -- 加锁失败 查看是否已经在队列中
                    if timeout ~= false then
                        -- the real timeout is the timeout of the prior thread
                        -- in the queue, but this is approximately correct, and
                        -- avoids having to traverse the queue
                        return timeout - tonumber(ARGV[3]) - tonumber(ARGV[4]);
                    end;

                    -- add the thread to the queue at the end, and set its timeout in the timeout set to the timeout of
                    -- the prior thread in the queue (or the timeout of the lock if the queue is empty) plus the
                    -- threadWaitTime
                    local lastThreadId = redis.call('lindex', KEYS[2], -1);
                    local ttl;
                    if lastThreadId ~= false and lastThreadId ~= ARGV[2] then
                        ttl = tonumber(redis.call('zscore', KEYS[3], lastThreadId)) - tonumber(ARGV[4]);" +
                    else 
                        ttl = redis.call('pttl', KEYS[1]);
                    end;
                    local timeout = ttl + tonumber(ARGV[3]) + tonumber(ARGV[4]);
                    if redis.call('zadd', KEYS[3], timeout, ARGV[2]) == 1 then
                        redis.call('rpush', KEYS[2], ARGV[2]); 
                    end;
                    return ttl;
```





# 4.JUC

## 1. 深入理解synchronized 细节（锁膨胀过程，标识, ）



## 2.Lock子类深入了解区别,以及源码实现.

![image-20230802170701303](C:\Users\Administrator\AppData\Roaming\Typora\typora-user-images\image-20230802170701303.png)





- AOS(AbstractOwnableSynchronizer)抽象父类 ,定义了同步器,将锁与线程id进行绑定的成员便利与get与set的方法,需要注意的是,对于保存了owner thread引用的成员变量`exclusiveOwnerThread`,加了transient关键字,确保对象经过序列化与反序列化其对象引用都不会发生变化

```java
public abstract class AbstractOwnableSynchronizer
    implements java.io.Serializable {

    /** Use serial ID even though all fields transient. */
    private static final long serialVersionUID = 3737899427754241961L;

    /**
     * Empty constructor for use by subclasses.
     */
    protected AbstractOwnableSynchronizer() { }

    /**
     * The current owner of exclusive mode synchronization.
     */
    private transient Thread exclusiveOwnerThread;
    //................
}
```

- AbstractQueuedSynchronizer(AQS) 抽象队列同步器

AQS还有个兄弟AbstractQueuedLongSynchronizer(AQLS) 两兄弟所有操作都是一致的,唯一的不同是他们维护的状态字是int与lang的区别.

通过阅读代码注释,有些比较关键的地方如下

`This class supports either or both a default exclusive mode and a shared mode`

AQS支持独占模式和享元模式.(默认是独占模式)

AQS提供了两种工作模式：独占(exclusive)模式和共享(shared)模式。它的所有子类中，要么实现并使用了它独占功能的 API，要么使用了共享功能的API，而不会同时使用两套 API，即便是它最有名的子类 ReentrantReadWriteLock，也是通过两个内部类：读锁和写锁，分别实现的两套 API 来实现的。

 独占模式即当锁被某个线程成功获取时，其他线程无法获取到该锁，共享模式即当锁被某个线程成功获取时，其他线程仍然可能获取到该锁。
reference: https://blog.csdn.net/weixin_43823391/article/details/114259447

```java
	This class does not "understand" these differences except in the mechanical sense that when a shared mode acquire succeeds, the next waiting thread (if one exists) must also determine whether it can acquire as well. Threads waiting in the different modes share the same FIFO queue. Usually, implementation subclasses support only one of these modes, but both can come into play for example in a ReadWriteLock. Subclasses that support only exclusive or only shared modes need not define the methods supporting the unused mode
```



```java
	This class defines a nested AbstractQueuedSynchronizer.ConditionObject class that can be used as a Condition implementation by subclasses supporting exclusive mode for which method isHeldExclusively reports whether synchronization is exclusively held with respect to the current thread,method release invoked with the current getState value fully releases this object, and acquire, given this saved state value, eventually restores this object to its previous acquired state. .... 
    The behavior of AbstractQueuedSynchronizer.ConditionObject depends <of course> on the semantics of its synchronizer implementation.
    
用ConditionObject来支持exclusive mode,并且通过isHeldExclusively方法report是否是独占性持有
ConditionObject的行为取决于实现类的逻辑
```



```java
Serialization of this class stores only the underlying atomic integer maintaining state, so deserialized objects have empty thread queues. Typical subclasses requiring serializability will define a readObject method that restores this to a known initial state upon deserialization
    对这个类的序列化指挥保存state属性,序列与反序列化后只会得到空的quere
```



```
To use this class as the basis of a synchronizer, redefine the following methods, as applicable, by inspecting and/or modifying the synchronization state using getState, setState and/or compareAndSetState:
tryAcquire
tryRelease
tryAcquireShared
tryReleaseShared
isHeldExclusively
用这个类作为同步器的基础,只需覆盖5个方法,方法中只是用state字段的方法getState,setState,compareAndSetState方法来控制同步行为

Each of these methods by default throws UnsupportedOperationException. Implementations of these methods must be internally thread-safe, and should in general be short and not block. Defining these methods is the only supported means of using this class. All other methods are declared final because they cannot be independently varied.
这些方法会默认抛出异常,继承覆盖的这些方法必须是线程安全,逻辑简洁,不会阻塞.除此之外,其他的方法都定义为final,或者不可变的.
```



```
Even though this class is based on an internal FIFO queue, it does not automatically enforce FIFO acquisition policies. The core of exclusive synchronization takes the form:
  Acquire:
      while (!tryAcquire(arg)) {
         enqueue thread if it is not already queued;
         possibly block current thread;
      }
 
  Release:
      if (tryRelease(arg))
         unblock the first queued thread;
 尽管该类基于内部先进先出队列，但它不会自动执行先进先出获取策略。独占同步的核心形式如代码所示
```



```
(Shared mode is similar but may involve cascading signals.)
	Because checks in acquire are invoked before enqueuing, a newly acquiring thread may barge ahead of others that are blocked and queued. However, you can, if desired, define tryAcquire and/or tryAcquireShared to disable barging by internally invoking one or more of the inspection methods, thereby providing a fair FIFO acquisition order. In particular, most fair synchronizers can define tryAcquire to return false if hasQueuedPredecessors (a method specifically designed to be used by fair synchronizers) returns true. Other variations are possible.
(共享模式类似，但可能涉及级联信号）。
由于获取中的检查是在排队之前调用的，因此新获取的线程可能会抢在其他被阻塞和排队的线程之前。不过，如果需要，可以定义 tryAcquire 和/或 tryAcquireShared，通过内部调用一个或多个检查方法来禁止闯入，从而提供公平的 FIFO 获取顺序。特别是，如果 hasQueuedPredecessors（一种专门设计用于公平同步器的方法）返回 true，大多数公平同步器可以定义 tryAcquire 为 false。其他变体也是可能的。
	Throughput and scalability are generally highest for the default barging (also known as greedy, renouncement, and convoy-avoidance) strategy. While this is not guaranteed to be fair or starvation-free, earlier queued threads are allowed to recontend before later queued threads, and each recontention has an unbiased chance to succeed against incoming threads. Also, while acquires do not "spin" in the usual sense, they may perform multiple invocations of tryAcquire interspersed with other computations before blocking. This gives most of the benefits of spins when exclusive synchronization is only briefly held, without most of the liabilities when it isn't. If so desired, you can augment this by preceding calls to acquire methods with "fast-path" checks, possibly prechecking hasContended and/or hasQueuedThreads to only do so if the synchronizer is likely not to be contended.
	一般来说，默认驳船（也称贪婪、放弃和避让）策略的吞吐量和可扩展性最高。虽然这不能保证公平或无饥饿，但允许排队较早的线程在排队较晚的线程之前重新连接，而且每次重新连接都有机会成功对抗进入的线程。此外，虽然获取线程不会进行通常意义上的 "旋转"，但它们可以在阻塞前多次调用 tryAcquire，并穿插其他计算。这样，当独占同步仅被短暂保持时，就能获得自旋的大部分好处，而当独占同步未被保持时，就不会有大部分的麻烦。如果需要，还可以在调用获取方法之前进行 "快速路径 "检查，可能的话预先检查 hasContended 和/或 hasQueuedThreads，只有在同步器可能不会被竞争的情况下才进行调用。
	This class provides an efficient and scalable basis for synchronization in part by specializing its range of use to synchronizers that can rely on int state, acquire, and release parameters, and an internal FIFO wait queue. When this does not suffice, you can build synchronizers from a lower level using atomic classes, your own custom java.util.Queue classes, and LockSupport blocking support.
	该类为同步提供了一个高效且可扩展的基础，部分原因是它将使用范围限定为可依赖 int 状态、获取和释放参数以及内部 FIFO 等待队列的同步器。如果这还不够，您可以使用原子类、您自己的自定义 java.util.Queue 类和 LockSupport 阻塞支持，从较低层次构建同步器。

```







## 3.信号量



## 4.深入了解 并知道使用场景,以及实现原理 :

AQS(AbstractQueuedSynchronizer)\ReentrantLock\ReentrantReadWriteLock\CountDownLatch\Semphore\

 

## 5.请解释一下Synchronized的锁粗化,什么场景使用到锁粗化? 怎么弄?



## 6.描述一下Synchronized锁膨胀 每一步的具体细节? 锁膨胀以后,没有请求了,依然是重量级

## 锁怎么办?



## 7.Synchronized头信息存了什么,膨胀的每一步存储哪些东西,为什么要存这些?





## 8.ReentrantLock中公平锁,非公平锁都实现了抽象类AbstractQueuedSynchronizer,  请问,AQS里面的原理是什么? 为什么要实现AQS?

## 9.ReentrantLock和ReentrantReadWriteLock实现原理的区别是什么? 写操作多于读操作的时候,应该用哪个锁?



##  10.ReentrantReadWriteLock什么情况下共享模式,什么情况下独占模式?请列举相关代码.



## 11.AQS如何实现的FIFO? 请什么原理以及列举相关代码.



## 12.Lock怎么实现的锁超时? 例如: tryLock(long ***\*timeout\****, TimeUnit unit)



## 13CountDownLatch 如何实现计数器? 其中await是怎么实现的? await( long timeout,TimeUnit unit) 超时机制是怎么实现的?

