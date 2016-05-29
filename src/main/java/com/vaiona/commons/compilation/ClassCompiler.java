/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package com.vaiona.commons.compilation;

import com.vaiona.commons.lang.Environment;
import com.vaiona.commons.logging.LoggerHelper;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import javax.tools.Diagnostic;
import javax.tools.DiagnosticCollector;
import javax.tools.JavaCompiler;
import javax.tools.JavaFileManager;
import javax.tools.JavaFileObject;
import javax.tools.ToolProvider;

/**
 *
 * @author standard
 */
public class ClassCompiler {
    private List<JavaFileObject> sources = new ArrayList<>();
    private JavaCompiler compiler;// = ToolProvider.getSystemJavaCompiler();
    private JavaFileManager fileManager;// = new InMemoryFileManager(compiler.getStandardFileManager(null, null, null));
    private DiagnosticCollector<JavaFileObject> diagnosticsCollector = new DiagnosticCollector<JavaFileObject>();
    
    public ClassCompiler(ClassLoader parent) throws Exception{
        Environment.setJDK();
        
        LoggerHelper.logDebug(MessageFormat.format("Checkpoint {0}: ClassCompiler.ctor. The compiler is instantiating...", 1));
        try{
            compiler = ToolProvider.getSystemJavaCompiler();
            if(compiler != null){
                fileManager = new InMemoryFileManager(compiler.getStandardFileManager(diagnosticsCollector, null, null), parent);
                LoggerHelper.logDebug(MessageFormat.format("Checkpoint {0}: ClassCompiler.ctor. The compiler is instantiated", 2));
            }else{
                String message = MessageFormat.format("Not able to get the Java Compiler (using: ToolProvider.getSystemJavaCompiler())!", 2);
                LoggerHelper.logError(message);                
                throw new Exception(message);
            }            
        } catch (Exception ex){
            String message = MessageFormat.format("Not able to get the Java Compiler. Cause: {0}", ex.getMessage());
            LoggerHelper.logError(message);            
            throw new Exception(message);
        }
    }
    
    public ClassCompiler addSource(String className, String body){
        sources.add(new InMemorySourceFile(className, body));
        return this;
    }
    
    public ClassCompiler addSource(InMemorySourceFile source){
        sources.add(source);
        return this;
    }
    // think of having the compiler, file manager or the whole class as static to save some 
    // compiler/ file, etc loading time. needs profiling
    public JavaFileManager compile(List<String> classes){
        //((InMemoryFileManager)fileManager).reset();
        // check whether it is Java 8, as some of its features are used in the sources
        LoggerHelper.logDebug(MessageFormat.format("Compiling the {0} source files is started.", sources.size()));
        final Collection<String> compilerOptions=new ArrayList<>();
        compilerOptions.add("-Xlint:unchecked");
        Boolean compiled = compiler
                //.getTask(null, fileManager, null, compilerOptions, classes, sources)
        		.getTask(null, fileManager, diagnosticsCollector, null, classes, sources)
        		.call();
        if(compiled){
            LoggerHelper.logDebug(MessageFormat.format("Compiling the {0} source files was successfully done.", sources.size()));           
        }else{
            LoggerHelper.logError(MessageFormat.format("Compiling the {0} source files failed.", sources.size()));
            List<Diagnostic<? extends JavaFileObject>> diagnostics = diagnosticsCollector.getDiagnostics();
            for (Diagnostic<? extends JavaFileObject> diagnostic : diagnostics) {
                // read error details from the diagnostic object
            	LoggerHelper.logError(diagnostic.getMessage(null));
            }
        }
        return fileManager;
    }

}
