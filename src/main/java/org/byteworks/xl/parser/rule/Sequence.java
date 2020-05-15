package org.byteworks.xl.parser.rule;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;

import org.byteworks.xl.parser.Node;
import org.byteworks.xl.parser.NodeList;
import org.byteworks.xl.parser.ParseContext;

public class Sequence<T extends Node> extends NodeParseRule<NodeList<T>> {
    private final NodeParseRule<T> elementRule;
    private final Predicate<ParseContext> terminationCondition;

    public Sequence(final NodeParseRule<T> elementRule, final Predicate<ParseContext> terminationCondition) {
        this.elementRule = elementRule;
        this.terminationCondition = terminationCondition;
    }

    @Override
    public NodeList<T> apply(final ParseContext parseContext) {
        List<T> nodes = new ArrayList<>();
        while(!(terminationCondition.test(parseContext))) {
            nodes.add(elementRule.apply(parseContext));
        }
        return new NodeList<T>(nodes);
    }
}
