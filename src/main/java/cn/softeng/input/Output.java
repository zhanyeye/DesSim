package cn.softeng.input;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * @date: 12/16/2020 4:10 PM
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface Output {
    public String name();
    public String description() default "";
    public boolean reportable() default false;
    public int sequence() default 100;  // determines the sequence in which outputs are listed
}
