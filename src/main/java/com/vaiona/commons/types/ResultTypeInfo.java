/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package com.vaiona.commons.types;

/**
 *
 * @author standard
 */
public class ResultTypeInfo {
    private String op1Type;
    private String op2Type;
    private String operator;
    private boolean isUnary = false;
    private String resultType;
    private String computation;
    private String errorMessage;

    public ResultTypeInfo(String op1Type, String op2Type, String operation, boolean isUnary, String resultType, String computation, String errorMessage){
        this.op1Type = op1Type;
        this.op2Type = op2Type;
        this.operator = operation;
        this.isUnary = isUnary;
        this.resultType = resultType;
        this.computation = computation;
        this.errorMessage = errorMessage;
    }
    
    public String getOp1Type() {
        return op1Type;
    }

    public void setOp1Type(String op1Type) {
        this.op1Type = op1Type;
    }

    public String getOp2Type() {
        return op2Type;
    }

    public void setOp2Type(String op2Type) {
        this.op2Type = op2Type;
    }

    public String getOperator() {
        return operator;
    }

    public void setOperator(String operator) {
        this.operator = operator;
    }

    public boolean isIsUnary() {
        return isUnary;
    }

    public void setIsUnary(boolean isUnary) {
        this.isUnary = isUnary;
    }

    public String getResultType() {
        return resultType;
    }

    public void setResultType(String resultType) {
        this.resultType = resultType;
    }

    public String getComputation() {
        return computation;
    }

    public void setComputation(String computation) {
        this.computation = computation;
    }

    public String getErrorMessage() {
        return errorMessage;
    }

    public void setErrorMessage(String errorMessage) {
        this.errorMessage = errorMessage;
    }
    
}
