/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.vaiona.commons.lang;

import com.vaiona.commons.io.CommandExecutor;
import com.vaiona.commons.logging.LoggerHelper;
import java.io.File;
import java.nio.file.Paths;
import java.text.MessageFormat;

/**
 *
 * @author Javad Chamanara <chamanara@gmail.com>
 */
public class Environment {
    private static String cachedJDKPath = null;
    public static void setJDK() throws Exception {
        System.setProperty("java.home", getJDK8Folder()); // throws an exception if the JDK was not found.
        String message = MessageFormat.format("The JDK 8 was set to {0}.", System.getProperty("java.home"));
        LoggerHelper.logDebug(message);
    }
    
    public static String getJDK8Folder() throws Exception {
        if(cachedJDKPath != null){
            return(cachedJDKPath);
        }
        
        String jdkRoot = null;
        
        LoggerHelper.logDebug(MessageFormat.format("Searching JDK_HOME at: {0}", System.getenv("JDK_HOME")));        
        jdkRoot = searchForJDK(System.getenv("JDK_HOME")); // no need to search when JDK_HOME is set, but  it is done to be sure
        if(jdkRoot != null){
            cachedJDKPath = jdkRoot;
            LoggerHelper.logDebug(MessageFormat.format("JDK was found at: {0}", jdkRoot));            
            return jdkRoot;
        }
        
        LoggerHelper.logDebug(MessageFormat.format("Searching JAVA_HOME at: {0}", System.getenv("JAVA_HOME")));
        jdkRoot = searchForJDK(System.getenv("JAVA_HOME"));
        if(jdkRoot != null){
            cachedJDKPath = jdkRoot;
            LoggerHelper.logDebug(MessageFormat.format("JDK was found at: {0}", jdkRoot));            
            return jdkRoot;
        }
        
        LoggerHelper.logDebug(MessageFormat.format("Searching application's java.home at: {0}", System.getProperty("java.home")));
        jdkRoot = searchForJDK(System.getProperty("java.home"));
        if(jdkRoot != null){
            cachedJDKPath = jdkRoot;
            LoggerHelper.logDebug(MessageFormat.format("JDK was found at: {0}", jdkRoot));            
            return jdkRoot;
        }

        CommandExecutor cmd = new CommandExecutor();
        for(String path : cmd.locateJDK()){
            LoggerHelper.logDebug(MessageFormat.format("Searching the JDK OS dependent via commands: {0}", path));
            jdkRoot = searchForJDK(path);
            cachedJDKPath = jdkRoot;
            if(jdkRoot != null){
                LoggerHelper.logDebug(MessageFormat.format("JDK was found at: {0}", jdkRoot));            
                return jdkRoot;
            }
        }
        
        String message = MessageFormat.format("JDK 8 was not found, or the jdk/lib/tools.jar is not available. The XQt query engine needs the {0} environement variable to point to a JDK version 8 or upper.", "JDK_HOME");
        LoggerHelper.logError(message);
        throw new Exception(message);            
        
    }

    private static String searchForJDK(String start){
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
    
    private static boolean isJDK8(String path){
        String lower = path.toLowerCase();
        if(lower.contains("1.8") && lower.contains("jdk"))
            return true;
        if(lower.contains("-8") && lower.contains("jdk")) // orcale jdk on linux
            return true;
        return false;
    }
    
}
