package com.woldier.limiter;

import lombok.Data;
import lombok.ToString;

import java.util.List;

@Data
@ToString
public class Node {
    Integer id;

    Integer parent;

    public Node(Integer id, Integer parent) {
        this.id = id;
        this.parent = parent;
    }

    private List<Node> kids;
}
