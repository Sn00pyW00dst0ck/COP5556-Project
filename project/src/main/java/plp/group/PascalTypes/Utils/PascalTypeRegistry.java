package plp.group.PascalTypes.Utils;

import java.util.HashMap;
import java.util.Map;

import plp.group.PascalTypes.PascalType;
import plp.group.PascalTypes.Scalars.Standard.PascalBoolean;
import plp.group.PascalTypes.Scalars.Standard.PascalByte;
import plp.group.PascalTypes.Scalars.Standard.PascalChar;
import plp.group.PascalTypes.Scalars.Standard.PascalInt64;
import plp.group.PascalTypes.Scalars.Standard.PascalInteger;
import plp.group.PascalTypes.Scalars.Standard.PascalLongint;
import plp.group.PascalTypes.Scalars.Standard.PascalLongword;
import plp.group.PascalTypes.Scalars.Standard.PascalShortint;
import plp.group.PascalTypes.Scalars.Standard.PascalSmallint;
import plp.group.PascalTypes.Scalars.Standard.PascalString;
import plp.group.PascalTypes.Scalars.Standard.PascalWord;
import plp.group.PascalTypes.Scalars.Standard.Reals.PascalReal;

public class PascalTypeRegistry {
    private final Map<String, Class<? extends PascalType>> typeAliases = new HashMap<>();

    {
        registerType("integer", PascalInteger.class);
        // TODO: Cardinal???
        registerType("shortint", PascalShortint.class);
        registerType("smallint", PascalSmallint.class);
        registerType("longint", PascalLongint.class);
        registerType("int64", PascalInt64.class);
        registerType("byte", PascalByte.class);
        registerType("word", PascalWord.class);
        registerType("longword", PascalLongword.class);

        registerType("real", PascalReal.class);

        registerType("boolean", PascalBoolean.class);

        registerType("string", PascalString.class);
        registerType("char", PascalChar.class);

        // TODO: more types??
    }

    public void registerType(String name, Class<? extends PascalType> typeClass) {
        typeAliases.put(name, typeClass);
    }

    public Class<? extends PascalType> getType(String name) {
        return typeAliases.get(name.toLowerCase());
    }
}
