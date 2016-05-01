/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.vaiona.commons.lang;

import com.vaiona.commons.io.CommandExecutor;
import com.vaiona.commons.logging.LoggerHelper;

import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.List;

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
        
        jdkRoot = searchForJDK(System.getenv("JDK_HOME")); // no need to search when JDK_HOME is set, but  it is done to be sure
        if(jdkRoot != null){
            cachedJDKPath = jdkRoot;
            LoggerHelper.logDebug(MessageFormat.format("JDK was found at: {0}", jdkRoot));            
            return jdkRoot;
        }
        LoggerHelper.logDebug(MessageFormat.format("Searching JDK_HOME at '{0}' was NOT successful.", System.getenv("JDK_HOME")));        
        
        jdkRoot = searchForJDK(System.getenv("JAVA_HOME"));
        if(jdkRoot != null){
            cachedJDKPath = jdkRoot;
            LoggerHelper.logDebug(MessageFormat.format("JDK was found at: {0}", jdkRoot));            
            return jdkRoot;
        }
        LoggerHelper.logDebug(MessageFormat.format("Searching JAVA_HOME at '{0}' was NOT successful.", System.getenv("JAVA_HOME")));
        
        jdkRoot = searchForJDK(System.getProperty("java.home"));
        if(jdkRoot != null){
            cachedJDKPath = jdkRoot;
            LoggerHelper.logDebug(MessageFormat.format("JDK was found at: {0}", jdkRoot));            
            return jdkRoot;
        }
        LoggerHelper.logDebug(MessageFormat.format("Searching application's java.home at '{0}' was NOT successful.", System.getProperty("java.home")));

        CommandExecutor cmd = new CommandExecutor();
        for(String path : cmd.locateJDK()){
            LoggerHelper.logDebug(MessageFormat.format("Searching for the JDK via OS specific commands: {0}", path));
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
            File home = Paths.get(start).toFile(); // maybe it directly point to the JDK, or it has been set in previous runs.
            boolean found = false;
            while(!found){ // breaks when a JDK folder is found or there is no JDK in the path and its parents
                // check the path itself
                if(isJDK8(home)){
                	Path jdkPath =Paths.get(home.getPath(), "lib", "tools.jar");
            		LoggerHelper.logDebug(MessageFormat.format("Checking whether JDK compiler tools exists at:  {0}", jdkPath.toString()));
                    if(jdkPath.toFile().exists()){                
                    	LoggerHelper.logDebug(MessageFormat.format("JDK compiler tool was found in: {0}", home.getPath().toString()));
                        return home.getPath();
                    }                                    
                }
                
                home = home.getParentFile(); // check siblings. the home that was checked before, gets checked again when the home is set to parent.
                if(home == null)
                    return null;
                File[] sibelings = home.listFiles((File dir, String name) -> isJDK8(dir)); // search for JDK 8 and upper
                for(File sibling: sibelings){
                	try{
                		Path jdkPath = Paths.get(sibling.getPath(), "lib", "tools.jar");
                		LoggerHelper.logDebug(MessageFormat.format("Checking whether JDK compiler tools exists at:  {0}", jdkPath.toString()));
	                    if(jdkPath.toFile().exists()){
	                    	LoggerHelper.logDebug(MessageFormat.format("JDK compiler tool was found in: {0}", sibling.getPath().toString()));
	                        return sibling.getPath();
	                    }
                	} catch(Exception ex){
                		// do not do anything. maybe the tools is not available!
                	}
                }            
            }
        } catch (Exception ex){
            return searchForJDK(Paths.get(start).getParent().toString());
        }
        return null;
    }
    
    private static boolean isJDK8(File file){
    	LoggerHelper.logDebug(MessageFormat.format("Checking whether {0} contains JDK", file.getPath()));
    	String name = file.getName().toLowerCase();
        if(name.contains("1.8") && name.contains("jdk"))
            return true;
        if(name.contains("-8") && name.contains("jdk")) // Oracle JDK on Linux
            return true;
        String osName = System.getProperty("os.name").toLowerCase();
        
        if (osName.contains("nix")) {
            if(name.contains("java") && name.contains("-8-oracle")) // Oracle JDK on Linux
                return true;        	
        }else if (osName.contains("mac")) {
        	String path = file.getPath().toLowerCase();
            if(path.contains("1.8") && path.contains("jdk")){
            	LoggerHelper.logDebug(MessageFormat.format("Potential JDK locatoin found on Mac at: {0}", path));
            	return true;
            }
        }
        return false;
    }
    
}
