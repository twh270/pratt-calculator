package org.byteworks.xl.interpreter;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

public class TypeList implements Type {
    private final List<Type> types;
    private final String name;

    public TypeList(final List<Type> types) {
        this.types = new ArrayList<>(types);
        name = types.stream().map(Type::name).collect(Collectors.joining(", "));
    }

    @Override
    public String name() {
        return name;
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        final TypeList list = (TypeList) o;
        return name.equals(list.name);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name);
    }

    @Override
    public String toString() {
        return name;
    }
}
