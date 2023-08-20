package com.woldier;

import java.util.SortedMap;
import java.util.TreeMap;

public class ConsistentHashing {

    private SortedMap<Integer, Node> hashCircle = new TreeMap<>();
    private int virtualNums; // 虚拟节点数

    public ConsistentHashing(Node[] nodes, int virtualNums) {
        this.virtualNums = virtualNums;
        // 初始化一致性hash环
        for (Node node : nodes) {
            // 创建虚拟节点
            add(node);
        }
    }

    /**
     * 添加服务器节点
     *
     * @param node the server
     */
    public void add(Node node) {
        for (int i = 0; i < virtualNums; i++) {
            hashCircle.put(hash(node.toString() + i), node);
        }
    }

    /**
     * 删除服务器节点
     *
     * @param node the server
     */
    public void remove(Node node) {
        for (int i = 0; i < virtualNums; i++) {
            hashCircle.remove(hash(node.toString() + i));
        }
    }

    /**
     * 获取服务器节点
     *
     * @param key the key
     * @return the server
     */
    public Node getNode(String key) {
        if (key == null || hashCircle.isEmpty())
            return null;
        int hash = hash(key);
        if (!hashCircle.containsKey(hash)) {
            // 未命中对应的节点
            SortedMap<Integer, Node> tailMap = hashCircle.tailMap(hash);
            hash = tailMap.isEmpty() ? hashCircle.firstKey() : tailMap.firstKey();
        }
        return hashCircle.get(hash);
    }

    /**
     * FNV1_32_HASH算法
     * refer: http://www.isthe.com/chongo/tech/comp/fnv/
     * @param key the key
     * @return
     */
    private int hash(String key) {
        final int p = 16777619;  //FNV 32 hash 的parameter
        int hash = (int) 2166136261L;
        for (int i = 0; i < key.length(); i++) {
            hash = (hash ^ key.charAt(i)) * p;
        }
        hash += hash << 13;
        hash ^= hash >> 7;
        hash += hash << 3;
        hash ^= hash >> 17;
        hash += hash << 5;
        // 如果算出来的值为负数则取其绝对值
        if (hash < 0) {
            hash = Math.abs(hash);
        }
        return hash;
    }

    /**
     * 集群节点的机器地址
     */
    public static class Node {
        private String ipAddr;
        private int port;
        private String name;

        public Node(String ipAddr, int port, String name) {
            this.ipAddr = ipAddr;
            this.port = port;
            this.name = name;
        }

        @Override
        public String toString() {
            return name + ":<" + ipAddr + ":" + port + ">";
        }
    }
}