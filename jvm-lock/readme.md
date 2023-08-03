# 1.JUC

## 1. 深入理解synchronized 细节（锁膨胀过程，标识, ）



## 2.Lock子类深入了解区别,以及源码实现.

![image-20230802170701303](C:\Users\Administrator\AppData\Roaming\Typora\typora-user-images\image-20230802170701303.png)



### 1. AOS(AbstractOwnableSynchronizer)

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

### 2.AbstractQueuedSynchronizer(AQS) 抽象队列同步器

#### 1.AQS简介

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

`Mutex` 互斥锁实现

```java
package com.woldier.concurrent;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.Serializable;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.AbstractQueuedSynchronizer;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;

/***
 *
 * description metex 互斥信号
 *
 * @author: woldier wong
 * @date: 2023/8/3 9:46
 */
public class Mutex implements Lock, Serializable {


    private static final long serialVersionUID = 468879461741563192L;

    static class Sync extends AbstractQueuedSynchronizer {


        @Override
        protected boolean tryAcquire(int arg) {
            //做的事情是尝试加锁,如果成功返回true,否则返回false
            assert arg == 1; //断言加锁参数是否是1 ,不是的话那么就会抛出异常
            if (compareAndSetState(0, 1)) {
                //如果是加锁成功了,那么就锁的拥有者为当前线程,
                //这里的逻辑不需要cas 是因为 加锁成功的只可能有一个,因此不存在并发修改
                setExclusiveOwnerThread(Thread.currentThread());
                return true;
            }
            return false;
        }


        @Override
        protected boolean tryRelease(int arg) {
            if (Thread.currentThread() != getExclusiveOwnerThread() || getState() == 0) { //请求解锁的线程并不是持有锁的线程
                throw new IllegalMonitorStateException("当前线程并未持有锁");
            }
            //这里需要先设置当前线程拥有者为null,然后再去改变state
            //如果是先改变state 再去设置setExclusiveOwnerThread 可能另一个线程以及设置了ExclusiveOwnerThread因此可能存在线程安全问题
            setExclusiveOwnerThread(null);
            setState(0);
            return true;
        }

        @Override
        protected boolean isHeldExclusively() {
            return getState() == 1; //是否是独占模式,mutex就是独占模式,因此如果状态是1的话 那么说当前lock被独占持有
        }

        Condition newCondition() {
            return new ConditionObject();
        }

        // Deserializes properly
        private void readObject(ObjectInputStream s) throws IOException, ClassNotFoundException {
            s.defaultReadObject();
            setState(0); // reset to unlocked state
        }

    }
    private final Sync sync = new Sync();
    @Override
    public void lock() {
        sync.acquire(1);
    }

    @Override
    public void lockInterruptibly() throws InterruptedException {
        sync.acquireInterruptibly(1);
    }

    @Override
    public boolean tryLock() {
        return sync.tryAcquire(1);
    }

    @Override
    public boolean tryLock(long time, TimeUnit unit) throws InterruptedException {
        return sync.tryAcquireNanos(1,unit.toNanos(time));
    }


    @Override
    public void unlock() {
        sync.release(1);
    }

    @Override
    public Condition newCondition() {
        return sync.newCondition();
    }
}

```

#### 2.AQS 内部等待队列



​	等待队列是 "CLH"（Craig、Landin 和 Hagersten）锁队列的一种变体。CLH 锁通常用于自旋锁。而我们将其用于阻塞同步器，但使用了相同的基本策略，即在线程节点的前置节点中保存线程的部分控制信息。每个节点中都有一个 "状态 "字段，用于跟踪线程是否应该阻塞。当一个节点的前节点释放时，就会发出信号。除此之外，队列的每个节点都是一个特定通知式监视器，负责监视一个等待线程。状态字段并不控制线程是否被授予锁等。线程可以尝试获取队列中的第一个锁。但是，排在第一位并不能保证成功，它只是给予了竞争的权利。因此，当前被释放的竞争者线程可能需要重新等待。

​	插入 CLH 队列只需要对 "尾部 "进行一次原子操作，因此从未入队到入队有一个简单的原子分界点。同样，去排队只涉及更新 "头 "和 "尾"。 不过，节点要确定谁是自己的继任者，还需要做更多的工作，原因是要处理可能因超时和中断而取消的情况

​	prev "链接（在最初的 CLH 锁中没有使用）主要用于处理取消。如果一个节点被取消，它的后继节点（通常）会重新链接到未被取消的前继节点。有关自旋锁中类似机制的解释，请参阅斯科特和舍勒的论文，网址是 http://www.cs.rochester.edu/u/scott/synchronization/。

We also use "next" links to implement blocking mechanics. The thread id for each node is kept in its own node, so a predecessor signals the next node to wake up by traversing next link to determine which thread it is. Determination of successor must avoid races with newly queued nodes to set the "next" fields of their predecessors. This is solved when necessary by checking backwards from the atomically updated "tail" when a node's successor appears to be null. (Or, said differently, the next-links are an optimization so that we don't usually need a backward scan.)

我们还使用 "下一个 "链接来实现阻塞机制。每个节点的线程 ID 都保存在自己的节点中，因此前节点会通过遍历下一个链接来确定下一个节点是哪个线程，从而发出唤醒信号。后继节点的确定必须避免与新排队的节点竞争，以设置其前辈节点的 "下一个 "字段。必要时，可以通过在节点的后继者似乎为空时从原子更新的 "尾部 "向后检查来解决这个问题。(或者换一种说法，"下一个链接 "是一种优化，因此我们通常不需要向后扫描）。

 AQS类中定义了一些与队列

```java
    /**
     * The number of nanoseconds for which it is faster to spin
     * rather than to use timed park. A rough estimate suffices
     * to improve responsiveness with very short timeouts.
     * 自旋比定时驻留时间快的阈值,这是一个粗略估计的值
     */
    static final long spinForTimeoutThreshold = 1000L;
```

```java
    /**
     * Inserts node into queue, initializing if necessary. See picture above.
     * @param node the node to insert
     * @return node's predecessor
     */
    private Node enq(final Node node) {
        for (;;) {  //自旋保证一定能够入队
            Node t = tail; //保存old队尾指针
            if (t == null) { // Must initialize 如果old tail指向的是null ,那么说明队空,那么需要初始化队列
                if (compareAndSetHead(new Node())) //设置队头
                    tail = head; //如果设置成功,那么队尾指向队头 ,然后结束本次循环,进入下次循环去做enq
            } else { //如果old 队尾指针不为null ,那么说明当前队列不为空,已经经过了初始化
                node.prev = t; //设置node节点的前驱为 t(old tial)
                if (compareAndSetTail(t, node)) { //把node 设置成新的队尾 
                    t.next = node; //如果竞争成功 那么把 t(old tial)的next(原来指向null) 指向node
                    return t; //返回 t(old tial)
                } //如果不成功
            }
        }
    }
```



```java
    /**
     * Creates and enqueues node for current thread and given mode.
     *
     * @param mode Node.EXCLUSIVE for exclusive, Node.SHARED for shared
     * @return the new node
     */
    private Node addWaiter(Node mode) {
        Node node = new Node(Thread.currentThread(), mode); //创建一个新的节点,设置该节点的nextWaiter属性指向mode
        // Try the fast path of enq; backup to full enq on failure
        // 下面到     enq(node);之前的代码其实就是想入队,这样做是为了快速入队,如果不行再去走完整的enq方法
        Node pred = tail; 
        if (pred != null) {
            node.prev = pred;
            if (compareAndSetTail(pred, node)) {
                pred.next = node;
                return node;
            }
        }
        enq(node);
        return node;
    }

   Node(Thread thread, Node mode) {     // Used by addWaiter (addWaiter方法中创建node节点所使用的构造方法)
            this.nextWaiter = mode;
            this.thread = thread;
        }
```



```java
class Node {
        /** waitStatus value to indicate thread has cancelled */
        static final int CANCELLED =  1;
        /** waitStatus value to indicate successor's thread needs unparking */
        static final int SIGNAL    = -1;
        /** waitStatus value to indicate thread is waiting on condition */
        static final int CONDITION = -2;
        /**
         * waitStatus value to indicate the next acquireShared should
         * unconditionally propagate
         */
        static final int PROPAGATE = -3;    
    // ...................
}

	/**
     * Wakes up node's successor, if one exists.
     * 
     * @param node the node
     */
    private void unparkSuccessor(Node node) {
        /*
         * If status is negative (i.e., possibly needing signal) try
         * to clear in anticipation of signalling.  It is OK if this
         * fails or if status is changed by waiting thread.
         如果状态为负值（即可能需要信号），则尝试
          清除，以等待信号。 如果这样做
         失败或状态被等待线程改变也没关系。
         */
        int ws = node.waitStatus; //获取node 当前的state
        if (ws < 0) //如果小于零 
            compareAndSetWaitStatus(node, ws, 0); //做cas设置状态为0  看到这里还不确定为什么需要使用cas

        /*
         * Thread to unpark is held in successor, which is normally
         * just the next node.  But if cancelled or apparently null,
         * traverse backwards from tail to find the actual
         * non-cancelled successor.
         解除park的线程由后继持节点持有，后继通常是
          只是下一个节点。 但如果取消或显然为空、
          从尾部向后遍历，找到实际的
          非取消的后继节点。
         */
        Node s = node.next; //得到node的后继
        if (s == null || s.waitStatus > 0) { //如果后继为null,这种情况有可能是出现在node节点原来为tail节点,但是现在有个新的节点enq,此时tail指向新的节点,但是由于线程切换了,node节点的next指针还没有从null指向新入队的节点,因此需要从tail往前找(可以往前找是因为新的节点在成为tail之前设置了他的prev指针指向旧的tail的,详细可以参阅enq方法);或者是s.waitStatus大于零 ,那么需要找到非取消的后继节点
            s = null;
            for (Node t = tail; t != null && t != node; t = t.prev) //从tail向前面遍历 找到,距离node最小的,waitStatus小于零的后继
                if (t.waitStatus <= 0) //判断t的state是否小于等于0,如果不小于零是不用更改s那么就可以保证,s要么指向null要么指向的节点一定state<=0
                    s = t;  //设置s
        }
        if (s != null) //如果s不等于null 
            LockSupport.unpark(s.thread); //unpack
    }
```



```java
    /**
     * Release action for shared mode -- signals successor and ensures
     * propagation. (Note: For exclusive mode, release just amounts
     * to calling unparkSuccessor of head if it needs signal.)
     */
    private void doReleaseShared() {
        /*
         * Ensure that a release propagates, even if there are other
         * in-progress acquires/releases.  This proceeds in the usual
         * way of trying to unparkSuccessor of head if it needs
         * signal. But if it does not, status is set to PROPAGATE to
         * ensure that upon release, propagation continues.
         * Additionally, we must loop in case a new node is added
         * while we are doing this. Also, unlike other uses of
         * unparkSuccessor, we need to know if CAS to reset status
         * fails, if so rechecking.
         确保release 传播，即使有其他正在进行的acquires/releases。 
         如果需要信号，则会尝试头部的unparkSuccessor。但如果不需要，则会将状态设置为 PROPAGATE，以确保释放后继续传播。
         此外，如果有新节点添加进来，我们必须循环处理时。
         另外，与 unparkSuccessor 不同，我们需要知道重置状态的 CAS 如果失败，则需要重新检查。
         */
        for (;;) {
            Node h = head;
            if (h != null && h != tail) {
                int ws = h.waitStatus;
                if (ws == Node.SIGNAL) {
                    if (!compareAndSetWaitStatus(h, Node.SIGNAL, 0))
                        continue;            // loop to recheck cases
                    unparkSuccessor(h);
                }
                else if (ws == 0 &&
                         !compareAndSetWaitStatus(h, 0, Node.PROPAGATE))
                    continue;                // loop on failed CAS
            }
            if (h == head)                   // loop if head changed
                break;
        }
    }
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

