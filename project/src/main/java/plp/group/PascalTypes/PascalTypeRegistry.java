package plp.group.PascalTypes;

import java.util.HashMap;
import java.util.Map;

public class PascalTypeRegistry {
    private final Map<String, Class<? extends PascalType>> typeAliases = new HashMap<>();

    public void registerType(String name, Class<? extends PascalType> typeClass) {
        typeAliases.put(name, typeClass);
    }

    public Class<? extends PascalType> getType(String name) {
        return typeAliases.get(name);
    }
}
