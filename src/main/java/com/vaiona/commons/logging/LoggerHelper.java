/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.vaiona.commons.logging;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.vaiona.commons.config.PropertyManager;

/**
 *
 * @author Javad Chamanara
 */
public class LoggerHelper {
    private static final Logger logger = LoggerFactory.getLogger("XQtWorkbench");
    
    public static void logDebug(String name, String message){
    	if(PropertyManager.getBooleanPropery("logging.enabled")){
        	logger.debug(message);
        	if(PropertyManager.getBooleanPropery("logging.console.enabled")){
                System.out.println(message);        		
        	}
    	}
    }

    public static void logDebug(String message){
    	if(PropertyManager.getBooleanPropery("logging.enabled")){
        	logger.debug(message);
        	if(PropertyManager.getBooleanPropery("logging.console.enabled")){
                System.out.println(message);        		
        	}
    	}
    }

    public static void logError(String message){
    	if(PropertyManager.getBooleanPropery("logging.enabled")){
        	logger.error(message);
        	if(PropertyManager.getBooleanPropery("logging.console.enabled")){
                System.out.println(message);        		
        	}
    	}
    }

    public static void logInfo(String message){
    	if(PropertyManager.getBooleanPropery("logging.enabled")){
        	logger.info(message);
        	if(PropertyManager.getBooleanPropery("logging.console.enabled")){
                System.out.println(message);        		
        	}
    	}
    }
}
