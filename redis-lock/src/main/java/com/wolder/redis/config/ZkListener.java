package com.wolder.redis.config;

import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.recipes.cache.*;
import org.apache.zookeeper.WatchedEvent;
import org.apache.zookeeper.Watcher;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import java.util.Optional;
/**
*
* description 监听器
*
* @author: woldier
* @date: 2023/7/27 上午6:45
*/
//@Component
public class ZkListener {
    @Resource
    private CuratorFramework curatorClient;

    @PostConstruct
    public void init() throws Exception {
        String path = "/zk";

        //监听当前节点变化，不监听子节点变化
        NodeCache nodeCache = new NodeCache(curatorClient, path);
        nodeCache.start();
        nodeCache.getListenable().addListener(() -> {
            String nodePath = nodeCache.getCurrentData().getPath();
            String data = new String(nodeCache.getCurrentData().getData());
            System.out.println("nodeChanged,nodePath:" + nodePath + " data:" + data);
        });

        //监听子节点变化，不监听当前节点变化
        PathChildrenCache pathChildrenCache = new PathChildrenCache(curatorClient, path, true);
        pathChildrenCache.start();
        pathChildrenCache.getListenable().addListener((curatorFramework, event) -> {
            String type = event.getType().name();
            System.out.println("pathChildrenCache, type:" + type);
            Optional.ofNullable(event.getData()).ifPresent(t->{
                String nodePath = t.getPath();
                String data = new String(t.getData());
                System.out.println("pathChildrenCache, nodePath:" + nodePath + " data:" + data + " type:" + type);
            });
        });

        //监听当前节点及其所有子节点变化
        TreeCache treeCache = new TreeCache(curatorClient, path);
        treeCache.start();
        treeCache.getListenable().addListener((curatorFramework, event) -> {
            String type = event.getType().name();
            System.out.println("treeCache, type:" + type);
            Optional.ofNullable(event.getData()).ifPresent(t->{
                String nodePath = t.getPath();
                String data = new String(t.getData());
                System.out.println("treeCache, nodePath:" + nodePath + " data:" + data + " type:" + type);
            });
        });
    }
}

