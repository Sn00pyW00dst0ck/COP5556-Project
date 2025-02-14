package plp.group.PascalTypes.Callables;

import java.util.List;

import plp.group.PascalTypes.PascalType;

public class PascalFunctionType extends PascalType {
    private final List<PascalType> parameterTypes;
    private final PascalType returnType;
    private final FunctionImplementation value;

    public PascalFunctionType(List<PascalType> parameterTypes, PascalType returnType,
            FunctionImplementation value) {
        this.parameterTypes = parameterTypes;
        this.returnType = returnType;
        this.value = value;
    }

    public List<PascalType> getParameterTypes() {
        return parameterTypes;
    }

    public PascalType getReturnType() {
        return returnType;
    }

    /**
     * Calling invoke() will call the function and return what the function returns.
     * 
     * @return whatever the stored function would return.
     */
    public Object invoke(Object... args) {
        return value.execute(args);
    }

    @Override
    public String getValue() {
        return "Function(" + parameterTypes + "): " + returnType;
    }

    @Override
    public String toString() {
        return "Function(" + parameterTypes + ") : " + returnType;
    }
}
