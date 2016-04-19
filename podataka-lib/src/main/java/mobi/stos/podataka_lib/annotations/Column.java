package mobi.stos.podataka_lib.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.FIELD, ElementType.METHOD})
public @interface Column {

    String name() default "";

    int length() default 255;

    boolean nullable() default true;

    String sqlType() default "";

}
