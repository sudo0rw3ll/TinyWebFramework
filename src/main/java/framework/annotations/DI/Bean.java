package framework.annotations.DI;

import framework.annotations.DI.scopes.BeanScope;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface Bean {
    BeanScope scope() default BeanScope.SINGLETON;
}
