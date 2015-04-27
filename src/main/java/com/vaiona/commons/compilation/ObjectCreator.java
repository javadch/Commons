package com.vaiona.commons.compilation;

import com.vaiona.commons.logging.LoggerHelper;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.text.MessageFormat;

/**
 *
 * @author standard
 */
public class ObjectCreator {
//    // does not work with my own class compiler. the classes are not registered with the default class loader!
    public static Object load(Class clazz){
        LoggerHelper.logError(MessageFormat.format("Call to method {0} is not allowed. Call to load the class: {1} from {2}, then {3}", "ObjectCreator.load", clazz.getName()
        ,Thread.currentThread().getStackTrace()[2].getClassName() + Thread.currentThread().getStackTrace()[2].getMethodName()
        , Thread.currentThread().getStackTrace()[3].getClassName() + Thread.currentThread().getStackTrace()[3].getMethodName()
        ));
        return null;
    }
    public static Object load(String fullClassName, Object[] ctorArgs) throws ClassNotFoundException, InstantiationException, IllegalAccessException, IllegalArgumentException, NoSuchMethodException, InvocationTargetException{        
        // ClassLoader cl2 = ClassLoader.getSystemClassLoader();
        // also using the fileManager.getClassLoader(null).loadClass(name) should return the class!
        LoggerHelper.logError(MessageFormat.format("Call to method {0} is not allowed. Call to load the class: {1}", "ObjectCreator.load", fullClassName));
        
        Class cls = Class.forName(fullClassName);// cl2.loadClass(packageName + "." + className); // the exception should not happen
        Constructor<?> c = cls.getConstructor(String.class, boolean.class, String.class);
        // c = cls.getDeclaredConstructor(
        // String.class.getClass(), boolean.class.getClass(), String.class.getClass());
        c.setAccessible(true);
        Object instance = c.newInstance(ctorArgs); // pass parameters
       return instance;
    }

    public static ClassLoader getURLClassLoader(String urlString) throws Exception {
        try{
            ClassLoader classLoader = new URLClassLoader(new URL[]{new URL(urlString)});
            return classLoader;
        } catch (MalformedURLException ex){
            throw new Exception(MessageFormat.format("Can not get the class {0} from the specified class loader {1}.", urlString));
        }
    }
    
    public static ClassLoader getURLClassLoader(URL url) {
        ClassLoader classLoader = new URLClassLoader(new URL[]{url});
        return classLoader;
    }

    public static Class getClass(String className, ClassLoader classLoader) throws Exception {
        try{
            Class claz = classLoader.loadClass(className);
        return claz;
        } catch(ClassNotFoundException ex){
            throw new Exception(MessageFormat.format("Can not get the class {0} from the specified class loader {1}.", className));
        }
    }
    public static Object createInstance(Class classObject) throws Exception {        
        try{
            Constructor<?> c = classObject.getConstructor(); // parameterless ctor
            c.setAccessible(true);
            Object instance = c.newInstance();
            return instance;
        } catch (NoSuchMethodException | InstantiationException | IllegalAccessException | IllegalArgumentException | InvocationTargetException ex){
            throw new Exception(MessageFormat.format("Can not get an instance of the class {0}.", classObject.getName()));
        }
    }
}
