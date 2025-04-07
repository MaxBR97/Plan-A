package Model;

import Exceptions.InternalErrors.BadRequestException;
import Exceptions.UserErrors.ZimplCompileError;

import org.antlr.v4.runtime.*;
import org.antlr.v4.runtime.tree.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

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
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.security.Policy;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import org.antlr.runtime.tree.TreeWizard;
import org.springframework.web.bind.annotation.RestController;

import com.google.protobuf.Type;

import DataAccess.ModelRepository;

public class Model extends ModelInterface {
    private final String id;
    ParseTree tree;
    private CommonTokenStream tokens;
    private final Map<String,ModelSet> sets = new HashMap<>();
    private final Map<String,ModelParameter> params = new HashMap<>();
    private final Map<String,ModelFunction> funcs = new HashMap<>();
    private final Map<String,ModelConstraint> constraints = new HashMap<>();
    private final Map<String,ModelPreference> preferences = new HashMap<>();
    private final Map<String,ModelVariable> variables = new HashMap<>();
    private final Set<String> toggledOffFunctionalities = new HashSet<>();
    private boolean loadElementsToRam = true;
    private final String zimplCompilationScript = "src/main/resources/zimpl/checkCompilation.sh";
    private final String zimplSolveScript = "src/main/resources/zimpl/solve.sh" ;
    private String originalSource;

    private ModelRepository modelRepository;
    
    
    public Model(ModelRepository repo, String id) throws Exception {
        modelRepository = repo;
        this.id = id;
        parseSource();
    }


    public InputStream getSource() throws Exception{
        InputStream inputStream = modelRepository.downloadDocument(id);
        return inputStream;
    }

    public String getSourcePathToFile() throws Exception {
        return modelRepository.getLocalStoreDir().resolve(id+".zpl").toString();
    }

    public String getSolutionPathToFile(String suffix) throws Exception {
        return modelRepository.getLocalStoreDir().resolve(id+suffix+".zpl").toString();
    }

    public void writeToSource(String newSource) throws Exception{
        InputStream inputStream = new ByteArrayInputStream(newSource.getBytes(StandardCharsets.UTF_8));
        modelRepository.uploadDocument(id, inputStream);
        modelRepository.downloadDocument(id);
    }

    public void writeSolution(String content, String suffix) throws Exception {
        InputStream inputStream = new ByteArrayInputStream(content.getBytes(StandardCharsets.UTF_8));
        modelRepository.uploadDocument(id + suffix, inputStream);
        modelRepository.downloadDocument(id + suffix);
    }
    
    private void parseSource() throws Exception {
        originalSource = new String(getSource().readAllBytes());
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
            throw new BadRequestException("set "+set.getIdentifier()+" is incompatible with given input: "+value+" , expected type: "+set.getType());
        
        ModifierVisitor modifier = new ModifierVisitor(tokens, set.getIdentifier(), value, ModifierVisitor.Action.APPEND, originalSource);
        modifier.visit(tree);
        
        if (modifier.isModified()) {
            // Write modified source back to file, preserving original formatting
            String modifiedSource = modifier.getModifiedSource();
            writeToSource(modifiedSource);
            parseSource();
        }
    }
    
    public void removeFromSet(ModelSet set, String value) throws Exception {
        // if (!sets.containsKey(setName)) {
        //     throw new IllegalArgumentException("Set " + setName + " not found");
        // }
        if(!set.isCompatible(value))
            throw new BadRequestException("set "+set.getIdentifier()+" is incompatible with given input: "+value+" , expected type: "+set.getType());

        ModifierVisitor modifier = new ModifierVisitor(tokens, set.getIdentifier(), value,  ModifierVisitor.Action.DELETE, originalSource);
        modifier.visit(tree);
        
        if (modifier.isModified()) {
            // Write modified source back to file, preserving original formatting
            String modifiedSource = modifier.getModifiedSource();
            writeToSource(modifiedSource);
            parseSource();
        }
    }

    public void setInput(ModelParameter identifier, String value) throws Exception {

        if(!identifier.isCompatible(value))
            throw new BadRequestException("parameter "+identifier.getIdentifier()+" is incompatible with given input: "+value +" expected type: "+identifier.getType());
        
        ModifierVisitor modifier = new ModifierVisitor(tokens, identifier.getIdentifier(), value,  ModifierVisitor.Action.SET, originalSource);
        modifier.visit(tree);
        
        if (modifier.isModified()) {
            // Write modified source back to file, preserving original formatting
            String modifiedSource = modifier.getModifiedSource();
            writeToSource(modifiedSource);
            parseSource();
        }
    }

    public void setInput(ModelSet identifier, String[] values) throws Exception {

        for(String str : values){
            if(!identifier.isCompatible(str))
                throw new BadRequestException("set "+identifier.getIdentifier()+" is incompatible with given input: "+str+" , expected type: "+identifier.getType());

        }
        
        ModifierVisitor modifier = new ModifierVisitor(tokens, identifier.getIdentifier(), values,  ModifierVisitor.Action.SET, originalSource);
        modifier.visit(tree);
        
        if (modifier.isModified()) {
            // Write modified source back to file, preserving original formatting
            String modifiedSource = modifier.getModifiedSource();
            writeToSource(modifiedSource);
            parseSource();
        }
    }
    
    //TODO: the design is fucked up, and it's apparent in getInput methods. I need to make a better design of things.
    @Override
    public String[] getInput(ModelParameter parameter) throws Exception {
        if(parameter == null)
            throw new BadRequestException("Trying to get input of a null parameter!");
        if(params.get(parameter.getIdentifier()) == null)
            throw new BadRequestException("parameter " + parameter.getIdentifier() + " doesnt exist ");
        if(params.get(parameter.getIdentifier()).hasValue() == false)
            throw new BadRequestException("parameter " + parameter.getIdentifier() + " is not set to have an input, or is not a declarative parameter");
        parameter = params.get(parameter.getIdentifier());

        return ModelType.convertStringToAtoms(parameter.getValue());
        
    }

    @Override
    public List<String[]> getInput(ModelSet set) throws Exception {
        if(set == null || set.getIdentifier() == null)
            throw new BadRequestException("Trying to get input of a null set!");
        if(sets.get(set.getIdentifier()) == null)
            throw new BadRequestException("set " + set.getIdentifier() + " doesnt exist ");
        if(sets.get(set.getIdentifier()).getElements() == null)
            throw new BadRequestException("set " + set.getIdentifier() + " is not set to have an input, or is not declarative set");
        
        set = sets.get(set.getIdentifier());
        List<String[]> ans = new LinkedList<>();
        for(String element :set.getElements() ){
            ans.add(ModelType.convertStringToAtoms(element));
        }
        return ans;
        
    }

    public void toggleFunctionality(ModelFunctionality mf, boolean turnOn) {
        if (!turnOn) {
            toggledOffFunctionalities.add(mf.getIdentifier());
        } else {
            toggledOffFunctionalities.remove(mf.getIdentifier());
        }
    }

    void commentOutToggledFunctionalities() throws Exception {
        if (toggledOffFunctionalities.isEmpty()) {
            return;
        }

        ModifierVisitor modifier = new ModifierVisitor(tokens, null, "", ModifierVisitor.Action.COMMENT_OUT, originalSource);
        modifier.setTargetFunctionalities(toggledOffFunctionalities); // Set functionalities to be commented out
        modifier.visit(tree);
        modifier.commentOutFunctionalities();
        
        if (modifier.isModified()) {
            String modifiedSource = modifier.getModifiedSource();
            writeToSource(modifiedSource);
        //    parseSource();
        }
    }

    void restoreToggledFunctionalities() throws Exception {
        if (toggledOffFunctionalities.isEmpty()) {
            return;
        }

        // Read the original file content and restore it
        writeToSource(originalSource);
        parseSource();
    }
    

    public boolean isCompiling(float timeout) throws BadRequestException {
        boolean ans = false;
        try {
            commentOutToggledFunctionalities();
    
            ProcessBuilder processBuilder = new ProcessBuilder(
                "scip", "-c", "read " + getSourcePathToFile() + " q"
            );
           
            processBuilder.redirectErrorStream(true);
            
            Process process;
            try {
                process = processBuilder.start();
            } catch (IOException e) {
                // Check if it's specifically a command not found error
                if (e.getMessage().contains("Cannot run program \"scip\"") || 
                    e.getMessage().contains("error=2")) {
                    throw new BadRequestException("SCIP solver not found. Please ensure SCIP is installed and available in system PATH");
                }
                throw e; // Rethrow other IOExceptions
            }
    
            // Executor service to handle timeouts
            ExecutorService executor = Executors.newSingleThreadExecutor();
            Future<Boolean> future = executor.submit(() -> {
                BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
                StringBuilder output = new StringBuilder();
                String line;
                while ((line = reader.readLine()) != null) {
                    output.append(line).append("\n");
                    
                    if (line.contains("original problem has")) {
                        return true;
                    } else if (line.contains("error reading file")) {
                        return false;
                    }
                }

                throw new BadRequestException("Error checking compilation: "+ output.toString());
            });
    
            try {
                ans = future.get((long) timeout, TimeUnit.SECONDS);
            } catch (TimeoutException e) {
                process.destroy(); // Kill the process if timeout occurs
                ans = false;
            } finally {
                executor.shutdown();
            }
    
            restoreToggledFunctionalities();
            return ans;
        } catch (Exception e) {
            try {
                restoreToggledFunctionalities();
            } catch (Exception ex) {
                ex.printStackTrace();
            }
            
            // If it's our SCIP not found exception, rethrow it
            if (e instanceof BadRequestException) {
                throw (BadRequestException) e;
            }
            
            e.printStackTrace();
            return false;
        }
    }
    //TODO: remove solutionFileSufix, it should be dedcided internally in Model class
    public Solution solve(float timeout, String solutionFileSufix) throws BadRequestException {
        if(solutionFileSufix == null)
            throw new BadRequestException("solutionFileSufix is null");
        Solution ans = null;
        try {
            commentOutToggledFunctionalities();
    
            ProcessBuilder processBuilder = new ProcessBuilder(
                "scip", "-c", 
                " set limits time "+ timeout + 
                " set timing reading TRUE" +
                " set presolving advanced abortfac 0.005" +
                " read " + getSourcePathToFile() + 
                " optimize"+
                " display solution q"
            );
            processBuilder.redirectErrorStream(true);
            processBuilder.redirectInput(ProcessBuilder.Redirect.PIPE); // Prevents waiting for input

            
            Process process;
            try {
                process = processBuilder.start();
            } catch (IOException e) {
                // Check if it's specifically a command not found error
                if (e.getMessage().contains("Cannot run program \"scip\"") || 
                    e.getMessage().contains("error=2")) {
                    throw new BadRequestException("SCIP solver not found. Please ensure SCIP is installed and available in system PATH");
                }
                throw e; // Rethrow other IOExceptions
            }
    
            // Executor service to handle timeouts
            ExecutorService executor = Executors.newSingleThreadExecutor();
            Future<Solution> future = executor.submit(() -> {
                BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
                StringBuilder buffer = new StringBuilder();
                boolean capture = false;
                String line;
                while ((line = reader.readLine()) != null) {
                    if (line.startsWith("@@")) continue;  // Ignore certain lines
                    if (line.trim().isEmpty() && (buffer.length() == 0 || buffer.toString().endsWith("\n"))) {
                        continue;
                    }
                    if (line.matches("file .* not found"))
                        throw new BadRequestException("Error: Tried solving non-existing file: " + line);
                    if (line.matches(".*Error [0-9]{1,4}:.*")) {
                        while ((line = reader.readLine()) != null) {} // Consume remaining error messages
                        throw new BadRequestException("Error: Solving unsuccessful: " + line);
                    }
                    if (line.contains("SCIP Status")) {
                        buffer.setLength(0);
                        capture = true;
                    }
                    if (capture) 
                        buffer.append(line).append("\n");
                }
            
                String filteredOutput = buffer.toString().lines()
                    .collect(Collectors.joining("\n"));
                writeSolution(filteredOutput, solutionFileSufix);
                return new Solution(getSolutionPathToFile(solutionFileSufix));
            });
            
            try {
                ans = future.get((long) timeout + 2, TimeUnit.SECONDS);
            } catch (TimeoutException e) {
                process.destroy(); 
                ans = null;
            } finally {
                executor.shutdown();
            }
    
            restoreToggledFunctionalities();
            if(ans == null)
                return new Solution();
            return ans;
        } catch (Exception e) {
            try {
                restoreToggledFunctionalities();
            } catch (Exception ex) {
                ex.printStackTrace();
            }
            
            // If it's our SCIP not found exception, rethrow it
            if (e instanceof BadRequestException) {
                throw (BadRequestException) e;
            }
            
            e.printStackTrace();
            if(ans == null)
                return new Solution();
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

        public CollectorVisitor(){

        }

        public Void visitParamDecl(FormulationParser.ParamDeclContext ctx){
            String paramName = extractName(ctx.sqRef().getText());
            TypeVisitor typer = new TypeVisitor();
            typer.visit(ctx.expr());
            ModelParameter param = new ModelParameter(id,paramName,typer.getType(), typer.getBasicSets(),typer.getBasicParams(),typer.getBasicFuncs());
            if(loadElementsToRam){
                param.setValue(ctx.expr().getText());
            }
            params.put(paramName,param);
            return super.visitParamDecl(ctx);
        }

        @Override
        public Void visitSetDecl(FormulationParser.SetDeclContext ctx) {
            String setName = extractName(ctx.sqRef().getText());
            
            sets.put(setName,new ModelSet(id,setName,ModelPrimitives.UNKNOWN));
            return super.visitSetDecl(ctx);
        }

        @Override
        public Void visitFunction(FormulationParser.FunctionContext ctx) {
            if(ctx.fnRef() != null && ctx.fnRef() instanceof FormulationParser.CustomFuncContext && ((FormulationParser.CustomFuncContext)ctx.fnRef()) != null) {
                String functionName = ((FormulationParser.CustomFuncContext)ctx.fnRef()).name.getText();
                ModelPrimitives type = ctx.FN_TYPE().getText().equals("defnumb") ? ModelPrimitives.FLOAT :
                                        ctx.FN_TYPE().getText().equals("defbool") ? ModelPrimitives.BINARY :
                                        ctx.FN_TYPE().getText().equals("defstrg") ? ModelPrimitives.TEXT :
                                        ModelPrimitives.UNKNOWN;
                TypeVisitor depVisitor = new TypeVisitor();
                depVisitor.visit(ctx.expr());                                         
                funcs.put(functionName,new ModelFunction(id,functionName,type,depVisitor.getBasicSets(), depVisitor.getBasicParams(), depVisitor.getBasicFuncs()));
            }

            return super.visitFunction(ctx);
        }
        
        @Override
        public Void visitSetDefExpr(FormulationParser.SetDefExprContext ctx) {
            String setName = extractName(ctx.sqRef().getText());
            // if(setName.equals("FormalTimes")) // for debugging
            //     System.out.println(ctx.setExpr().getText());
            TypeVisitor typer = new TypeVisitor();
            typer.visit(ctx.setExpr());
            ModelSet set = new ModelSet(id,setName,typer.getType(),typer.getBasicSets(),typer.getBasicParams(),typer.getBasicFuncs());
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
            constraints.put(constName, new ModelConstraint(id, constName,visitor.getBasicSets(),visitor.getBasicParams(),visitor.getBasicFuncs()));
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
                    new ModelPreference(id,expressionComponent.getText(), 
                                        visitor.getBasicSets(), 
                                        visitor.getBasicParams(),
                                        visitor.getBasicFuncs())
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
            variables.put(varName, new ModelVariable(id,varName, visitor.getBasicSets(), visitor.getBasicParams(),visitor.getBasicFuncs(),visitor.type,isComplex));
            return super.visitVariable(ctx);
        }

        private String extractName(String sqRef) {
            // Handle indexed sets by taking the base name
            int bracketIndex = sqRef.indexOf('[');
            return bracketIndex == -1 ? sqRef : sqRef.substring(0, bracketIndex);
        }
        
        private java.util.List<String> parseSetElements(FormulationParser.SetExprContext ctx) {
            java.util.List<String> elements = new ArrayList<>();
        String name = ctx.getText();
            if (ctx instanceof FormulationParser.SetExprStackContext) {
                FormulationParser.SetExprStackContext stackCtx = (FormulationParser.SetExprStackContext) ctx;
        
                if (stackCtx.setDesc() instanceof FormulationParser.SetDescStackContext) {
                    FormulationParser.SetDescStackContext descCtx = (FormulationParser.SetDescStackContext) stackCtx.setDesc();
        
                    // --- Handle range: {0 .. 24 by 2} ---
                    if (descCtx.range() != null) {
                        FormulationParser.RangeContext rangeCtx = descCtx.range();
        
                        try {
                            // Only allow if both ends and step are integer literals
                            int start = Integer.parseInt(rangeCtx.lhs.getText());
                            int end = Integer.parseInt(rangeCtx.rhs.getText());
                            int step = (rangeCtx.step != null) ? Integer.parseInt(rangeCtx.step.getText()) : 1;
        
                            for (int i = start; i <= end; i += step) {
                                elements.add(String.valueOf(i));
                            }
                        } catch (NumberFormatException e) {
                            // One of the parts isn't a literal number (e.g., NumberOfSoldiers)
                            return null;
                        }
        
                    } 
                    // --- Handle CSV: {1, 2}, {"a"}, {<1,"b",3>} ---
                    else if (descCtx.csv() != null) {
                        String csvText = descCtx.csv().getText();
        
                        StringBuilder currentElement = new StringBuilder();
                        boolean inAngleBrackets = false;
        
                        for (int i = 0; i < csvText.length(); i++) {
                            char c = csvText.charAt(i);
        
                            if (c == '<') inAngleBrackets = true;
                            if (c == '>') inAngleBrackets = false;
        
                            if (c == ',' && !inAngleBrackets) {
                                elements.add(currentElement.toString().trim());
                                currentElement.setLength(0);
                            } else {
                                currentElement.append(c);
                            }
                        }
        
                        if (currentElement.length() > 0) {
                            elements.add(currentElement.toString().trim());
                        }
        
                        // Reject if any element is not a literal (contains identifiers)
                        for (String el : elements) {
                            // Very basic check: allow if it's a number, string, or tuple of literals
                            if (!el.matches("\\d+") &&                       // int
                                !el.matches("\"[^\"]*\"") &&                // string
                                !el.matches("<.*>")) {                      // assume tuples are valid
                                return null;
                            }
        
                            // Optional: be stricter on tuples if needed
                        }
        
                    } else {
                        return null; // not a valid literal set
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
        private List<FormulationParser.ConstraintContext> toCommentOut;

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
            toCommentOut = new LinkedList<>();
        }

        public ModifierVisitor(CommonTokenStream tokens, String targetIdentifier, String[] values, Action act, String originalSource) {
            this.tokens = tokens;
            this.targetIdentifier = targetIdentifier;
            this.targetValues = values;
            this.act = act;
            this.originalSource = originalSource;
            this.modifiedSource = new StringBuilder(originalSource);
            toCommentOut = new LinkedList<>();
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
                    toCommentOut.add(ctx);
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

        private void commentOutConstraints(List<FormulationParser.ConstraintContext> constraints) {
            List<int[]> ranges = new ArrayList<>();
            
            // Collect and compute correct ranges for replacement
            for (FormulationParser.ConstraintContext ctx : constraints) {
                int startIndex = ctx.start.getStartIndex();
                int stopIndex = ctx.stop.getStopIndex();
        
                // Move stopIndex forward until we find ';', skipping whitespace and newlines
                while (stopIndex + 1 < originalSource.length()) {
                    char nextChar = originalSource.charAt(stopIndex + 1);
                    if (nextChar == ';') {
                        stopIndex++;  // Include the semicolon
                        break;
                    } else if (!Character.isWhitespace(nextChar)) {
                        break;  // Stop if we hit another non-whitespace, non-';' character
                    }
                    stopIndex++;
                }
        
                ranges.add(new int[]{startIndex, stopIndex});
            }
        
            // Sort ranges in **reverse order** (highest index first)
            ranges.sort((a, b) -> Integer.compare(b[0], a[0]));
        
            // Apply replacements in reverse order
            for (int[] range : ranges) {
                int startIndex = range[0];
                int stopIndex = range[1];
        
                // Extract full constraint text
                String fullStatement = originalSource.substring(startIndex, stopIndex + 1);
        
                // Split into lines while preserving line breaks
                String[] lines = fullStatement.split("(?<=\n)");
                StringBuilder commentedOut = new StringBuilder();
        
                // Comment out each line while keeping indentation
                for (String line : lines) {
                    commentedOut.append("# ").append(line);
                }
        
                // Replace in modifiedSource
                modifiedSource.replace(startIndex, stopIndex + 1, commentedOut.toString());
            }
        
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

        public void commentOutFunctionalities(){
            commentOutConstraints(toCommentOut);
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

    public class TypeVisitor extends FormulationBaseVisitor<Void> {
        private ModelType type = ModelPrimitives.UNKNOWN;
        private  List<ModelSet> basicSets ;
        private  List<ModelParameter> basicParams;
        private  List<ModelFunction> basicFuncs;

        public TypeVisitor(){
            basicSets = new LinkedList<>();
            basicParams = new LinkedList<>();
            basicFuncs = new LinkedList<>();
        }

        private void appendType(ModelType add) {
            if(type == ModelPrimitives.UNKNOWN) {
                if(add instanceof ModelPrimitives)
                    type = add;
                else {
                    type = new Tuple();
                    ((Tuple)type).append((Tuple)add);
                }
            }
            else if (type instanceof Tuple){
                ((Tuple)type).append(add);
            }
            else {
                type = new Tuple (new ModelPrimitives[]{(ModelPrimitives)type});
                ((Tuple)type).append(add);
            }
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
                basicFuncs.addAll(leftVisitor.getBasicFuncs());
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
                basicFuncs.addAll(rightVisitor.getBasicFuncs());
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
            } else if (ctx.op.getText().equals("union")) {
                type = ModelPrimitives.UNKNOWN;
                if(leftSet != null)
                    this.appendType(leftSet.getType());
                else
                    this.appendType(leftVisitor.getType());
            } else  {
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
                basicFuncs.addAll(v.basicFuncs);
            }
            TypeVisitor v = new TypeVisitor();
            v.visit(ctx.boolExpr());
            basicParams.addAll(v.basicParams);
            basicSets.addAll(v.basicSets);
            basicFuncs.addAll(v.basicFuncs);

            return null;
        }

        @Override
        public Void visitComparisonNExpr(FormulationParser.ComparisonNExprContext ctx){
            this.visit(ctx.nExpr());
            for(FormulationParser.BoundContext bndCtx : ctx.bound())
                this.visit(bndCtx);
            return null;
        }

        @Override
        public Void visitNExpr(FormulationParser.NExprContext ctx){
            this.visit(ctx.uExpr());
            return null;
        }

        @Override
        public Void visitUExpr(FormulationParser.UExprContext ctx){
            if(ctx.op != null){
                TypeVisitor t1 = new TypeVisitor();
                TypeVisitor t2 = new TypeVisitor();
                t1.visit(ctx.uExpr(0));
                t2.visit(ctx.uExpr(1));
                basicSets.addAll(t1.basicSets);
                basicSets.addAll(t2.basicSets);
                basicParams.addAll(t1.basicParams);
                basicParams.addAll(t2.basicParams);
                basicFuncs.addAll(t1.basicFuncs);
                basicFuncs.addAll(t2.basicFuncs);
                this.type = t1.getType();
            } else if (ctx.basicExpr() != null){
                TypeVisitor t = new TypeVisitor();
                t.visit(ctx.basicExpr());
                basicSets.addAll(t.basicSets);
                basicParams.addAll(t.basicParams);
                basicFuncs.addAll(t.basicFuncs);
                this.type = t.getType();
            }
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

        // else if(ctx.ID().getText() != null && getFunction(ctx.ID().getText()) != null){
        //     ModelFunction func = getFunction(ctx.ID().getText());
        //     basicFuncs.add(func);
        //     appendType(func.getType());
        // }
        @Override
        public Void visitCustomFunc(FormulationParser.CustomFuncContext ctx){
            TypeVisitor newVisitor = new TypeVisitor();
            if(ctx.csv() != null)
                newVisitor.visit(ctx.csv());
            else if (ctx.index() != null)
                newVisitor.visit(ctx.index());
            basicSets.addAll(newVisitor.getBasicSets());
            basicParams.addAll(newVisitor.getBasicParams());
            basicFuncs.addAll(newVisitor.getBasicFuncs());
            ModelFunction func = getFunction(ctx.ID().getText());
            basicFuncs.add(func);
            appendType(func.getType());
            return null;
        }

        @Override
        public Void visitBasicExprStack(FormulationParser.BasicExprStackContext ctx){
            if(ctx.fnRef() != null)
                this.visit(ctx.fnRef());
            else if(ctx.redExpr() != null)
                this.visit(ctx.redExpr());
            else if(ctx.sqRef() != null)
                this.visit(ctx.sqRef());
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
            TypeVisitor visitor = new TypeVisitor();
            if(ctx.index() != null)
                visitor.visit(ctx.index());
            else if(ctx.csv() != null)
                visitor.visit(ctx.csv());
            if(ctx.op.getText().equals("max") || ctx.op.getText().equals("min")){
                appendType(visitor.getType());
                basicSets.addAll(visitor.getBasicSets());
                basicParams.addAll(visitor.getBasicParams());
                basicFuncs.addAll(visitor.getBasicFuncs());
            } else if(ctx.op.getText().equals("card")){
                appendType(ModelPrimitives.INT);
                basicSets.addAll(visitor.getBasicSets());
                basicParams.addAll(visitor.getBasicParams());
                basicFuncs.addAll(visitor.getBasicFuncs());
            } else if(ctx.op.getText().equals("floor")){
                appendType(ModelPrimitives.INT);
                basicSets.addAll(visitor.getBasicSets());
                basicParams.addAll(visitor.getBasicParams());
                basicFuncs.addAll(visitor.getBasicFuncs());
            } else if(ctx.op.getText().equals("random")){
                appendType(ModelPrimitives.FLOAT);
                basicSets.addAll(visitor.getBasicSets());
                basicParams.addAll(visitor.getBasicParams());
                basicFuncs.addAll(visitor.getBasicFuncs());
            } 
            return null;
        }

        @Override
        public Void visitOrdRedExpr(FormulationParser.OrdRedExprContext ctx){
            TypeVisitor visitor = new TypeVisitor();
            visitor.visit(ctx.setExpr());            
            appendType(ModelPrimitives.valueOf(visitor.getType().typeList().get(Integer.parseInt(ctx.comp.getText()) - 1)));
            basicSets.addAll(visitor.getBasicSets());
            basicParams.addAll(visitor.getBasicParams());
            basicFuncs.addAll(visitor.getBasicFuncs());

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
                ModelSet s = new ModelSet(id,"anonymous_set", elementVisitor.type,elementVisitor.basicSets,elementVisitor.basicParams,elementVisitor.basicFuncs);
                basicSets.add(s);
                type = elementVisitor.getType();
            }
            else if (ctx.csv() != null) {
                // Handle explicit set elements
                TypeVisitor elementVisitor = new TypeVisitor();
                elementVisitor.visit(ctx.csv().expr(0));
                ModelSet s = new ModelSet(id,"anonymous_set", elementVisitor.type,elementVisitor.basicSets,elementVisitor.basicParams,elementVisitor.basicFuncs);
                // Add this as a basic set since it's explicitly defined
                basicSets.add(s);
                type = elementVisitor.getType();
            } else if (ctx.range() != null) {
                ModelSet s = new ModelSet(id,"anonymous_set", ModelPrimitives.INT);
                basicSets.add(s);
                type = ModelPrimitives.INT;
                TypeVisitor visitor = new TypeVisitor();
                visitor.visit(ctx.range());
                s.paramDependencies.addAll(visitor.getBasicParams());
                s.setDependencies.addAll(visitor.getBasicSets());
                s.functionDependencies.addAll(visitor.getBasicFuncs());
            } 
            return null;
        }

        @Override
        public Void visitSetDescExtended(FormulationParser.SetDescExtendedContext ctx) {
            TypeVisitor elementVisitor = new TypeVisitor();
            elementVisitor.visit(ctx.condition());
            HashMap<String,ModelParameter> placeHolder = new HashMap<>(); 
            HashMap<String,ModelPrimitives> tupleCompToType = new HashMap<>();
            FormulationParser.ConditionContext cond = ((FormulationParser.ConditionContext)ctx.condition());
            String strCond = cond.getText();
            FormulationParser.TupleContext tup = ((FormulationParser.TupleContext)(cond.tuple()));
            String strTup = tup.getText();
            FormulationParser.CsvContext csv = ((FormulationParser.CsvContext)(tup.csv()));
            String strCsv = csv.getText();
            int i = 0;
            for( ExprContext expr : csv.expr()){
                ModelPrimitives exprType = ModelPrimitives.valueOf(elementVisitor.getType().typeList().get(i));
                tupleCompToType.put(expr.getText(),exprType);
                placeHolder.put(expr.getText(),getParameter(expr.getText()));
                params.put(expr.getText(), new ModelParameter("no image!",expr.getText(),exprType));
                i++;
            }

            FormulationParser.TupleContext tup2 = ((FormulationParser.TupleContext)(ctx.tuple()));
            String strTup2 = tup2.getText();
            FormulationParser.CsvContext csv2 = ((FormulationParser.CsvContext)(tup2.csv()));
            String strCsv2 = csv2.getText();
            i = 0;
            List<ModelSet> newSets = new LinkedList<>();
            List<ModelParameter> newParams = new LinkedList<>();
            List<ModelFunction> newFuncs = new LinkedList<>();
            newParams.addAll(elementVisitor.getBasicParams());
            newSets.addAll(elementVisitor.getBasicSets());
            newFuncs.addAll(elementVisitor.getBasicFuncs());
            for( ExprContext expr : csv2.expr()){
                TypeVisitor exprVisitor = new TypeVisitor();
                exprVisitor.visit(expr);
                appendType(exprVisitor.getType());
                newParams.addAll(exprVisitor.getBasicParams());
                newSets.addAll(exprVisitor.getBasicSets());
                newFuncs.addAll(exprVisitor.getBasicFuncs());
                i++;
            }

            for ( Map.Entry<String,ModelParameter> entry : placeHolder.entrySet()) {
                if (entry.getValue() != null)
                    params.put(entry.getKey(),entry.getValue());
                else{
                    params.remove(entry.getKey());
                    newParams.removeIf(param -> param.getIdentifier().equals(entry.getKey()));
                }
            }

            newSets = new ArrayList<>(new LinkedHashSet<>(newSets));
            newParams = new ArrayList<>(new LinkedHashSet<>(newParams));
            newFuncs = new ArrayList<>(new LinkedHashSet<>(newFuncs));

            ModelSet s = new ModelSet(id,"anonymous_set", type,newSets,newParams,newFuncs);
            basicSets.add(s);
            
            return null;
        }

        @Override
        public Void visitRange(FormulationParser.RangeContext ctx){
            TypeVisitor visitLeft = new TypeVisitor();
            TypeVisitor visitRight = new TypeVisitor();
            
            visitLeft.visit(ctx.lhs);
            visitRight.visit(ctx.rhs);
            if(ctx.step != null) {
                TypeVisitor visitStep = new TypeVisitor();
                visitStep.visit(ctx.step);
                basicParams.addAll(visitStep.getBasicParams());
                basicSets.addAll(visitStep.getBasicSets());
                basicFuncs.addAll(visitStep.getBasicFuncs());
            }
            
            // if(params.get(ctx.lhs.getText()) != null){
            //     basicParams.add(params.get(ctx.lhs.getText()));
            // }
            // if(params.get(ctx.rhs.getText()) != null){
            //     basicParams.add(params.get(ctx.rhs.getText()));
            // }
            // if(ctx.step != null && params.get(ctx.step.getText()) != null){
            //     basicParams.add(params.get(ctx.step.getText()));
            // }
            basicParams.addAll(visitLeft.getBasicParams());
            basicParams.addAll(visitRight.getBasicParams());
            
            basicSets.addAll(visitLeft.getBasicSets());
            basicSets.addAll(visitRight.getBasicSets());
            
            basicFuncs.addAll(visitLeft.getBasicFuncs());
            basicFuncs.addAll(visitRight.getBasicFuncs());
            
            type = ModelPrimitives.INT;
            return null;
        }

        @Override
        public Void visitCsv(FormulationParser.CsvContext ctx){
            for(ExprContext subCtx : ctx.expr()){
                visit(subCtx);
            }
            return null;
        }

        @Override
        public Void visitExpr(FormulationParser.ExprContext ctx){
            if(ctx.setExpr() != null)
                visit(ctx.setExpr());
            else if(ctx.nExpr() != null)
                visit(ctx.nExpr());
            else if(ctx.strExpr() != null)
                visit(ctx.strExpr());
            else if(ctx.boolExpr() != null)
                visit(ctx.boolExpr());
            else if(ctx.tuple() != null)
                visit(ctx.tuple());
            return null;
        }

        

        @Override
        public Void visitSqRefCsv(FormulationParser.SqRefCsvContext ctx){

            if(ctx.ID().getText() != null && getSet(ctx.ID().getText()) != null){
                ModelSet set = getSet(ctx.ID().getText());
                basicSets.add(set);
                appendType(set.getType());
            } else if(ctx.ID().getText() != null && getParameter(ctx.ID().getText()) != null){
                ModelParameter param = getParameter(ctx.ID().getText());
                basicParams.add(param);
                appendType(param.getType());
            } 

            if(ctx.csv() != null && getSet(ctx.csv().getText()) != null){
                ModelSet set = getSet(ctx.csv().getText());
                basicSets.add(set);
                appendType(set.getType());
            }
            else if(ctx.csv() != null && getParameter(ctx.csv().getText()) != null){
                ModelParameter param = getParameter(ctx.csv().getText());
                basicParams.add(param);
                appendType(param.getType());
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
                    totalStructure[count] = new StructureBlock(s, sb == null && s.getIdentifier().equals("anonymous_set") ? i : sb.position);
                    count++;
                    i++;
                }   
            }

            StructureBlock[] resultingStructure = new StructureBlock[pointersToSetComp.size()];
            count = 0;
            for(Integer p : pointersToSetComp){
                resultingStructure[count++] = totalStructure[p-1];
            }
            ModelSet newSet = new ModelSet(id,"anonymous_set",visitor.getBasicSets(),visitor.getBasicParams(),visitor.getBasicFuncs(),resultingStructure);
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
            String text = ctx.setExpr().getText();
            this.visit(ctx.setExpr());
            if(ctx.boolExpr() != null){
                TypeVisitor visitor = new TypeVisitor();
                visitor.visit(ctx.boolExpr());
                basicSets.addAll(visitor.getBasicSets());
                basicParams.addAll(visitor.getBasicParams());
                basicFuncs.addAll(visitor.getBasicFuncs());
            }
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
        public Void visitBoolExprFunc(FormulationParser.BoolExprFuncContext ctx){
            this.visit(ctx.fnRef());
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
        public List<ModelFunction> getBasicFuncs() {
            return basicFuncs;
        }
    }
    
    
    public ModelSet getSet(String identifier) {
        return sets.get(identifier);
    }
    
    public ModelParameter getParameter(String identifier) {
        return params.get(identifier);
    }

    public ModelFunction getFunction(String identifier) {
        return funcs.get(identifier);
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
    public Collection<ModelFunction> getFunctions(){
        return this.funcs.values();
    }

    @Override
    public Collection<ModelVariable> getVariables(Collection<String> identifiers){
        HashSet<ModelVariable> set = new HashSet<>();
        for (String identifier : identifiers) {
            set.add(getVariable(identifier));
        }
        return set;
    }

    @Override
    public ModelComponent getComponent(String mc){
        return getSet(mc) != null ? getSet(mc) :
        getParameter(mc) != null ? getParameter(mc) :
        getVariable(mc) != null ? getVariable(mc) :
        getConstraint(mc) != null ? getConstraint(mc) :
        getPreference(mc);

    }

}
