package com.woldier;

import com.wolder.Main;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.api.transaction.CuratorTransaction;
import org.apache.zookeeper.CreateMode;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import javax.annotation.Resource;

@SpringBootTest(classes = {Main.class})
class Test4Zk {

    @Resource
    private CuratorFramework curatorClient;

    @Test
    void zkTest() throws Exception {
        //创建结点，输出如下：
        //        treeCache, type:NODE_ADDED
        //        treeCache, nodePath:/zk data: type:NODE_ADDED
        //        nodeChanged,nodePath:/zk data:
//        curatorClient.create().creatingParentsIfNeeded().forPath("/zk-test");

        //创建结点，同时设置值，输出如下：
        //        treeCache, type:NODE_ADDED
        //        treeCache, nodePath:/zk data: type:NODE_ADDED
        //        nodeChanged,nodePath:/zk data:
       //curatorClient.create().creatingParentsIfNeeded().forPath("/zk-test", "this is zk1".getBytes());
        byte[] bytes = curatorClient.getData().forPath("/zk-test");
        System.out.println(new String(bytes));
        //单独设置结点值
        //curatorClient.setData().forPath("/zk-test", "th   is is zk6".getBytes());

        //创建包含父结点的结点，输出如下：
        //        treeCache, type:NODE_ADDED
        //        treeCache, nodePath:/zk/one data:this is one type:NODE_ADDED
        //curatorClient.create().creatingParentsIfNeeded().forPath("/zk-test/one", "this is one".getBytes());

        //单独设置结点值，输出如下：
        //        treeCache, type:NODE_UPDATED
        //        treeCache, nodePath:/zk/one data:this is zk one2 type:NODE_UPDATED
        //curatorClient.setData().forPath("/zk-test/one", "this is zk one2".getBytes());


        //事务，执行多个操作
//        CuratorTransaction curatorTransaction = curatorClient.inTransaction();
//        curatorTransaction.create().withMode(CreateMode.EPHEMERAL).forPath("/zk-test/three", "this is three".getBytes())
//                .and().create().withMode(CreateMode.PERSISTENT).forPath("/zk-test/four", "this is four".getBytes())
//                .and().commit();


        //节点删除
        //curatorClient.delete().deletingChildrenIfNeeded().forPath("/zk-test");

    }

}

