import java.util.function.*;

class ParseContext<T> {
  // ...
}

class NodeList {
  // ...
}

class Node {
  // ...
}

class IdentifierNode extends Node {
  // ...
}

class TypeExpressionNode extends Node {
  // ...
}

abstract class NodeParseRule<T, R> implements Function<ParseContext<T>, R> {
  // ...
}

class Compose<T, U, R> extends NodeParseRule<T, R> {
  public R apply(final ParseContext<T> context) { return null; }
}

class Sequence<T, U> extends NodeParseRule<T, NodeList> {
    private final NodeParseRule<T, U> elementRule;
    private final Predicate<ParseContext<T>> terminationCondition;

    public Sequence(final NodeParseRule<T, U> elementRule, final Predicate<ParseContext<T>> terminationCondition) {
        this.elementRule = elementRule;
        this.terminationCondition = terminationCondition;
    }

    @Override
    public NodeList apply(final ParseContext<T> parseContext) {
        terminationCondition.test(parseContext);
	return new NodeList();
    }
}

class Trouble {
  private Compose<IdentifierNode, IdentifierNode, TypeExpressionNode> parameterTypeParser = null; // ...
    
  private Sequence<IdentifierNode, TypeExpressionNode> sequence = new Sequence<>(parameterTypeParser, 
		  (ParseContext<IdentifierNode> pc) -> false);

}



