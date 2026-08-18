package meshIneBits.util.annotations;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;
import static java.lang.annotation.ElementType.*;
import static java.lang.annotation.RetentionPolicy.*;

/**
 * Annotation that provides information about argument's name.
 *
 * Copied from Javafx to keep metadata of Pair class. Purely for developper's comfort.
 */
@Retention(RUNTIME)
@Target(PARAMETER)
public @interface NamedArg {
    /**
     * The name of the annotated argument.
     * @return the name of the annotated argument
     */
    public String value();

    /**
     * The default value of the annotated argument.
     * @return the default value of the annotated argument
     */
    public String defaultValue() default "";
}