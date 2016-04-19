package mobi.stos.podataka_lib.reflection;

import java.lang.reflect.Method;

public class PropertyDescriptor {

    private final String name;
    private final Method readMethod;
    private final Method writeMethod;

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

    @Override
    public String toString() {
        String type = this.writeMethod.getParameterTypes()[0].getName();
        return this.getClass().getName() + " [name = " + this.name + " read = " + this.readMethod.getName() + " write =" + this.writeMethod.getName() + " type = " + type + "]";
    }
}

