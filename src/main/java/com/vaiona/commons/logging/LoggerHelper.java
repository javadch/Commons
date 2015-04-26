/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.vaiona.commons.logging;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author jfd
 */
public class LoggerHelper {
    private static final Logger logger = LoggerFactory.getLogger("SciQuest");
    public static void logDebug(String name, String message){
        //Logger logger = LoggerFactory.getLogger(name);
        logger.debug(message);
    }

    public static void logDebug(String message){
        logger.debug(message);
    }

    public static void logError(String message){
        logger.error(message);
    }

    public static void logInfo(String message){
        logger.info(message);
    }
}
