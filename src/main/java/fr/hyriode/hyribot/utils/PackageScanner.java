package fr.hyriode.hyribot.utils;

import com.google.common.reflect.ClassPath;

import java.util.*;
import java.util.function.Consumer;
import java.util.function.Function;

public class PackageScanner {

    @SuppressWarnings("unchecked")
    public static <T> Set<T> scan(Class<T> clazz, String packageName, Function<Class<?>, ?> filter) {
        final Set<T> classes = new HashSet<>();
        try {
            final ClassPath classPath = ClassPath.from(clazz.getClassLoader());

            for(ClassPath.ClassInfo classInfo : classPath.getTopLevelClassesRecursive(packageName)) {
                Class<?> c = classInfo.load();

                if(clazz.isAssignableFrom(c)) {
                    T clazzInstance = (T) filter.apply(c);

                    if(clazzInstance != null) {
                        classes.add(clazzInstance);
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return classes;
    }

    public static class FilterClass {

        private final List<Class<?>> clazzs = new ArrayList<>();
        private final List<Object> objects = new ArrayList<>();

        public FilterClass(Class<?>... clazz) {
            this.clazzs.addAll(List.of(clazz));
        }

        public FilterClass instance(Object... obj) {
            this.objects.addAll(List.of(obj));
            return this;
        }

        public Class<?>[] getClasss() {
            return this.clazzs.toArray(new Class[0]);
        }

        public List<Object> getObjects() {
            return this.objects;
        }

        public static FilterClass constructor(Class<?>... clazz) {
            return new FilterClass(clazz);
        }

    }
}
