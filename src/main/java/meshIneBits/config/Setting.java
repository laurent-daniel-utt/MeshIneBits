package meshIneBits.config;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
public @interface Setting {
	public double step() default 1;

	public String title() default "";

	public String description() default "";

	public double minValue() default Double.MIN_VALUE;

	public double maxValue() default Double.MAX_VALUE;
}
