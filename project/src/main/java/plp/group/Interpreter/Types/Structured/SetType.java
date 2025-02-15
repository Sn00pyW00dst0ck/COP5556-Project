package plp.group.Interpreter.Types.Structured;

import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

import plp.group.Interpreter.Types.GeneralType;
import plp.group.Interpreter.Types.Simple.BooleanType;

public class SetType<T> extends GeneralType {
    private final Class<T> elementType;

    @SuppressWarnings("unchecked")
    public SetType(HashSet<T> value) {
        super(value);
        if (value.isEmpty())
            throw new UnsupportedOperationException(
                    "Use public SetType(Class<T> elementType) constructor to make an empty set!");
        this.elementType = (Class<T>) value.iterator().next().getClass();
        defineOperations();
    }

    public SetType(Class<T> elementType) {
        super(new HashSet<T>());
        this.elementType = elementType;
        defineOperations();
    }

    public Class<T> getElementType() {
        return this.elementType;
    }

    private void defineOperations() {
        defineOperation("+", this::union);
        defineOperation("-", this::difference);
        defineOperation("*", this::intersection);
        defineOperation("<=", this::isSubsetOf);
        defineOperation(">=", this::isSupersetOf);
        defineOperation("=", this::equalTo);
        defineOperation("<>", this::notEqualTo);
        defineOperation("IN", this::contains);
    }

    @SuppressWarnings("unchecked")
    private <A> GeneralType union(GeneralType lhs, GeneralType rhs) {
        if (!(lhs instanceof SetType<?> || rhs instanceof SetType<?>)) {
            throw new UnsupportedOperationException("Cannot determine union with non SetType.");
        }

        if (((SetType<?>) lhs).getElementType() != ((SetType<?>) rhs).getElementType()) {
            throw new UnsupportedOperationException("Cannot determine union of SetTypes with different element types.");
        }

        // Ugly but the only way I can get it to work...
        HashSet<A> resultSet = new HashSet<>();
        resultSet.addAll((HashSet<A>) (((SetType<?>) lhs).getValue()));
        resultSet.addAll((HashSet<A>) (((SetType<?>) rhs).getValue()));
        return new SetType<A>(resultSet);
    }

    @SuppressWarnings("unchecked")
    private <A> GeneralType intersection(GeneralType lhs, GeneralType rhs) {
        if (!(lhs instanceof SetType<?> || rhs instanceof SetType<?>)) {
            throw new UnsupportedOperationException("Cannot determine union with non SetType.");
        }

        if (((SetType<?>) lhs).getElementType() != ((SetType<?>) rhs).getElementType()) {
            throw new UnsupportedOperationException("Cannot determine union of SetTypes with different element types.");
        }

        // Ugly but the only way I can get it to work...
        HashSet<A> resultSet = new HashSet<>();
        resultSet.addAll((HashSet<A>) (((SetType<?>) lhs).getValue()));
        resultSet.retainAll((HashSet<A>) (((SetType<?>) rhs).getValue()));
        return new SetType<A>(resultSet);
    }

    @SuppressWarnings("unchecked")
    private <A> GeneralType difference(GeneralType lhs, GeneralType rhs) {
        if (!(lhs instanceof SetType<?> || rhs instanceof SetType<?>)) {
            throw new UnsupportedOperationException("Cannot determine union with non SetType.");
        }

        if (((SetType<?>) lhs).getElementType() != ((SetType<?>) rhs).getElementType()) {
            throw new UnsupportedOperationException("Cannot determine union of SetTypes with different element types.");
        }

        // Ugly but the only way I can get it to work...
        HashSet<A> resultSet = new HashSet<>();
        resultSet.addAll((HashSet<A>) (((SetType<?>) lhs).getValue()));
        resultSet.removeAll((HashSet<A>) (((SetType<?>) rhs).getValue()));
        return new SetType<A>(resultSet);
    }

    private GeneralType isSubsetOf(GeneralType lhs, GeneralType rhs) {
        if (lhs instanceof SetType<?> && rhs instanceof SetType<?>) {
            var result = ((Set<?>) rhs.getValue()).containsAll((Set<?>) lhs.getValue());
            return new BooleanType(result);
        }
        throw new UnsupportedOperationException("Cannot determine superset with non SetType.");
    }

    private GeneralType isSupersetOf(GeneralType lhs, GeneralType rhs) {
        if (lhs instanceof SetType<?> && rhs instanceof SetType<?>) {
            var result = ((Set<?>) lhs.getValue()).containsAll((Set<?>) rhs.getValue());
            return new BooleanType(result);
        }
        throw new UnsupportedOperationException("Cannot determine superset with non SetType.");
    }

    private GeneralType equalTo(GeneralType lhs, GeneralType rhs) {
        if (lhs instanceof SetType<?> || rhs instanceof SetType<?>) {
            return new BooleanType(lhs.getValue().equals(rhs.getValue()));
        }
        throw new UnsupportedOperationException("Cannot compare " + rhs.getType() + " to a SetType.");
    }

    private GeneralType notEqualTo(GeneralType lhs, GeneralType rhs) {
        if (lhs instanceof SetType<?> || rhs instanceof SetType<?>) {
            return new BooleanType(!(lhs.getValue().equals(rhs.getValue())));
        }
        throw new UnsupportedOperationException("Cannot compare " + rhs.getType() + " to a SetType.");
    }

    private GeneralType contains(GeneralType lhs, GeneralType rhs) {
        if (lhs instanceof SetType<?>) {
            var result = ((Set<?>) lhs.getValue()).contains(rhs.getValue()) || ((Set<?>) lhs.getValue()).contains(rhs);
            return new BooleanType(result);
        }
        throw new UnsupportedOperationException("Cannot determine if non SetType contains an element.");
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (o == null || getClass() != o.getClass())
            return false;

        return Objects.equals(this.getValue(), ((GeneralType) o).getValue());
    }

    @Override
    public int hashCode() {
        return Objects.hash(this.getValue());
    }

}
