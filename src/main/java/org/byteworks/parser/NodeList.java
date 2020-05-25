package org.byteworks.parser;

import java.util.List;

public class NodeList<T> extends Node {
    private final List<T> nodes;

    public NodeList(final List<T> nodes) {
        this.nodes = nodes;
    }

    public List<T> getNodes() {
        return nodes;
    }
}

