package parser;

import org.antlr.v4.runtime.*;
import org.antlr.v4.runtime.tree.*;
import java.util.*;

public class ConfigurableParser {
    private ParseTree parseTree;
    private Map<String, Object> extractedData = new HashMap<>();

    public void parse(String input) {
        CharStream charStream = CharStreams.fromString(input);
        FormulationLexer lexer = new FormulationLexer(charStream);
        CommonTokenStream tokens = new CommonTokenStream(lexer);
        FormulationParser parser = new FormulationParser(tokens);
        parseTree = parser.program();
    }

    public static class DataExtractor extends FormulationBaseVisitor<Object> {
        private final Map<String, Object> extractedData;
        private int expressionCounter = 1;
        private int statementCounter = 1;
        private int termCounter = 1;

        public DataExtractor(Map<String, Object> extractedData) {
            this.extractedData = extractedData;
        }

        @Override
        public Object visitSetDefExpr(FormulationParser.SetDefExprContext ctx) {
            if (ctx.getChildCount() > 1) {
                String termText = ctx.getText();
                extractedData.put("term_" + (termCounter++), termText);
            }
            return super.visitSetDefExpr(ctx);
        }
       
    }

    public Map<String, Object> extractData() {
        if (parseTree != null) {
            DataExtractor extractor = new DataExtractor(extractedData);
            extractor.visit(parseTree);
        }
        return extractedData;
    }

    public static void main(String[] args) {
        String input = "set v := {\"a\", \"b\"};";
        ConfigurableParser parser = new ConfigurableParser();
        
        try {
            parser.parse(input);
                      
            // Extract all data
            Map<String, Object> data = parser.extractData();
            System.out.println("Extracted Data: " + data);
            
            // // Find specific patterns (e.g., all additions)
            // List<String> additions = parser.findPattern(".*\\+.*");
            // System.out.println("Additions found: " + additions);
            
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}