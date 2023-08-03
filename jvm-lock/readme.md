# 1.JUC

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

