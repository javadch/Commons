package com.vaiona.commons.config;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

public class PropertyManager {
	private static Properties applicationProps = null;
	public static void init(String propertiesFilePath){ // load the properties here
		String defaultProperties = "default.properties";
		if(propertiesFilePath == null || propertiesFilePath.isEmpty())
			propertiesFilePath = "config/config.properties";
		Properties defaultProps = new Properties();
		InputStream in;
		try {
			in = Thread.currentThread().getContextClassLoader().getResourceAsStream(defaultProperties); // from the resources folder resources/default.properties
			defaultProps.load(in);
			in.close();
			
			applicationProps = new Properties(defaultProps);
			in = new FileInputStream(propertiesFilePath); // should be config/config.properties
			applicationProps.load(in);
			in.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	public static String getPropery(String propertyName){
		return applicationProps.getProperty(propertyName);
	}
	
	public static Boolean getBooleanPropery(String propertyName){
		return Boolean.parseBoolean(applicationProps.getProperty(propertyName));
	}

	public static Integer getIntegerPropery(String propertyName){
		return Integer.parseInt(applicationProps.getProperty(propertyName));
	}

	public static Properties getProperties(){
		return applicationProps;
	}
	
	
}
