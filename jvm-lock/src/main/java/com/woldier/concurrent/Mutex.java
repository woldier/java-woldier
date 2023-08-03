package com.woldier.concurrent;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.Serializable;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.AbstractQueuedSynchronizer;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;

/**
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

