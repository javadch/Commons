/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.vaiona.commons.data;

import com.vaiona.commons.compilation.ClassGenerator;
import com.vaiona.commons.compilation.InMemorySourceFile;
import com.vaiona.commons.data.AttributeInfo;
import com.vaiona.commons.data.FieldInfo;
import com.vaiona.commons.types.TypeSystem;
import java.io.IOException;
import java.text.MessageFormat;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.StringTokenizer;
import java.util.stream.Collectors;

/**
 *
 * @author Javad Chamanara <chamanara@gmail.com>
 */
public abstract class DataReaderBuilderBase {
    
    protected String statementId = "";
    protected String namespace = "";
    protected String baseClassName = "";
    protected String leftClassName = "";
    protected String rightClassName = "";
    protected String dialect = "default";
    protected Boolean namesCaseSensitive = false;
    
    protected String joinType = ""; // must remain empty for non join statements
    protected String joinOperator;
    protected String leftJoinKey;    
    protected String rightJoinKey;
    protected Map<String, FieldInfo> fields = new LinkedHashMap<>();
    protected Map<String, FieldInfo> rightFields = new LinkedHashMap<>();
    
    protected Map<String, AttributeInfo> resultEntityAttributes = new LinkedHashMap<>();
    protected Map<String, AttributeInfo> rowEntityAttributes = new LinkedHashMap<>();
    protected String whereClause = "";
    protected String whereClauseTranslated = "";
    protected Map<String, AttributeInfo> referencedAttributes = new LinkedHashMap<>();
    protected Map<String, AttributeInfo> postAttributes = new LinkedHashMap<>();
    protected Map<String, AttributeInfo> joinKeyAttributes = new LinkedHashMap<>();
    protected Integer skip = -1;
    protected Integer take = -1;    
    protected boolean writeResultsToFile = false;
    protected Map<String, Object> entityContext = new HashMap<>();
    protected Map<String, Object> recordContext = new HashMap<>();
    protected Map<String, Object> readerContext = new HashMap<>();
    protected Map<AttributeInfo, String> orderItems = new LinkedHashMap<>();        
    protected List<AttributeInfo> groupByAttributes = new ArrayList<>();
    protected String entityResourceName = "Entity";
    protected String readerResourceName = "Reader";
    
    protected String sourceOfData = "container";

    public DataReaderBuilderBase statementId(String value){ 
        this.statementId = value;
        return this;
    }

    public String getStatementId(){
        return statementId;
    }

    public String getSourceOfData(){ 
        return sourceOfData;
    }
    
    public DataReaderBuilderBase sourceOfData(String value){
        this.sourceOfData = value;
        return this;
    }

    public Boolean hasAggregate(){
        // in this case the resultEntityAttributes is populated for the result set schema and rowEntityAttributes is for the first phase data reading ...
        return rowEntityAttributes.size() > 0 || (groupByAttributes.size() > 0); 
    }

    public DataReaderBuilderBase where(String whereClause, boolean isJoinMode) throws Exception{ 
        this.whereClause = whereClause;
        // extract used attributes and put them in the pre population list
        whereClauseTranslated = extractUsedAttributes(whereClause, isJoinMode);
        return this;
    }
    
    public DataReaderBuilderBase dialect(String value){ 
        this.dialect = value;
        return this;
    }

    public String getDialect(){
        return dialect;
    }
    
    public DataReaderBuilderBase namesCaseSensitive(Boolean value){ 
        this.namesCaseSensitive = value;
        return this;
    }

    public Boolean areNamesCaseSensitive(){
        return namesCaseSensitive;
    }
    
    public DataReaderBuilderBase baseClassName(String value){
        this.baseClassName = value;
        return this;
    }
    
    public String getSourceRowType(){ return leftClassName;}
    public DataReaderBuilderBase sourceRowType(String value){
        this.leftClassName = value;
        return this;
    }

    public String getLeftClassName(){ return leftClassName;}
    public DataReaderBuilderBase leftClassName(String value){
        this.leftClassName = value;
        return this;
    }

    public String getRightClassName(){ return rightClassName;}
    public DataReaderBuilderBase rightClassName(String value){
        this.rightClassName = value;
        return this;
    }

    
    public DataReaderBuilderBase namespace(String value){
        this.namespace = value;
        return this;
    }

    public DataReaderBuilderBase skip(Integer value){
        skip = value;
        return this;
    } 
    
    public DataReaderBuilderBase take(Integer value){
        take = value;
        return this;
    }    
 
    public Map<String, FieldInfo> getFields() {
        return fields;
    }
  
    public Map<String, FieldInfo> getLeftFields() {
        return fields;
    }

    public Map<String, FieldInfo> getRightFields() {
        return rightFields;
    }

    public Map<AttributeInfo, String> getOrdering() {
        return orderItems;
    }

    public DataReaderBuilderBase orderBy(Map<AttributeInfo, String> value) {
        this.orderItems = value;
        return this;
    }
    
    public void groupBy(List<AttributeInfo> value) {
        groupByAttributes = value;
    }
    
//    public DataReaderBuilderBase addSort(String attributeName, String direction){
//        if(!ordering.containsKey(attributeName)){
//            ordering.put(attributeName, direction);                    
//        }
//        return this;
//    }    

    public Map<String, AttributeInfo> getResultAttributes() {
        return resultEntityAttributes;
    }

    public DataReaderBuilderBase addResultAttributes(Map<String, AttributeInfo> value) {
        this.resultEntityAttributes = value;
        return this;
    }    
  
    public Map<String, AttributeInfo> getRowAttributes() {
        return rowEntityAttributes;
    }

    public DataReaderBuilderBase addRowAttributes(Map<String, AttributeInfo> value) {
        this.rowEntityAttributes = value;
        return this;
    }    

    // it would be good to have an overload that takes the index also. it removes the need to register unused fields
    public DataReaderBuilderBase addField(String fieldName, String dataTypeRef){
        fieldName = namesCaseSensitive == true? fieldName: fieldName.toLowerCase();
        if(!fields.containsKey(fieldName)){
            FieldInfo fd = new FieldInfo();
            fd.name = fieldName;
            fd.internalDataType = dataTypeRef;
            fd.index = fields.size();
            fields.put(fieldName, fd);
        }                
        return this;
    }
    
    public DataReaderBuilderBase addFields(Map<String, FieldInfo> fields){
        this.fields.clear();
        //fieldName = namesCaseSensitive == true? fieldName: fieldName.toLowerCase();
        fields.values().stream().forEach(f -> 
            { 
                f.name = namesCaseSensitive == true? f.name: f.name.toLowerCase();
                this.fields.put(f.name, f);
            }
        );
        //this.fields.putAll(fields);
        return this;
    }

    public DataReaderBuilderBase addLeftFields(Map<String, FieldInfo> fields){
        return addFields(fields);
    }
    
    public DataReaderBuilderBase addRightFields(Map<String, FieldInfo> fields){
        this.rightFields.clear();
        //fieldName = namesCaseSensitive == true? fieldName: fieldName.toLowerCase();
        fields.values().stream().forEach(f -> 
            { 
                f.name = namesCaseSensitive == true? f.name: f.name.toLowerCase();
                this.rightFields.put(f.name, f);
            }
        );
        return this;
    }
   
    public DataReaderBuilderBase dateFormat(String format) throws ParseException{
        if(TypeSystem.getTypes().containsKey("Date"))            
            TypeSystem.getTypes().get("Date").setCastPattern("(new SimpleDateFormat(\"" + format + "\")).parse($data$)");
        return this;
    }

    public DataReaderBuilderBase writeResultsToFile(boolean value) {
        writeResultsToFile = value;
        return this;
    }
    
    public String getJoinType() {
        return joinType;
    }

    public DataReaderBuilderBase joinType(String value) {
        this.joinType = value;
        return this;
    }

    public String getJoinOperation() {
        return joinOperator;
    }

    public DataReaderBuilderBase joinOperator(String value) {
        this.joinOperator = value;
        return this;
    }

    public String getLeftJoinKey() {
        return leftJoinKey;
    }

    public DataReaderBuilderBase leftJoinKey(String value) {
        this.leftJoinKey = value;
        return this;
    }

    public String getRightJoinKey() {
        return rightJoinKey;
    }

    public DataReaderBuilderBase rightJoinKey(String value) {
        this.rightJoinKey = value;
        return this;
    }

    public String getEntityResourceName() {
        return entityResourceName;
    }

    public DataReaderBuilderBase entityResourceName(String value) {
        this.entityResourceName = value;
        return this;
    }

    public String getReaderResourceName() {
        return readerResourceName;
    }

    public DataReaderBuilderBase readerResourceName(String value) {
        this.readerResourceName = value;
        return this;
    }
        
    protected abstract String translate(AttributeInfo attribute, boolean rightSide);
    
    private String extractUsedAttributes(String expression, boolean isJoinMode) throws Exception {
        String translated = "";
        referencedAttributes.clear();
        for (StringTokenizer stringTokenizer = new StringTokenizer(expression, " ");
                stringTokenizer.hasMoreTokens();) {
            String token = stringTokenizer.nextToken();
            if(hasAggregate()){
                // non aggregate attributes apear in both row and result entities, so if an attribute apears in the result but not in the row 
                // entity, it is an aggregate attribute.
                if (resultEntityAttributes.containsKey(token) && rowEntityAttributes != null && !rowEntityAttributes.containsKey(token)){
                    throw new Exception(MessageFormat.format("{0} is an aggregate attribute. Aggregate attributes can not be used in the WHERE clause. Consider using them in the HAVING clause.", token));
                }
                if (rowEntityAttributes.containsKey(token) && !referencedAttributes.containsKey(token)) {
                    referencedAttributes.put(token, rowEntityAttributes.get(token));
                } else {
                    // thw wehre clause is referring to an undefined attribute
                    // check if the token is one of the fields! then add it to the rowEntityAttributes...
                }  
                // translate the where clause
                if(rowEntityAttributes.containsKey(token)){
                    if(!isJoinMode)
                        translated = translated + " " + "p." + token;
                    else
                        translated = translated + " " + "rowEntity." + token;
                }
                else {
                    translated = translated + " " + token;
                }                                      
            } else {
                if (resultEntityAttributes.containsKey(token) && !referencedAttributes.containsKey(token)) {
                    referencedAttributes.put(token, resultEntityAttributes.get(token));
                } else {
                    // the where clause is referring to an undefined attribute
                }  
                // translate the where clause
                if(resultEntityAttributes.containsKey(token)){
                	translated = translated + " " + enhanceAttribute(token, isJoinMode, "rowEntity", "p");
                }
                else {
                    translated = translated + " " + token;
                }                      
            }
        }
        return translated;
    }
        
    /*
     * Based on the join mode appends the token with a p or an entity prefix. The token is usually an attribute name.
     * The method may be overridden by concrete adapters.
     */
    protected String enhanceAttribute(String token, boolean isJoinMode, String joinPrefix, String nonJoinPrefix) {
        if(!isJoinMode)
            return nonJoinPrefix + "." + token;
        else
            return joinPrefix + "." + token;
	}

	public String enhanceExpression(String expression, boolean isJoinMode, String nonJoinPrefix, String joinPrefix) throws Exception {
        String translated = "";
        for (StringTokenizer stringTokenizer = new StringTokenizer(expression, " ");
                stringTokenizer.hasMoreTokens();) {
            String token = stringTokenizer.nextToken();
            if(hasAggregate()){
                // non aggregate attributes appear in both row and result entities, so if an attribute appears in the result but not in the row 
                // entity, it is an aggregate attribute.
                // translate the expression
                if(rowEntityAttributes.containsKey(token)){
                    translated = translated + " " + enhanceAttribute(token, isJoinMode, joinPrefix, nonJoinPrefix);
                }
                else {
                    translated = translated + " " + token;
                }                                      
            } else {
                // translate the where clause
                if(resultEntityAttributes.containsKey(token)){
                    translated = translated + " " + enhanceAttribute(token, isJoinMode, joinPrefix, nonJoinPrefix);
                }
                else {
                    translated = translated + " " + token;
                }                      
            }
        }
        return translated;
    }
    
    public LinkedHashMap<String, InMemorySourceFile> createSources() throws IOException{
        // check if the statement has no adapter, throw an exception
        buildSharedSegments();
        if(this.joinType.equalsIgnoreCase("")){ // Single Source
            buildSingleSourceSegments();
        } else {
            buildJoinedSourceSegments();
        }
        
        return buildSources();
    }    

    protected LinkedHashMap<String, InMemorySourceFile> buildSources() throws IOException{
        String resultEntityString;
        String rowEntityString;
        String readerString;
        rowEntityAttributes.entrySet().stream().map((entry) -> entry.getValue()).forEach((ad) -> {
            if(ad.joinSide.equalsIgnoreCase("R"))
                ad.forwardMapTranslated = translate(ad, true);
            else
                ad.forwardMapTranslated = translate(ad, false);
        });

        resultEntityAttributes.entrySet().stream().map((entry) -> entry.getValue()).forEach((ad) -> {
            if(ad.joinSide.equalsIgnoreCase("R"))
                ad.forwardMapTranslated = translate(ad, true);
            else
                ad.forwardMapTranslated = translate(ad, false);
        });
        LinkedHashMap<String, InMemorySourceFile> sources = new LinkedHashMap<>();
        ClassGenerator generator = new ClassGenerator();
        if(entityResourceName!= null && !entityResourceName.isEmpty()){
            resultEntityString = generator.generate(this, entityResourceName, "Resource", entityContext);
            if(resultEntityString!= null && !resultEntityString.isEmpty()){
                InMemorySourceFile ef = new InMemorySourceFile(baseClassName + "Entity", resultEntityString);
                ef.setFullName(namespace + "." + baseClassName + "Entity");
                sources.put(ef.getFullName(), ef); // the reader must be added first
            }
            if(hasAggregate() && recordContext.size() > 0) { // this is a query which contains aggregates!
                rowEntityString = generator.generate(this, entityResourceName, "Resource", recordContext); // use the same resource template but different context
                if(rowEntityString!= null && !rowEntityString.isEmpty()){
                    InMemorySourceFile ef = new InMemorySourceFile(baseClassName + "Record", rowEntityString);
                    ef.setFullName(namespace + "." + baseClassName + "Record");
                    sources.put(ef.getFullName(), ef); // the reader must be added first
                }
            }
        }    
        readerString = generator.generate(this, readerResourceName, "Resource", readerContext);
        InMemorySourceFile rf = new InMemorySourceFile(baseClassName + "Reader", readerString);
        rf.setEntryPoint(true);
        rf.setFullName(namespace + "." + baseClassName + "Reader");
        sources.put(rf.getFullName(), rf); // the reader must be added first
        return sources;
    }
    protected void buildSharedSegments() {
        if(baseClassName == null || baseClassName.isEmpty()){
            //baseClassName = "C" + (new Date()).getTime(); // sometimes causes duplicate names
            baseClassName = "Stmt_" +  statementId + "_"; //(new Date()).getTime();
        }             
        
        String recordClassName = baseClassName + "Record";
        String entityClassName = baseClassName + "Entity";
        String readerClassName = baseClassName + "Reader";
        
        entityContext.put("namespace", namespace);
        entityContext.put("BaseClassName", baseClassName);
        entityContext.put("ReaderClassName", readerClassName);
        entityContext.put("EntityClassName", entityClassName );
        if(hasAggregate()){
            entityContext.put("RecordClassName", recordClassName);
        } else {
            entityContext.put("RecordClassName", "");
        }
        
        recordContext.put("namespace", namespace);
        recordContext.put("BaseClassName", baseClassName);
        recordContext.put("ReaderClassName", readerClassName);
        if(hasAggregate()){
            recordContext.put("RecordClassName", "");
            recordContext.put("EntityClassName", recordClassName );
        } else {
            recordContext.put("RecordClassName", "");
            recordContext.put("EntityClassName", "" );            
        }

        readerContext.put("namespace", namespace);
        readerContext.put("BaseClassName", baseClassName);
        readerContext.put("ReaderClassName", readerClassName);
        readerContext.put("EntityClassName", entityClassName);
        if(hasAggregate()){ // it is not needed for the reader.
            readerContext.put("RecordClassName", recordClassName);        
        } else {
            readerContext.put("RecordClassName", "");        
        }

        entityContext.put("Attributes", resultEntityAttributes.values().stream().collect(Collectors.toList()));        
        entityContext.put("dialect", dialect);
        
        readerContext.put("Attributes", resultEntityAttributes.values().stream().collect(Collectors.toList()));
        // the output row header, when the reader, pushes the resultset to another file
        if(hasAggregate()){
            readerContext.put("RowAttributes", rowEntityAttributes.values().stream().collect(Collectors.toList()));
        }
        readerContext.put("Where", whereClauseTranslated);
        readerContext.put("Ordering", orderItems);
        readerContext.put("skip", skip);
        readerContext.put("take", take);
        readerContext.put("writeResultsToFile", writeResultsToFile);
        readerContext.put("joinType", ""); // to avoid null joinType in case of single containers.
        readerContext.put("joinOperator", "");            
        readerContext.put("leftJoinKey", "");
        readerContext.put("rightJoinKey", ""); 
        readerContext.put("dialect", dialect);
    }

    protected void buildSingleSourceSegments() {
        // Pre list contains the attributes referenced from the where clause
        if(hasAggregate()){
            recordContext.put("Pre", referencedAttributes.values().stream().collect(Collectors.toList()));
            postAttributes = rowEntityAttributes.entrySet().stream()
                .filter((entry) -> (!referencedAttributes.containsKey(entry.getKey())))
                .collect(Collectors.toMap(p->p.getKey(), p->p.getValue()));
            // Single container does not have the Mid attributes
            // Post list contains all the other attributes except those in the Pre
            recordContext.put("Post", postAttributes.values().stream().collect(Collectors.toList()));
            
            // Post_Left and Post_Right should be emtpy in single container cases.
            List<AttributeInfo> leftOuterItems = postAttributes.values().stream()
                    .filter(p-> p.joinSide.equalsIgnoreCase("L"))
                    .collect(Collectors.toList());
            if(!hasAggregate()){
                leftOuterItems.addAll(orderItems.keySet().stream().filter(p-> !leftOuterItems.contains(p)).collect(Collectors.toList()));
            }
            recordContext.put("Post_Left", leftOuterItems);

            List<AttributeInfo> rightOuterItems = postAttributes.values().stream()
                    .filter(p-> p.joinSide.equalsIgnoreCase("R"))
                    .collect(Collectors.toList());
            if(!hasAggregate()){
                rightOuterItems.addAll(orderItems.keySet().stream().filter(p-> !rightOuterItems.contains(p)).collect(Collectors.toList()));
            }
            recordContext.put("Post_Right", rightOuterItems);

            entityContext.put("Pre", null);
             // all the attributes are populated at once, as there is no where or join clause on the result entity. There are done on the row entity, before the aggregation
            entityContext.put("Post", resultEntityAttributes.values().stream().collect(Collectors.toList()));
            entityContext.put("Mid", null);
            entityContext.put("Post_Left", null);
            entityContext.put("Post_Right", null);
            
            readerContext.put("GroupBy", groupByAttributes);
            
        } else {
            entityContext.put("Pre", referencedAttributes.values().stream().collect(Collectors.toList()));
            postAttributes = resultEntityAttributes.entrySet().stream()
                .filter((entry) -> (!referencedAttributes.containsKey(entry.getKey())))
                .collect(Collectors.toMap(p->p.getKey(), p->p.getValue()));
            // Single container does not have the Mid attributes
            // Post list contains all the other attributes except those in the Pre
            entityContext.put("Post", postAttributes.values().stream().collect(Collectors.toList()));
            // Post_Left and Post_Right should be emtpy in single container cases.
            List<AttributeInfo> leftOuterItems = postAttributes.values().stream()
                    .filter(p-> p.joinSide.equalsIgnoreCase("L"))
                    .collect(Collectors.toList());
            if(orderItems != null)
                leftOuterItems.addAll(orderItems.keySet().stream().filter(p-> !leftOuterItems.contains(p)).collect(Collectors.toList()));
            entityContext.put("Post_Left", leftOuterItems);

            List<AttributeInfo> rightOuterItems = postAttributes.values().stream()
                    .filter(p-> p.joinSide.equalsIgnoreCase("R"))
                    .collect(Collectors.toList());
            if(orderItems != null)
                rightOuterItems.addAll(orderItems.keySet().stream().filter(p-> !rightOuterItems.contains(p)).collect(Collectors.toList()));
            entityContext.put("Post_Right", rightOuterItems);
        }
    }

    protected void buildJoinedSourceSegments() {
        if(hasAggregate()){
            // set pre to join keys, mid: where clause keys
           recordContext.put("joinType", this.joinType);
           joinKeyAttributes.put(leftJoinKey, rowEntityAttributes.get(leftJoinKey));
           joinKeyAttributes.put(rightJoinKey, rowEntityAttributes.get(rightJoinKey));
           // Pre list contains the attribtes used as join keys
           recordContext.put("Pre", joinKeyAttributes.values().stream().collect(Collectors.toList()));
           // Mid contains the attrbutes referenced from the where clause
           recordContext.put("Mid", referencedAttributes.values().stream().collect(Collectors.toList()));
           postAttributes = rowEntityAttributes.entrySet().stream()
               .filter((entry) -> (!referencedAttributes.containsKey(entry.getKey())))
               .filter((entry) -> (!joinKeyAttributes.containsKey(entry.getKey())))
               .collect(Collectors.toMap(p->p.getKey(), p->p.getValue()));
           // Post list contains all the attributes except those used as join key or in the where clause
           recordContext.put("Post", postAttributes.values().stream().collect(Collectors.toList()));
           // In case of outer join, if ordering (check also for frouping) is present, the ordering attributes should be unioned
           // with the post population attributes, so that the sort method on the data reader should hev proper values populaed into the entity
           List<AttributeInfo> leftOuterItems = postAttributes.values().stream()
                   .filter(p-> p.joinSide.equalsIgnoreCase("L")).collect(Collectors.toList());
           leftOuterItems.addAll(orderItems.keySet().stream().filter(p-> !leftOuterItems.contains(p)).collect(Collectors.toList()));
           recordContext.put("Post_Left", leftOuterItems);

           List<AttributeInfo> rightOuterItems = postAttributes.values().stream()
                   .filter(p-> p.joinSide.equalsIgnoreCase("R")).collect(Collectors.toList());
           rightOuterItems.addAll(orderItems.keySet().stream().filter(p-> !rightOuterItems.contains(p)).collect(Collectors.toList()));
           recordContext.put("Post_Right", rightOuterItems);            
        } else {
            // set pre to join keys, mid: where clause keys
           entityContext.put("joinType", this.joinType);
           joinKeyAttributes.put(leftJoinKey, resultEntityAttributes.get(leftJoinKey));
           joinKeyAttributes.put(rightJoinKey, resultEntityAttributes.get(rightJoinKey));
           // Pre list contains the attribtes used as join keys
           entityContext.put("Pre", joinKeyAttributes.values().stream().collect(Collectors.toList()));
           // Mid contains the attrbutes referenced from the where clause
           entityContext.put("Mid", referencedAttributes.values().stream().collect(Collectors.toList()));
           postAttributes = resultEntityAttributes.entrySet().stream()
               .filter((entry) -> (!referencedAttributes.containsKey(entry.getKey())))
               .filter((entry) -> (!joinKeyAttributes.containsKey(entry.getKey())))
               .collect(Collectors.toMap(p->p.getKey(), p->p.getValue()));
           // Post list contains all the attributes except those used as join key or in the where clause
           entityContext.put("Post", postAttributes.values().stream().collect(Collectors.toList()));
           // In case of outer join, if ordering (check also for frouping) is present, the ordering attributes should be unioned
           // with the post population attributes, so that the sort method on the data reader should hev proper values populaed into the entity
           List<AttributeInfo> leftOuterItems = postAttributes.values().stream()
                   .filter(p-> p.joinSide.equalsIgnoreCase("L")).collect(Collectors.toList());
           leftOuterItems.addAll(orderItems.keySet().stream().filter(p-> !leftOuterItems.contains(p)).collect(Collectors.toList()));
           entityContext.put("Post_Left", leftOuterItems);

           List<AttributeInfo> rightOuterItems = postAttributes.values().stream()
                   .filter(p-> p.joinSide.equalsIgnoreCase("R")).collect(Collectors.toList());
           rightOuterItems.addAll(orderItems.keySet().stream().filter(p-> !rightOuterItems.contains(p)).collect(Collectors.toList()));
           entityContext.put("Post_Right", rightOuterItems);            
        }
        readerContext.put("joinType", this.joinType);
        readerContext.put("joinOperator", this.joinOperator);            
        readerContext.put("leftJoinKey", this.leftJoinKey);
        readerContext.put("rightJoinKey", this.rightJoinKey);
        // Mid is passed to the reader in order to prevent calling midPopulate when not neccessary; the case when there is no WHERE clause.
        readerContext.put("Mid", referencedAttributes.values().stream().collect(Collectors.toList()));
    }
}
