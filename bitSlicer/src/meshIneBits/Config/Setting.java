package meshIneBits.Slicer.Config;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
public @interface Setting
{
	public String title() default "";
	
	public String description() default "";
	
	public double minValue() default Double.MIN_VALUE;
	
	public double maxValue() default Double.MAX_VALUE;
	
	public String enumName() default "";
	
	public String group() default "";
}
