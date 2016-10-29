package mobi.stos.podataka_lib.reflection;

import java.lang.reflect.Method;

public class PropertyDescriptor {

    private String name;
    private Method readMethod;
    private Method writeMethod;

    public PropertyDescriptor() {
    }

    public PropertyDescriptor(String name, Method read, Method write) {
        this.name = name.toLowerCase();
        this.readMethod = read;
        this.writeMethod = write;
    }

    public Method getReadMethod() {
        return this.readMethod;
    }

    public Method getWriteMethod() {
        return this.writeMethod;
    }

    public String getName() {
        return this.name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setReadMethod(Method readMethod) {
        this.readMethod = readMethod;
    }

    public void setWriteMethod(Method writeMethod) {
        this.writeMethod = writeMethod;
    }

    @Override
    public String toString() {
        String type = this.writeMethod.getParameterTypes()[0].getName();
        return this.getClass().getName() + " [name = " + this.name + " read = " + this.readMethod.getName() + " write =" + this.writeMethod.getName() + " type = " + type + "]";
    }
}

