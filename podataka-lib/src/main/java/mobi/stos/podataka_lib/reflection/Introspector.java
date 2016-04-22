package mobi.stos.podataka_lib.reflection;

import java.util.List;

import mobi.stos.podataka_lib.exception.IntrospectionException;

public class Introspector {

    public static String decapitalize(String name) {
        if (name == null || "".equals(name)) {
            return null;
        }
        if (name.length() > 1 && Character.isUpperCase(name.charAt(1)) && Character.isUpperCase(name.charAt(0))) {
            return name;
        }
        return name.substring(0,1).toLowerCase() + name.substring(1);
    }

    public static BeanInfo getBeanInfo(Class<? extends Object> clazz) throws IntrospectionException {
        BeanInfo beanInfo = new BeanInfo();

        List<PropertyDescriptor> propertyDescriptors = PropertyUtils.getPropertyDescriptors(clazz);
        PropertyDescriptor[] descriptors = new PropertyDescriptor[propertyDescriptors.size()];
        descriptors = propertyDescriptors.toArray(descriptors);

        beanInfo.setPropertyDescriptors(descriptors);
        return beanInfo;
    }
}
