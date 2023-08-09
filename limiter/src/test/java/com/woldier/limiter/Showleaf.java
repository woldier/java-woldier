package com.woldier.limiter;

import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * description 想象有一个如下的场景
 * 某个人具有权限 ,如果具有某三级权限,那么对应的耳机权限,一级权限也应该查询出来
 *
 * @author: woldier wong
 * @date: 2023/8/8 17:08
 */
@Slf4j
public class Showleaf {

    //保存了树信息的list
    List<Node> nodeList = new ArrayList() {{
        add(new Node(1, null));
        add(new Node(2, 1));
        add(new Node(3, 1));
        add(new Node(4, 2));
        add(new Node(5, 2));
        add(new Node(6, 3));
        add(new Node(7, 3));
        add(new Node(8, 4));
        add(new Node(9, 4));
        add(new Node(10, 5));
        add(new Node(11, 5));
        add(new Node(12, 6));
        add(new Node(13, 6));
        add(new Node(14, 7));
        add(new Node(15, 7));
    }};

    @Test
    public void test() {
        Map<Integer, Node> nodeMap = nodeList.stream().collect(Collectors.toMap(e -> e.id, e -> e)); //得到包含所有node map 索引时节点的id
//        Node node10 = nodeList.get(10 - 1);
//        Node node11 = nodeList.get(11 - 1);
//        Node node6 = nodeList.get(6 - 1);

        //模拟查询到的数据
        List<Node> authList = new ArrayList<>();
//        authList.add(node10);
//        authList.add(node11);
//        authList.add(node6);

        //创建一个找父亲的map
        Map<Integer, Node> parentMap = new HashMap<>();
        List<Node> resList = new ArrayList<>();
        authList.forEach(e -> {
            Integer parentId = e.parent;
            Node node = e;
            for (; ; ) {
                Node p = parentMap.getOrDefault(parentId, null);//查看当前节点的父亲是否在map结果集中
                if (p == null) {
                    p = nodeMap.get(parentId);
                    parentMap.put(parentId, p); //放入结果集
                    List<Node> kids = p.getKids();
                    if (kids == null) {
                        kids = new ArrayList<>();
                        p.setKids(kids);
                    }
                    kids.add(node);
                    node = p;
                    parentId = p.parent;
                    if (parentId == null) {
                        resList.add(p);
                        break;
                    }
                } else {
                    List<Node> kids = p.getKids();
                    kids.add(node);
                    break;
                }
            }
        });

        log.debug(resList.toString());
    }
}
