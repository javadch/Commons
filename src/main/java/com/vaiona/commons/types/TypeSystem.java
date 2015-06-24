
package com.vaiona.commons.types;

import com.vaiona.commons.data.DataTypeInfo;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 *
 * @author Javad Chamanara
 */
public class TypeSystem {
    public class TypeName {
        public static final String Boolean = "Boolean";
        public static final String Byte = "Byte";
        public static final String String = "String";
        public static final String Integer = "Integer";
        public static final String Long = "Long";
        public static final String Real = "Real";
        public static final String Date = "Date";
        public static final String Time = "Time";
        public static final String DateTime = "DateTime";
        public static final String Unknown = "Unknown";
        public static final String Invalid = "Invalid";    
        public static final String DontCare = "*";
    }
    
    public static class Convert{
        public static double toPrimitive(Double value){
            return (double)value;
        }

        public static int toPrimitive(Integer value){
            return (int)value;
        }

        public static String toPrimitive(String value){
            return value;
        }

        public static long toPrimitive(Long value){
            return (long)value;
        }

        public static boolean toPrimitive(Boolean value){
            return (boolean)value;
        }
        
    }
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
                    &&      p.getOp2Type().equalsIgnoreCase(TypeSystem.TypeName.DontCare)
                    &&      p.getOperator().equalsIgnoreCase(operator)
                    &&      p.isIsUnary() == isUnary
                ).findFirst();
        }
        // check whether */op2/op/isUnary exists
        else if(!match.isPresent()){
            match = resultTypeTable.stream().filter(
                    p ->    p.getOp1Type().equalsIgnoreCase(TypeSystem.TypeName.DontCare)
                    &&      p.getOp2Type().equalsIgnoreCase(op2Type)
                    &&      p.getOperator().equalsIgnoreCase(operator)
                    &&      p.isIsUnary() == isUnary
                ).findFirst();
        }
        // check whether */*/op/isUnary exists
        else if(!match.isPresent()){
            match = resultTypeTable.stream().filter(
                    p ->    p.getOp1Type().equalsIgnoreCase(TypeSystem.TypeName.DontCare)
                    &&      p.getOp2Type().equalsIgnoreCase(TypeSystem.TypeName.DontCare)
                    &&      p.getOperator().equalsIgnoreCase(operator)
                    &&      p.isIsUnary() == isUnary
                ).findFirst();
        }
        // with care: if noy found swap op1Typ1 and op2Type and do the procedure again. in case consider sawping #1 and #2 also.
        // if all fail, return Unknown type
        if(!match.isPresent()){
            return TypeSystem.TypeName.Unknown;
        }
        // when found see whether the resulttype is: #1, #2, or a type. #1 means return the op1Type, #2 means return op2Type, otherwise the specific type should be returned.
        String resultType = match.get().getResultType();
        switch (resultType) {
            case "#1":
                //return match.get().getOp1Type();
                return op1Type;
            case "#2":
                //return match.get().getOp2Type(); 
                return op2Type;
        }
        return resultType;
    }
    
    static { // configure conceptual types and thier parsing, evaluation counterparts for Java 
        
        types.put(TypeSystem.TypeName.Boolean,    new DataTypeInfo(TypeSystem.TypeName.Boolean, "Boolean.parseBoolean($data$)", "Boolean.compare($first$, $second$)", "boolean", boolean.class));
        types.put(TypeSystem.TypeName.Byte,       new DataTypeInfo(TypeSystem.TypeName.Byte, "Byte.parseByte($data$)", "Boolean.compare($first$, $second$)", "byte", byte.class));
        types.put(TypeSystem.TypeName.String,     new DataTypeInfo(TypeSystem.TypeName.String, "String.valueOf($data$)", "$first$.compareTo($second$)", "String", String.class));
        types.put(TypeSystem.TypeName.Integer,    new DataTypeInfo(TypeSystem.TypeName.Integer, "Integer.parseInt($data$)", "Integer.compare($first$, $second$)", "int", int.class));
        types.put(TypeSystem.TypeName.Long,       new DataTypeInfo(TypeSystem.TypeName.Long, "Long.parseLong($data$)", "Long.compare($first$, $second$)", "long", long.class));
        types.put(TypeSystem.TypeName.Real,       new DataTypeInfo(TypeSystem.TypeName.Real, "Double.parseDouble($data$)", "Double.compare($first$, $second$)", "double", double.class));
        types.put(TypeSystem.TypeName.Date,       new DataTypeInfo(TypeSystem.TypeName.Date, "(new SimpleDateFormat(\"yyyy-MM-dd\")).parse($data$)", "$first$.compareTo($second$)", "java.util.Date", java.util.Date.class));             
        // add all conversions to the table, and the DateTime and Time to the grammar
//        types.put(TypeSystem.TypeName.Date,       new DataTypeInfo(TypeSystem.TypeName.Date, "(new SimpleDateFormat(\"yyyy-MM-dd\")).parse($data$)", "$first$.compareTo($second$)", "java.time.LocalDate", LocalDate.class));             
//        types.put(TypeSystem.TypeName.Time,       new DataTypeInfo(TypeSystem.TypeName.Date, "(new SimpleDateFormat(\"HH:mm:ss\")).parse($data$)", "$first$.compareTo($second$)", "java.time.LocalTime", LocalTime.class));             
//        types.put(TypeSystem.TypeName.DateTime,   new DataTypeInfo(TypeSystem.TypeName.Date, "(new SimpleDateFormat(\"yyyy-MM-dd'T'HH:mm:ssX\")).parse($data$)", "$first$.compareTo($second$)", "java.time.LocalDateTime", LocalDateTime.class));             
        // candidates: Decimal, Geometry
    }
    
    static { // setup the result type determination table 
        // one of the sources: https://msdn.microsoft.com/en-us/library/ms235255.aspx
        //unary plus, the second operator is not used, but its set as the first one for simplicity in the search
        resultTypeTable.add(new ResultTypeInfo(TypeSystem.TypeName.Unknown,   TypeSystem.TypeName.Unknown,  "+",    true,   TypeSystem.TypeName.Real,     "(+ (op1))",  ""));
        resultTypeTable.add(new ResultTypeInfo(TypeSystem.TypeName.Boolean,   TypeSystem.TypeName.Boolean,  "+",    true,   TypeSystem.TypeName.Byte,     "(+ (op1))",  ""));
        resultTypeTable.add(new ResultTypeInfo(TypeSystem.TypeName.String,    TypeSystem.TypeName.String,   "+",    true,   TypeSystem.TypeName.Invalid,  "",         "Unary plus does not apply to String."));
        resultTypeTable.add(new ResultTypeInfo(TypeSystem.TypeName.DontCare,         TypeSystem.TypeName.DontCare,        "+",    true,   "#1",       "(+ (op1))",  ""));

        //unary minus, the second operator is not used, but its set as the first one for simplicity in the search
        resultTypeTable.add(new ResultTypeInfo(TypeSystem.TypeName.Unknown,   TypeSystem.TypeName.Unknown,  "-",    true,   TypeSystem.TypeName.Real,     "(-(op1))",  ""));
        resultTypeTable.add(new ResultTypeInfo(TypeSystem.TypeName.Boolean,   TypeSystem.TypeName.Boolean,  "-",    true,   TypeSystem.TypeName.Byte,     "(-(op1))",  ""));
        resultTypeTable.add(new ResultTypeInfo(TypeSystem.TypeName.String, TypeSystem.TypeName.String, "-", true, TypeSystem.TypeName.Invalid, "", "Unary minus does not apply to String."));
        resultTypeTable.add(new ResultTypeInfo(TypeSystem.TypeName.Date, TypeSystem.TypeName.Date, "-", true, TypeSystem.TypeName.Invalid, "", "Unary minus does not apply to String."));
        resultTypeTable.add(new ResultTypeInfo(TypeSystem.TypeName.DontCare, TypeSystem.TypeName.DontCare, "-", true, "#1", "(-(op1))", ""));
        
        // Add (+), it is symetric
        resultTypeTable.add(new ResultTypeInfo(TypeSystem.TypeName.Unknown, TypeSystem.TypeName.DontCare, "+", false, TypeSystem.TypeName.Real, "((op1) + (op2))", ""));
        resultTypeTable.add(new ResultTypeInfo(TypeSystem.TypeName.DontCare, TypeSystem.TypeName.Unknown, "+", false, TypeSystem.TypeName.Real, "((op1) + (op2))", ""));

        resultTypeTable.add(new ResultTypeInfo(TypeSystem.TypeName.Boolean, TypeSystem.TypeName.Boolean, "+", false, TypeSystem.TypeName.Byte, "((op1) + (op2))", ""));
        resultTypeTable.add(new ResultTypeInfo(TypeSystem.TypeName.Boolean, TypeSystem.TypeName.DontCare, "+", false, "#2", "((op1) + (op2))", ""));
        
        resultTypeTable.add(new ResultTypeInfo(TypeSystem.TypeName.Byte, TypeSystem.TypeName.Boolean, "+", false, TypeSystem.TypeName.Byte, "((op1) + (op2))", ""));
        resultTypeTable.add(new ResultTypeInfo(TypeSystem.TypeName.Byte, TypeSystem.TypeName.DontCare, "+", false, "#2", "((op1) + (op2))", ""));
        
        resultTypeTable.add(new ResultTypeInfo(TypeSystem.TypeName.String, TypeSystem.TypeName.DontCare, "+", false, TypeSystem.TypeName.String, "((op1) + (op2))", ""));
        resultTypeTable.add(new ResultTypeInfo(TypeSystem.TypeName.DontCare, TypeSystem.TypeName.String, "+", false, TypeSystem.TypeName.String, "((op1) + (op2))", ""));
        
        resultTypeTable.add(new ResultTypeInfo(TypeSystem.TypeName.Integer, TypeSystem.TypeName.Boolean, "+", false, TypeSystem.TypeName.Integer, "((op1) + (op2))", ""));
        resultTypeTable.add(new ResultTypeInfo(TypeSystem.TypeName.Integer, TypeSystem.TypeName.Byte, "+", false, TypeSystem.TypeName.Integer, "((op1) + (op2))", ""));
        resultTypeTable.add(new ResultTypeInfo(TypeSystem.TypeName.Integer, TypeSystem.TypeName.DontCare, "+", false, "#2", "((op1) + (op2))", ""));

        resultTypeTable.add(new ResultTypeInfo(TypeSystem.TypeName.Long, TypeSystem.TypeName.Boolean, "+", false, TypeSystem.TypeName.Long, "((op1) + (op2))", ""));
        resultTypeTable.add(new ResultTypeInfo(TypeSystem.TypeName.Long, TypeSystem.TypeName.Byte, "+", false, TypeSystem.TypeName.Long, "((op1) + (op2))", ""));
        resultTypeTable.add(new ResultTypeInfo(TypeSystem.TypeName.Long, TypeSystem.TypeName.Integer, "+", false, TypeSystem.TypeName.Long, "((op1) + (op2))", ""));
        //resultTypeTable.add(new ResultTypeInfo(TypeSystem.TypeName.Long, TypeSystem.TypeName.Long, "+", false, TypeSystem.TypeName.Long, "((op1) + (op2))", ""));
        resultTypeTable.add(new ResultTypeInfo(TypeSystem.TypeName.Long, TypeSystem.TypeName.DontCare, "+", false, "#2", "((op1) + (op2))", ""));

        resultTypeTable.add(new ResultTypeInfo(TypeSystem.TypeName.Real, TypeSystem.TypeName.String, "+", false, TypeSystem.TypeName.String, "((op1) + (op2))", ""));
        resultTypeTable.add(new ResultTypeInfo(TypeSystem.TypeName.Real, TypeSystem.TypeName.DontCare, "+", false, TypeSystem.TypeName.Real, "((op1) + (op2))", ""));
        resultTypeTable.add(new ResultTypeInfo(TypeSystem.TypeName.Real, TypeSystem.TypeName.Date, "+", false, TypeSystem.TypeName.Date, "((op1) + (op2))", ""));

        resultTypeTable.add(new ResultTypeInfo(TypeSystem.TypeName.Date, TypeSystem.TypeName.String, "+", false, TypeSystem.TypeName.String, "((op1) + (op2))", ""));
        resultTypeTable.add(new ResultTypeInfo(TypeSystem.TypeName.Date, TypeSystem.TypeName.DontCare, "+", false, TypeSystem.TypeName.Date, "((op1) + (op2))", ""));
        resultTypeTable.add(new ResultTypeInfo(TypeSystem.TypeName.DontCare, TypeSystem.TypeName.Date, "+", false, TypeSystem.TypeName.Date, "((op1) + (op2))", ""));
        
        // Sub (-),
        resultTypeTable.add(new ResultTypeInfo(TypeSystem.TypeName.Unknown, TypeSystem.TypeName.DontCare, "-", false, TypeSystem.TypeName.Real, "((op1) - (op2))", ""));
        resultTypeTable.add(new ResultTypeInfo(TypeSystem.TypeName.DontCare, TypeSystem.TypeName.Unknown, "-", false, TypeSystem.TypeName.Real, "((op1) - (op2))", ""));

        resultTypeTable.add(new ResultTypeInfo(TypeSystem.TypeName.Boolean, TypeSystem.TypeName.Boolean, "-", false, TypeSystem.TypeName.Byte, "((op1) - (op2))", ""));
        resultTypeTable.add(new ResultTypeInfo(TypeSystem.TypeName.Boolean, TypeSystem.TypeName.String, "-", false, TypeSystem.TypeName.Invalid, "", ""));       
        resultTypeTable.add(new ResultTypeInfo(TypeSystem.TypeName.Boolean, TypeSystem.TypeName.DontCare, "-", false, "#2", "((op1) - (op2))", ""));
        
        resultTypeTable.add(new ResultTypeInfo(TypeSystem.TypeName.Byte, TypeSystem.TypeName.Boolean, "-", false, TypeSystem.TypeName.Byte, "((op1) - (op2))", ""));
        resultTypeTable.add(new ResultTypeInfo(TypeSystem.TypeName.Byte, TypeSystem.TypeName.String, "-", false, TypeSystem.TypeName.Invalid, "", ""));       
        resultTypeTable.add(new ResultTypeInfo(TypeSystem.TypeName.Byte, TypeSystem.TypeName.DontCare, "-", false, "#2", "((op1) - (op2))", ""));
        
        resultTypeTable.add(new ResultTypeInfo(TypeSystem.TypeName.String, TypeSystem.TypeName.String, "-", false, TypeSystem.TypeName.String, "((op1) - (op2))", ""));
        resultTypeTable.add(new ResultTypeInfo(TypeSystem.TypeName.String, TypeSystem.TypeName.Date, "-", false, TypeSystem.TypeName.Date, "((op1) - (op2))", ""));
        resultTypeTable.add(new ResultTypeInfo(TypeSystem.TypeName.String, TypeSystem.TypeName.DontCare, "-", false, TypeSystem.TypeName.Invalid, "", ""));
        resultTypeTable.add(new ResultTypeInfo(TypeSystem.TypeName.DontCare, TypeSystem.TypeName.String, "-", false, TypeSystem.TypeName.Invalid, "", ""));
        
        resultTypeTable.add(new ResultTypeInfo(TypeSystem.TypeName.Integer, TypeSystem.TypeName.Boolean, "-", false, TypeSystem.TypeName.Integer, "((op1) - (op2))", ""));
        resultTypeTable.add(new ResultTypeInfo(TypeSystem.TypeName.Integer, TypeSystem.TypeName.Byte, "-", false, TypeSystem.TypeName.Integer, "((op1) - (op2))", ""));
        resultTypeTable.add(new ResultTypeInfo(TypeSystem.TypeName.Integer, TypeSystem.TypeName.String, "-", false, TypeSystem.TypeName.Invalid, "", ""));       
        resultTypeTable.add(new ResultTypeInfo(TypeSystem.TypeName.Integer, TypeSystem.TypeName.DontCare, "-", false, "#2", "((op1) - (op2))", ""));

        resultTypeTable.add(new ResultTypeInfo(TypeSystem.TypeName.Long, TypeSystem.TypeName.Boolean, "-", false, TypeSystem.TypeName.Long, "((op1) - (op2))", ""));
        resultTypeTable.add(new ResultTypeInfo(TypeSystem.TypeName.Long, TypeSystem.TypeName.Byte, "-", false, TypeSystem.TypeName.Long, "((op1) - (op2))", ""));
        resultTypeTable.add(new ResultTypeInfo(TypeSystem.TypeName.Long, TypeSystem.TypeName.Integer, "-", false, TypeSystem.TypeName.Long, "((op1) - (op2))", ""));
        resultTypeTable.add(new ResultTypeInfo(TypeSystem.TypeName.Long, TypeSystem.TypeName.String, "-", false, TypeSystem.TypeName.Invalid, "", ""));       
        resultTypeTable.add(new ResultTypeInfo(TypeSystem.TypeName.Long, TypeSystem.TypeName.DontCare, "+", false, "#2", "((op1) - (op2))", ""));

        resultTypeTable.add(new ResultTypeInfo(TypeSystem.TypeName.Real, TypeSystem.TypeName.String, "-", false, TypeSystem.TypeName.Invalid, "", ""));
        resultTypeTable.add(new ResultTypeInfo(TypeSystem.TypeName.Real, TypeSystem.TypeName.DontCare, "-", false, TypeSystem.TypeName.Real, "((op1) - (op2))", ""));
        resultTypeTable.add(new ResultTypeInfo(TypeSystem.TypeName.Real, TypeSystem.TypeName.Date, "-", false, TypeSystem.TypeName.Date, "((op1) - (op2))", ""));

        resultTypeTable.add(new ResultTypeInfo(TypeSystem.TypeName.Date, TypeSystem.TypeName.DontCare, "-", false, TypeSystem.TypeName.Date, "((op1) - (op2))", ""));
        resultTypeTable.add(new ResultTypeInfo(TypeSystem.TypeName.DontCare, TypeSystem.TypeName.Date, "-", false, TypeSystem.TypeName.Date, "((op1) - (op2))", ""));
        
        // Mul (*), it is symetric
        resultTypeTable.add(new ResultTypeInfo(TypeSystem.TypeName.Unknown, TypeSystem.TypeName.DontCare, "*", false, TypeSystem.TypeName.Real, "((op1) * (op2))", ""));
        resultTypeTable.add(new ResultTypeInfo(TypeSystem.TypeName.DontCare, TypeSystem.TypeName.Unknown, "*", false, TypeSystem.TypeName.Real, "((op1) * (op2))", ""));

        resultTypeTable.add(new ResultTypeInfo(TypeSystem.TypeName.Boolean, TypeSystem.TypeName.Boolean, "*", false, TypeSystem.TypeName.Byte, "((op1) * (op2))", ""));
        resultTypeTable.add(new ResultTypeInfo(TypeSystem.TypeName.Boolean, TypeSystem.TypeName.String, "*", false, TypeSystem.TypeName.Invalid, "", ""));       
        resultTypeTable.add(new ResultTypeInfo(TypeSystem.TypeName.Boolean, TypeSystem.TypeName.DontCare, "*", false, "#2", "((op1) * (op2))", ""));
        
        resultTypeTable.add(new ResultTypeInfo(TypeSystem.TypeName.Byte, TypeSystem.TypeName.Boolean, "*", false, TypeSystem.TypeName.Byte, "((op1) * (op2))", ""));
        resultTypeTable.add(new ResultTypeInfo(TypeSystem.TypeName.Byte, TypeSystem.TypeName.String, "*", false, TypeSystem.TypeName.Invalid, "", ""));       
        resultTypeTable.add(new ResultTypeInfo(TypeSystem.TypeName.Byte, TypeSystem.TypeName.DontCare, "*", false, "#2", "((op1) * (op2))", ""));
        
        resultTypeTable.add(new ResultTypeInfo(TypeSystem.TypeName.String, TypeSystem.TypeName.DontCare, "*", false, TypeSystem.TypeName.Invalid, "", ""));
        resultTypeTable.add(new ResultTypeInfo(TypeSystem.TypeName.DontCare, TypeSystem.TypeName.String, "*", false, TypeSystem.TypeName.Invalid, "", ""));
        
        resultTypeTable.add(new ResultTypeInfo(TypeSystem.TypeName.Integer, TypeSystem.TypeName.Boolean, "*", false, TypeSystem.TypeName.Integer, "((op1) * (op2))", ""));
        resultTypeTable.add(new ResultTypeInfo(TypeSystem.TypeName.Integer, TypeSystem.TypeName.Byte, "*", false, TypeSystem.TypeName.Long, "((op1) * (op2))", ""));
        //resultTypeTable.add(new ResultTypeInfo(TypeSystem.TypeName.Integer, TypeSystem.TypeName.String, "*", false, TypeSystem.TypeName.Invalid, "", ""));       
        resultTypeTable.add(new ResultTypeInfo(TypeSystem.TypeName.Integer, TypeSystem.TypeName.Integer, "*", false, TypeSystem.TypeName.Long, "((op1) * (op2))", ""));
        resultTypeTable.add(new ResultTypeInfo(TypeSystem.TypeName.Integer, TypeSystem.TypeName.Long, "*", false, TypeSystem.TypeName.Long, "((op1) * (op2))", ""));
        resultTypeTable.add(new ResultTypeInfo(TypeSystem.TypeName.Integer, TypeSystem.TypeName.Real, "*", false, TypeSystem.TypeName.Real, "((op1) * (op2))", ""));
        //resultTypeTable.add(new ResultTypeInfo(TypeSystem.TypeName.Integer, TypeSystem.TypeName.Date, "*", false, TypeSystem.TypeName.Invalid, "", ""));

        resultTypeTable.add(new ResultTypeInfo(TypeSystem.TypeName.Long, TypeSystem.TypeName.Boolean, "*", false, TypeSystem.TypeName.Long, "((op1) * (op2))", ""));
        resultTypeTable.add(new ResultTypeInfo(TypeSystem.TypeName.Long, TypeSystem.TypeName.Byte, "*", false, TypeSystem.TypeName.Long, "((op1) * (op2))", ""));
        //resultTypeTable.add(new ResultTypeInfo(TypeSystem.TypeName.Long, TypeSystem.TypeName.String, "*", false, TypeSystem.TypeName.Invalid, "", ""));       
        resultTypeTable.add(new ResultTypeInfo(TypeSystem.TypeName.Long, TypeSystem.TypeName.Integer, "*", false, TypeSystem.TypeName.Long, "((op1) * (op2))", ""));
        resultTypeTable.add(new ResultTypeInfo(TypeSystem.TypeName.Long, TypeSystem.TypeName.Long, "*", false, TypeSystem.TypeName.Long, "((op1) * (op2))", ""));
        resultTypeTable.add(new ResultTypeInfo(TypeSystem.TypeName.Long, TypeSystem.TypeName.Real, "*", false, TypeSystem.TypeName.Real, "((op1) * (op2))", ""));
        //resultTypeTable.add(new ResultTypeInfo(TypeSystem.TypeName.Long, TypeSystem.TypeName.Date, "*", false, TypeSystem.TypeName.Invalid, "", ""));

        resultTypeTable.add(new ResultTypeInfo(TypeSystem.TypeName.Real, TypeSystem.TypeName.String, "*", false, TypeSystem.TypeName.Invalid, "", ""));
        resultTypeTable.add(new ResultTypeInfo(TypeSystem.TypeName.Real, TypeSystem.TypeName.Date, "*", false, TypeSystem.TypeName.Invalid, "", ""));
        resultTypeTable.add(new ResultTypeInfo(TypeSystem.TypeName.Real, TypeSystem.TypeName.DontCare, "*", false, TypeSystem.TypeName.Real, "((op1) * (op2))", ""));

        resultTypeTable.add(new ResultTypeInfo(TypeSystem.TypeName.Date, TypeSystem.TypeName.DontCare, "*", false, TypeSystem.TypeName.Invalid, "", ""));
        resultTypeTable.add(new ResultTypeInfo(TypeSystem.TypeName.DontCare, TypeSystem.TypeName.Date, "*", false, TypeSystem.TypeName.Invalid, "", ""));
        
        // Div (/), it is symetric
        resultTypeTable.add(new ResultTypeInfo(TypeSystem.TypeName.Unknown, TypeSystem.TypeName.DontCare, "/", false, TypeSystem.TypeName.Real, "((op1) / (op2))", ""));
        resultTypeTable.add(new ResultTypeInfo(TypeSystem.TypeName.DontCare, TypeSystem.TypeName.Unknown, "/", false, TypeSystem.TypeName.Real, "((op1) / (op2))", ""));

        resultTypeTable.add(new ResultTypeInfo(TypeSystem.TypeName.Boolean, TypeSystem.TypeName.Boolean, "/", false, TypeSystem.TypeName.Byte, "((op1) / (op2))", ""));
        resultTypeTable.add(new ResultTypeInfo(TypeSystem.TypeName.Boolean, TypeSystem.TypeName.String, "/", false, TypeSystem.TypeName.Invalid, "", ""));       
        resultTypeTable.add(new ResultTypeInfo(TypeSystem.TypeName.Boolean, TypeSystem.TypeName.Date, "/", false, TypeSystem.TypeName.Invalid, "", ""));       
        resultTypeTable.add(new ResultTypeInfo(TypeSystem.TypeName.Boolean, TypeSystem.TypeName.DontCare, "/", false, TypeSystem.TypeName.Real, "((op1) / (op2))", ""));
        
        resultTypeTable.add(new ResultTypeInfo(TypeSystem.TypeName.Byte, TypeSystem.TypeName.Boolean, "/", false, TypeSystem.TypeName.Byte, "((op1) / (op2))", ""));
        resultTypeTable.add(new ResultTypeInfo(TypeSystem.TypeName.Byte, TypeSystem.TypeName.String, "/", false, TypeSystem.TypeName.Invalid, "", ""));       
        resultTypeTable.add(new ResultTypeInfo(TypeSystem.TypeName.Byte, TypeSystem.TypeName.Date, "/", false, TypeSystem.TypeName.Invalid, "", ""));       
        resultTypeTable.add(new ResultTypeInfo(TypeSystem.TypeName.Byte, TypeSystem.TypeName.DontCare, "/", false, TypeSystem.TypeName.Real, "((op1) / (op2))", ""));
        
        resultTypeTable.add(new ResultTypeInfo(TypeSystem.TypeName.String, TypeSystem.TypeName.DontCare, "/", false, TypeSystem.TypeName.Invalid, "", ""));
        resultTypeTable.add(new ResultTypeInfo(TypeSystem.TypeName.DontCare, TypeSystem.TypeName.String, "/", false, TypeSystem.TypeName.Invalid, "", ""));
        
        resultTypeTable.add(new ResultTypeInfo(TypeSystem.TypeName.Integer, TypeSystem.TypeName.Byte, "/", false, TypeSystem.TypeName.Real, "((op1) / (op2))", ""));
        resultTypeTable.add(new ResultTypeInfo(TypeSystem.TypeName.Integer, TypeSystem.TypeName.Integer, "/", false, TypeSystem.TypeName.Real, "((op1) / (op2))", ""));
        resultTypeTable.add(new ResultTypeInfo(TypeSystem.TypeName.Integer, TypeSystem.TypeName.Long, "/", false, TypeSystem.TypeName.Real, "((op1) / (op2))", ""));
        resultTypeTable.add(new ResultTypeInfo(TypeSystem.TypeName.Integer, TypeSystem.TypeName.Real, "/", false, TypeSystem.TypeName.Real, "((op1) / (op2))", ""));

        resultTypeTable.add(new ResultTypeInfo(TypeSystem.TypeName.Long, TypeSystem.TypeName.Byte, "/", false, TypeSystem.TypeName.Real, "((op1) / (op2))", ""));
        resultTypeTable.add(new ResultTypeInfo(TypeSystem.TypeName.Long, TypeSystem.TypeName.Integer, "/", false, TypeSystem.TypeName.Real, "((op1) / (op2))", ""));
        resultTypeTable.add(new ResultTypeInfo(TypeSystem.TypeName.Long, TypeSystem.TypeName.Long, "/", false, TypeSystem.TypeName.Real, "((op1) / (op2))", ""));
        resultTypeTable.add(new ResultTypeInfo(TypeSystem.TypeName.Long, TypeSystem.TypeName.Real, "/", false, TypeSystem.TypeName.Real, "((op1) / (op2))", ""));

        resultTypeTable.add(new ResultTypeInfo(TypeSystem.TypeName.Real, TypeSystem.TypeName.Byte, "/", false, TypeSystem.TypeName.Real, "((op1) / (op2))", ""));
        resultTypeTable.add(new ResultTypeInfo(TypeSystem.TypeName.Real, TypeSystem.TypeName.Integer, "/", false, TypeSystem.TypeName.Real, "((op1) / (op2))", ""));
        resultTypeTable.add(new ResultTypeInfo(TypeSystem.TypeName.Real, TypeSystem.TypeName.Long, "/", false, TypeSystem.TypeName.Real, "((op1) / (op2))", ""));
        resultTypeTable.add(new ResultTypeInfo(TypeSystem.TypeName.Real, TypeSystem.TypeName.Real, "/", false, TypeSystem.TypeName.Real, "((op1) / (op2))", ""));

        resultTypeTable.add(new ResultTypeInfo(TypeSystem.TypeName.Date, TypeSystem.TypeName.DontCare, "/", false, TypeSystem.TypeName.Invalid, "", ""));
        resultTypeTable.add(new ResultTypeInfo(TypeSystem.TypeName.DontCare, TypeSystem.TypeName.Date, "/", false, TypeSystem.TypeName.Invalid, "", ""));
        
        // Mod (%), it is NOT symetric
        resultTypeTable.add(new ResultTypeInfo(TypeSystem.TypeName.Unknown, TypeSystem.TypeName.DontCare, "%", false, TypeSystem.TypeName.Real, "((op1) % (op2))", ""));
        resultTypeTable.add(new ResultTypeInfo(TypeSystem.TypeName.DontCare, TypeSystem.TypeName.Unknown, "%", false, TypeSystem.TypeName.Real, "((op1) % (op2))", ""));

        resultTypeTable.add(new ResultTypeInfo(TypeSystem.TypeName.Boolean, TypeSystem.TypeName.Boolean, "%", false, TypeSystem.TypeName.Byte, "((op1) % (op2))", ""));
        resultTypeTable.add(new ResultTypeInfo(TypeSystem.TypeName.Boolean, TypeSystem.TypeName.String, "%", false, TypeSystem.TypeName.Invalid, "", ""));       
        resultTypeTable.add(new ResultTypeInfo(TypeSystem.TypeName.Boolean, TypeSystem.TypeName.Date, "%", false, TypeSystem.TypeName.Invalid, "", ""));   
        resultTypeTable.add(new ResultTypeInfo(TypeSystem.TypeName.Boolean, TypeSystem.TypeName.Real, "%", false, TypeSystem.TypeName.Long, "((op1) % (op2))", ""));        
        resultTypeTable.add(new ResultTypeInfo(TypeSystem.TypeName.Boolean, TypeSystem.TypeName.DontCare, "%", false, "#2", "((op1) % (op2))", ""));
        
        resultTypeTable.add(new ResultTypeInfo(TypeSystem.TypeName.Byte, TypeSystem.TypeName.Boolean, "%", false, TypeSystem.TypeName.Byte, "((op1) % (op2))", ""));
        resultTypeTable.add(new ResultTypeInfo(TypeSystem.TypeName.Byte, TypeSystem.TypeName.String, "%", false, TypeSystem.TypeName.Invalid, "", ""));       
        resultTypeTable.add(new ResultTypeInfo(TypeSystem.TypeName.Byte, TypeSystem.TypeName.Date, "%", false, TypeSystem.TypeName.Invalid, "", ""));       
        resultTypeTable.add(new ResultTypeInfo(TypeSystem.TypeName.Byte, TypeSystem.TypeName.Real, "%", false, TypeSystem.TypeName.Long, "((op1) % (op2))", ""));        
        resultTypeTable.add(new ResultTypeInfo(TypeSystem.TypeName.Byte, TypeSystem.TypeName.DontCare, "%", false, "#2", "((op1) % (op2))", ""));
        
        resultTypeTable.add(new ResultTypeInfo(TypeSystem.TypeName.String, TypeSystem.TypeName.DontCare, "%", false, TypeSystem.TypeName.Invalid, "", ""));
        resultTypeTable.add(new ResultTypeInfo(TypeSystem.TypeName.DontCare, TypeSystem.TypeName.String, "%", false, TypeSystem.TypeName.Invalid, "", ""));
        
        resultTypeTable.add(new ResultTypeInfo(TypeSystem.TypeName.Integer, TypeSystem.TypeName.Boolean, "%", false, TypeSystem.TypeName.Byte, "((op1) % (op2))", ""));
        resultTypeTable.add(new ResultTypeInfo(TypeSystem.TypeName.Integer, TypeSystem.TypeName.String, "%", false, TypeSystem.TypeName.Invalid, "", ""));       
        resultTypeTable.add(new ResultTypeInfo(TypeSystem.TypeName.Integer, TypeSystem.TypeName.Date, "%", false, TypeSystem.TypeName.Invalid, "", ""));       
        resultTypeTable.add(new ResultTypeInfo(TypeSystem.TypeName.Integer, TypeSystem.TypeName.Real, "%", false, TypeSystem.TypeName.Long, "((op1) % (op2))", ""));        
        resultTypeTable.add(new ResultTypeInfo(TypeSystem.TypeName.Integer, TypeSystem.TypeName.DontCare, "%", false, "#2", "((op1) % (op2))", ""));

        resultTypeTable.add(new ResultTypeInfo(TypeSystem.TypeName.Long, TypeSystem.TypeName.Boolean, "%", false, TypeSystem.TypeName.Byte, "((op1) % (op2))", ""));
        resultTypeTable.add(new ResultTypeInfo(TypeSystem.TypeName.Long, TypeSystem.TypeName.String, "%", false, TypeSystem.TypeName.Invalid, "", ""));       
        resultTypeTable.add(new ResultTypeInfo(TypeSystem.TypeName.Long, TypeSystem.TypeName.Date, "%", false, TypeSystem.TypeName.Invalid, "", ""));       
        resultTypeTable.add(new ResultTypeInfo(TypeSystem.TypeName.Long, TypeSystem.TypeName.Real, "%", false, TypeSystem.TypeName.Long, "((op1) % (op2))", ""));        
        resultTypeTable.add(new ResultTypeInfo(TypeSystem.TypeName.Long, TypeSystem.TypeName.DontCare, "%", false, "#2", "((op1) % (op2))", ""));

        resultTypeTable.add(new ResultTypeInfo(TypeSystem.TypeName.Real, TypeSystem.TypeName.Boolean, "%", false, TypeSystem.TypeName.Byte, "((op1) % (op2))", ""));
        resultTypeTable.add(new ResultTypeInfo(TypeSystem.TypeName.Real, TypeSystem.TypeName.String, "%", false, TypeSystem.TypeName.Invalid, "", ""));       
        resultTypeTable.add(new ResultTypeInfo(TypeSystem.TypeName.Real, TypeSystem.TypeName.Date, "%", false, TypeSystem.TypeName.Invalid, "", ""));       
        resultTypeTable.add(new ResultTypeInfo(TypeSystem.TypeName.Real, TypeSystem.TypeName.Real, "%", false, TypeSystem.TypeName.Long, "((op1) % (op2))", ""));        
        resultTypeTable.add(new ResultTypeInfo(TypeSystem.TypeName.Real, TypeSystem.TypeName.DontCare, "%", false, "#2", "((op1) % (op2))", ""));

        resultTypeTable.add(new ResultTypeInfo(TypeSystem.TypeName.Date, TypeSystem.TypeName.DontCare, "%", false, TypeSystem.TypeName.Invalid, "", ""));
        resultTypeTable.add(new ResultTypeInfo(TypeSystem.TypeName.DontCare, TypeSystem.TypeName.Date, "%", false, TypeSystem.TypeName.Invalid, "", ""));
        
        // Binary compariosn operators
        resultTypeTable.add(new ResultTypeInfo(TypeSystem.TypeName.DontCare, TypeSystem.TypeName.DontCare, "eq", false, TypeSystem.TypeName.Boolean, "((op1) == (op2))", ""));
        resultTypeTable.add(new ResultTypeInfo(TypeSystem.TypeName.DontCare, TypeSystem.TypeName.DontCare, "noteq", false, TypeSystem.TypeName.Boolean, "((op1) != (op2))", ""));
        resultTypeTable.add(new ResultTypeInfo(TypeSystem.TypeName.DontCare, TypeSystem.TypeName.DontCare, "gt", false, TypeSystem.TypeName.Boolean, "((op1) > (op2))", ""));
        resultTypeTable.add(new ResultTypeInfo(TypeSystem.TypeName.DontCare, TypeSystem.TypeName.DontCare, "gteq", false, TypeSystem.TypeName.Boolean, "((op1) >= (op2))", ""));
        resultTypeTable.add(new ResultTypeInfo(TypeSystem.TypeName.DontCare, TypeSystem.TypeName.DontCare, "lt", false, TypeSystem.TypeName.Boolean, "((op1) < (op2))", ""));
        resultTypeTable.add(new ResultTypeInfo(TypeSystem.TypeName.DontCare, TypeSystem.TypeName.DontCare, "lteq", false, TypeSystem.TypeName.Boolean, "((op1) <= (op2))", ""));
        resultTypeTable.add(new ResultTypeInfo(TypeSystem.TypeName.DontCare, TypeSystem.TypeName.DontCare, "like", false, TypeSystem.TypeName.Boolean, "((op1) ~ (op2))", ""));

        // unary comaprison operators
        resultTypeTable.add(new ResultTypeInfo(TypeSystem.TypeName.DontCare, TypeSystem.TypeName.DontCare, "isnull", false, TypeSystem.TypeName.Boolean, "((op1) == null)", ""));
        resultTypeTable.add(new ResultTypeInfo(TypeSystem.TypeName.DontCare, TypeSystem.TypeName.DontCare, "isnotnull", false, TypeSystem.TypeName.Boolean, "((op1) != null)", ""));

        resultTypeTable.add(new ResultTypeInfo(TypeSystem.TypeName.DontCare, TypeSystem.TypeName.DontCare, "isnumber", false, TypeSystem.TypeName.Boolean, "", ""));
        resultTypeTable.add(new ResultTypeInfo(TypeSystem.TypeName.DontCare, TypeSystem.TypeName.DontCare, "isnotnumber", false, TypeSystem.TypeName.Boolean, "", ""));
        
        resultTypeTable.add(new ResultTypeInfo(TypeSystem.TypeName.DontCare, TypeSystem.TypeName.DontCare, "isdate", false, TypeSystem.TypeName.Boolean, "", ""));
        resultTypeTable.add(new ResultTypeInfo(TypeSystem.TypeName.DontCare, TypeSystem.TypeName.DontCare, "isnotdate", false, TypeSystem.TypeName.Boolean, "", ""));

        resultTypeTable.add(new ResultTypeInfo(TypeSystem.TypeName.DontCare, TypeSystem.TypeName.DontCare, "isempty", false, TypeSystem.TypeName.Boolean, "", ""));
        resultTypeTable.add(new ResultTypeInfo(TypeSystem.TypeName.DontCare, TypeSystem.TypeName.DontCare, "isnotempty", false, TypeSystem.TypeName.Boolean, "", ""));
        
        resultTypeTable.add(new ResultTypeInfo(TypeSystem.TypeName.DontCare, TypeSystem.TypeName.DontCare, "not", false, TypeSystem.TypeName.Boolean, "(!(op1))", ""));
        resultTypeTable.add(new ResultTypeInfo(TypeSystem.TypeName.DontCare, TypeSystem.TypeName.DontCare, "and", false, TypeSystem.TypeName.Boolean, "((op1) || (op2))", ""));
        resultTypeTable.add(new ResultTypeInfo(TypeSystem.TypeName.DontCare, TypeSystem.TypeName.DontCare, "or", false, TypeSystem.TypeName.Boolean, "((op1) && (op2))", ""));
        
    }
    
}
