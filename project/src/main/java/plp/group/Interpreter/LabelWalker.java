package plp.group.Interpreter;

import java.util.*;
import plp.group.project.delphi;

public class LabelWalker {
    public static Map<String, List<delphi.StatementContext>> walk(delphi.BlockContext ctx) {
        Map<String, List<delphi.StatementContext>> labelMap = new HashMap<>();
        List<delphi.StatementContext> allStatements = ctx.compoundStatement().statements().statement();

        for (int i = 0; i < allStatements.size(); i++) {
            delphi.StatementContext stmt = allStatements.get(i);
            if (stmt.getChildCount() >= 3 && stmt.getChild(1).getText().equals(":")) {
                String labelName = stmt.getChild(0).getText();
                if (labelMap.containsKey(labelName)) {
                    throw new RuntimeException("Duplicate label: " + labelName);
                }
                // start AFTER the label statement
                labelMap.put(labelName, allStatements.subList(i + 1, allStatements.size()));
            }
        }

        return labelMap;
    }
}
