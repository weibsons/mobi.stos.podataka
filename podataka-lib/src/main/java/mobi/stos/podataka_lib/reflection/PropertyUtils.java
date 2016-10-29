package mobi.stos.podataka_lib.reflection;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

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
    private Map<String, Method> accessors(Class clazz) {
        Map<String, Method> retval = new HashMap<>();
        for (Method method : clazz.getMethods()) {
            if (!method.getName().endsWith("Class") && !(method.getName().length() == 11 && method.getName().endsWith("Property"))) {
                if (method.getName().startsWith("is") || method.getName().startsWith("get") || method.getName().startsWith("set")) {
                    retval.put(method.getName(), method);
                }
            }
        }
        return retval;
    }

    private void initHashMap() {
        Map<String, Method> methods = accessors(this.target.getClass());
        for (Map.Entry<String, Method> map : methods.entrySet()) {
            PropertyDescriptor descriptor;
            String name;
            if (map.getKey().startsWith("is")) {
                name = map.getKey().substring(2);
            } else {
                name = map.getKey().substring(3);
            }
            name = name.toLowerCase();

            if (!hashMap.containsKey(name)) {
                descriptor = new PropertyDescriptor();
                descriptor.setName(name);
            } else {
                descriptor = hashMap.get(name);
            }
            if (map.getKey().startsWith("is") || map.getKey().startsWith("get")) {
                descriptor.setReadMethod(map.getValue());
            } else {
                descriptor.setWriteMethod(map.getValue());
            }
            hashMap.put(name, descriptor);
        }
    }

}
