/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.vaiona.commons.io;

import com.vaiona.commons.logging.LoggerHelper;
import java.io.File;
import java.io.IOException;
import java.nio.file.Paths;
import java.text.MessageFormat;

/**
 *
 * @author Javad Chamanara <chamanara@gmail.com>
 */
public class FileHelper {
    // If the path is relative change it to a absolute one, based on the application execution location
    public static String makeAbsolute(String path) throws IOException{
        if((new File(path)).isAbsolute())
            return path;
        String base = System.getProperty("user.dir");
        return makeAbsolute(base, path);
    }

    public static String makeAbsolute(String base, String path) throws IOException{
        if((new File(path)).isAbsolute())
            return path;
        File baseFile = new File(base);
        if(baseFile.isFile())
            baseFile = baseFile.getParentFile();
        File full = new File(baseFile, path);
        String absolute = full.getCanonicalPath(); // may throw IOException        
        return absolute;
    }
    
    public static String getConfigPath(String basePaths){
        // The root of the application is the default location for the config folder.
        // the config folder must contain the adapters.xml file.
        try{
            LoggerHelper.logDebug(MessageFormat.format("Searching {0} for adapter configuration.", Paths.get("config", "adapters.xml").toString()));
            if(Paths.get(makeAbsolute(Paths.get("config", "adapters.xml").toString())).toFile().exists())
                return Paths.get("config").toString();
            // basePaths is a comma separated list of possible locations for the config folder.
            String[] pathItems = basePaths.split(",");
            for(String pathItem: pathItems){
                LoggerHelper.logDebug(MessageFormat.format("Searching {0} for adapter configuration.", Paths.get(pathItem.trim(), "config", "adapters.xml").toString()));
                if(Paths.get(makeAbsolute(Paths.get(pathItem.trim(), "config", "adapters.xml").toString())).toFile().exists())
                    return Paths.get(pathItem.trim(), "config").toString();
            }
            return ("");
        } catch (Exception ex){
            return "";
        }
    }
    
    
}
