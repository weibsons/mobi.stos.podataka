package mobi.stos.podataka_lib.reflection;

public class ConvertUtils {

    public static Object convert(String string, Class targetClass) {
        return org.apache.commons.beanutils.ConvertUtils.convert(string, targetClass);
    }
}
