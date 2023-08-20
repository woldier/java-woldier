package com.woldier;

import java.util.*;

public class ConsistentHashingTest {

    public static void main(String[] args) {
        ConsistentHashing.Node[] nodes = new ConsistentHashing.Node[4];

        // make nodes 4台服务器节点
        for (int i = 0; i < nodes.length; i++) {
            nodes[i] = new ConsistentHashing.Node("10.1.32.2" + i, 8070, "myNode" + i);
        }

        ConsistentHashing ch = new ConsistentHashing(nodes, 160);

        // make keys 100万个key
        String[] keys = new String[1_000_000];
        for (int i = 0; i < keys.length; i++) {
            keys[i] = "key" + (i + 17) + "ss" + (i * 19);
        }

        // 产生结果

        showHashCount(keys, ch);


        //添加节点后
        ConsistentHashing.Node node4 = new ConsistentHashing.Node("10.1.32.2" + 4, 8070, "myNode" + 4);
        ConsistentHashing.Node node5 = new ConsistentHashing.Node("10.1.32.2" + 5, 8070, "myNode" + 5);
        ch.add(node4);
        ch.add(node5);

        // 产生结果
        showHashCount(keys, ch);


        //删除节点后
        ch.remove(node4);
        showHashCount(keys, ch);
        //statistic(nodes, map);
    }

    private static void showHashCount(String[] keys, ConsistentHashing ch) {
        Map<ConsistentHashing.Node, List<String>> map = new HashMap<>();

        for (String key : keys) {
            ConsistentHashing.Node n = ch.getNode(key);
            List<String> list = map.computeIfAbsent(n, k -> new ArrayList<>());
            list.add(key);
        }
        System.out.println(" --------------------------------- ");
        map.keySet().forEach(key->{
            List<String> strings = map.get(key);
            System.out.println(key.toString()+"--------->"+"count:"+strings.size());

        });
    }

    private static void statistic(ConsistentHashing.Node[] nodes, Map<ConsistentHashing.Node, List<String>> map) {
        // 统计标准差，评估服务器节点的负载均衡性
        int[] loads = new int[nodes.length];
        int x = 0;
        for (Iterator<ConsistentHashing.Node> i = map.keySet().iterator(); i.hasNext(); ) {
            ConsistentHashing.Node key = i.next();
            List<String> list = map.get(key);
            loads[x++] = list.size();
        }
        int min = Integer.MAX_VALUE;
        int max = 0;
        for (int load : loads) {
            min = Math.min(min, load);
            max = Math.max(max, load);
        }
        System.out.println("最小值: " + min + "; 最大值: " + max);
        System.out.println("方差：" + variance(loads));
    }

    public static double variance(int[] data) {
        double variance = 0;
        double expect = (double) sum(data) / data.length;
        for (double datum : data) {
            variance += (Math.pow(datum - expect, 2));
        }
        variance /= data.length;
        return Math.sqrt(variance);
    }

    private static int sum(int[] data) {
        int sum = 0;
        for (int i = 0; i < data.length; i++) {
            sum += data[i];
        }
        return sum;
    }
}