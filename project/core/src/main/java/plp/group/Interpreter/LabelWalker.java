package plp.group.Interpreter;

import java.util.*;
import plp.group.project.delphi;
import plp.group.project.delphiBaseVisitor;

public class LabelWalker extends delphiBaseVisitor<Object> {
    public Map<String, List<delphi.StatementContext>> labelMap = new HashMap<>();

    public Map<String, List<delphi.StatementContext>> walk(delphi.BlockContext ctx) {
        List<delphi.StatementContext> allStatements = ctx.compoundStatement().statements().statement();

        for (int i = 0; i < allStatements.size(); i++) {
            delphi.StatementContext stmt = allStatements.get(i);

            if (stmt.getChildCount() >= 3 && stmt.getChild(1).getText().equals(":")) {
                String labelName = stmt.getChild(0).getText();
                if (labelMap.containsKey(labelName)) {
                    throw new RuntimeException("Duplicate label: " + labelName);
                }
                // start with the labelled statement
                labelMap.put(labelName, allStatements.subList(i, allStatements.size()));
            }
        }
 
        return labelMap;
    }

}
