/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 *//*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package com.vaiona.commons.data;

import com.vaiona.commons.types.TypeSystem;

/**
 *
 * @author standard
 */
public class FieldInfo {
    public static final String UNKOWN_TYPE = "String";
    
    public static final String UNKOWN_UNIT = "Unknown";
    public String unit = UNKOWN_UNIT; // unit of measurement
    
    //private String missingValue = null;
    //private String format = null;
    
    public String name = "";
    public String conceptualDataType = TypeSystem.Unknown;    
    public String internalDataType = UNKOWN_TYPE;
    public int index = 0;
    // its the object the field or the attribute is constructed from! usually it is a
    // PerspectiveAttribute but to keep the common library independent from the domain, its declared as an Object.
    public Object reference;
}
