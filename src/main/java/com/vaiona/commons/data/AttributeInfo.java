/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package com.vaiona.commons.data;

import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author standard
 */
public class AttributeInfo extends FieldInfo {
    // Data type is the data type defined in the adpater usable in Java and mapp-able to the underlying data
    // Type ref is the type declared by the query

    public String forwardMap = "";
    public String forwardMapTranslated = "";
    public List<String> fields = new ArrayList<>();
    public String runtimeType;
    public String joinSide;
    
    public AttributeInfo(){
        // do nothing
    }
    
    public AttributeInfo(AttributeInfo original){
        this.forwardMap = original.forwardMap;
        this.forwardMapTranslated = original.forwardMapTranslated;
        this.runtimeType = original.runtimeType;
        this.joinSide = original.joinSide;
        this.name = original.name;
        this.conceptualDataType = original.conceptualDataType;
        this.internalDataType = original.internalDataType;
        this.index = original.index;
        this.reference = original.reference;
        this.reference = original;
        this.fields = new ArrayList<>(original.fields);
    }    
}
