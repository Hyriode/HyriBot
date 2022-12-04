package fr.hyriode.hyribot.utils;

import com.google.common.reflect.ClassPath;

import java.io.IOException;
import java.util.HashSet;
import java.util.Set;

public class PackageScanner {

    public static <T> Set<Class<?>> scan(Class<?> clazz, String packageName) {
        try {
            final Set<Class<?>> classes = new HashSet<>();
            final ClassPath classPath = ClassPath.from(clazz.getClassLoader());

            for(ClassPath.ClassInfo classInfo : classPath.getTopLevelClassesRecursive(packageName)) {
                classes.add(Class.forName(classInfo.getName()));
            }

            return classes;
        } catch (IOException | ClassNotFoundException e) {
            e.printStackTrace();
        }
        return null;
    }
}
