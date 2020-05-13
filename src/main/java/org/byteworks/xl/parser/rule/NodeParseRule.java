package org.byteworks.xl.parser.rule;

import java.util.function.Function;

import org.byteworks.xl.parser.Node;
import org.byteworks.xl.parser.ParseContext;

public interface NodeParseRule<T extends Node> extends Function<ParseContext, T> {
}

