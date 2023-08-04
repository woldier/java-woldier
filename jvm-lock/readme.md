# 1.JUC

## 1.1. 深入理解synchronized 细节（锁膨胀过程，标识, ）



## 1.2.Lock子类深入了解区别,以及源码实现.

![image-20230802170701303](C:\Users\Administrator\AppData\Roaming\Typora\typora-user-images\image-20230802170701303.png)



### 1.2.1. AOS(AbstractOwnableSynchronizer)

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

### 1.2.2.AbstractQueuedSynchronizer(AQS) 抽象队列同步器

#### 1.2.2.1.AQS简介

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

#### 1.2.2.2.AQS 内部等待队列



​	等待队列是 "CLH"（Craig、Landin 和 Hagersten）锁队列的一种变体。CLH 锁通常用于自旋锁。而我们将其用于阻塞同步器，但使用了相同的基本策略，即在线程节点的前置节点中保存线程的部分控制信息。每个节点中都有一个 "状态 "字段，用于跟踪线程是否应该阻塞。当一个节点的前节点释放时，就会发出信号。除此之外，队列的每个节点都是一个特定通知式监视器，负责监视一个等待线程。状态字段并不控制线程是否被授予锁等。线程可以尝试获取队列中的第一个锁。但是，排在第一位并不能保证成功，它只是给予了竞争的权利。因此，当前被释放的竞争者线程可能需要重新等待。

​	插入 CLH 队列只需要对 "尾部 "进行一次原子操作，因此从未入队到入队有一个简单的原子分界点。同样，去排队只涉及更新 "头 "和 "尾"。 不过，节点要确定谁是自己的继任者，还需要做更多的工作，原因是要处理可能因超时和中断而取消的情况

​	prev "链接（在最初的 CLH 锁中没有使用）主要用于处理取消。如果一个节点被取消，它的后继节点（通常）会重新链接到未被取消的前继节点。有关自旋锁中类似机制的解释，请参阅斯科特和舍勒的论文，网址是 http://www.cs.rochester.edu/u/scott/synchronization/。

We also use "next" links to implement blocking mechanics. The thread id for each node is kept in its own node, so a predecessor signals the next node to wake up by traversing next link to determine which thread it is. Determination of successor must avoid races with newly queued nodes to set the "next" fields of their predecessors. This is solved when necessary by checking backwards from the atomically updated "tail" when a node's successor appears to be null. (Or, said differently, the next-links are an optimization so that we don't usually need a backward scan.)

我们还使用 "下一个 "链接来实现阻塞机制。每个节点的线程 ID 都保存在自己的节点中，因此前节点会通过遍历下一个链接来确定下一个节点是哪个线程，从而发出唤醒信号。后继节点的确定必须避免与新排队的节点竞争，以设置其前辈节点的 "下一个 "字段。必要时，可以通过在节点的后继者似乎为空时从原子更新的 "尾部 "向后检查来解决这个问题。(或者换一种说法，"下一个链接 "是一种优化，因此我们通常不需要向后扫描）。

 AQS类中定义了一些与队列

##### 1.2.2.2.1.enq

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
                if (compareAndSetHead(new Node())) //设置队头 此队列时有哨兵节点的
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

##### 1.2.2.2.2.addWaiter

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

##### 1.2.2.2.3.unparkSuccessor

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
            compareAndSetWaitStatus(node, ws, 0); //做cas设置状态为0  (这里使用cas操作是因为其他的线程也有可能重新设置waitState的值)

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

##### 1.2.2.2.4.doReleaseShared

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
         如果需要信号，则会尝试头节点的unparkSuccessor。但如果不需要，则会将状态设置为 PROPAGATE，以确保释放后继续传播。
         此外，如果有新节点添加进来，我们必须循环处理时。
         另外，与 unparkSuccessor 不同，我们需要知道重置状态的 CAS 如果失败，则需要重新检查。
         */
        for (;;) {
            Node h = head; //记录head节点的引用 
            if (h != null && h != tail) { //如果h 为null 说明现在队列还没有初始化,或者h = tial 说明只有一个节点(哨兵) 那么是空队列
                int ws = h.waitStatus;  //记录头节点的state
                if (ws == Node.SIGNAL) { //如果当前节点的waitState是Node.SIGNA
                    if (!compareAndSetWaitStatus(h, Node.SIGNAL, 0))  //cas设置成0不成功,进行下次循环
                        continue;            // loop to recheck cases
                    unparkSuccessor(h); //cas 成功设置成0 那么就唤醒后继
                }
                else if (ws == 0 && //ws是初始状态并且设置成Node.PROPAGATE失败,进行下次循环
                         !compareAndSetWaitStatus(h, 0, Node.PROPAGATE))
                    continue;                // loop on failed CAS
            }
             //能够执行到这里  说明现在已经将head的值设置成了Node.PROPAGATE
            if (h == head)                   // loop if head changed 
                break;
        }
    }
```

##### 1.2.2.2.5.setHeadAndPropagate

```java
    /**
     * Sets head of queue, and checks if successor may be waiting
     * in shared mode, if so propagating if either propagate > 0 or
     * PROPAGATE status was set.
     * 设置头节点,并且检查后继是否是共享模式,如果是共享模式那么进行传播,
     * @param node the node
     * @param propagate the return value from a tryAcquireShared
     */
    private void setHeadAndPropagate(Node node, int propagate) {
        Node h = head; // Record old head for check below
        setHead(node);
        /*
         * Try to signal next queued node if:
         *   Propagation was indicated by caller,
         *     or was recorded (as h.waitStatus either before
         *     or after setHead) by a previous operation
         *     (note: this uses sign-check of waitStatus because
         *      PROPAGATE status may transition to SIGNAL.)
         * and
         *   The next node is waiting in shared mode,
         *     or we don't know, because it appears null
         *
         * The conservatism in both of these checks may cause
         * unnecessary wake-ups, but only when there are multiple
         * racing acquires/releases, so most need signals now or soon
         * anyway.
         */
        if (propagate > 0 || h == null || h.waitStatus < 0 ||
            (h = head) == null || h.waitStatus < 0) {
            Node s = node.next;
            if (s == null || s.isShared())
                doReleaseShared();
        }
    }

```

##### 1.2.2.2.6.cancelAcquire

```java
    /**
     * Cancels an ongoing attempt to acquire.
     *
     * @param node the node
     */
    private void cancelAcquire(Node node) {
        // Ignore if node doesn't exist
        if (node == null)  //node 不存在
            return;

        node.thread = null; //取消node 与thread的绑定

        // Skip cancelled predecessors
        Node pred = node.prev; //记录node 的前驱
        while (pred.waitStatus > 0) //如果前驱节点waitStatus是 1 说明已经cancel ,那么我们循环找前驱的前驱 直到跳出循环
            node.prev = pred = pred.prev;

        // predNext is the apparent node to unsplice. CASes below will
        // fail if not, in which case, we lost race vs another cancel
        // or signal, so no further action is necessary.
        Node predNext = pred.next;

        // Can use unconditional write instead of CAS here.
        // After this atomic step, other Nodes can skip past us.
        // Before, we are free of interference from other threads.
        node.waitStatus = Node.CANCELLED;

        // If we are the tail, remove ourselves.
        if (node == tail && compareAndSetTail(node, pred)) { //如果cancle的节点是tail
            compareAndSetNext(pred, predNext, null);
        } else {
            // If successor needs signal, try to set pred's next-link
            // so it will get one. Otherwise wake it up to propagate.
            int ws;
            if (pred != head &&  //如果node 的前驱不是头节点
                ((ws = pred.waitStatus) == Node.SIGNAL || //并且 (前驱的ws是signal或者(ws的状态<=0且成功cas设置前驱的ws为signal))
                 (ws <= 0 && compareAndSetWaitStatus(pred, ws, Node.SIGNAL))) &&
                pred.thread != null) { //并且 前驱中保存的线程引用不为null
                Node next = node.next; //记录node 的后继
                if (next != null && next.waitStatus <= 0) //后继不为null并且后继的ws<=0
                    compareAndSetNext(pred, predNext, next); //cas 设置 node的前驱(pred)的新 后继为node的后继(next)
            } else { //不满足的话
                unparkSuccessor(node); //释放unpack 后继
            }

            node.next = node; // help GC
        }
    }
```

##### 1.2.2.2.7.shouldParkAfterFailedAcquire

```java
    /**
     * Checks and updates status for a node that failed to acquire.
     * Returns true if thread should block. This is the main signal
     * control in all acquire loops.  Requires that pred == node.prev.
     *
     * @param pred node's predecessor holding status
     * @param node the node
     * @return {@code true} if thread should block
     */
    private static boolean shouldParkAfterFailedAcquire(Node pred, Node node) {
        int ws = pred.waitStatus;
        if (ws == Node.SIGNAL)
            /*
             * This node has already set status asking a release
             * to signal it, so it can safely park.
             */
            return true;
        if (ws > 0) {
            /*
             * Predecessor was cancelled. Skip over predecessors and
             * indicate retry.
             */
            do {
                node.prev = pred = pred.prev;
            } while (pred.waitStatus > 0);
            pred.next = node; //找到第一个ws不大于零的前驱
        } else {
            /*
             * waitStatus must be 0 or PROPAGATE.  Indicate that we
             * need a signal, but don't park yet.  Caller will need to
             * retry to make sure it cannot acquire before parking.
             */
            compareAndSetWaitStatus(pred, ws, Node.SIGNAL);
        }
        return false;
    }
```

##### 1.2.2.2.8.acquireQueued

```java
    /**
     * Acquires in exclusive uninterruptible mode for thread already in
     * queue. Used by condition wait methods as well as acquire.
     *
     * @param node the node
     * @param arg the acquire argument
     * @return {@code true} if interrupted while waiting
     * 本方法pack被打断会继续尝试获取锁,而不会因为线程被打断而停止回去锁,为了检测获取锁过程是否被打断过,因此设置了一个interrupted来记录
     */
    final boolean acquireQueued(final Node node, int arg) {
        boolean failed = true;
        try {
            boolean interrupted = false;
            for (;;) {
                final Node p = node.predecessor();  // 得到前驱
                //如果p是头节点,并且尝试获取锁成功
                if (p == head && tryAcquire(arg)) { //如果p是前驱,并且尝试获取锁成功了
                    setHead(node); //那么该节点成为新的头节点
                    p.next = null; // help GC 设置原来头节点的next 指针为null 相当于p出队了
                    failed = false; //设置失败标志为false
                    return interrupted; //返回打断标记
                }
                //如果上面if方法内的逻辑没有执行 那么会走到这里
                if (shouldParkAfterFailedAcquire(p, node) && // 进入shouldParkAfterFailedAcquire方法 如果当前节点的ws是signal那么就接着执行 parkAndCheckInterrupt,如果ws的状态不是signal 那么会设置ws为signal 然后返回false 那么if条件不成立,进入下一次循环,下次执行到这里的时候 进入shouldParkAfterFailedAcquire方法就会返回true了.
                    parkAndCheckInterrupt()) //如果shouldParkAfterFailedAcquire返回true就会进入parkAndCheckInterrupt pack住了,当被unpack会返回false 或者 被打断就会 返回 ture ,此时if条件成立了,那么就会设置interrupted标记为真,便于后续返回
                    interrupted = true;
            }
        } finally {  
            if (failed)  // 最终会执行这个判断,如果是走的加锁成功那么failed一定是flase,其他情况就是true,这时候就要从queue中移除node
                cancelAcquire(node);
        }
    }
```



> # 按照目前的理解 现在能够走到finally 那么一定是获取锁成功了,不知道失败是怎么跳出来的
>
> 首先我猜测是不是在没有pack时调用线程的interrupt方法,看看是不是会跳出循环
>
> ```java
>  private static int tag = 1;
>     @Test
>     public void test() throws InterruptedException {
>         Thread thread = new Thread(() -> {
>             boolean failed = true;
>             try {
>                 boolean interrupted = false;
>                 log.debug("进入死循环");
>                 for (;;) {
> 
>                     if(tag == 0){
>                         failed = false;
>                         return;
>                     }
>                 }
>             } finally {
>                 if (failed)  // 最终会执行这个判断,如果是走的加锁成功那么failed一定是flase,其他情况就是true,这时候就要从queue中移除node
>                     log.debug("失败标记为true");
>             }
>         }
>         );
> 
>         thread.start();
>         TimeUnit.SECONDS.sleep(2);
>         log.debug("去打断");
>         thread.interrupt();
>         thread.join();
>     }
> ```
>
> 查看控制台
>
> ```shell
> 14:27:18.526 [Thread-1] DEBUG com.woldier.TsetInterrapt - 进入死循环
> 14:27:20.538 [main] DEBUG com.woldier.TsetInterrapt - 去打断
> runing..
> ```
>
> 显然 这无法导致跳出循环
>
> 然后acquireQueued里面已有的代码是都没有办法做跳出循环的操作的,唯一看不到代码的是由子类实现的tryAcquire(arg)方法,我去找了一下Reentraent的源码,其tryAcquire(arg)中有这么一行代码
>
> ```java
>         /**
>          * Performs non-fair tryLock.  tryAcquire is implemented in
>          * subclasses, but both need nonfair try for trylock method.
>          */
>         final boolean nonfairTryAcquire(int acquires) {
>            //....
>             else if (current == getExclusiveOwnerThread()) {
>                 int nextc = c + acquires;
>                 if (nextc < 0) // overflow
>                     throw new Error("Maximum lock count exceeded");
>             	//...
>             }
>        //...
>         }
> ```
>
> 可以看到她抛出了一个Error异常
>
> 跟进去看到源码注释,注释说这个类是应用不需要cacth的
>
> ```java
> /**
>  * An {@code Error} is a subclass of {@code Throwable}
>  * that indicates serious problems that a reasonable application
>  * should not try to catch. Most such errors are abnormal conditions.
>  * The {@code ThreadDeath} error, though a "normal" condition,
>  * is also a subclass of {@code Error} because most applications
>  * should not try to catch it.
>  * <p>
>  * A method is not required to declare in its {@code throws}
>  * clause any subclasses of {@code Error} that might be thrown
>  * during the execution of the method but not caught, since these
>  * errors are abnormal conditions that should never occur.
>  *
>  * That is, {@code Error} and its subclasses are regarded as unchecked
>  * exceptions for the purposes of compile-time checking of exceptions.
>  *
>  * @author  Frank Yellin
>  * @see     java.lang.ThreadDeath
>  * @jls 11.2 Compile-Time Checking of Exceptions
>  * @since   JDK1.0
>  */
> public class Error extends Throwable {
> ```
>
> 修改测试代码
>
> ```java
>  @Test
>     public void test2() throws InterruptedException {
>         Thread thread = new Thread(() -> {
>             boolean failed = true;
>             try {
>                 boolean interrupted = false;
>                 log.debug("进入死循环");
>                 for (;;) {
>                     if(tag == 0){
>                         failed = false;
>                         return;
>                     }
> //                    throw new Error("异常跳出");
>                     throw new RuntimeException("出错了");
>                 }
>             } finally {
>                 if (failed)  // 最终会执行这个判断,如果是走的加锁成功那么failed一定是flase,其他情况就是true,这时候就要从queue中移除node
>                     log.debug("失败标记为true");
>             }
>         }
>         );
>         thread.start();
>         TimeUnit.SECONDS.sleep(2);
>         thread.join();
>     }
> ```
>
> 控制台打印的结果如下
>
> ```shell
> 
> 14:49:26.447 [Thread-1] DEBUG com.woldier.TsetInterrapt - 进入死循环
> 14:49:26.451 [Thread-1] DEBUG com.woldier.TsetInterrapt - 失败标记为true
> Exception in thread "Thread-1" java.lang.RuntimeException: 出错了
> 	at com.woldier.TsetInterrapt.lambda$test2$1(TsetInterrapt.java:55)
> 	at java.lang.Thread.run(Thread.java:748)
> 
> Process finished with exit code 0
> ```
>
> 跳出了循环











##### 1.2.2.2.9.doAcquireInterruptibly

```java
    /**
     * Acquires in exclusive interruptible mode.
     * @param arg the acquire argument
     此方法的逻辑与acquireQueued逻辑时差不多的
	唯一的不同是parkAndCheckInterrupt unpack后如果是被打断会返回ture那么会进入if的内部抛出异常
     */
    private void doAcquireInterruptibly(int arg)
        throws InterruptedException {
        final Node node = addWaiter(Node.EXCLUSIVE);  //添加一个节点到queue中,并且设置为独占模式
        boolean failed = true;
        try {
            for (;;) {
                final Node p = node.predecessor();
                if (p == head && tryAcquire(arg)) {
                    setHead(node);
                    p.next = null; // help GC
                    failed = false;
                    return;
                }
                if (shouldParkAfterFailedAcquire(p, node) &&
                    parkAndCheckInterrupt())//唯一的不同是parkAndCheckInterrupt unpack后如果是被打断后会进入if的内部抛出异常
                    throw new InterruptedException();
            }
        } finally {
            if (failed)
                cancelAcquire(node); //如果是失败,那么就会cancel
        }
    }
```

##### 1.2.2.2.10.doAcquireNanos

```java
    /**
     * Acquires in exclusive timed mode.
     * 独占模式,并且有超时时间(允许打断)
     * @param arg the acquire argument
     * @param nanosTimeout max wait time
     * @return {@code true} if acquired
     */
    private boolean doAcquireNanos(int arg, long nanosTimeout)
            throws InterruptedException {
        if (nanosTimeout <= 0L)
            return false;
        final long deadline = System.nanoTime() + nanosTimeout; //得到超时时间
        final Node node = addWaiter(Node.EXCLUSIVE); //入队并指定模式为独占
        //后续逻辑与doAcquireInterruptibly基本相同,因此不再赘述
        boolean failed = true;
        try {
            for (;;) {
                final Node p = node.predecessor();
                if (p == head && tryAcquire(arg)) {  //获取成功的判断
                    setHead(node);
                    p.next = null; // help GC
                    failed = false;
                    return true;
                }
                nanosTimeout = deadline - System.nanoTime(); //计算剩余多少的时间
                if (nanosTimeout <= 0L) //如果小于零那么 返回
                    return false;
                if (shouldParkAfterFailedAcquire(p, node) && 
                    nanosTimeout > spinForTimeoutThreshold) //如果剩余的超时时间大于自旋阈值时间才去pack否则直接往下走进行下一次自旋
                    LockSupport.parkNanos(this, nanosTimeout); //pack
                if (Thread.interrupted()) //检查线程是否被打断了,如果是的话,那么就抛出异常
                    throw new InterruptedException();
            }
        } finally {
            if (failed)
                cancelAcquire(node);
        }
    }
```

##### 1.2.2.2.11.doAcquireShared

```java
    /**
     * Acquires in shared uninterruptible mode.
     * @param arg the acquire argument
     */
    private void doAcquireShared(int arg) {
        final Node node = addWaiter(Node.SHARED);//新入队一个节点,设置为共享模式
        boolean failed = true; 
        try {
            boolean interrupted = false;
            for (;;) {
                final Node p = node.predecessor();
                if (p == head) { //如果前去是头节点,进入if条件
/**
* a negative value on failure; zero if acquisition in shared mode succeeded but no subsequent shared-mode acquire can succeed; and a positive value if acquisition in shared mode succeeded and subsequent shared-mode acquires might also succeed, in which case a subsequent waiting thread must check availability. (Support for three different return values enables this method to be used in contexts where acquires only sometimes act exclusively.) Upon success, this object has been acquired
失败时为负值；如果共享模式下的获取成功，但后续的共享模式获取不能成功，则为零； 如果共享模式下的获取成功,并且后续的共享模式获取也可能成功了,则返回正值,后续等待的线程必须考虑可用性.  
*/
                    int r = tryAcquireShared(arg); //尝试获取锁(共享模式)
                    if (r >= 0) {
                        setHeadAndPropagate(node, r);
                        p.next = null; // help GC
                        if (interrupted)
                            selfInterrupt(); //抛出异常方法
                        failed = false;
                        return;
                    }
                }
                if (shouldParkAfterFailedAcquire(p, node) &&
                    parkAndCheckInterrupt())
                    interrupted = true;
            }
        } finally {
            if (failed)
                cancelAcquire(node);
        }
    }
```



##### 1.2.2.2.12.doAcquireSharedInterruptibly

```java
    /**
     * Acquires in shared interruptible mode.
     * @param arg the acquire argument
     共享模式可打断的acquire
     */
    private void doAcquireSharedInterruptibly(int arg)
        throws InterruptedException {
        final Node node = addWaiter(Node.SHARED);
        boolean failed = true;
        try {
            for (;;) {
                final Node p = node.predecessor();
                if (p == head) {
                    int r = tryAcquireShared(arg);
                    if (r >= 0) {
                        setHeadAndPropagate(node, r);
                        p.next = null; // help GC
                        failed = false;
                        return;
                    }
                }
                if (shouldParkAfterFailedAcquire(p, node) &&
                    parkAndCheckInterrupt())
                    throw new InterruptedException();
            }
        } finally {
            if (failed)
                cancelAcquire(node);
        }
    }
```

##### 1.2.2.2.12.doAcquireSharedNanos

```java
    /**
     * Acquires in shared timed mode.
     *
     * @param arg the acquire argument
     * @param nanosTimeout max wait time
     * @return {@code true} if acquired
     */
    private boolean doAcquireSharedNanos(int arg, long nanosTimeout)
            throws InterruptedException {
        if (nanosTimeout <= 0L)
            return false;
        final long deadline = System.nanoTime() + nanosTimeout;
        final Node node = addWaiter(Node.SHARED);
        boolean failed = true;
        try {
            for (;;) {
                final Node p = node.predecessor();
                if (p == head) {
                    int r = tryAcquireShared(arg);
                    if (r >= 0) {
                        setHeadAndPropagate(node, r);
                        p.next = null; // help GC
                        failed = false;
                        return true;
                    }
                }
                nanosTimeout = deadline - System.nanoTime();
                if (nanosTimeout <= 0L)
                    return false;
                if (shouldParkAfterFailedAcquire(p, node) &&
                    nanosTimeout > spinForTimeoutThreshold)
                    LockSupport.parkNanos(this, nanosTimeout);
                if (Thread.interrupted())
                    throw new InterruptedException();
            }
        } finally {
            if (failed)
                cancelAcquire(node);
        }
    }
```

##### 1.2.2.2.13.tryAcquire && tryRelease && tryAcquireShared && tryReleaseShared && isHeldExclusively

实现类需要实现的方法

```java
  // Main exported methods

    /**
     * Attempts to acquire in exclusive mode. This method should query
     * if the state of the object permits it to be acquired in the
     * exclusive mode, and if so to acquire it.
     *
     * <p>This method is always invoked by the thread performing
     * acquire.  If this method reports failure, the acquire method
     * may queue the thread, if it is not already queued, until it is
     * signalled by a release from some other thread. This can be used
     * to implement method {@link Lock#tryLock()}.
     *
     * <p>The default
     * implementation throws {@link UnsupportedOperationException}.
     *
     * @param arg the acquire argument. This value is always the one
     *        passed to an acquire method, or is the value saved on entry
     *        to a condition wait.  The value is otherwise uninterpreted
     *        and can represent anything you like.
     * @return {@code true} if successful. Upon success, this object has
     *         been acquired.
     * @throws IllegalMonitorStateException if acquiring would place this
     *         synchronizer in an illegal state. This exception must be
     *         thrown in a consistent fashion for synchronization to work
     *         correctly.
     * @throws UnsupportedOperationException if exclusive mode is not supported
     */
    protected boolean tryAcquire(int arg) {
        throw new UnsupportedOperationException();
    }

    /**
     * Attempts to set the state to reflect a release in exclusive
     * mode.
     *
     * <p>This method is always invoked by the thread performing release.
     *
     * <p>The default implementation throws
     * {@link UnsupportedOperationException}.
     *
     * @param arg the release argument. This value is always the one
     *        passed to a release method, or the current state value upon
     *        entry to a condition wait.  The value is otherwise
     *        uninterpreted and can represent anything you like.
     * @return {@code true} if this object is now in a fully released
     *         state, so that any waiting threads may attempt to acquire;
     *         and {@code false} otherwise.
     * @throws IllegalMonitorStateException if releasing would place this
     *         synchronizer in an illegal state. This exception must be
     *         thrown in a consistent fashion for synchronization to work
     *         correctly.
     * @throws UnsupportedOperationException if exclusive mode is not supported
     */
    protected boolean tryRelease(int arg) {
        throw new UnsupportedOperationException();
    }

    /**
     * Attempts to acquire in shared mode. This method should query if
     * the state of the object permits it to be acquired in the shared
     * mode, and if so to acquire it.
     *
     * <p>This method is always invoked by the thread performing
     * acquire.  If this method reports failure, the acquire method
     * may queue the thread, if it is not already queued, until it is
     * signalled by a release from some other thread.
     *
     * <p>The default implementation throws {@link
     * UnsupportedOperationException}.
     *
     * @param arg the acquire argument. This value is always the one
     *        passed to an acquire method, or is the value saved on entry
     *        to a condition wait.  The value is otherwise uninterpreted
     *        and can represent anything you like.
     * @return a negative value on failure; zero if acquisition in shared
     *         mode succeeded but no subsequent shared-mode acquire can
     *         succeed; and a positive value if acquisition in shared
     *         mode succeeded and subsequent shared-mode acquires might
     *         also succeed, in which case a subsequent waiting thread
     *         must check availability. (Support for three different
     *         return values enables this method to be used in contexts
     *         where acquires only sometimes act exclusively.)  Upon
     *         success, this object has been acquired.
     * @throws IllegalMonitorStateException if acquiring would place this
     *         synchronizer in an illegal state. This exception must be
     *         thrown in a consistent fashion for synchronization to work
     *         correctly.
     * @throws UnsupportedOperationException if shared mode is not supported
     */
    protected int tryAcquireShared(int arg) {
        throw new UnsupportedOperationException();
    }

    /**
     * Attempts to set the state to reflect a release in shared mode.
     *
     * <p>This method is always invoked by the thread performing release.
     *
     * <p>The default implementation throws
     * {@link UnsupportedOperationException}.
     *
     * @param arg the release argument. This value is always the one
     *        passed to a release method, or the current state value upon
     *        entry to a condition wait.  The value is otherwise
     *        uninterpreted and can represent anything you like.
     * @return {@code true} if this release of shared mode may permit a
     *         waiting acquire (shared or exclusive) to succeed; and
     *         {@code false} otherwise
     * @throws IllegalMonitorStateException if releasing would place this
     *         synchronizer in an illegal state. This exception must be
     *         thrown in a consistent fashion for synchronization to work
     *         correctly.
     * @throws UnsupportedOperationException if shared mode is not supported
     */
    protected boolean tryReleaseShared(int arg) {
        throw new UnsupportedOperationException();
    }

    /**
     * Returns {@code true} if synchronization is held exclusively with
     * respect to the current (calling) thread.  This method is invoked
     * upon each call to a non-waiting {@link ConditionObject} method.
     * (Waiting methods instead invoke {@link #release}.)
     *
     * <p>The default implementation throws {@link
     * UnsupportedOperationException}. This method is invoked
     * internally only within {@link ConditionObject} methods, so need
     * not be defined if conditions are not used.
     *
     * @return {@code true} if synchronization is held exclusively;
     *         {@code false} otherwise
     * @throws UnsupportedOperationException if conditions are not supported
     */
    protected boolean isHeldExclusively() {
        throw new UnsupportedOperationException();
    }
```

##### 1.2.2.2.14.acquire

```java
    /**
     * Acquires in exclusive mode, ignoring interrupts.  Implemented
     * by invoking at least once {@link #tryAcquire},
     * returning on success.  Otherwise the thread is queued, possibly
     * repeatedly blocking and unblocking, invoking {@link
     * #tryAcquire} until success.  This method can be used
     * to implement method {@link Lock#lock}.
     * 独占模式的获取,忽略interrupts
     * @param arg the acquire argument.  This value is conveyed to
     *        {@link #tryAcquire} but is otherwise uninterpreted and
     *        can represent anything you like.
     */
    public final void acquire(int arg) {
        if (!tryAcquire(arg) && //先调用一次tryAcquire(arg) 如果成功返回true那么说明加锁成功了,此时if短路,直接跳出了;返回false if继续调用addWaiter创建一个与当前线程相关的node到队列尾部,并且返回node的引用作为 acquireQueued 的第一参数 调用acquireQueued,然后循环阻塞获取锁,直到成功 acquireQueued 的返回值是表明获取锁的过程中是否被打断过被打断过返回ture,因此if 条件成立此时会执行selfInterrupt(),如果返回false 那么不会执行selfInterrupt
            
            acquireQueued(addWaiter(Node.EXCLUSIVE), arg))
            selfInterrupt(); //设置打断标记为true(这是因为acquireQueued中pack被打断了的话会)
    }

    /**
     * Convenience method to interrupt current thread.
     首先，一个线程不应该由其他线程来强制中断或停止，而是应该由线程自己自行停止。
	所以，Thread.stop, Thread.suspend, Thread.resume 都已经被废弃了。
	而 Thread.interrupt 的作用其实也不是中断线程，而是「通知线程应该中断了」，
	具体到底中断还是继续运行，应该由被通知的线程自己处理。

	具体来说，当对一个线程，调用 interrupt() 时，
	① 如果线程处于被阻塞状态（例如处于sleep, wait, join 等状态），那么线程将立即退出被阻塞状态，并抛出一个InterruptedException异常。仅此而已。
	② 如果线程处于正常活动状态，那么会将该线程的中断标志设置为 true，仅此而已。被设置中断标志的线程将继续正常运行，不受影响。
     */
    static void selfInterrupt() {
        Thread.currentThread().interrupt();
    }

```

##### 1.2.2.2.15.acquireInterruptibly

```java
    /**
     * Acquires in exclusive mode, aborting if interrupted.
     * Implemented by first checking interrupt status, then invoking
     * at least once {@link #tryAcquire}, returning on
     * success.  Otherwise the thread is queued, possibly repeatedly
     * blocking and unblocking, invoking {@link #tryAcquire}
     * until success or the thread is interrupted.  This method can be
     * used to implement method {@link Lock#lockInterruptibly}.
     *
     * @param arg the acquire argument.  This value is conveyed to
     *        {@link #tryAcquire} but is otherwise uninterpreted and
     *        can represent anything you like.
     * @throws InterruptedException if the current thread is interrupted
     * 可以打断的获取
     */
    public final void acquireInterruptibly(int arg)
            throws InterruptedException {
        if (Thread.interrupted())
            throw new InterruptedException();
        if (!tryAcquire(arg)) //先尝试获取一次锁如果获取失败(返回false) 那么就会进入doAcquireInterruptibly(arg)
            doAcquireInterruptibly(arg);
    }
```

##### 1.2.2.2.16.tryAcquireNanos

```java
    /**
     * Attempts to acquire in exclusive mode, aborting if interrupted,
     * and failing if the given timeout elapses.  Implemented by first
     * checking interrupt status, then invoking at least once {@link
     * #tryAcquire}, returning on success.  Otherwise, the thread is
     * queued, possibly repeatedly blocking and unblocking, invoking
     * {@link #tryAcquire} until success or the thread is interrupted
     * or the timeout elapses.  This method can be used to implement
     * method {@link Lock#tryLock(long, TimeUnit)}.
     *
     * @param arg the acquire argument.  This value is conveyed to
     *        {@link #tryAcquire} but is otherwise uninterpreted and
     *        can represent anything you like.
     * @param nanosTimeout the maximum number of nanoseconds to wait
     * @return {@code true} if acquired; {@code false} if timed out
     * @throws InterruptedException if the current thread is interrupted
     */
    public final boolean tryAcquireNanos(int arg, long nanosTimeout)
            throws InterruptedException {
        if (Thread.interrupted()) //检查线程的打断标记
            throw new InterruptedException();
        return tryAcquire(arg) || //如果tryAcquire成功那么逻辑与短路 直接返回true, 如果tryAcquire失败 进入doAcquireNanos(arg, nanosTimeout)
            doAcquireNanos(arg, nanosTimeout);
    }
```

##### 1.2.2.2.16.release

```java
    /**
     * Releases in exclusive mode.  Implemented by unblocking one or
     * more threads if {@link #tryRelease} returns true.
     * This method can be used to implement method {@link Lock#unlock}.
     *
     * @param arg the release argument.  This value is conveyed to
     *        {@link #tryRelease} but is otherwise uninterpreted and
     *        can represent anything you like.
     * @return the value returned from {@link #tryRelease}
     */
    public final boolean release(int arg) {
        //tryRelease return true if this object is now in a fully released state, so that any waiting threads may attempt to acquire; and false otherwise.
        if (tryRelease(arg)) { //调用子类实现的release方法 
            //如果能进if说明是fully release 因此 需要唤醒队头元素
            Node h = head; 
            if (h != null && h.waitStatus != 0) //如果h为null,说明当前没有等待的线程(因为queue还没有初始化) if短路跳出,如果不为null 那么判断h的等待状态是否等于0如果不等于 说明需要unpack后继  
                unparkSuccessor(h);
            return true; //退锁到0
        }
        return false; //退重入次数
    }
```



##### 1.2.2.2.17.getFirstQueuedThread && fullGetFirstQueuedThread

```java
    /**
     * Returns the first (longest-waiting) thread in the queue, or
     * {@code null} if no threads are currently queued.
     *
     * <p>In this implementation, this operation normally returns in
     * constant time, but may iterate upon contention if other threads are
     * concurrently modifying the queue.
     *
     * @return the first (longest-waiting) thread in the queue, or
     *         {@code null} if no threads are currently queued
     * 获取queue中的第一个元素,如果是未初始化的队列或者是只有哨兵节点的队列,那么就返回null,否则就返回第一个节点
     */
    public final Thread getFirstQueuedThread() {
        // handle only fast path, else relay
        return (head == tail) ? null : fullGetFirstQueuedThread();
    }

    /**
     * Version of getFirstQueuedThread called when fastpath fails
     */
    private Thread fullGetFirstQueuedThread() {
        /*
         * The first node is normally head.next. Try to get its
         * thread field, ensuring consistent reads: If thread
         * field is nulled out or s.prev is no longer head, then
         * some other thread(s) concurrently performed setHead in
         * between some of our reads. We try this twice before
         * resorting to traversal.
         * 头节点就是哨兵节点(head)的next指针指向的节点.
         * 获取头节点中保存的线程引用需要保证一致性读:
         * 当我们多次读取node节点锁保存的线程信息时,如果node节点(后面成为s节点)保存的线程引用被设置成null或者s的前驱不再是head的引用,	
         * 那么表明有其他线程并发的调用了setHead.
         * 这样做的好处是避免了每次都从队尾向队头遍历,提升了性能
         */
        Node h, s;
        Thread st;
        if (( (h = head) != null && (s = h.next) != null &&
             s.prev == head && (st = s.thread) != null) || //如果h(保存当前head引用的指针)不是null,并且s(哨兵/head节点的后继)也不为null,并且s的前驱任然是head(即没有存在并发),并且st(头节点所保存的线程引用)不为null,逻辑判断为ture,那么if短路,if条件成立 返回st
            ( (h = head) != null && (s = h.next) != null && //如果是前面判断为false,说明存在并发,不能保证一致性读,那么再来一次相同的判断,如果成功的话就进入if内的逻辑,不成功那么就向下指向
             s.prev == head && (st = s.thread) != null   ))
         
            return st;

        /*
         * Head's next field might not have been set yet, or may have
         * been unset after setHead. So we must check to see if tail
         * is actually first node. If not, we continue on, safely
         * traversing from tail back to head to find first,
         * guaranteeing termination.
         * 头节点的next指针有可能还未被设置,或者是还没有被setHead函数执行到设置next指针.
         *  因此我们必须检查现在的tail是否也指向了哨兵节点(head).如果不是,我们就继续循环,安全的从队尾遍历到队头,确认终止.
         */

        Node t = tail; //得到当前队尾引用
        Thread firstThread = null;
        while (t != null && t != head) { //循环条件 t不等空,且t不是head
            Thread tt = t.thread; //保存当前t指针指向的node中保存的thread引用
            if (tt != null) //如果 tt不为空 设置给firstThread
                firstThread = tt;
            t = t.prev;  //t指针左移(向队头移动)
        }
        return firstThread; 
    }
```



##### 1.2.2.2.17.isQueued

```java
    /**
     * Returns true if the given thread is currently queued.
     *
     * <p>This implementation traverses the queue to determine
     * presence of the given thread.
     *
     * @param thread the thread
     * @return {@code true} if the given thread is on the queue
     * @throws NullPointerException if the thread is null
     */
    public final boolean isQueued(Thread thread) {
        if (thread == null) //非空判断
            throw new NullPointerException();
        for (Node p = tail; p != null; p = p.prev) //从队尾遍历
            if (p.thread == thread)
                return true;
        return false;
    }
```

##### 1.2.2.2.17.apparentlyFirstQueuedIsExclusive

```java
    /**
     * Returns {@code true} if the apparent first queued thread, if one
     * exists, is waiting in exclusive mode.  If this method returns
     * {@code true}, and the current thread is attempting to acquire in
     * shared mode (that is, this method is invoked from {@link
     * #tryAcquireShared}) then it is guaranteed that the current thread
     * is not the first queued thread.  Used only as a heuristic in
     * ReentrantReadWriteLock.
     */
    final boolean apparentlyFirstQueuedIsExclusive() {
        Node h, s;
        return (h = head) != null &&
            (s = h.next)  != null &&
            !s.isShared()         &&
            s.thread != null;
    }
```

##### 1.2.2.2.18.hasQueuedPredecessors

```java
    /**
     * Queries whether any threads have been waiting to acquire longer
     * than the current thread.
     * 查询是否后其他线程等待着获取锁的时间超过了当前线程
     * <p>An invocation of this method is equivalent to (but may be
     * more efficient than):
     本方法等同于下面的逻辑,但是有可能性能比如下逻辑更好
     *  <pre> {@code
     * getFirstQueuedThread() != Thread.currentThread() &&
     * hasQueuedThreads()}</pre>
     *
     * <p>Note that because cancellations due to interrupts and
     * timeouts may occur at any time, a {@code true} return does not
     * guarantee that some other thread will acquire before the current
     * thread.  Likewise, it is possible for another thread to win a
     * race to enqueue after this method has returned {@code false},
     * due to the queue being empty.
     * 需要注意的是,因为打断和timeout造成的退出可能在任何时间发生,本方法返回ture并不能确保其他线程在本线程之前获取到锁
     * 同样的, 对于其他线程来说有可能竞争成功来入队 当此方法返回之后
     
     * <p>This method is designed to be used by a fair synchronizer to
     * avoid <a href="AbstractQueuedSynchronizer#barging">barging</a>.
     * Such a synchronizer's {@link #tryAcquire} method should return
     * {@code false}, and its {@link #tryAcquireShared} method should
     * return a negative value, if this method returns {@code true}
     * (unless this is a reentrant acquire).  For example, the {@code
     * tryAcquire} method for a fair, reentrant, exclusive mode
     * synchronizer might look like this:
     * 这个方法被设计用于公平式同步,来避免barging(). 
     * 如果本方法返回true那么同步器的tryAcquire方法返回false,tryAcquireShared方法返回负值(重入情况除外)
     * 举个例子,tryAcquire方法(针对公平锁,重入,独占模式的同步器)应该有如下的逻辑
     *  <pre> {@code
     * protected boolean tryAcquire(int arg) {
     *   if (isHeldExclusively()) {
     *     // A reentrant acquire; increment hold count
     *     return true;
     *   } else if (hasQueuedPredecessors()) {
     *     return false; //有前驱节点排队
     *   } else {
     *     // try to acquire normally
     *   }
     * }}</pre>
     *
     * @return {@code true} if there is a queued thread preceding the
     *         current thread, and {@code false} if the current thread
     *         is at the head of the queue or the queue is empty
     			如果说队列中有一个线程在当前线程之前那么返回ture,如果当前线程在queue的头部,或者是当前queue是空的,那么就返回false
     * @since 1.7
     */
    public final boolean hasQueuedPredecessors() {
        // The correctness of this depends on head being initialized
        // before tail and on head.next being accurate if the current
        // thread is first in queue.
        Node t = tail; // Read fields in reverse initialization order
        Node h = head;
        Node s;
        return h != t &&
            ((s = h.next) == null || s.thread != Thread.currentThread());
    }
```

##### 1.2.2.2.19.getQueueLength && getQueuedThreads && getExclusiveQueuedThreads && getSharedQueuedThreads

```java
    /**
     * Returns an estimate of the number of threads waiting to
     * acquire.  The value is only an estimate because the number of
     * threads may change dynamically while this method traverses
     * internal data structures.  This method is designed for use in
     * monitoring system state, not for synchronization
     * control.
     * 返回当前等待获取锁的线程数目. 这个值是一个估计值,因为当遍历内部的queue时,线程的数量也会动态的改变
     * @return the estimated number of threads waiting to acquire
     */
    public final int getQueueLength() {
        int n = 0;
        for (Node p = tail; p != null; p = p.prev) { //从后向前遍历
            if (p.thread != null)
                ++n;
        }
        return n;
    }


    /**
     * Returns a collection containing threads that may be waiting to
     * acquire.  Because the actual set of threads may change
     * dynamically while constructing this result, the returned
     * collection is only a best-effort estimate.  The elements of the
     * returned collection are in no particular order.  This method is
     * designed to facilitate construction of subclasses that provide
     * more extensive monitoring facilities.
     * 返回一个等待获取线程的集合. 因为在产生结果时,实际的线程集合动态的改变,返回的结果时尽最大能力预估.
     * 返回的集合中的元素并没有一个特别的排列顺序. 本方法被设计用于方便为子类提供更多的monitoring.
     * @return the collection of threads
     */
    public final Collection<Thread> getQueuedThreads() {
        ArrayList<Thread> list = new ArrayList<Thread>();
        for (Node p = tail; p != null; p = p.prev) {
            Thread t = p.thread;
            if (t != null)
                list.add(t);
        }
        return list;
    }

    /**
     * Returns a collection containing threads that may be waiting to
     * acquire in exclusive mode. This has the same properties
     * as {@link #getQueuedThreads} except that it only returns
     * those threads waiting due to an exclusive acquire.
     * 返回等待着独占获取的线程集合.
     * @return the collection of threads
     */
    public final Collection<Thread> getExclusiveQueuedThreads() {
        ArrayList<Thread> list = new ArrayList<Thread>();
        for (Node p = tail; p != null; p = p.prev) {
            if (!p.isShared()) {
                Thread t = p.thread;
                if (t != null)
                    list.add(t);
            }
        }
        return list;
    }


    /**
     * Returns a collection containing threads that may be waiting to
     * acquire in shared mode. This has the same properties
     * as {@link #getQueuedThreads} except that it only returns
     * those threads waiting due to a shared acquire.
     * 返回等待着共享获取的线程集合.
     * @return the collection of threads
     */
    public final Collection<Thread> getSharedQueuedThreads() {
        ArrayList<Thread> list = new ArrayList<Thread>();
        for (Node p = tail; p != null; p = p.prev) {
            if (p.isShared()) {
                Thread t = p.thread;
                if (t != null)
                    list.add(t);
            }
        }
        return list;
    }
```



> ## 接下来的方法是 有关内部condition的



##### 1.2.2.2.20.isOnSyncQueue

```java
    /**
     * Returns true if a node, always one that was initially placed on
     * a condition queue, is now waiting to reacquire on sync queue.
     * @param node the node
     如果一个节点（总是最初置于 条件队列中的节点)，现在正在同步队列中等待重新获取，则返回 true。
     * @return true if is reacquiring
     	
     */
    final boolean isOnSyncQueue(Node node) {
        if (node.waitStatus == Node.CONDITION || node.prev == null) //节点的等待状态等于condition或者节点的前驱不存在(即是哨兵)
            return false;
        if (node.next != null) // If has successor, it must be on queue
            return true;
        /*
         * node.prev can be non-null, but not yet on queue because
         * the CAS to place it on queue can fail. So we have to
         * traverse from tail to make sure it actually made it.  It
         * will always be near the tail in calls to this method, and
         * unless the CAS failed (which is unlikely), it will be
         * there, so we hardly ever traverse much.
         * 虽然经过前面if判断node.next是空,但是其也有可能非空,因为cas操作替换node.next可能失败.
         * 因此我们不得不从队尾遍历来确保他是否完成了. 这种情况的出现永远都只是存在队尾附近,因此也不会遍历太多节点(除非是cas失败了)
         */
        return findNodeFromTail(node);
    }
    /**
     * Returns true if node is on sync queue by searching backwards from tail.
     * Called only when needed by isOnSyncQueue.
     * @return true if present
     * 通过从尾部向前遍历,如果node节点在queue中返回ture.Called only when needed by isOnSyncQueue.
     */
    private boolean findNodeFromTail(Node node) {
        Node t = tail;
        for (;;) {
            if (t == node)
                return true;
            if (t == null)
                return false;
            t = t.prev;
        }
    }
```

##### 1.2.2.2.20.transferForSignal && transferAfterCancelledWait

```java
    /**
     * Transfers a node from a condition queue onto sync queue.
     * Returns true if successful.
     * @param node the node
     * @return true if successfully transferred (else the node was
     * cancelled before signal)
     * 将一个node 从condition queue(条件等待队列)转换到 sync queue(同步队列)
     * 如果成功返回ture
     */
    final boolean transferForSignal(Node node) {
        /*
         * If cannot change waitStatus, the node has been cancelled.
         * 如果不能改变ws,说明当前节点已经cacelled
         */
        if (!compareAndSetWaitStatus(node, Node.CONDITION, 0))
            return false;
        /*
         * Splice onto queue and try to set waitStatus of predecessor to
         * indicate that thread is (probably) waiting. If cancelled or
         * attempt to set waitStatus fails, wake up to resync (in which
         * case the waitStatus can be transiently and harmlessly wrong).
         * 滑动到队列中，并尝试将前置线程的 waitStatus 设置为 表示线程可能在等待 。如果被取消了或尝试设置 waitStatus 失败，
         * 则唤醒以重新同步（在这种情况下，waitStatus 可能会出现短暂的、无害的错误）。
         */
        Node p = enq(node); //node 进入 sync queue 得到node 的前驱
        int ws = p.waitStatus; //得到等待状态 
        if (ws > 0 || !compareAndSetWaitStatus(p, ws, Node.SIGNAL)) //如果ws大于零,或者是尝试设置 waitStatus 失败, 那么就唤醒该线程以重新同步 
            LockSupport.unpark(node.thread);
        return true;
    }


    /**
     * Transfers node, if necessary, to sync queue after a cancelled wait.
     * Returns true if thread was cancelled before being signalled.
     * 如果必要的话,在 cancel wait 后 ,将node转到sync queue  
     * @param node the node
     * @return true if cancelled before the node was signalled
     * 如果node节点在signal之前被cacel了那么返回true
     */
    final boolean transferAfterCancelledWait(Node node) {
        if ( compareAndSetWaitStatus(node, Node.CONDITION, 0) ) { //如果cas从Node.CONDITIO设置成0成功那么说明没有cancel
            enq(node);
            return true; 
        }
        /*
         * If we lost out to a signal(), then we can't proceed
         * until it finishes its enq().  Cancelling during an
         * incomplete transfer is both rare and transient, so just
         * spin.
         * 如果我们输给了signal()方法,那么我们不能继续直到它做完自己的enq, 在transfer过程中的cancel非常少见且短暂,因此只需要自旋即可.
         */
        while (!isOnSyncQueue(node)) //当前node 是否在sync queue中 
            Thread.yield(); 
        return false;
    }
```

##### 1.2.2.2.21.transferForSignal && transferAfterCancelledWait

```java
    /**
     * Invokes release with current state value; returns saved state.
     * Cancels node and throws exception on failure.
     * @param node the condition node for this wait  条件等待的node
     * @return previous sync state 之前的同步状态
     * 用当前state唤起release,返回一个被保存的state
     * 此方法是做当前拥有锁的线程去从condition中等待
     */
    final int fullyRelease(Node node) {
        boolean failed = true; //初始化失败标记为true
        try {
            int savedState = getState(); //得到当前同步器state
            if (release(savedState)) { //调用release(savedState)如果返回true 说明释放成功,进入if内部
                failed = false; //设置失败标记为false
                return savedState; //返回旧的同步器state
            } else { //调用release(savedState)如果返回fasle 
                throw new IllegalMonitorStateException(); //抛出异常
            }
        } finally {
            if (failed) //如果是在release(savedState)中抛出了异常,那么failed任然为true 因此会将node的ws设置为Node.CANCELLED
                node.waitStatus = Node.CANCELLED;
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

