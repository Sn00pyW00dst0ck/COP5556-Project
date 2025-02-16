package plp.group.Interpreter.Types.Simple;

import plp.group.Interpreter.Types.GeneralType;

public class SubrangeType<T extends Comparable<T>> extends GeneralType {
    private final T lowerBound;
    private final T upperBound;

    public SubrangeType(T lowerBound, T upperBound) {
        super(lowerBound.getClass());
        if (lowerBound.compareTo(upperBound) > 0) {
            throw new IllegalArgumentException("Lower bound cannot be greater than the upper bound.");
        }
        this.lowerBound = lowerBound;
        this.upperBound = upperBound;
        defineOperations();
    }

    public SubrangeType(T lowerBound, T upperBound, T value) {
        super(value);
        if (lowerBound.compareTo(upperBound) > 0) {
            throw new IllegalArgumentException("Lower bound cannot be greater than the upper bound.");
        }
        this.lowerBound = lowerBound;
        this.upperBound = upperBound;
        defineOperations();
    }

    public T getLowerBound() {
        return lowerBound;
    }

    public T getUpperBound() {
        return upperBound;
    }

    private void defineOperations() {
        defineOperation("IN", this::contains);
        defineOperation("=", this::equalTo);
        defineOperation("<>", this::notEqualTo);
        defineOperation("<", this::lessThan);
        defineOperation("<=", this::lessThanOrEqualTo);
        defineOperation(">", this::greaterThan);
        defineOperation(">=", this::greaterThanOrEqualTo);
    }

    @SuppressWarnings("unchecked")
    private GeneralType contains(GeneralType lhs, GeneralType rhs) {
        if (lhs instanceof SubrangeType<?>) {
            var rLower = (((SubrangeType<T>) lhs).getLowerBound() instanceof GeneralType)
                    ? (Comparable<T>) ((GeneralType) ((SubrangeType<T>) lhs).getLowerBound()).getValue()
                    : (T) ((SubrangeType<T>) lhs).getLowerBound();

            var rUpper = (((SubrangeType<T>) lhs).getUpperBound() instanceof GeneralType)
                    ? (Comparable<T>) ((GeneralType) ((SubrangeType<T>) lhs).getUpperBound()).getValue()
                    : (T) ((SubrangeType<T>) lhs).getUpperBound();

            var result = rLower.compareTo((T) rhs.getValue()) <= 0 && rUpper.compareTo((T) rhs.getValue()) >= 0;
            return new BooleanType(result);
        }
        throw new UnsupportedOperationException("Cannot determine if non SubrangeType contains an element.");
    }

    private GeneralType equalTo(GeneralType lhs, GeneralType rhs) {
        if (!(lhs instanceof SubrangeType<?> && rhs instanceof SubrangeType<?>)) {
            throw new UnsupportedOperationException("Cannot determine equality with non SubrangeType.");
        }

        @SuppressWarnings("unchecked")
        var result = ((T) lhs.getValue()).compareTo(((T) rhs.getValue()));
        return new BooleanType(result == 0);
    }

    private GeneralType notEqualTo(GeneralType lhs, GeneralType rhs) {
        if (!(lhs instanceof SubrangeType<?> && rhs instanceof SubrangeType<?>)) {
            throw new UnsupportedOperationException("Cannot determine equality with non SubrangeType.");
        }

        @SuppressWarnings("unchecked")
        var result = ((T) ((SubrangeType<?>) lhs).getValue()).compareTo(((T) ((SubrangeType<?>) rhs).getValue()));
        return new BooleanType(result != 0);
    }

    private GeneralType lessThan(GeneralType lhs, GeneralType rhs) {
        if (!(lhs instanceof SubrangeType<?> && rhs instanceof SubrangeType<?>)) {
            throw new UnsupportedOperationException("Cannot determine equality with non SubrangeType.");
        }

        @SuppressWarnings("unchecked")
        var result = ((T) ((SubrangeType<?>) lhs).getValue()).compareTo(((T) ((SubrangeType<?>) rhs).getValue()));
        return new BooleanType(result < 0);
    }

    private GeneralType lessThanOrEqualTo(GeneralType lhs, GeneralType rhs) {
        if (!(lhs instanceof SubrangeType<?> && rhs instanceof SubrangeType<?>)) {
            throw new UnsupportedOperationException("Cannot determine equality with non SubrangeType.");
        }

        @SuppressWarnings("unchecked")
        var result = ((T) ((SubrangeType<?>) lhs).getValue()).compareTo(((T) ((SubrangeType<?>) rhs).getValue()));
        return new BooleanType(result <= 0);
    }

    private GeneralType greaterThan(GeneralType lhs, GeneralType rhs) {
        if (!(lhs instanceof SubrangeType<?> && rhs instanceof SubrangeType<?>)) {
            throw new UnsupportedOperationException("Cannot determine equality with non SubrangeType.");
        }

        @SuppressWarnings("unchecked")
        var result = ((T) ((SubrangeType<?>) lhs).getValue()).compareTo(((T) ((SubrangeType<?>) rhs).getValue()));
        return new BooleanType(result > 0);
    }

    private GeneralType greaterThanOrEqualTo(GeneralType lhs, GeneralType rhs) {
        if (!(lhs instanceof SubrangeType<?> && rhs instanceof SubrangeType<?>)) {
            throw new UnsupportedOperationException("Cannot determine equality with non SubrangeType.");
        }

        @SuppressWarnings("unchecked")
        var result = ((T) ((SubrangeType<?>) lhs).getValue()).compareTo(((T) ((SubrangeType<?>) rhs).getValue()));
        return new BooleanType(result >= 0);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (o == null || getClass() != o.getClass())
            return false;
        return this.getValue().equals(((SubrangeType<?>) o).getValue());
    }

    @Override
    public int hashCode() {
        return this.getValue().hashCode();
    }
}
