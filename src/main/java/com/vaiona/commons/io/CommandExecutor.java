/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.vaiona.commons.io;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 *
 * @author Javad Chamanara <chamanara@gmail.com>
 */
public class CommandExecutor {
    
    public List<String> locateJDK() {
        String JAVA_WINDOWS = "where javac";
        String JAVA_LINUX = "readlink -f $(whereis javac)"; // readlink -f $(which java)
        //String JAVA_MAC = "ls -l `which javac`";
        String JAVA_MAC = "/usr/libexec/java_home -v 1.8";
        String osName = System.getProperty("os.name").toLowerCase();
        //osName = "mac"; // for simulating a mac machine
        List<String> candidates = new ArrayList<>();
        if (osName.contains("win")) {
            //C:\Program Files\Java\jdk1.8.0\bin\javac.exe            
            String commandResult = executeCommand(JAVA_WINDOWS);
            Pattern pattern = Pattern.compile("\\s*((?<path>.:\\\\.*?\\\\javac.exe)(\\s|$))", Pattern.CASE_INSENSITIVE );
            Matcher matcher = pattern.matcher(commandResult);
            while(matcher.find()){ 
                String path = matcher.group("path").trim();
                candidates.add(path);
            }
        } else if(osName.contains("nix") || osName.contains("nux") || osName.contains("aix")){
            //String commandResult = "/javac: /usr/lib/jvm/java-7-openjdk-amd64/bin/javac /usr/lib/jvm/java-7-openjdk-amd64/bin/javac /usr/lib/jvm/java-7-openjdk-amd64/man/man1/javac.1.gz"; //just for test
            String commandResult = executeCommand(JAVA_LINUX);
            Pattern pattern = Pattern.compile("\\s*((?<path>\\/.*?\\/javac.*?)(\\s|$))", Pattern.CASE_INSENSITIVE );
            Matcher matcher = pattern.matcher(commandResult);
            while(matcher.find()){ 
                String path = matcher.group("path").trim();
                candidates.add(path);
            }
        } else if(osName.contains("mac")){
            //String commandResult = "lrwx-xr-x 1 root wheel 75 Jun 22 10:27 /usr/bin/javac -> /System/Library/Frameworks/JavaVM.framework/Versions/Current/Commands/javac"
        	//String commandResult = "/Library/Java/JavaVirtualMachines/jdk1.8.0_66.jdk/Contents/Home /Library/Java/JavaVirtualMachines/jdk1.8.0_66.jdk/Contents/Home";        	
            String commandResult = executeCommand(JAVA_MAC);
            if(commandResult.contains("Unable to finad any JVMs matching version"))
            	return candidates;
            Pattern pattern = Pattern.compile("((?<path>\\/.*?\\/Home))", Pattern.CASE_INSENSITIVE );
            Matcher matcher = pattern.matcher(commandResult);
            while(matcher.find()){
                String path = matcher.group().trim();
                candidates.add(path);
            }
        }
        return candidates;
    }
    
    private static String executeCommand(String command) {
        String result = getCommandOutput(command);
        return result;
    }

    private static String getCommandOutput(String command) {
        String output = null;
        Process process = null;
        try {
            process = Runtime.getRuntime().exec(command);
            output = ConvertStreamToString(process.getInputStream());
        } catch (IOException e) {
            //System.err.println("Cannot retrieve output of command");
            //System.err.println(e);
            output = null;
        }
        return output;
    }

    private static String ConvertStreamToString(InputStream stream) {
        BufferedReader reader = null;
        InputStreamReader streamReader = null;
        try {
            streamReader = new InputStreamReader(stream);
            reader = new BufferedReader(streamReader);

            String currentLine = null;  //store current line of output from the cmd
            StringBuilder commandOutput = new StringBuilder();  //build up the output from cmd
            while ((currentLine = reader.readLine()) != null) {
                commandOutput.append(currentLine);
            }
            return commandOutput.toString();
        } catch (IOException e) {
            System.err.println(e);
        } finally {
            if (stream != null) {
                try {
                    stream.close();
                } catch (IOException e) {
                    System.err.println(e);
                }
            }
            if (streamReader != null) {
                try {
                    streamReader.close();
                } catch (IOException e) {
                    System.err.println(e);
                }
            }
            if (reader != null) {
                try {
                    streamReader.close();
                } catch (IOException e) {
                    System.err.println(e);
                }
            }
        }
        return null;
    }    
}
