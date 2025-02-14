package plp.group.PascalTypes;

import java.util.List;

public class PascalProcedureType extends PascalType {
    private final List<PascalType> parameterTypes;
    private final ProcedureImplementation value;

    public PascalProcedureType(List<PascalType> parameterTypes, ProcedureImplementation value) {
        this.parameterTypes = parameterTypes;
        this.value = value;
    }

    public List<PascalType> getParameterTypes() {
        return parameterTypes;
    }

    /**
     * Calling invoke() will call the function and return what the function returns.
     */
    public void invoke(Object... args) {
        value.execute(args);
    }

    @Override
    public String getValue() {
        return "Procedure(" + parameterTypes + ")";
    }

    @Override
    public String toString() {
        return "Procedure(" + parameterTypes + ")";
    }
}
