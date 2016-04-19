package mobi.stos.podataka_lib.reflection;

public class BeanInfo {

    private PropertyDescriptor[] propertyDescriptors;

    public void setPropertyDescriptors(PropertyDescriptor[] propertyDescriptors) {
        this.propertyDescriptors = propertyDescriptors;
    }

    public PropertyDescriptor[] getPropertyDescriptors() {
        return this.propertyDescriptors;
    }
}
