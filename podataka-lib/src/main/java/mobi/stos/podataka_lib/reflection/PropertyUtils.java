package mobi.stos.podataka_lib.reflection;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class PropertyUtils {

    private final Object target;
    private HashMap<String, PropertyDescriptor> hashMap;

    public PropertyUtils(Object target) {
        this.hashMap = new HashMap<>();
        this.target = target;

        initHashMap();
    }

    public Class getPropertyType(String name) {
        return hashMap.get(name.toLowerCase()).getWriteMethod().getParameterTypes()[0];
    }

    public void setProperty(String name, Object value) throws IllegalAccessException, InvocationTargetException, NoSuchMethodException {
        hashMap.get(name.toLowerCase()).getWriteMethod().invoke(target, new Object[]{value});
    }

    public PropertyDescriptor getPropertyDescriptor(String name) {
        return hashMap.get(name.toLowerCase());
    }

    public boolean exists(String propertyName) {
        return hashMap.containsKey(propertyName.toLowerCase()) && isReadable(propertyName);
    }

    public boolean isReadable(String propertyName) {
        return hashMap.get(propertyName.toLowerCase()).getReadMethod() != null;
    }

    public boolean isWriteable(String name) {
        return hashMap.get(name.toLowerCase()).getWriteMethod() != null;
    }

    public Object getProperty(String propertyName) throws IllegalAccessException, InvocationTargetException, NoSuchMethodException {
        return hashMap.get(propertyName.toLowerCase()).getReadMethod().invoke(this.target, new Object[]{});
    }

    /**
     * Return a list of methods from the class which have the specified prefix.
     */
    @SuppressWarnings("unchecked")
    private List<Method> accessors(Class clazz) {
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

    private void initHashMap() {
        List<Method> methods = accessors(this.target.getClass());
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

            PropertyDescriptor descriptor = new PropertyDescriptor(property, readMethod, writeMethod);
            hashMap.put(descriptor.getName().toLowerCase(), descriptor);
        }
    }

    private Method getMethodWithPrefix(List<Method> methods, String prefix, String name) {
        for (Method method : methods) {
            if (method.getName().toLowerCase().equals((prefix + name).toLowerCase())) {
                return method;
            }
        }
        return null;
    }
}
