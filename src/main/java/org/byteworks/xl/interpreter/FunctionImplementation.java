package org.byteworks.xl.interpreter;

import java.util.Stack;
import java.util.function.Function;

public interface FunctionImplementation<T extends Stack<Value>, U extends Value> extends Function<T, U> {
}
