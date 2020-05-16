package org.byteworks.xl.parser.rule;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;

import org.byteworks.xl.parser.NodeList;
import org.byteworks.xl.parser.ParseContext;

public class Sequence<T, U> extends NodeParseRule<T, NodeList> {
    private final NodeParseRule<T, U> elementRule;
    private final Predicate<ParseContext<T>> terminationCondition;

    public Sequence(final NodeParseRule<T, U> elementRule, final Predicate<ParseContext<T>> terminationCondition) {
        this.elementRule = elementRule;
        this.terminationCondition = terminationCondition;
    }

    @Override
    public NodeList apply(final ParseContext<T> parseContext) {
        List<U> nodes = new ArrayList<>();
        while(!(terminationCondition.test(parseContext))) {
            nodes.add(elementRule.apply(parseContext));
        }
        return new NodeList(nodes);
    }
}
