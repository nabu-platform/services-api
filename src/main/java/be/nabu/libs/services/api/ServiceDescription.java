package be.nabu.libs.services.api;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface ServiceDescription {
	public String name() default "";
	public String namespace() default "";
	public String description() default "";
	public String comment() default "";
	public String summary() default "";
}