/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package com.vaiona.commons.compilation;

import com.vaiona.commons.io.CommandExecutor;
import com.vaiona.commons.logging.LoggerHelper;
import java.io.File;
import java.io.FilenameFilter;
import java.nio.file.Paths;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.List;
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
    
    public ClassCompiler(ClassLoader parent) throws Exception{
        setJDK("");
        
        LoggerHelper.logDebug(MessageFormat.format("Checkpoint {0}: ClassCompiler.ctor. The compiler is istantiating...", 1));
        try{
            compiler = ToolProvider.getSystemJavaCompiler();
            if(compiler != null){
                fileManager = new InMemoryFileManager(compiler.getStandardFileManager(null, null, null), parent);
                LoggerHelper.logDebug(MessageFormat.format("Checkpoint {0}: ClassCompiler.ctor. The compiler is istantiated", 2));
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
        Boolean compiled = compiler
                .getTask(null, fileManager, null, null, classes, sources)
                .call();
        if(compiled){
            LoggerHelper.logDebug(MessageFormat.format("Compiling the {0} source files was successfully done.", sources.size()));           
        }else{
            LoggerHelper.logError(MessageFormat.format("Compiling the {0} source files has failed.", sources.size()));
        }
        return fileManager;
    }

    private String searchForJDK(String start){
        if(start == null || start.isEmpty())
            return null;
        try{
            File home = Paths.get(start).toFile(); // maybe it directly point to the JDK, or it hab been set in prvious runs.
            boolean found = false;
            while(!found){ // breaks when a jdk folder is found or there is no JDK in the path and its parents
                // check the path itself
                if(isJDK8(home.getName())){
                    if(Paths.get(home.getPath(), "lib", "tools.jar").toFile().exists()){                
                        return home.getPath();
                    }                                    
                }
                
                home = home.getParentFile(); // check sibelings. the home that was checked before, gets checked again when the home is set to parent.
                if(home == null)
                    return null;
                File[] sibelings = home.listFiles((File dir, String name) -> isJDK8(name)); // search for JDK 8 and upper

//                String osName = System.getProperty("os.name").toLowerCase();
//                if (osName.contains("win")) {                    
//                } else if(osName.contains("nix") || osName.contains("nux") || osName.contains("aix")){
//                } else if(osName.contains("mac")){
//                }

                for(File sibeling: sibelings){
                    if(Paths.get(sibeling.getPath(), "lib", "tools.jar").toFile().exists()){                
                        return sibeling.getPath();
                    }                
                }            
            }
        } catch (Exception ex){
            return searchForJDK(Paths.get(start).getParent().toString());
        }
        return null;
    }
    
    private boolean isJDK8(String path){
        String lower = path.toLowerCase();
        if(lower.contains("1.8") && lower.contains("jdk"))
            return true;
        if(lower.contains("-8") && lower.contains("jdk")) // orcale jdk on linux
            return true;
        return false;
    }
    
    private static String cachedJDKPath = null;
    public void setJDK(String base) throws Exception {
        if(cachedJDKPath != null){
            System.setProperty("java.home", cachedJDKPath);
            return;
        }
        
        String jdkRoot = null;
        
        LoggerHelper.logDebug(MessageFormat.format("Searching JDK_HOME at: {0}", System.getenv("JDK_HOME")));        
        jdkRoot = System.getenv("JDK_HOME"); // no need to search when JDK_HOME is set.
        if(jdkRoot != null){
            System.setProperty("java.home", jdkRoot);
            cachedJDKPath = jdkRoot;
            LoggerHelper.logDebug(MessageFormat.format("JDK was found at: {0}", jdkRoot));            
            return;
        }
        
        LoggerHelper.logDebug(MessageFormat.format("Searching JAVA_HOME at: {0}", System.getenv("JAVA_HOME")));
        jdkRoot = searchForJDK(System.getenv("JAVA_HOME"));
        if(jdkRoot != null){
            System.setProperty("java.home", jdkRoot);
            cachedJDKPath = jdkRoot;
            LoggerHelper.logDebug(MessageFormat.format("JDK was found at: {0}", jdkRoot));            
            return;
        }
        
        LoggerHelper.logDebug(MessageFormat.format("Searching application's java.home at: {0}", System.getProperty("java.home")));
        jdkRoot = searchForJDK(System.getProperty("java.home"));
        if(jdkRoot != null){
            System.setProperty("java.home", jdkRoot);
            cachedJDKPath = jdkRoot;
            LoggerHelper.logDebug(MessageFormat.format("JDK was found at: {0}", jdkRoot));            
            return;
        }

        CommandExecutor cmd = new CommandExecutor();
        for(String path : cmd.locateJDK()){
            LoggerHelper.logDebug(MessageFormat.format("Searching via command line: {0}", path));
            jdkRoot = searchForJDK(path);
            cachedJDKPath = jdkRoot;
            if(jdkRoot != null){
                System.setProperty("java.home", jdkRoot);
                LoggerHelper.logDebug(MessageFormat.format("JDK was found at: {0}", jdkRoot));            
                return;
            }
        }
        
        String message = MessageFormat.format("No JDK was not found, or the jdk/lib/tools.jar is not available. The XQt engine needs the {0} environement variable to point to a JDK version 8 or upper.", "JDK_HOME");
        LoggerHelper.logError(message);
        throw new Exception(message);            
        
    }
}
