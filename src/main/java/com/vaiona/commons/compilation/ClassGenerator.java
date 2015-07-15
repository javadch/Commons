/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package com.vaiona.commons.compilation;

import com.vaiona.commons.logging.LoggerHelper;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.text.MessageFormat;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.rythmengine.RythmEngine;

/**
 *
 * @author standard
 */
public class ClassGenerator {
    static RythmEngine engine = null;
    public ClassGenerator(){
        if(engine == null){
            Map<String, Object> conf = new HashMap<>();
            try {
                //conf.put("engine.load_precompiled.enabled", true);
                //conf.put("rythm.engine.mode", "dev");
                //String tmpPath = Paths.get("./tmp").toAbsolutePath().normalize().toString();
                //conf.put("rythm.engine.file_write.enabled", false); // do not write the cached files/ sources to a file
                //conf.put("rythm.home.tmp.dir", Paths.get("./temp").toAbsolutePath().normalize().toString());
                Files.createDirectories(Paths.get("./temp"));
                conf.put("rythm.home.tmp.dir", new File("./temp"));
            } catch (IOException ex) {
                LoggerHelper.logError(MessageFormat.format("Can not create the temp folder!", 1));
            }
            //conf.put("rythm.home.precompiled.dir", Paths.get("./temp/compiled").toAbsolutePath().normalize().toString());
            engine = new RythmEngine(conf);
        }
    }
    public String generate(Object resourceContainer, String source, String sourceType, Map<String, Object> contextData) throws IOException{
        if(sourceType.toUpperCase().equals("FILE")){
            return generateFromFile(source, contextData);
        } else if(sourceType.toUpperCase().equals("RESOURCE")){
            return generateFromResource(resourceContainer, source, contextData);
        }
        return null;
    }

    public String generateFromFile(String fileName, Object contextData) throws IOException{
        String template = new String(Files.readAllBytes(Paths.get(fileName)), StandardCharsets.UTF_8);
        return template;
    }
    
    public String generateFromResource(Object resourceContainer, String resourceName, Map<String, Object> contextData) throws IOException{
        InputStream stream = resourceContainer.getClass().getClassLoader().getResourceAsStream("resources/" + resourceName + ".jt");
        //InputStream stream = this.getClass().getResourceAsStream("resources/" + resourceName + ".jt");
        if (stream == null) return "";
        try(java.util.Scanner s = new java.util.Scanner(stream)){
            // its ia trick: The reason it works is because Scanner iterates over tokens in the stream, and in this case we separate tokens 
            // using "beginning of the input boundary" (\A) thus giving us only one token for the entire contents of the stream
            String template = s.useDelimiter("\\A").hasNext() ? s.next() : "";
            // Set source elevel to 1.8
            return(engine.render(template, contextData));
        }
        catch (Exception ex) { 
            return ex.getMessage();
        }
        finally {
            stream.close();
        }
    }
    
}
