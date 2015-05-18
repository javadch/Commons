/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package com.vaiona.commons.data;

/**
 *
 * @author standard
 */
public class DataTypeInfo {
    String name;
    String lowerCaseName;
    String castPattern;
    String comparePattern;
    String runtimeType;
    
    public DataTypeInfo(String name, String castPattern, String comparePattern, String runtimeType){
        this.name = name;
        this.lowerCaseName = name.toLowerCase();
        this.castPattern = castPattern;
        this.comparePattern = comparePattern;
        this.runtimeType = runtimeType;
    }

    public String getName() {
        return name;
    }

    public String getLowerCaseName() {
        return lowerCaseName;
    }

    public String getCastPattern() {
        return castPattern;
    }

    public String getComparePattern() {
        return comparePattern;
    }

    public void setCastPattern(String castPattern) {
        this.castPattern = castPattern;
    }

    public void setComparePattern(String comparePattern) {
        this.comparePattern = comparePattern;
    }

    public String getRuntimeType() {
        return runtimeType;
    }

    public void setRuntimeType(String runtimeType) {
        this.runtimeType = runtimeType;
    }

    public String makeDateCastPattern(String format) {
        String pattern = "(new SimpleDateFormat(\"" + format + "\")).parse($data$)";
        return pattern;
    }
    
    
}
