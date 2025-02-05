package Model;

import org.antlr.v4.runtime.*;
import org.antlr.v4.runtime.tree.*;

import Model.ModelInput.StructureBlock;
import parser.*;
import parser.FormulationBaseVisitor;
import parser.FormulationLexer;
import parser.FormulationParser;
import parser.FormulationParser.ExprContext;
import parser.FormulationParser.ParamDeclContext;
import parser.FormulationParser.SetDeclContext;
import parser.FormulationParser.SetDefExprContext;
import parser.FormulationParser.SetDescStackContext;
import parser.FormulationParser.SetExprContext;
import parser.FormulationParser.SetExprStackContext;
import parser.FormulationParser.TupleContext;
import parser.FormulationParser.UExprContext;

import java.io.*;
import java.nio.file.*;
import java.util.*;
import java.util.stream.Collectors;

import org.antlr.runtime.tree.TreeWizard;

public class Model implements ModelInterface {
    private final String sourceFilePath;
    ParseTree tree;
    private CommonTokenStream tokens;
    private final Map<String,ModelSet> sets = new HashMap<>();
    private final Map<String,ModelParameter> params = new HashMap<>();
    private final Map<String,ModelConstraint> constraints = new HashMap<>();
    private final Map<String,ModelPreference> preferences = new HashMap<>();
    private final Map<String,ModelVariable> variables = new HashMap<>();
    private final Set<String> toggledOffFunctionalities = new HashSet<>();
    private boolean loadElementsToRam = true;
    private final String zimplCompilationScript = "./src/main/resources/zimpl/checkCompilation.sh";
    private final String zimplSolveScript = "./src/main/resources/zimpl/solve.sh" ;
    private String originalSource;
    
    public Model(String sourceFilePath) throws IOException {
        this.sourceFilePath = sourceFilePath;
        parseSource();
    }
    
    private void parseSource() throws IOException {
        originalSource = new String(Files.readAllBytes(Paths.get(sourceFilePath)));
        CharStream charStream = CharStreams.fromString(originalSource);
        FormulationLexer lexer = new FormulationLexer(charStream);
        tokens = new CommonTokenStream(lexer);
        FormulationParser parser = new FormulationParser(tokens);
        tree = parser.program();
        
        // Initial parse to collect all declarations
        CollectorVisitor collector = new CollectorVisitor();
        collector.visit(tree);
    }
    
    public void appendToSet(ModelSet set, String value) throws Exception {
        // if (!sets.containsKey(setName)) {
        //     throw new IllegalArgumentException("Set " + setName + " not found");
        // }
        if(!set.isCompatible(value))
            throw new Exception("Incompatible types!");
        ModifierVisitor modifier = new ModifierVisitor(tokens, set.getIdentifier(), value, ModifierVisitor.Action.APPEND, originalSource);
        modifier.visit(tree);
        
        if (modifier.isModified()) {
            // Write modified source back to file, preserving original formatting
            String modifiedSource = modifier.getModifiedSource();
            Files.write(Paths.get(sourceFilePath), modifiedSource.getBytes());
            parseSource();
        }
    }
    
    public void removeFromSet(ModelSet set, String value) throws Exception {
        // if (!sets.containsKey(setName)) {
        //     throw new IllegalArgumentException("Set " + setName + " not found");
        // }
        if(!set.isCompatible(value))
            throw new Exception("Incompatible types!");
        ModifierVisitor modifier = new ModifierVisitor(tokens, set.getIdentifier(), value,  ModifierVisitor.Action.DELETE, originalSource);
        modifier.visit(tree);
        
        if (modifier.isModified()) {
            // Write modified source back to file, preserving original formatting
            String modifiedSource = modifier.getModifiedSource();
            Files.write(Paths.get(sourceFilePath), modifiedSource.getBytes());
            parseSource();
        }
    }

    public void setInput(ModelParameter identifier, String value) throws Exception {

        if(!identifier.isCompatible(value))
            throw new Exception("incompatible type");
        
        ModifierVisitor modifier = new ModifierVisitor(tokens, identifier.getIdentifier(), value,  ModifierVisitor.Action.SET, originalSource);
        modifier.visit(tree);
        
        if (modifier.isModified()) {
            // Write modified source back to file, preserving original formatting
            String modifiedSource = modifier.getModifiedSource();
            Files.write(Paths.get(sourceFilePath), modifiedSource.getBytes());
            parseSource();
        }
    }

    public void setInput(ModelSet identifier, String[] values) throws Exception {

        for(String str : values){
           if( !identifier.isCompatible(str))
             throw new Exception(str + " is an incompatible value");
        }
        
        ModifierVisitor modifier = new ModifierVisitor(tokens, identifier.getIdentifier(), values,  ModifierVisitor.Action.SET, originalSource);
        modifier.visit(tree);
        
        if (modifier.isModified()) {
            // Write modified source back to file, preserving original formatting
            String modifiedSource = modifier.getModifiedSource();
            Files.write(Paths.get(sourceFilePath), modifiedSource.getBytes());
            parseSource();
        }
    }
    //TODO: make toggling (commenting out) work for preferences too!
    public void toggleFunctionality(ModelFunctionality mf, boolean turnOn) {
        if (!turnOn) {
            toggledOffFunctionalities.add(mf.getIdentifier());
        } else {
            toggledOffFunctionalities.remove(mf.getIdentifier());
        }
    }

    private void commentOutToggledFunctionalities() throws IOException {
        if (toggledOffFunctionalities.isEmpty()) {
            return;
        }

        ModifierVisitor modifier = new ModifierVisitor(tokens, null, "", ModifierVisitor.Action.COMMENT_OUT, originalSource);
        modifier.setTargetFunctionalities(toggledOffFunctionalities); // Set functionalities to be commented out
        modifier.visit(tree);
        
        if (modifier.isModified()) {
            String modifiedSource = modifier.getModifiedSource();
            Files.write(Paths.get(sourceFilePath), modifiedSource.getBytes());
        //    parseSource();
        }
    }

    private void restoreToggledFunctionalities() throws IOException {
        if (toggledOffFunctionalities.isEmpty()) {
            return;
        }

        // Read the original file content and restore it
        Files.write(Paths.get(sourceFilePath), originalSource.getBytes());
        parseSource();
    }
    public boolean isCompiling(float timeout) {
        try {
            commentOutToggledFunctionalities();
            
            String[] command = {"/bin/bash", zimplCompilationScript, sourceFilePath, String.valueOf(timeout)};
            Process process = Runtime.getRuntime().exec(command);
            BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
            StringBuilder output = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                output.append(line).append("\n");
            }
            int exitCode = process.waitFor();
            boolean result = exitCode == 0 && output.toString().contains("Compilation Successful");
            
            restoreToggledFunctionalities();
            return result;
        } catch (Exception e) {
            try {
                restoreToggledFunctionalities();
            } catch (IOException ex) {
                ex.printStackTrace();
            }
            e.printStackTrace();
            return false;
        }
    }
    
    public Solution solve(float timeout) {
        try {
            commentOutToggledFunctionalities();
            
            String[] command = {"/bin/bash", zimplSolveScript, sourceFilePath, String.valueOf(timeout)};
            Process process = Runtime.getRuntime().exec(command);
            BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
            StringBuilder output = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                output.append(line).append("\n");
            }

            int exitCode = process.waitFor();
            Solution result = exitCode == 0 ? new Solution(sourceFilePath+"SOLUTION",output.toString(),true) : null;
            
            restoreToggledFunctionalities();
            return result;
        } catch (Exception e) {
            try {
                restoreToggledFunctionalities();
            } catch (IOException ex) {
                ex.printStackTrace();
            }
            e.printStackTrace();
            return null;
        }
    }

    List<FormulationParser.UExprContext> findComponentContexts(FormulationParser.NExprContext ctx) {
        List<FormulationParser.UExprContext> components = new ArrayList<>();
        findComponentContextsRecursive(ctx.uExpr(), components);
        return components;
    }

    private void findComponentContextsRecursive(FormulationParser.UExprContext ctx, List<FormulationParser.UExprContext> components) {
        String all = ctx == null ? null : ctx.getText();
        if(ctx == null)
            return;
        // if(ctx.basicExpr() != null){
        //     components.add(ctx);
        //     return;
        // }
        // findComponentContextsRecursive(ctx.uExpr(0), components);
        // findComponentContextsRecursive(ctx.uExpr(1), components);
        if (components.size() == 0 && ctx.uExpr() != null && ctx.uExpr(1) != null) {
            String a = ctx.uExpr(1).getText();
            components.add(ctx.uExpr(1));
        } else if (components.size() == 0){
            components.add(ctx);
        }
        String b = ctx.getText();
        if (ctx.uExpr(0) != null && ctx.uExpr(0).uExpr(1) != null) {
            String c = ctx.uExpr(0).uExpr(1).getText();
            if(ctx.uExpr(0).uExpr(0).basicExpr() != null){
                components.add(ctx.uExpr(0));    
                return;
            }
            else
                components.add(ctx.uExpr(0).uExpr(1));
        } else if(ctx.uExpr(0) != null){
            components.add(ctx.uExpr(0));
        } else {
            components.add(ctx);
        }
        
        findComponentContextsRecursive(ctx.uExpr(0), components);
    }

    private class CollectorVisitor extends FormulationBaseVisitor<Void> {


        public Void visitParamDecl(FormulationParser.ParamDeclContext ctx){
            String paramName = extractName(ctx.sqRef().getText());
            TypeVisitor typer = new TypeVisitor();
            typer.visit(ctx.expr());
            ModelParameter param = new ModelParameter(paramName,typer.getType(), typer.getBasicSets(),typer.getBasicParams());
            if(loadElementsToRam){
                param.setValue(ctx.expr().getText());
            }
            params.put(paramName,param);
            return super.visitParamDecl(ctx);
        }

        @Override
        public Void visitSetDecl(FormulationParser.SetDeclContext ctx) {
            String setName = extractName(ctx.sqRef().getText());
            
            sets.put(setName,new ModelSet(setName,ModelPrimitives.UNKNOWN));
            return super.visitSetDecl(ctx);
        }
        
        @Override
        public Void visitSetDefExpr(FormulationParser.SetDefExprContext ctx) {
            String setName = extractName(ctx.sqRef().getText());
            
            TypeVisitor typer = new TypeVisitor();
            typer.visit(ctx.setExpr());
            ModelSet set = new ModelSet(setName,typer.getType(),typer.getBasicSets(),typer.getBasicParams());
            if(loadElementsToRam){
                java.util.List<String> elements = parseSetElements(ctx.setExpr());
                set.setElements(elements);
            }
            sets.put(setName, set);
            return super.visitSetDefExpr(ctx);
        }

        @Override
        public Void visitConstraint(FormulationParser.ConstraintContext ctx) {
            String constName = extractName(ctx.name.getText());
            TypeVisitor visitor = new TypeVisitor();
            visitor.visit(ctx);
            constraints.put(constName, new ModelConstraint(constName,visitor.getBasicSets(),visitor.getBasicParams()));
            return super.visitConstraint(ctx);
        }

        @Override
        public Void visitObjective(FormulationParser.ObjectiveContext ctx) {
            List<UExprContext> components = findComponentContexts(ctx.nExpr());
    
            for (UExprContext expressionComponent : components) {
                String name = expressionComponent.getText();
                // Create a parse tree for the specific component
                //ParseTree componentParseTree = parseComponentExpression(expressionComponent);
                TypeVisitor visitor = new TypeVisitor();
                visitor.visit(expressionComponent);
                
                preferences.put(expressionComponent.getText(), 
                    new ModelPreference(expressionComponent.getText(), 
                                        visitor.getBasicSets(), 
                                        visitor.getBasicParams())
                );
            }
            
            return super.visitObjective(ctx);
        }
        
        public Void visitVariable(FormulationParser.VariableContext ctx){
            String varName = extractName(ctx.sqRef().getText());
            TypeVisitor visitor = new TypeVisitor();
            visitor.visit(ctx);
            boolean isComplex = true;
            if(ctx.sqRef() instanceof FormulationParser.SqRefCsvContext){
                isComplex = ((FormulationParser.SqRefCsvContext)(ctx.sqRef())).csv() == null ? false : true;
            }
            variables.put(varName, new ModelVariable(varName, visitor.getBasicSets(), visitor.getBasicParams(),isComplex));
            return super.visitVariable(ctx);
        }

        private String extractName(String sqRef) {
            // Handle indexed sets by taking the base name
            int bracketIndex = sqRef.indexOf('[');
            return bracketIndex == -1 ? sqRef : sqRef.substring(0, bracketIndex);
        }
        
        
        
        @Deprecated
        private ParseTree parseComponentExpression(String component) {
            
            CharStream input = CharStreams.fromString(component);
            FormulationLexer lexer = new FormulationLexer(input);
            CommonTokenStream tokens = new CommonTokenStream(lexer);
            FormulationParser parser = new FormulationParser(tokens);
            
            // Parse as a numerical expression
            return parser.nExpr().uExpr();
        }
        @Deprecated
        private List<String> splitExpression(String expr) {
            List<String> components = new ArrayList<>();
            int parenthesesLevel = 0;
            StringBuilder currentComponent = new StringBuilder();
            
            for (char c : expr.toCharArray()) {
                if (c == '(') {
                    parenthesesLevel++;
                } else if (c == ')') {
                    parenthesesLevel--;
                }
                
                if ((c == '+' || c == '-') && parenthesesLevel == 0) {
                    if (currentComponent.length() > 0) {
                        components.add(currentComponent.toString().trim());
                        currentComponent = new StringBuilder();
                    }
                }
                
                currentComponent.append(c);
            }
            
            if (currentComponent.length() > 0) {
                components.add(currentComponent.toString().trim());
            }
            
            return components;
        }
        
        private java.util.List<String> parseSetElements(FormulationParser.SetExprContext ctx) {
            java.util.List<String> elements = new ArrayList<>();
            
            if (ctx instanceof FormulationParser.SetExprStackContext) {
                FormulationParser.SetExprStackContext stackCtx = (FormulationParser.SetExprStackContext) ctx;
                if (stackCtx.setDesc() != null) {
                    // Handle explicit set descriptions
                    if (stackCtx.setDesc() instanceof FormulationParser.SetDescStackContext) {
                        FormulationParser.SetDescStackContext descCtx = (FormulationParser.SetDescStackContext) stackCtx.setDesc();
                        if (descCtx.csv() != null) {
                            String csvText = descCtx.csv().getText();
                            
                            // Split the CSV text by commas, but not within angle brackets
                            StringBuilder currentElement = new StringBuilder();
                            boolean inAngleBrackets = false;
                            
                            for (int i = 0; i < csvText.length(); i++) {
                                char c = csvText.charAt(i);
                                
                                if (c == '<') {
                                    inAngleBrackets = true;
                                    currentElement.append(c);
                                } else if (c == '>') {
                                    inAngleBrackets = false;
                                    currentElement.append(c);
                                } else if (c == ',' && !inAngleBrackets) {
                                    // When encountering a comma outside of angle brackets, add the current element to the list
                                    elements.add(currentElement.toString().trim());
                                    currentElement.setLength(0); // Reset the current element
                                } else {
                                    currentElement.append(c);
                                }
                            }
                            
                            // Add the last element if it exists
                            if (currentElement.length() > 0) {
                                elements.add(currentElement.toString().trim());
                            }
                        }
                    }
                }
            }
            
            return elements;
        }
    }
    
    private class ModifierVisitor extends FormulationBaseVisitor<Void> {
        private final CommonTokenStream tokens;
        private String targetIdentifier; // For single-target operations
        private String[] targetValues; // For single-target operations
        private Set<String> targetFunctionalities; // For multi-target operations
        private final Action act;
        private final String originalSource;
        private boolean modified = false;
        private StringBuilder modifiedSource;

        enum Action {
            APPEND,
            DELETE,
            SET,
            COMMENT_OUT,
            UNCOMMENT
        }

        // Original constructor for backward compatibility
        public ModifierVisitor(CommonTokenStream tokens, String targetIdentifier, String value, Action act, String originalSource) {
            this.tokens = tokens;
            this.targetIdentifier = targetIdentifier;
            this.targetValues = new String[]{value};
            this.act = act;
            this.originalSource = originalSource;
            this.modifiedSource = new StringBuilder(originalSource);
        }

        public ModifierVisitor(CommonTokenStream tokens, String targetIdentifier, String[] values, Action act, String originalSource) {
            this.tokens = tokens;
            this.targetIdentifier = targetIdentifier;
            this.targetValues = values;
            this.act = act;
            this.originalSource = originalSource;
            this.modifiedSource = new StringBuilder(originalSource);
        }

        // Method to set target functionalities for commenting out
        public void setTargetFunctionalities(Set<String> functionalities) {
            this.targetFunctionalities = functionalities;
        }

        private void modifyParamContent(FormulationParser.ExprContext ctx) {
            // Get the original text with its formatting
            int startIndex = ctx.start.getStartIndex();
            int stopIndex = ctx.stop.getStopIndex();
            String originalLine = originalSource.substring(startIndex, stopIndex + 1);
            
            // Preserve indentation
            String indentation = "";
            int lineStart = originalSource.lastIndexOf('\n', startIndex);
            if (lineStart != -1) {
                indentation = originalSource.substring(lineStart + 1, startIndex);
            }

            // Modify the set content while preserving formatting
            String modifiedLine = originalLine.replaceFirst(ctx.getText(), targetValues[0]);
            
            if (!originalLine.equals(modifiedLine)) {
                modifiedSource.replace(startIndex, stopIndex + 1, modifiedLine);
                modified = true;
            }
        }

        private void modifySetContent(FormulationParser.SetDefExprContext ctx, 
                                    FormulationParser.SetExprStackContext stackCtx) {
            // Get the original text with its formatting
            int startIndex = ctx.start.getStartIndex();
            int stopIndex = ctx.stop.getStopIndex();
            String originalLine = originalSource.substring(startIndex, stopIndex + 1);
            
            // Preserve indentation
            String indentation = "";
            int lineStart = originalSource.lastIndexOf('\n', startIndex);
            if (lineStart != -1) {
                indentation = originalSource.substring(lineStart + 1, startIndex);
            }
            String modifiedLine = originalLine;
            // Modify the set content while preserving formatting
            if(act == Action.APPEND)
                modifiedLine = modifySetLine(originalLine, targetValues, true);
            else if (act == Action.DELETE)
                modifiedLine = modifySetLine(originalLine, targetValues, false);
            else if (act == Action.SET)
                modifiedLine = modifySetLine(originalLine, targetValues, true);
            else
                System.out.println("ERROR - shouldnt reach this line (Model.java - modifySetContent(...))");

            if (!originalLine.equals(modifiedLine)) {
                modifiedSource.replace(startIndex, stopIndex + 1, indentation + modifiedLine);
                modified = true;
            }
        }
        
        private void commentOutParameter(FormulationParser.ParamDeclContext ctx) {
            // Get the original text with its formatting
            int startIndex = ctx.start.getStartIndex();
            int stopIndex = ctx.stop.getStopIndex();
            String originalLine = originalSource.substring(startIndex, stopIndex + 1);
            
            // Preserve indentation
            String indentation = "";
            int lineStart = originalSource.lastIndexOf('\n', startIndex);
            if (lineStart != -1) {
                indentation = originalSource.substring(lineStart + 1, startIndex);
            }
            
            // Add comment marker while preserving indentation
            modifiedSource.replace(startIndex, stopIndex + 1, 
                indentation + "# " + originalLine.substring(indentation.length()));
            modified = true;
        }
        
        private void commentOutSet(FormulationParser.SetDefExprContext ctx) {
            // Get the original text with its formatting
            int startIndex = ctx.start.getStartIndex();
            int stopIndex = ctx.stop.getStopIndex();
            String originalLine = originalSource.substring(startIndex, stopIndex + 1);
            
            // Preserve indentation
            String indentation = "";
            int lineStart = originalSource.lastIndexOf('\n', startIndex);
            if (lineStart != -1) {
                indentation = originalSource.substring(lineStart + 1, startIndex);
            }
            
            // Add comment marker while preserving indentation
            modifiedSource.replace(startIndex, stopIndex + 1, 
                indentation + "# " + originalLine.substring(indentation.length()));
            modified = true;
        }

        @Override
        public Void visitParamDecl(FormulationParser.ParamDeclContext ctx) {
            String paramName = extractName(ctx.sqRef().getText());
            if (paramName.equals(targetIdentifier)) {
                if(act == Action.SET)
                    modifyParamContent(ctx.expr());
                else if (act == Action.COMMENT_OUT)
                    commentOutParameter(ctx);
            }
            return super.visitParamDecl(ctx);
        }

        @Override
        public Void visitSetDefExpr(FormulationParser.SetDefExprContext ctx) {
            String setName = extractName(ctx.sqRef().getText());
            if (setName.equals(targetIdentifier)) {
                if (act == Action.COMMENT_OUT)
                    commentOutSet(ctx);
                else if (ctx.setExpr() instanceof FormulationParser.SetExprStackContext) {
                    FormulationParser.SetExprStackContext stackCtx = 
                        (FormulationParser.SetExprStackContext) ctx.setExpr();
                    if (stackCtx.setDesc() instanceof FormulationParser.SetDescStackContext) {
                        modifySetContent(ctx, stackCtx);
                    }
                }
            }
            return super.visitSetDefExpr(ctx);
        }

        @Override
        public Void visitConstraint(FormulationParser.ConstraintContext ctx) {
            String constraintName = extractName(ctx.name.getText());
            if ((targetFunctionalities != null && targetFunctionalities.contains(constraintName)) ||
                (targetIdentifier != null && constraintName.equals(targetIdentifier))) {
                if (act == Action.COMMENT_OUT)
                    commentOutConstraint(ctx);
            }
            return super.visitConstraint(ctx);
        }

        @Override
        public Void visitObjective(FormulationParser.ObjectiveContext ctx) {
            List<UExprContext> components = findComponentContexts(ctx.nExpr());
            for( UExprContext subCtx : components){
                
                    String objectiveName = subCtx.getText();
                    if ((targetFunctionalities != null && targetFunctionalities.contains(objectiveName)) ||
                        (targetIdentifier != null && objectiveName.equals(targetIdentifier))) {
                        if (act == Action.COMMENT_OUT)
                            zeroOutPreference(subCtx);
                    }
                
            }
            return super.visitObjective(ctx);
        }

        // ... keep all existing helper methods (modifyParamContent, commentOutParameter, etc.) ...

        private String modifySetLine(String line, String[] values, boolean isAppend) {
            // Find the set content between braces
            int openBrace = line.indexOf('{');
            int closeBrace = line.lastIndexOf('}');
            
            if (openBrace != -1 && closeBrace != -1) {
                String beforeBraces = line.substring(0, openBrace + 1);
                String afterBraces = line.substring(closeBrace);
                String content = ""; // if Action.SET, then leave content empty string
                if(this.act == Action.APPEND || this.act == Action.DELETE)
                    content = line.substring(openBrace + 1, closeBrace).trim();
                for(String val : values){
                    if (isAppend) {
                        // Add value
                        content = content.isEmpty() ? val : content + ", " + val;
                    } else {
                        // Remove value
                        content = Arrays.stream(content.split(","))
                                    .map(String::trim)
                                    .filter(s -> !s.equals(val))
                                    .collect(Collectors.joining(", "));
                    }
                }
                return beforeBraces + content + afterBraces;
            }
            return line;
        }

        private void commentOutConstraint(FormulationParser.ConstraintContext ctx) {
            int startIndex = ctx.start.getStartIndex();
            int stopIndex = ctx.stop.getStopIndex();
            String fullStatement = originalSource.substring(startIndex, stopIndex + 1);
            
            // Split into lines while preserving the original line endings
            String[] lines = fullStatement.split("(?<=\n)");
            StringBuilder commentedOut = new StringBuilder();
            
            // Get the initial indentation from the first line
            String initialIndent = "";
            int lineStart = originalSource.lastIndexOf('\n', startIndex);
            if (lineStart != -1) {
                initialIndent = originalSource.substring(lineStart + 1, startIndex);
            }
            
            // Comment out each line while preserving its relative indentation
            for (String line : lines) {
                // If it's not the last line (which won't have a newline)
                if (line.endsWith("\n")) {
                    commentedOut.append(initialIndent).append("# ").append(line.substring(0, line.length()-1)).append("\n");
                } else {
                    commentedOut.append(initialIndent).append("# ").append(line);
                }
            }
            
            modifiedSource.replace(startIndex, stopIndex + 1, commentedOut.toString());
            modified = true;
        }

        private void commentOutPreference(FormulationParser.UExprContext ctx) {
            int startIndex = ctx.start.getStartIndex();
            int stopIndex = ctx.stop.getStopIndex();
            String originalLine = originalSource.substring(startIndex, stopIndex + 1);
            
            String indentation = "";
            int lineStart = originalSource.lastIndexOf('\n', startIndex);
            if (lineStart != -1) {
                indentation = originalSource.substring(lineStart + 1, startIndex);
            }
            
            modifiedSource.replace(startIndex, stopIndex + 1, 
                indentation + "# " + originalLine.substring(indentation.length()));
            modified = true;
        }

        private void zeroOutPreference(FormulationParser.UExprContext ctx) {
            int startIndex = ctx.start.getStartIndex();
            int stopIndex = ctx.stop.getStopIndex();
            String originalLine = originalSource.substring(startIndex, stopIndex + 1);
            
            String indentation = "";
            int lineStart = originalSource.lastIndexOf('\n', startIndex);
            if (lineStart != -1) {
                indentation = originalSource.substring(lineStart + 1, startIndex);
            }
            
            modifiedSource.replace(startIndex, stopIndex + 1, 
                  "((" + originalLine + ")*0)");
            modified = true;
        }

        private String extractName(String sqRef) {
            int bracketIndex = sqRef.indexOf('[');
            return bracketIndex == -1 ? sqRef : sqRef.substring(0, bracketIndex);
        }

        public boolean isModified() {
            return modified;
        }

        public String getModifiedSource() {
            return modifiedSource.toString();
        }
    }

    // private class TypeVisitor extends FormulationBaseVisitor<Void> {
    //     ModelType type = ModelPrimitives.UNKNOWN;
    //     List<ModelSet> setComposition = new LinkedList<ModelSet>();
    //     List<ModelParameter> paramComposition = new LinkedList<ModelParameter>();
    //     boolean isVariable = false;

    //     public ModelType getType(){
    //         return type;
    //     }
    //     public List<ModelSet> getComposition() {
    //         //implement
    //     } 

    //     public Void visitStrExprToken(FormulationParser.StrExprTokenContext ctx){
    //         if(type == ModelPrimitives.UNKNOWN)
    //             type = ModelPrimitives.TEXT;
    //         else if( type instanceof Tuple){
    //             ((Tuple)type).append(ModelPrimitives.TEXT);
    //         } else {
    //             //nothing
    //         }
    //         return super.visitStrExprToken(ctx);
    //     }

    //     public Void visitBasicExprToken(FormulationParser.BasicExprTokenContext ctx){
    //         ModelPrimitives tmp = ModelPrimitives.UNKNOWN;
    //         if(ctx.FLOAT() != null){
    //             tmp = ModelPrimitives.FLOAT;
    //         } else if(ctx.INFINITY() != null){
    //             tmp = ModelPrimitives.INFINITY;
    //         } else if(ctx.INT() != null){
    //             tmp = ModelPrimitives.INT;
    //         }
    //         if(type == ModelPrimitives.UNKNOWN){
    //             type = tmp;
    //         } else if ( type instanceof Tuple){
    //             ((Tuple) type).append(tmp);
    //         } else {

    //         }
    //         return super.visitBasicExprToken(ctx);
    //     }
    
    //     public Void visitTuple(FormulationParser.TupleContext ctx){
    //         if(type == ModelPrimitives.UNKNOWN){
    //             type = new Tuple();
    //         } else if (type instanceof Tuple) {

    //         }
    //         return super.visitTuple(ctx);
    //     }
        
    //     public Void visitVairable (FormulationParser.VariableContext ctx){
    //         isVariable = true;

    //         return super.visitVariable(ctx);
    //     }

    //     public Void visitSetExprBin(FormulationParser.SetExprBinContext ctx) {
    //         TypeVisitor left = new TypeVisitor();
    //         TypeVisitor right = new TypeVisitor();
    //         left.visit(ctx.setExpr(0));
    //         right.visit(ctx.setExpr(0));
    //         setComposition.addAll(left.getComposition());
    //         setComposition.addAll(right.getComposition());
    //         return super.visitSetExprBin(ctx);
    //     }
    // }

    public class TypeVisitor extends FormulationBaseVisitor<Void> {
        private ModelType type = ModelPrimitives.UNKNOWN;
        private  List<ModelSet> basicSets ;
        private  List<ModelParameter> basicParams;
        

        public TypeVisitor(){
            basicSets = new LinkedList<>();
            basicParams = new LinkedList<>();
        }
    
        // Main visitor methods for type analysis
        @Override
        public Void visitSetExprBin(FormulationParser.SetExprBinContext ctx) {
            // Handle binary set operations (*, +, \, -)
            TypeVisitor leftVisitor = new TypeVisitor();
            TypeVisitor rightVisitor = new TypeVisitor();
        
            // Check if the left set is already identified
            ModelSet leftSet = getSet(ctx.setExpr(0).getText());
            if (leftSet != null) {
                basicSets.add(leftSet);
                type = leftSet.getType(); // Inherit the type
            } else {
                leftVisitor.visit(ctx.setExpr(0));
                basicSets.addAll(leftVisitor.getBasicSets());
                basicParams.addAll(leftVisitor.getBasicParams());
            }
        
            // Check if the right set is already identified
            ModelSet rightSet = getSet(ctx.setExpr(1).getText());
            if (rightSet != null) {
                basicSets.add(rightSet);
                type = rightSet.getType(); // Inherit the type
            } else {
                rightVisitor.visit(ctx.setExpr(1));
                basicSets.addAll(rightVisitor.getBasicSets());
                basicParams.addAll(rightVisitor.getBasicParams());
            }
        
            // Handle type combination based on the operator
            if (ctx.op.getText().equals("*") || ctx.op.getText().equals("cross")) {
                // For Cartesian product, combine types into a tuple
                type = new Tuple();
                if (leftSet != null) {
                    ((Tuple) type).append(leftSet.getType());
                } else if (leftVisitor.getType() instanceof Tuple) {
                    ((Tuple) type).append((Tuple) leftVisitor.getType());
                } else {
                    ((Tuple) type).append(leftVisitor.getType());
                }
        
                if (rightSet != null) {
                    ((Tuple) type).append(rightSet.getType());
                } else if (rightVisitor.getType() instanceof Tuple) {
                    ((Tuple) type).append((Tuple) rightVisitor.getType());
                } else {
                    ((Tuple) type).append(rightVisitor.getType());
                }
            } else {
                // For union, difference, etc., types must match
                if (leftSet != null) {
                    type = leftSet.getType();
                } else if (rightSet != null) {
                    type = rightSet.getType();
                } else {
                    type = leftVisitor.getType();
                }
            }
        
            return null;
        }

        @Override
        public Void visitVariable(FormulationParser.VariableContext ctx){
            visit(ctx.sqRef());
            return null;
        }
        
        @Override
        public Void visitConstraint(FormulationParser.ConstraintContext ctx){
            
            for(FormulationParser.ForallContext ctxFA : ctx.forall()){
                TypeVisitor v = new TypeVisitor();
                v.visit(ctxFA);
                basicParams.addAll(v.basicParams);
                basicSets.addAll(v.basicSets);
            }
            TypeVisitor v = new TypeVisitor();
            v.visit(ctx.comparison());
            basicParams.addAll(v.basicParams);
            basicSets.addAll(v.basicSets);

            return null;
        }

        @Override
        public Void visitComparisonIfExpr(FormulationParser.ComparisonIfExprContext ctx){
            this.visit(ctx.ifExpr());
            return null;
        }

        @Override
        public Void visitObjective(FormulationParser.ObjectiveContext ctx){
            this.visit(ctx.nExpr());
            return null;
        }
        
        @Override
        public Void visitForall(FormulationParser.ForallContext ctx){
            this.visit(ctx.condition());
            return null;
        }

        @Override
        public Void visitLongRedExpr(FormulationParser.LongRedExprContext ctx){
            this.visit(ctx.condition());
            this.visit(ctx.nExpr());
            return null;
        }

        @Override
        public Void visitShortRedExpr(FormulationParser.ShortRedExprContext ctx){
            this.visit(ctx.index());
            return null;
        }

        @Override
        public Void visitRegIfExpr(FormulationParser.RegIfExprContext ctx){
            visit(ctx.boolExpr());
            visit(ctx.thenExpr);
            if (ctx.elseExpr != null)
                visit(ctx.elseExpr);

            return null;
        }

        @Override
        public Void visitVarIfExpr(FormulationParser.VarIfExprContext ctx){
            visit(ctx.boolExpr());
            visit(ctx.thenExpr);
            if (ctx.elseExpr != null)
                visit(ctx.elseExpr);

            return null;
        }
    
        @Override
        public Void visitSetDescStack(FormulationParser.SetDescStackContext ctx) {
            if (ctx.condition() != null){
                TypeVisitor elementVisitor = new TypeVisitor();
                elementVisitor.visit(ctx.condition());
                ModelSet s = new ModelSet("anonymous_set", elementVisitor.type,elementVisitor.basicSets,elementVisitor.basicParams);
                basicSets.add(s);
                type = elementVisitor.getType();
            }
            else if (ctx.csv() != null) {
                // Handle explicit set elements
                TypeVisitor elementVisitor = new TypeVisitor();
                elementVisitor.visit(ctx.csv().expr(0));
                ModelSet s = new ModelSet("anonymous_set", elementVisitor.type,elementVisitor.basicSets,elementVisitor.basicParams);
                // Add this as a basic set since it's explicitly defined
                basicSets.add(s);
                type = elementVisitor.getType();
            } else if (ctx.range() != null) {
                ModelSet s = new ModelSet("anonymous_set", ModelPrimitives.INT);
                basicSets.add(s);
                type = ModelPrimitives.INT;
                TypeVisitor visitor = new TypeVisitor();
                visitor.visit(ctx.range());
                s.paramDependencies.addAll(visitor.getBasicParams());
                s.setDependencies.addAll(visitor.getBasicSets());
            } 
            return null;
        }

        @Override
        public Void visitRange(FormulationParser.RangeContext ctx){

            if(params.get(ctx.lhs.getText()) != null){
                basicParams.add(params.get(ctx.lhs.getText()));
            }
            if(params.get(ctx.rhs.getText()) != null){
                basicParams.add(params.get(ctx.rhs.getText()));
            }
            if(ctx.step != null && params.get(ctx.step.getText()) != null){
                basicParams.add(params.get(ctx.step.getText()));
            }
            type = ModelPrimitives.INT;
            return null;
        }

        

        @Override
        public Void visitSqRefCsv(FormulationParser.SqRefCsvContext ctx){

            if(ctx.ID().getText() != null && getSet(ctx.ID().getText()) != null){
                basicSets.add(getSet(ctx.ID().getText()));
            } else if(ctx.ID().getText() != null && getParameter(ctx.ID().getText()) != null){
                basicParams.add(getParameter(ctx.ID().getText()));
            }

            if(ctx.csv() != null && getSet(ctx.csv().getText()) != null){
                basicSets.add(getSet(ctx.csv().getText()));
            }
            else if(ctx.csv() != null && getParameter(ctx.csv().getText()) != null){
                basicParams.add(getParameter(ctx.csv().getText()));
            }  
            else if(ctx.csv() != null){
                visit(ctx.csv());
            }
            return null;
        }

        @Override
        public Void visitProjFunc(FormulationParser.ProjFuncContext ctx) {
            TypeVisitor visitor = new TypeVisitor();
            visitor.visit(ctx.setExpr());
            ModelType customType = new Tuple();
            List<Integer> pointersToSetComp = new LinkedList<>();
            String structureTuple = ctx.tuple().csv().getText();
            String[] d = structureTuple.split(",");
            for(String tctx : structureTuple.split(",")){
                pointersToSetComp.add(Integer.parseInt(tctx));
            }
            
            int count = 0;
            for(ModelSet s : visitor.basicSets){
                count += s.getStructure().length;
            }
            StructureBlock[] totalStructure = new StructureBlock[count];
            count = 0;
            for(ModelSet s : visitor.basicSets){
                int i = 1;
                for(StructureBlock sb : s.getStructure()){
                    totalStructure[count] = new StructureBlock(s, sb == null && s.identifier.equals("anonymous_set") ? i : sb.position);
                    count++;
                    i++;
                }   
            }

            StructureBlock[] resultingStructure = new StructureBlock[pointersToSetComp.size()];
            count = 0;
            for(Integer p : pointersToSetComp){
                resultingStructure[count++] = totalStructure[p-1];
            }
            ModelSet newSet = new ModelSet("anonymous_set",visitor.getBasicSets(),visitor.getBasicParams(),resultingStructure);
            basicSets.add(newSet);
            if(type == null || type == ModelPrimitives.UNKNOWN)
                type = newSet.getType();
            else if(type instanceof Tuple)
                ((Tuple)type).append(newSet.getType());

            return null;
        }
        
        @Override
        public Void visitStrExprToken(FormulationParser.StrExprTokenContext ctx) {
            handleBasicType(ModelPrimitives.TEXT);
            return null;
        }
        
        @Override
        public Void visitBasicExprToken(FormulationParser.BasicExprTokenContext ctx) {
            if (ctx.FLOAT() != null) {
                handleBasicType(ModelPrimitives.FLOAT);
            } else if (ctx.INT() != null) {
                handleBasicType(ModelPrimitives.INT);
            } else if (ctx.INFINITY() != null) {
                handleBasicType(ModelPrimitives.INFINITY);
            }
            return null;
        }

        @Override
        public Void visitCondition(FormulationParser.ConditionContext ctx){
            this.visit(ctx.setExpr());
            if(ctx.boolExpr() != null)
                this.visit(ctx.boolExpr());
            return null;
        }

        @Override
        public Void visitComparisonStrExpr(FormulationParser.ComparisonStrExprContext ctx){
            this.visit(ctx.lhs);
            this.visit(ctx.rhs);
            return null;
        }
        @Override
        public Void visitBoolExprBin(FormulationParser.BoolExprBinContext ctx){
            this.visit(ctx.boolExpr(0));
            this.visit(ctx.boolExpr(1));
            return null;
        }
        
        @Override
        public Void visitTuple(FormulationParser.TupleContext ctx) {
            Tuple tupleType = new Tuple();
            
            // Visit each element in the tuple
            if (ctx.csv() != null) {
                for (ExprContext ec : ctx.csv().expr()){
                    TypeVisitor elementVisitor = new TypeVisitor();
                    elementVisitor.visit(ec);
                    if (elementVisitor.getType() instanceof Tuple) {
                        tupleType.append((Tuple) elementVisitor.getType());
                    } else {
                        tupleType.append(elementVisitor.getType());
                    }
                }
            }
            
            type = tupleType;
            return null;
        }
    
        private void handleBasicType(ModelType newType) {
            if (type == ModelPrimitives.UNKNOWN) {
                type = newType;
            } else if (type instanceof Tuple) {
                ((Tuple) type).append(newType);
            }
        }
    
        // Getter methods
        public ModelType getType() {
            return type;
        }
        
        public List<ModelSet> getBasicSets() {
            return basicSets;
        }
        public List<ModelParameter> getBasicParams() {
            return basicParams;
        }
    }
    
    
    public ModelSet getSet(String identifier) {
        return sets.get(identifier);
    }
    
    public ModelParameter getParameter(String identifier) {
        return params.get(identifier);
    }
    
    public ModelConstraint getConstraint(String identifier) {
        return constraints.get(identifier);
    }

    @Override
    public Collection<ModelConstraint> getConstraints() {
        return constraints.values();
    }

    public ModelPreference getPreference(String identifier) {
        return preferences.get(identifier);
    }

    @Override
    public Collection<ModelPreference> getPreferences() {
        return preferences.values();
    }

    public ModelVariable getVariable(String identifier) {
        return variables.get(identifier);
    }

    @Override
    public Collection<ModelVariable> getVariables() {
        return variables.values();
    }
    @Override
    public Collection<ModelSet> getSets(){
        return this.sets.values();
    }
    
    @Override
    public Collection<ModelParameter> getParameters(){
        return this.params.values();
    }
    @Override
    public Collection<ModelVariable> getVariables(Collection<String> identifiers){
        HashSet<ModelVariable> set = new HashSet<>();
        for (String identifier : identifiers) {
            set.add(getVariable(identifier));
        }
        return set;
    }


}