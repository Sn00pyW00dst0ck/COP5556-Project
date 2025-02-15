package plp.group.Interpreter.Types.Structured;

import java.util.HashMap;

import plp.group.Interpreter.Types.GeneralType;

public class RecordType extends GeneralType {

    public RecordType(HashMap<String, GeneralType> value) {
        super(value);
    }

    // Seems like we only perform operations on RecordType fields individually.
    // To do that, get the value then lookup the field

}
