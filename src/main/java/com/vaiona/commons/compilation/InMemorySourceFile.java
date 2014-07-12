package com.vaiona.commons.compilation;

import java.net.URI;
import javax.tools.SimpleJavaFileObject;

public class InMemorySourceFile extends SimpleJavaFileObject {

    private final CharSequence content;
    private Class compiledClass;
    private boolean isEntryPoint = false;
    private String fullName;

    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    public boolean isEntryPoint() {
        return isEntryPoint;
    }

    public void setEntryPoint(boolean value) {
        this.isEntryPoint = value;
    }
    

    public Class getCompiledClass() {
        return compiledClass;
    }

    public void setCompiledClass(Class compiledClass) {
        this.compiledClass = compiledClass;
    }

    public InMemorySourceFile(String className, CharSequence content) {
        super(URI.create("string:///" + className.replace('.', '/')
            + Kind.SOURCE.extension), Kind.SOURCE);
        this.content = content;
    }

    @Override
    public CharSequence getCharContent(boolean ignoreEncodingErrors) {
        return content;
    }
    

}