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
import plp.group.PascalTypes.Scalars.Standard.PascalReal;
import plp.group.PascalTypes.Scalars.Standard.PascalShortint;
import plp.group.PascalTypes.Scalars.Standard.PascalSmallint;
import plp.group.PascalTypes.Scalars.Standard.PascalString;
import plp.group.PascalTypes.Scalars.Standard.PascalWord;

public class PascalTypeRegistry {
    private final Map<String, Class<? extends PascalType>> typeAliases = new HashMap<>();

    {
        registerType("Integer", PascalInteger.class);
        // TODO: Cardinal???
        registerType("Shortint", PascalShortint.class);
        registerType("Smallint", PascalSmallint.class);
        registerType("Longint", PascalLongint.class);
        registerType("Int64", PascalInt64.class);
        registerType("Byte", PascalByte.class);
        registerType("Word", PascalWord.class);
        registerType("Longword", PascalLongword.class);

        registerType("Real", PascalReal.class);

        registerType("Boolean", PascalBoolean.class);

        registerType("String", PascalString.class);
        registerType("Char", PascalChar.class);

        // TODO: more types??
    }

    public void registerType(String name, Class<? extends PascalType> typeClass) {
        typeAliases.put(name, typeClass);
    }

    public Class<? extends PascalType> getType(String name) {
        return typeAliases.get(name);
    }
}
