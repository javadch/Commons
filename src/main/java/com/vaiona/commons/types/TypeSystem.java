/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 *//*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package com.vaiona.commons.types;

import com.vaiona.commons.data.DataTypeInfo;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 *
 * @author standard
 */
public class TypeSystem {
    public static final String Boolean = "Boolean";
    public static final String Byte = "Byte";
    public static final String String = "String";
    public static final String Integer = "Integer";
    public static final String Long = "Long";
    public static final String Real = "Real";
    public static final String Date = "Date";
    public static final String Unknown = "Unknown";
    public static final String Invalid = "Invalid";    
    public static final String DontCare = "*";

    private static final Map<String, DataTypeInfo> types = new HashMap<>();
    private static final List<ResultTypeInfo> resultTypeTable = new ArrayList<>();
    
    public static Map<String, DataTypeInfo> getTypes(){
        return types;
    }
    
    public static String getResultType(String op1Type, String op2Type, String operator, boolean isUnary, boolean isSymentric){
        // check whether op1/op2/op/isUnary exists        
        Optional<ResultTypeInfo> match = resultTypeTable.stream().filter(
                    p ->    p.getOp1Type().equalsIgnoreCase(op1Type)
                    &&      p.getOp2Type().equalsIgnoreCase(op2Type)
                    &&      p.getOperator().equalsIgnoreCase(operator)
                    &&      p.isIsUnary() == isUnary
                ).findFirst();
        // check whether op1/*/op/isUnary exists
        if(!match.isPresent()){
            match = resultTypeTable.stream().filter(
                    p ->    p.getOp1Type().equalsIgnoreCase(op1Type)
                    &&      p.getOp2Type().equalsIgnoreCase(TypeSystem.DontCare)
                    &&      p.getOperator().equalsIgnoreCase(operator)
                    &&      p.isIsUnary() == isUnary
                ).findFirst();
        }
        // check whether */op2/op/isUnary exists
        else if(!match.isPresent()){
            match = resultTypeTable.stream().filter(
                    p ->    p.getOp1Type().equalsIgnoreCase(TypeSystem.DontCare)
                    &&      p.getOp2Type().equalsIgnoreCase(op2Type)
                    &&      p.getOperator().equalsIgnoreCase(operator)
                    &&      p.isIsUnary() == isUnary
                ).findFirst();
        }
        // check whether */*/op/isUnary exists
        else if(!match.isPresent()){
            match = resultTypeTable.stream().filter(
                    p ->    p.getOp1Type().equalsIgnoreCase(TypeSystem.DontCare)
                    &&      p.getOp2Type().equalsIgnoreCase(TypeSystem.DontCare)
                    &&      p.getOperator().equalsIgnoreCase(operator)
                    &&      p.isIsUnary() == isUnary
                ).findFirst();
        }
        // with care: if noy found swap op1Typ1 and op2Type and do the procedure again. in case consider sawping #1 and #2 also.
        // if all fail, return Unknown type
        if(!match.isPresent()){
            return TypeSystem.Unknown;
        }
        // when found see whether the resulttype is: #1, #2, or a type. #1 means return the op1Type, #2 means return op2Type, otherwise the specific type should be returned.
        String resultType = match.get().getResultType();
        switch (resultType) {
            case "#1":
                return match.get().getOp1Type();
            case "#2":
                return match.get().getOp2Type(); 
        }
        return resultType;
    }
    
    static { // configure conceptual types and thier parsing, evaluation counterparts for Java 
        
        types.put(TypeSystem.Boolean,    new DataTypeInfo(TypeSystem.Boolean, "Boolean.parseBoolean($data$)", "Boolean.compare($first$, $second$)", "boolean"));
        types.put(TypeSystem.Byte,       new DataTypeInfo(TypeSystem.Byte, "Byte.parseByte($data$)", "Boolean.compare($first$, $second$)", "Byte"));
        types.put(TypeSystem.String,     new DataTypeInfo(TypeSystem.String, "String.valueOf($data$)", "$first$.compareTo($second$)", "String"));
        types.put(TypeSystem.Integer,    new DataTypeInfo(TypeSystem.Integer, "Integer.parseInt($data$)", "Integer.compare($first$, $second$)", "int"));
        types.put(TypeSystem.Long,       new DataTypeInfo(TypeSystem.Long, "Long.parseLong($data$)", "Long.compare($first$, $second$)", "long"));
        types.put(TypeSystem.Real,       new DataTypeInfo(TypeSystem.Real, "Double.parseDouble($data$)", "Double.compare($first$, $second$)", "Double"));
        types.put(TypeSystem.Date,       new DataTypeInfo(TypeSystem.Date, "(new SimpleDateFormat(\"yyyy-MM-dd'T'HH:mm:ssX\")).parse($data$)", "$first$.compareTo($second$)", "Date"));             
        // candidates: Decimal, Geometry
    }
    
    static { // setup the result type determination table 
        
        //unary plus, the second operator is not used, but its set as the first one for simplicity in the search
        resultTypeTable.add(new ResultTypeInfo(TypeSystem.Unknown,   TypeSystem.Unknown,  "+",    true,   TypeSystem.Real,     "(+ (op1))",  ""));
        resultTypeTable.add(new ResultTypeInfo(TypeSystem.Boolean,   TypeSystem.Boolean,  "+",    true,   TypeSystem.Byte,     "(+ (op1))",  ""));
        resultTypeTable.add(new ResultTypeInfo(TypeSystem.String,    TypeSystem.String,   "+",    true,   TypeSystem.Invalid,  "",         "Unary plus does not apply to String."));
        resultTypeTable.add(new ResultTypeInfo(TypeSystem.DontCare,         TypeSystem.DontCare,        "+",    true,   "#1",       "(+ (op1))",  ""));

        //unary minus, the second operator is not used, but its set as the first one for simplicity in the search
        resultTypeTable.add(new ResultTypeInfo(TypeSystem.Unknown,   TypeSystem.Unknown,  "-",    true,   TypeSystem.Real,     "(-(op1))",  ""));
        resultTypeTable.add(new ResultTypeInfo(TypeSystem.Boolean,   TypeSystem.Boolean,  "-",    true,   TypeSystem.Byte,     "(-(op1))",  ""));
        resultTypeTable.add(new ResultTypeInfo(TypeSystem.String, TypeSystem.String, "-", true, TypeSystem.Invalid, "", "Unary minus does not apply to String."));
        resultTypeTable.add(new ResultTypeInfo(TypeSystem.Date, TypeSystem.Date, "-", true, TypeSystem.Invalid, "", "Unary minus does not apply to String."));
        resultTypeTable.add(new ResultTypeInfo(TypeSystem.DontCare, TypeSystem.DontCare, "-", true, "#1", "(-(op1))", ""));
        
        // Add (+), it is symetric
        resultTypeTable.add(new ResultTypeInfo(TypeSystem.Unknown, TypeSystem.DontCare, "+", false, TypeSystem.Real, "((op1) + (op2))", ""));
        resultTypeTable.add(new ResultTypeInfo(TypeSystem.DontCare, TypeSystem.Unknown, "+", false, TypeSystem.Real, "((op1) + (op2))", ""));

        resultTypeTable.add(new ResultTypeInfo(TypeSystem.Boolean, TypeSystem.Boolean, "+", false, TypeSystem.Byte, "((op1) + (op2))", ""));
        resultTypeTable.add(new ResultTypeInfo(TypeSystem.Boolean, TypeSystem.DontCare, "+", false, "#2", "((op1) + (op2))", ""));
        
        resultTypeTable.add(new ResultTypeInfo(TypeSystem.Byte, TypeSystem.Boolean, "+", false, TypeSystem.Byte, "((op1) + (op2))", ""));
        resultTypeTable.add(new ResultTypeInfo(TypeSystem.Byte, TypeSystem.DontCare, "+", false, "#2", "((op1) + (op2))", ""));
        
        resultTypeTable.add(new ResultTypeInfo(TypeSystem.String, TypeSystem.DontCare, "+", false, TypeSystem.String, "((op1) + (op2))", ""));
        resultTypeTable.add(new ResultTypeInfo(TypeSystem.DontCare, TypeSystem.String, "+", false, TypeSystem.String, "((op1) + (op2))", ""));
        
        resultTypeTable.add(new ResultTypeInfo(TypeSystem.Integer, TypeSystem.Boolean, "+", false, TypeSystem.Integer, "((op1) + (op2))", ""));
        resultTypeTable.add(new ResultTypeInfo(TypeSystem.Integer, TypeSystem.Byte, "+", false, TypeSystem.Integer, "((op1) + (op2))", ""));
        resultTypeTable.add(new ResultTypeInfo(TypeSystem.Integer, TypeSystem.DontCare, "+", false, "#2", "((op1) + (op2))", ""));

        resultTypeTable.add(new ResultTypeInfo(TypeSystem.Long, TypeSystem.Boolean, "+", false, TypeSystem.Long, "((op1) + (op2))", ""));
        resultTypeTable.add(new ResultTypeInfo(TypeSystem.Long, TypeSystem.Byte, "+", false, TypeSystem.Long, "((op1) + (op2))", ""));
        resultTypeTable.add(new ResultTypeInfo(TypeSystem.Long, TypeSystem.Integer, "+", false, TypeSystem.Long, "((op1) + (op2))", ""));
        resultTypeTable.add(new ResultTypeInfo(TypeSystem.Long, TypeSystem.DontCare, "+", false, "#2", "((op1) + (op2))", ""));

        resultTypeTable.add(new ResultTypeInfo(TypeSystem.Real, TypeSystem.String, "+", false, TypeSystem.String, "((op1) + (op2))", ""));
        resultTypeTable.add(new ResultTypeInfo(TypeSystem.Real, TypeSystem.DontCare, "+", false, TypeSystem.Real, "((op1) + (op2))", ""));
        resultTypeTable.add(new ResultTypeInfo(TypeSystem.Real, TypeSystem.Date, "+", false, TypeSystem.Date, "((op1) + (op2))", ""));

        resultTypeTable.add(new ResultTypeInfo(TypeSystem.Date, TypeSystem.String, "+", false, TypeSystem.String, "((op1) + (op2))", ""));
        resultTypeTable.add(new ResultTypeInfo(TypeSystem.Date, TypeSystem.DontCare, "+", false, TypeSystem.Date, "((op1) + (op2))", ""));
        resultTypeTable.add(new ResultTypeInfo(TypeSystem.DontCare, TypeSystem.Date, "+", false, TypeSystem.Date, "((op1) + (op2))", ""));
        
        // Sub (-),
        resultTypeTable.add(new ResultTypeInfo(TypeSystem.Unknown, TypeSystem.DontCare, "-", false, TypeSystem.Real, "((op1) - (op2))", ""));
        resultTypeTable.add(new ResultTypeInfo(TypeSystem.DontCare, TypeSystem.Unknown, "-", false, TypeSystem.Real, "((op1) - (op2))", ""));

        resultTypeTable.add(new ResultTypeInfo(TypeSystem.Boolean, TypeSystem.Boolean, "-", false, TypeSystem.Byte, "((op1) - (op2))", ""));
        resultTypeTable.add(new ResultTypeInfo(TypeSystem.Boolean, TypeSystem.String, "-", false, TypeSystem.Invalid, "", ""));       
        resultTypeTable.add(new ResultTypeInfo(TypeSystem.Boolean, TypeSystem.DontCare, "-", false, "#2", "((op1) - (op2))", ""));
        
        resultTypeTable.add(new ResultTypeInfo(TypeSystem.Byte, TypeSystem.Boolean, "-", false, TypeSystem.Byte, "((op1) - (op2))", ""));
        resultTypeTable.add(new ResultTypeInfo(TypeSystem.Byte, TypeSystem.String, "-", false, TypeSystem.Invalid, "", ""));       
        resultTypeTable.add(new ResultTypeInfo(TypeSystem.Byte, TypeSystem.DontCare, "-", false, "#2", "((op1) - (op2))", ""));
        
        resultTypeTable.add(new ResultTypeInfo(TypeSystem.String, TypeSystem.String, "-", false, TypeSystem.String, "((op1) - (op2))", ""));
        resultTypeTable.add(new ResultTypeInfo(TypeSystem.String, TypeSystem.Date, "-", false, TypeSystem.Date, "((op1) - (op2))", ""));
        resultTypeTable.add(new ResultTypeInfo(TypeSystem.String, TypeSystem.DontCare, "-", false, TypeSystem.Invalid, "", ""));
        resultTypeTable.add(new ResultTypeInfo(TypeSystem.DontCare, TypeSystem.String, "-", false, TypeSystem.Invalid, "", ""));
        
        resultTypeTable.add(new ResultTypeInfo(TypeSystem.Integer, TypeSystem.Boolean, "-", false, TypeSystem.Integer, "((op1) - (op2))", ""));
        resultTypeTable.add(new ResultTypeInfo(TypeSystem.Integer, TypeSystem.Byte, "-", false, TypeSystem.Integer, "((op1) - (op2))", ""));
        resultTypeTable.add(new ResultTypeInfo(TypeSystem.Integer, TypeSystem.String, "-", false, TypeSystem.Invalid, "", ""));       
        resultTypeTable.add(new ResultTypeInfo(TypeSystem.Integer, TypeSystem.DontCare, "-", false, "#2", "((op1) - (op2))", ""));

        resultTypeTable.add(new ResultTypeInfo(TypeSystem.Long, TypeSystem.Boolean, "-", false, TypeSystem.Long, "((op1) - (op2))", ""));
        resultTypeTable.add(new ResultTypeInfo(TypeSystem.Long, TypeSystem.Byte, "-", false, TypeSystem.Long, "((op1) - (op2))", ""));
        resultTypeTable.add(new ResultTypeInfo(TypeSystem.Long, TypeSystem.Integer, "-", false, TypeSystem.Long, "((op1) - (op2))", ""));
        resultTypeTable.add(new ResultTypeInfo(TypeSystem.Long, TypeSystem.String, "-", false, TypeSystem.Invalid, "", ""));       
        resultTypeTable.add(new ResultTypeInfo(TypeSystem.Long, TypeSystem.DontCare, "+", false, "#2", "((op1) - (op2))", ""));

        resultTypeTable.add(new ResultTypeInfo(TypeSystem.Real, TypeSystem.String, "-", false, TypeSystem.Invalid, "", ""));
        resultTypeTable.add(new ResultTypeInfo(TypeSystem.Real, TypeSystem.DontCare, "-", false, TypeSystem.Real, "((op1) - (op2))", ""));
        resultTypeTable.add(new ResultTypeInfo(TypeSystem.Real, TypeSystem.Date, "-", false, TypeSystem.Date, "((op1) - (op2))", ""));

        resultTypeTable.add(new ResultTypeInfo(TypeSystem.Date, TypeSystem.DontCare, "-", false, TypeSystem.Date, "((op1) - (op2))", ""));
        resultTypeTable.add(new ResultTypeInfo(TypeSystem.DontCare, TypeSystem.Date, "-", false, TypeSystem.Date, "((op1) - (op2))", ""));
        
        // Mul (*), it is symetric
        resultTypeTable.add(new ResultTypeInfo(TypeSystem.Unknown, TypeSystem.DontCare, "*", false, TypeSystem.Real, "((op1) * (op2))", ""));
        resultTypeTable.add(new ResultTypeInfo(TypeSystem.DontCare, TypeSystem.Unknown, "*", false, TypeSystem.Real, "((op1) * (op2))", ""));

        resultTypeTable.add(new ResultTypeInfo(TypeSystem.Boolean, TypeSystem.Boolean, "*", false, TypeSystem.Byte, "((op1) * (op2))", ""));
        resultTypeTable.add(new ResultTypeInfo(TypeSystem.Boolean, TypeSystem.String, "*", false, TypeSystem.Invalid, "", ""));       
        resultTypeTable.add(new ResultTypeInfo(TypeSystem.Boolean, TypeSystem.DontCare, "*", false, "#2", "((op1) * (op2))", ""));
        
        resultTypeTable.add(new ResultTypeInfo(TypeSystem.Byte, TypeSystem.Boolean, "*", false, TypeSystem.Byte, "((op1) * (op2))", ""));
        resultTypeTable.add(new ResultTypeInfo(TypeSystem.Byte, TypeSystem.String, "*", false, TypeSystem.Invalid, "", ""));       
        resultTypeTable.add(new ResultTypeInfo(TypeSystem.Byte, TypeSystem.DontCare, "*", false, "#2", "((op1) * (op2))", ""));
        
        resultTypeTable.add(new ResultTypeInfo(TypeSystem.String, TypeSystem.DontCare, "*", false, TypeSystem.Invalid, "", ""));
        resultTypeTable.add(new ResultTypeInfo(TypeSystem.DontCare, TypeSystem.String, "*", false, TypeSystem.Invalid, "", ""));
        
        resultTypeTable.add(new ResultTypeInfo(TypeSystem.Integer, TypeSystem.Boolean, "*", false, TypeSystem.Integer, "((op1) * (op2))", ""));
        resultTypeTable.add(new ResultTypeInfo(TypeSystem.Integer, TypeSystem.Byte, "*", false, TypeSystem.Long, "((op1) * (op2))", ""));
        //resultTypeTable.add(new ResultTypeInfo(TypeSystem.Integer, TypeSystem.String, "*", false, TypeSystem.Invalid, "", ""));       
        resultTypeTable.add(new ResultTypeInfo(TypeSystem.Integer, TypeSystem.Integer, "*", false, TypeSystem.Long, "((op1) * (op2))", ""));
        resultTypeTable.add(new ResultTypeInfo(TypeSystem.Integer, TypeSystem.Long, "*", false, TypeSystem.Long, "((op1) * (op2))", ""));
        resultTypeTable.add(new ResultTypeInfo(TypeSystem.Integer, TypeSystem.Real, "*", false, TypeSystem.Real, "((op1) * (op2))", ""));
        //resultTypeTable.add(new ResultTypeInfo(TypeSystem.Integer, TypeSystem.Date, "*", false, TypeSystem.Invalid, "", ""));

        resultTypeTable.add(new ResultTypeInfo(TypeSystem.Long, TypeSystem.Boolean, "*", false, TypeSystem.Long, "((op1) * (op2))", ""));
        resultTypeTable.add(new ResultTypeInfo(TypeSystem.Long, TypeSystem.Byte, "*", false, TypeSystem.Long, "((op1) * (op2))", ""));
        //resultTypeTable.add(new ResultTypeInfo(TypeSystem.Long, TypeSystem.String, "*", false, TypeSystem.Invalid, "", ""));       
        resultTypeTable.add(new ResultTypeInfo(TypeSystem.Long, TypeSystem.Integer, "*", false, TypeSystem.Long, "((op1) * (op2))", ""));
        resultTypeTable.add(new ResultTypeInfo(TypeSystem.Long, TypeSystem.Long, "*", false, TypeSystem.Long, "((op1) * (op2))", ""));
        resultTypeTable.add(new ResultTypeInfo(TypeSystem.Long, TypeSystem.Real, "*", false, TypeSystem.Real, "((op1) * (op2))", ""));
        //resultTypeTable.add(new ResultTypeInfo(TypeSystem.Long, TypeSystem.Date, "*", false, TypeSystem.Invalid, "", ""));

        resultTypeTable.add(new ResultTypeInfo(TypeSystem.Real, TypeSystem.String, "*", false, TypeSystem.Invalid, "", ""));
        resultTypeTable.add(new ResultTypeInfo(TypeSystem.Real, TypeSystem.Date, "*", false, TypeSystem.Invalid, "", ""));
        resultTypeTable.add(new ResultTypeInfo(TypeSystem.Real, TypeSystem.DontCare, "*", false, TypeSystem.Real, "((op1) * (op2))", ""));

        resultTypeTable.add(new ResultTypeInfo(TypeSystem.Date, TypeSystem.DontCare, "*", false, TypeSystem.Invalid, "", ""));
        resultTypeTable.add(new ResultTypeInfo(TypeSystem.DontCare, TypeSystem.Date, "*", false, TypeSystem.Invalid, "", ""));
        
        // Div (/), it is symetric
        resultTypeTable.add(new ResultTypeInfo(TypeSystem.Unknown, TypeSystem.DontCare, "/", false, TypeSystem.Real, "((op1) / (op2))", ""));
        resultTypeTable.add(new ResultTypeInfo(TypeSystem.DontCare, TypeSystem.Unknown, "/", false, TypeSystem.Real, "((op1) / (op2))", ""));

        resultTypeTable.add(new ResultTypeInfo(TypeSystem.Boolean, TypeSystem.Boolean, "/", false, TypeSystem.Byte, "((op1) / (op2))", ""));
        resultTypeTable.add(new ResultTypeInfo(TypeSystem.Boolean, TypeSystem.String, "/", false, TypeSystem.Invalid, "", ""));       
        resultTypeTable.add(new ResultTypeInfo(TypeSystem.Boolean, TypeSystem.Date, "/", false, TypeSystem.Invalid, "", ""));       
        resultTypeTable.add(new ResultTypeInfo(TypeSystem.Boolean, TypeSystem.DontCare, "/", false, TypeSystem.Real, "((op1) / (op2))", ""));
        
        resultTypeTable.add(new ResultTypeInfo(TypeSystem.Byte, TypeSystem.Boolean, "/", false, TypeSystem.Byte, "((op1) / (op2))", ""));
        resultTypeTable.add(new ResultTypeInfo(TypeSystem.Byte, TypeSystem.String, "/", false, TypeSystem.Invalid, "", ""));       
        resultTypeTable.add(new ResultTypeInfo(TypeSystem.Byte, TypeSystem.Date, "/", false, TypeSystem.Invalid, "", ""));       
        resultTypeTable.add(new ResultTypeInfo(TypeSystem.Byte, TypeSystem.DontCare, "/", false, TypeSystem.Real, "((op1) / (op2))", ""));
        
        resultTypeTable.add(new ResultTypeInfo(TypeSystem.String, TypeSystem.DontCare, "/", false, TypeSystem.Invalid, "", ""));
        resultTypeTable.add(new ResultTypeInfo(TypeSystem.DontCare, TypeSystem.String, "/", false, TypeSystem.Invalid, "", ""));
        
        resultTypeTable.add(new ResultTypeInfo(TypeSystem.Integer, TypeSystem.Byte, "/", false, TypeSystem.Real, "((op1) / (op2))", ""));
        resultTypeTable.add(new ResultTypeInfo(TypeSystem.Integer, TypeSystem.Integer, "/", false, TypeSystem.Real, "((op1) / (op2))", ""));
        resultTypeTable.add(new ResultTypeInfo(TypeSystem.Integer, TypeSystem.Long, "/", false, TypeSystem.Real, "((op1) / (op2))", ""));
        resultTypeTable.add(new ResultTypeInfo(TypeSystem.Integer, TypeSystem.Real, "/", false, TypeSystem.Real, "((op1) / (op2))", ""));

        resultTypeTable.add(new ResultTypeInfo(TypeSystem.Long, TypeSystem.Byte, "/", false, TypeSystem.Real, "((op1) / (op2))", ""));
        resultTypeTable.add(new ResultTypeInfo(TypeSystem.Long, TypeSystem.Integer, "/", false, TypeSystem.Real, "((op1) / (op2))", ""));
        resultTypeTable.add(new ResultTypeInfo(TypeSystem.Long, TypeSystem.Long, "/", false, TypeSystem.Real, "((op1) / (op2))", ""));
        resultTypeTable.add(new ResultTypeInfo(TypeSystem.Long, TypeSystem.Real, "/", false, TypeSystem.Real, "((op1) / (op2))", ""));

        resultTypeTable.add(new ResultTypeInfo(TypeSystem.Real, TypeSystem.Byte, "/", false, TypeSystem.Real, "((op1) / (op2))", ""));
        resultTypeTable.add(new ResultTypeInfo(TypeSystem.Real, TypeSystem.Integer, "/", false, TypeSystem.Real, "((op1) / (op2))", ""));
        resultTypeTable.add(new ResultTypeInfo(TypeSystem.Real, TypeSystem.Long, "/", false, TypeSystem.Real, "((op1) / (op2))", ""));
        resultTypeTable.add(new ResultTypeInfo(TypeSystem.Real, TypeSystem.Real, "/", false, TypeSystem.Real, "((op1) / (op2))", ""));

        resultTypeTable.add(new ResultTypeInfo(TypeSystem.Date, TypeSystem.DontCare, "/", false, TypeSystem.Invalid, "", ""));
        resultTypeTable.add(new ResultTypeInfo(TypeSystem.DontCare, TypeSystem.Date, "/", false, TypeSystem.Invalid, "", ""));
        
        // Mod (%), it is NOT symetric
        resultTypeTable.add(new ResultTypeInfo(TypeSystem.Unknown, TypeSystem.DontCare, "%", false, TypeSystem.Real, "((op1) % (op2))", ""));
        resultTypeTable.add(new ResultTypeInfo(TypeSystem.DontCare, TypeSystem.Unknown, "%", false, TypeSystem.Real, "((op1) % (op2))", ""));

        resultTypeTable.add(new ResultTypeInfo(TypeSystem.Boolean, TypeSystem.Boolean, "%", false, TypeSystem.Byte, "((op1) % (op2))", ""));
        resultTypeTable.add(new ResultTypeInfo(TypeSystem.Boolean, TypeSystem.String, "%", false, TypeSystem.Invalid, "", ""));       
        resultTypeTable.add(new ResultTypeInfo(TypeSystem.Boolean, TypeSystem.Date, "%", false, TypeSystem.Invalid, "", ""));   
        resultTypeTable.add(new ResultTypeInfo(TypeSystem.Boolean, TypeSystem.Real, "%", false, TypeSystem.Long, "((op1) % (op2))", ""));        
        resultTypeTable.add(new ResultTypeInfo(TypeSystem.Boolean, TypeSystem.DontCare, "%", false, "#2", "((op1) % (op2))", ""));
        
        resultTypeTable.add(new ResultTypeInfo(TypeSystem.Byte, TypeSystem.Boolean, "%", false, TypeSystem.Byte, "((op1) % (op2))", ""));
        resultTypeTable.add(new ResultTypeInfo(TypeSystem.Byte, TypeSystem.String, "%", false, TypeSystem.Invalid, "", ""));       
        resultTypeTable.add(new ResultTypeInfo(TypeSystem.Byte, TypeSystem.Date, "%", false, TypeSystem.Invalid, "", ""));       
        resultTypeTable.add(new ResultTypeInfo(TypeSystem.Byte, TypeSystem.Real, "%", false, TypeSystem.Long, "((op1) % (op2))", ""));        
        resultTypeTable.add(new ResultTypeInfo(TypeSystem.Byte, TypeSystem.DontCare, "%", false, "#2", "((op1) % (op2))", ""));
        
        resultTypeTable.add(new ResultTypeInfo(TypeSystem.String, TypeSystem.DontCare, "%", false, TypeSystem.Invalid, "", ""));
        resultTypeTable.add(new ResultTypeInfo(TypeSystem.DontCare, TypeSystem.String, "%", false, TypeSystem.Invalid, "", ""));
        
        resultTypeTable.add(new ResultTypeInfo(TypeSystem.Integer, TypeSystem.Boolean, "%", false, TypeSystem.Byte, "((op1) % (op2))", ""));
        resultTypeTable.add(new ResultTypeInfo(TypeSystem.Integer, TypeSystem.String, "%", false, TypeSystem.Invalid, "", ""));       
        resultTypeTable.add(new ResultTypeInfo(TypeSystem.Integer, TypeSystem.Date, "%", false, TypeSystem.Invalid, "", ""));       
        resultTypeTable.add(new ResultTypeInfo(TypeSystem.Integer, TypeSystem.Real, "%", false, TypeSystem.Long, "((op1) % (op2))", ""));        
        resultTypeTable.add(new ResultTypeInfo(TypeSystem.Integer, TypeSystem.DontCare, "%", false, "#2", "((op1) % (op2))", ""));

        resultTypeTable.add(new ResultTypeInfo(TypeSystem.Long, TypeSystem.Boolean, "%", false, TypeSystem.Byte, "((op1) % (op2))", ""));
        resultTypeTable.add(new ResultTypeInfo(TypeSystem.Long, TypeSystem.String, "%", false, TypeSystem.Invalid, "", ""));       
        resultTypeTable.add(new ResultTypeInfo(TypeSystem.Long, TypeSystem.Date, "%", false, TypeSystem.Invalid, "", ""));       
        resultTypeTable.add(new ResultTypeInfo(TypeSystem.Long, TypeSystem.Real, "%", false, TypeSystem.Long, "((op1) % (op2))", ""));        
        resultTypeTable.add(new ResultTypeInfo(TypeSystem.Long, TypeSystem.DontCare, "%", false, "#2", "((op1) % (op2))", ""));

        resultTypeTable.add(new ResultTypeInfo(TypeSystem.Real, TypeSystem.Boolean, "%", false, TypeSystem.Byte, "((op1) % (op2))", ""));
        resultTypeTable.add(new ResultTypeInfo(TypeSystem.Real, TypeSystem.String, "%", false, TypeSystem.Invalid, "", ""));       
        resultTypeTable.add(new ResultTypeInfo(TypeSystem.Real, TypeSystem.Date, "%", false, TypeSystem.Invalid, "", ""));       
        resultTypeTable.add(new ResultTypeInfo(TypeSystem.Real, TypeSystem.Real, "%", false, TypeSystem.Long, "((op1) % (op2))", ""));        
        resultTypeTable.add(new ResultTypeInfo(TypeSystem.Real, TypeSystem.DontCare, "%", false, "#2", "((op1) % (op2))", ""));

        resultTypeTable.add(new ResultTypeInfo(TypeSystem.Date, TypeSystem.DontCare, "%", false, TypeSystem.Invalid, "", ""));
        resultTypeTable.add(new ResultTypeInfo(TypeSystem.DontCare, TypeSystem.Date, "%", false, TypeSystem.Invalid, "", ""));
        
        // Binary compariosn operators
        resultTypeTable.add(new ResultTypeInfo(TypeSystem.DontCare, TypeSystem.DontCare, "eq", false, TypeSystem.Boolean, "((op1) == (op2))", ""));
        resultTypeTable.add(new ResultTypeInfo(TypeSystem.DontCare, TypeSystem.DontCare, "noteq", false, TypeSystem.Boolean, "((op1) != (op2))", ""));
        resultTypeTable.add(new ResultTypeInfo(TypeSystem.DontCare, TypeSystem.DontCare, "gt", false, TypeSystem.Boolean, "((op1) > (op2))", ""));
        resultTypeTable.add(new ResultTypeInfo(TypeSystem.DontCare, TypeSystem.DontCare, "gteq", false, TypeSystem.Boolean, "((op1) >= (op2))", ""));
        resultTypeTable.add(new ResultTypeInfo(TypeSystem.DontCare, TypeSystem.DontCare, "lt", false, TypeSystem.Boolean, "((op1) < (op2))", ""));
        resultTypeTable.add(new ResultTypeInfo(TypeSystem.DontCare, TypeSystem.DontCare, "lteq", false, TypeSystem.Boolean, "((op1) <= (op2))", ""));
        resultTypeTable.add(new ResultTypeInfo(TypeSystem.DontCare, TypeSystem.DontCare, "like", false, TypeSystem.Boolean, "((op1) ~ (op2))", ""));

        // unary comaprison operators
        resultTypeTable.add(new ResultTypeInfo(TypeSystem.DontCare, TypeSystem.DontCare, "isnull", false, TypeSystem.Boolean, "((op1) == null)", ""));
        resultTypeTable.add(new ResultTypeInfo(TypeSystem.DontCare, TypeSystem.DontCare, "isnotnull", false, TypeSystem.Boolean, "((op1) != null)", ""));

        resultTypeTable.add(new ResultTypeInfo(TypeSystem.DontCare, TypeSystem.DontCare, "isnumber", false, TypeSystem.Boolean, "", ""));
        resultTypeTable.add(new ResultTypeInfo(TypeSystem.DontCare, TypeSystem.DontCare, "isnotnumber", false, TypeSystem.Boolean, "", ""));
        
        resultTypeTable.add(new ResultTypeInfo(TypeSystem.DontCare, TypeSystem.DontCare, "isdate", false, TypeSystem.Boolean, "", ""));
        resultTypeTable.add(new ResultTypeInfo(TypeSystem.DontCare, TypeSystem.DontCare, "isnotdate", false, TypeSystem.Boolean, "", ""));

        resultTypeTable.add(new ResultTypeInfo(TypeSystem.DontCare, TypeSystem.DontCare, "isempty", false, TypeSystem.Boolean, "", ""));
        resultTypeTable.add(new ResultTypeInfo(TypeSystem.DontCare, TypeSystem.DontCare, "isnotempty", false, TypeSystem.Boolean, "", ""));
        
        resultTypeTable.add(new ResultTypeInfo(TypeSystem.DontCare, TypeSystem.DontCare, "not", false, TypeSystem.Boolean, "(!(op1))", ""));
        resultTypeTable.add(new ResultTypeInfo(TypeSystem.DontCare, TypeSystem.DontCare, "and", false, TypeSystem.Boolean, "((op1) || (op2))", ""));
        resultTypeTable.add(new ResultTypeInfo(TypeSystem.DontCare, TypeSystem.DontCare, "or", false, TypeSystem.Boolean, "((op1) && (op2))", ""));
        
    }
    
}
