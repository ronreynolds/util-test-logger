package com.ronreynolds.util.classes;

import java.lang.reflect.Field;
import java.net.URL;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * useful for finding info about classes (class-file location, fields, etc)
 */
public class ClassInfo {
    // added to try to debug issues finding a MDC class with an mdcAdapter field :-/
    public static String findClass(Class<?> classToFind) {
        ClassLoader loader = classToFind.getClassLoader();
        if (loader == null) {
            loader = ClassLoader.getSystemClassLoader();
        }
        URL classFileUrl = loader.getResource(classToFind.getName().replace('.', '/').concat(".class"));
        return classFileUrl != null ? classFileUrl.toExternalForm() : classToFind + " not found";
    }

    public static CharSequence getClassInfo(Class<?> classToFind) {
        StringBuilder classInfo = new StringBuilder();
        classInfo.append("\nname:").append(classToFind.getName())
                 .append("\nloc:").append(findClass(classToFind))
                 .append("\nfields:")
                 .append(Stream.of(classToFind.getFields()).map(Field::getName).collect(Collectors.joining(",")))
                 .append("\ndec-fields:")
                 .append(Stream.of(classToFind.getDeclaredFields()).map(Field::getName).collect(Collectors.joining(",")));
        return classInfo;
    }
}
