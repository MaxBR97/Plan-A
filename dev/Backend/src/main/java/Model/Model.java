package Model;

import org.antlr.v4.runtime.*;
import org.antlr.v4.runtime.tree.*;
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

import java.io.*;
import java.nio.file.*;
import java.util.*;
import java.util.stream.Collectors;

import org.antlr.runtime.tree.TreeWizard;

public class Model {
    private final String sourceFilePath;
    private ParseTree tree;
    private CommonTokenStream tokens;
    private final Set<ModelSet> sets = new HashSet<>();
    private final Set<ModelParameter> params = new HashSet<>();
    private final Set<ModelConstraint> constraints = new HashSet<>();
    private final Set<ModelPreference> preferences = new HashSet<>();
    private final Set<ModelVariable> variables = new HashSet<>();
    private boolean loadElementsToRam = true;
    private final String zimplCompilationScript = "/Plan-A/dev/Backend/src/main/resources/zimpl/checkCompilation.sh";
    private final String zimplSolveScript = "/Plan-A/dev/Backend/src/main/resources/zimpl/solve.sh" ;
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

    public void setInput(ModelInput identifier, String value) throws Exception {

        if(!identifier.isCompatible(value))
            throw new Exception("incompatible type");
        if(identifier instanceof ModelSet)
            throw new Exception("unimplemented for sets yet!");
        
        ModifierVisitor modifier = new ModifierVisitor(tokens, identifier.getIdentifier(), value,  ModifierVisitor.Action.SET, originalSource);
        modifier.visit(tree);
        
        if (modifier.isModified()) {
            // Write modified source back to file, preserving original formatting
            String modifiedSource = modifier.getModifiedSource();
            Files.write(Paths.get(sourceFilePath), modifiedSource.getBytes());
            parseSource();
        }
    }

    public void toggleFunctionality (ModelFunctionality mf, boolean turnOn) throws Exception {
        ModifierVisitor modifier;
        if(turnOn == false)
             modifier = new ModifierVisitor(tokens, mf.getIdentifier(), null,  ModifierVisitor.Action.COMMENT_OUT, originalSource);
        else
        {
            modifier = new ModifierVisitor(tokens, mf.getIdentifier(), null,  ModifierVisitor.Action.UNCOMMENT, originalSource);
            throw new Exception("didnt implement uncomment functionality");
        }
        modifier.visit(tree);
        
        if (modifier.isModified()) {
            
            String modifiedSource = modifier.getModifiedSource();
            Files.write(Paths.get(sourceFilePath), modifiedSource.getBytes());
            parseSource();
        }
    }

    public boolean isCompiling(float timeout) {
        try {
            String[] command = {"/bin/bash", zimplCompilationScript, sourceFilePath, String.valueOf(timeout)};
            Process process = Runtime.getRuntime().exec(command);
            BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
            StringBuilder output = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                output.append(line).append("\n");
            }

            int exitCode = process.waitFor();
            if (exitCode == 0 && output.toString().contains("Compilation Successful")) {
                return true; 
            } else {
                return false; 
            }
        } catch (Exception e) {
            e.printStackTrace();
            return false; 
        }
    }
    
    public SolutionDTO solve(float timeout) {
        try {
            String[] command = {"/bin/bash", zimplSolveScript, sourceFilePath, String.valueOf(timeout)};
            Process process = Runtime.getRuntime().exec(command);
            BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
            StringBuilder output = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                output.append(line).append("\n");
            }

            int exitCode = process.waitFor();
            if (exitCode == 0) {
                //SolutionDTO ans = new SolutionDTO()
            } else {
                return null; 
            }
        } catch (Exception e) {
            e.printStackTrace();
            return null; 
        }
    }
    private class CollectorVisitor extends FormulationBaseVisitor<Void> {


        public Void visitParamDecl(FormulationParser.ParamDeclContext ctx){
            String paramName = extractName(ctx.sqRef().getText());
            TypeVisitor typer = new TypeVisitor();
            typer.visit(ctx.expr());
            ModelParameter param = new ModelParameter(paramName,typer.getType());
            if(loadElementsToRam){
                param.setValue(ctx.expr().getText());
            }
            params.add(param);
            return super.visitParamDecl(ctx);
        }

        @Override
        public Void visitSetDecl(FormulationParser.SetDeclContext ctx) {
            String setName = extractName(ctx.sqRef().getText());
            
            sets.add(new ModelSet(setName,ModelPrimitives.UNKNOWN));
            return super.visitSetDecl(ctx);
        }
        
        @Override
        public Void visitSetDefExpr(FormulationParser.SetDefExprContext ctx) {
            String setName = extractName(ctx.sqRef().getText());
            
            TypeVisitor typer = new TypeVisitor();
            typer.visit(ctx.setExpr());
            ModelSet set = new ModelSet(setName,typer.getType());
            if(loadElementsToRam){
                java.util.List<String> elements = parseSetElements(ctx.setExpr());
                set.setElements(elements);
            }
            sets.add(set);
            return super.visitSetDefExpr(ctx);
        }

        @Override
        public Void visitConstraint(FormulationParser.ConstraintContext ctx) {
            String constName = extractName(ctx.name.getText());
            constraints.add(new ModelConstraint(constName));
            return super.visitConstraint(ctx);
        }

        @Override
        public Void visitObjective(FormulationParser.ObjectiveContext ctx) {
            //String constName = extractName(ctx.name.getText());
            //preferences.add(new ModelConstraint(constName,));
            //return super.visitSetDefExpr(ctx);
            return super.visitObjective(ctx);
        }
        
        public Void visitVariable(FormulationParser.VariableContext ctx){
            String varName = extractName(ctx.sqRef().getText());
            variables.add(new ModelVariable(varName));
            return super.visitVariable(ctx);
        }

        private String extractName(String sqRef) {
            // Handle indexed sets by taking the base name
            int bracketIndex = sqRef.indexOf('[');
            return bracketIndex == -1 ? sqRef : sqRef.substring(0, bracketIndex);
        }
        
        private java.util.List<String> parseSetElements(FormulationParser.SetExprContext ctx) {
            java.util.List<String> elements = new ArrayList<>();
            // Parse set elements based on the context type
            
            if (ctx instanceof FormulationParser.SetExprStackContext) {
                FormulationParser.SetExprStackContext stackCtx = (FormulationParser.SetExprStackContext) ctx;
                if (stackCtx.setDesc() != null) {
                    // Handle explicit set descriptions
                    if (stackCtx.setDesc() instanceof FormulationParser.SetDescStackContext) {
                        FormulationParser.SetDescStackContext descCtx = (FormulationParser.SetDescStackContext) stackCtx.setDesc();
                        if (descCtx.csv() != null) {
                            String[] values = descCtx.csv().getText().split(",");
                            elements.addAll(Arrays.asList(values));
                        }
                    }
                }
            }
            return elements;
        }
    }
    
    private class ModifierVisitor extends FormulationBaseVisitor<Void> {
        private final CommonTokenStream tokens;
        private final String targetSet;
        private final String value;
        private final Action act;
        private final String originalSource;
        private boolean modified = false;
        private StringBuilder modifiedSource;
        enum Action {
            APPEND,
            DELETE,
            SET,
            COMMENT_OUT, 
            UNCOMMENT;
        }
        public ModifierVisitor(CommonTokenStream tokens, String targetSet, String value, Action act, String originalSource) {
            this.tokens = tokens;
            this.targetSet = targetSet;
            this.value = value;
            this.act = act;
            this.originalSource = originalSource;
            this.modifiedSource = new StringBuilder(originalSource);
        }
        
        @Override
        public Void visitParamDecl(FormulationParser.ParamDeclContext ctx) {
            String paramName = extractName(ctx.sqRef().getText());
            if (paramName.equals(targetSet)) {
                if(act == Action.SET)
                    modifyParamContent(ctx.expr());
                else if (act == Action.COMMENT_OUT)
                    commentOutParameter(ctx);
            }
            return super.visitParamDecl(ctx);
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
            String modifiedLine = originalLine.replaceFirst(ctx.getText(), value);
            
            if (!originalLine.equals(modifiedLine)) {
                modifiedSource.replace(startIndex, stopIndex + 1, modifiedLine);
                modified = true;
            }
        }

        @Override
        public Void visitSetDefExpr(FormulationParser.SetDefExprContext ctx) {
            String setName = extractName(ctx.sqRef().getText());
            if (setName.equals(targetSet)) {
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
            if (constraintName.equals(targetSet)) {
                if (act == Action.COMMENT_OUT)
                    commentOutConstraint(ctx);
                
            }
            return super.visitConstraint(ctx);
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
                modifiedLine = modifySetLine(originalLine, value, true);
            else if (act == Action.DELETE)
                modifiedLine = modifySetLine(originalLine, value, false);
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
        
        private void commentOutConstraint(FormulationParser.ConstraintContext ctx) {
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
        
        private void commentOutPreference(FormulationParser.ObjectiveContext ctx) {
            // To be implemented
        }

        private String modifySetLine(String line, String value, boolean isAppend) {
            // Find the set content between braces
            int openBrace = line.indexOf('{');
            int closeBrace = line.lastIndexOf('}');
            
            if (openBrace != -1 && closeBrace != -1) {
                String beforeBraces = line.substring(0, openBrace + 1);
                String afterBraces = line.substring(closeBrace);
                String content = line.substring(openBrace + 1, closeBrace).trim();
                
                if (isAppend) {
                    // Add value
                    content = content.isEmpty() ? value : content + ", " + value;
                } else {
                    // Remove value
                    content = Arrays.stream(content.split(","))
                                  .map(String::trim)
                                  .filter(s -> !s.equals(value))
                                  .collect(Collectors.joining(", "));
                }
                
                return beforeBraces + content + afterBraces;
            }
            return line;
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
    
    private class TypeVisitor extends FormulationBaseVisitor<Void> {
        ModelType type = ModelPrimitives.UNKNOWN;

        public ModelType getType(){
            return type;
        }

        public Void visitStrExprToken(FormulationParser.StrExprTokenContext ctx){
            if(type == ModelPrimitives.UNKNOWN)
                type = ModelPrimitives.TEXT;
            else if( type instanceof Tuple){
                ((Tuple)type).append(ModelPrimitives.TEXT);
            } else {
                //nothing
            }
            return super.visitStrExprToken(ctx);
        }

        public Void visitBasicExprToken(FormulationParser.BasicExprTokenContext ctx){
            ModelPrimitives tmp = ModelPrimitives.UNKNOWN;
            if(ctx.FLOAT() != null){
                tmp = ModelPrimitives.FLOAT;
            } else if(ctx.INFINITY() != null){
                tmp = ModelPrimitives.INFINITY;
            } else if(ctx.INT() != null){
                tmp = ModelPrimitives.INT;
            }
            if(type == ModelPrimitives.UNKNOWN){
                type = tmp;
            } else if ( type instanceof Tuple){
                ((Tuple) type).append(tmp);
            } else {

            }
            return super.visitBasicExprToken(ctx);
        }
        public Void visitTuple(FormulationParser.TupleContext ctx){
            if(type == ModelPrimitives.UNKNOWN){
                type = new Tuple();
            } else if (type instanceof Tuple) {

            }
            return super.visitTuple(ctx);
        }
    }

    public Set<ModelSet> getSets() {
        return Collections.unmodifiableSet(sets);
    }
    
    public Set<ModelParameter> getParameters() {
        return Collections.unmodifiableSet(params);
    }
    
    public Set<ModelConstraint> getConstraints() {
        return Collections.unmodifiableSet(constraints);
    }
    
    public Set<ModelPreference> getPreferences() {
        return Collections.unmodifiableSet(preferences);
    }
    
    public Set<ModelVariable> getVariables() {
        return Collections.unmodifiableSet(variables);
    }
    
    // Getters for individual elements by identifier
    public ModelSet getSet(String identifier) {
        return sets.stream()
            .filter(set -> set.getIdentifier().equals(identifier))
            .findFirst()
            .orElse(null);
    }
    
    public ModelParameter getParameter(String identifier) {
        return params.stream()
            .filter(param -> param.getIdentifier().equals(identifier))
            .findFirst()
            .orElse(null);
    }
    
    public ModelConstraint getConstraint(String identifier) {
        return constraints.stream()
            .filter(constraint -> constraint.getIdentifier().equals(identifier))
            .findFirst()
            .orElse(null);
    }
    
    public ModelPreference getPreference(String identifier) {
        return preferences.stream()
            .filter(preference -> preference.getIdentifier().equals(identifier))
            .findFirst()
            .orElse(null);
    }
    
    public ModelVariable getVariable(String identifier) {
        return variables.stream()
            .filter(variable -> variable.getIdentifier().equals(identifier))
            .findFirst()
            .orElse(null);
    }
    
}