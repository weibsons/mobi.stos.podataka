package mobi.stos.podataka_lib.reflection;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class PropertyUtils {


    public static boolean isWriteable(Object target, String name) {
        PropertyDescriptor descriptor = getPropertyDescriptor(target, name);
        boolean retval = descriptor.getWriteMethod() != null;
        return retval;
    }

    public static Class getPropertyType(Object target, String name) {
        PropertyDescriptor descriptor = getPropertyDescriptor(target, name);
        Class retval = descriptor.getWriteMethod().getParameterTypes()[0];
        return retval;
    }

    public static void setProperty(Object target, String name, Object value) throws IllegalAccessException, InvocationTargetException, NoSuchMethodException {
        PropertyDescriptor descriptor = getPropertyDescriptor(target, name);
        descriptor.getWriteMethod().invoke(target, new Object[]{value});
    }

    public static PropertyDescriptor getPropertyDescriptor(Object target, String name) {
        List<PropertyDescriptor> descriptors = getPropertyDescriptors(target.getClass());
        for (PropertyDescriptor descriptor : descriptors) {
            if (descriptor.getName().toLowerCase().equals(name.toLowerCase())) {
                //logger.debug("Returning property descriptor " + descriptor);
                return descriptor;
            }
        }
        return null;
    }

    public static List<PropertyDescriptor> getPropertyDescriptors(Class<? extends Object> clazz) {
        List<Method> methods = accessors(clazz);
        List<PropertyDescriptor> propertyDescriptors = new ArrayList<>();
        for (Method method : methods) {
            Method readMethod;
            String property;

            if (method.getName().startsWith("is")) {
                readMethod = getMethodWithPrefix(methods, "is", method.getName().substring(2));
                property = method.getName().substring(2);
            } else {
                readMethod = getMethodWithPrefix(methods, "get", method.getName().substring(3));
                property = method.getName().substring(3);
            }
            Method writeMethod = getMethodWithPrefix(methods, "set", property);

            propertyDescriptors.add(new PropertyDescriptor(property, readMethod, writeMethod));
        }
        Collections.sort(propertyDescriptors, new Comparator<PropertyDescriptor>() {

            @Override
            public int compare(PropertyDescriptor t, PropertyDescriptor t1) {
                return t.getName().compareTo(t1.getName());
            }

        });
        return propertyDescriptors;
    }

    public static boolean isReadable(Object bean, String propertyName) {
        return getPropertyDescriptor(bean, propertyName).getReadMethod() != null;
    }

    public static Object getProperty(Object bean, String propertyName) throws IllegalAccessException, InvocationTargetException, NoSuchMethodException {
        return getPropertyDescriptor(bean, propertyName).getReadMethod().invoke(bean, new Object[]{});
    }

    /**
     * Return a list of methods from the class which have the specified prefix.
     */
    @SuppressWarnings("unchecked")
    private static List<Method> accessors(Class clazz) {
        List<Method> retval = new ArrayList<>();
        for (Method method : clazz.getMethods()) {
            if (!method.getName().endsWith("Class") && !(method.getName().length() == 11 && method.getName().endsWith("Property"))) {
                if (method.getName().startsWith("is") || method.getName().startsWith("get") || method.getName().startsWith("set")) {
                    retval.add(method);
                }
            }
        }
        return retval;
    }

    private static Method getMethodWithPrefix(List<Method> methods, String prefix, String name) {
        for (Method method : methods) {
            if (method.getName().toLowerCase().equals((prefix + name).toLowerCase())) {
                return method;
            }
        }
        return null;
    }
}
