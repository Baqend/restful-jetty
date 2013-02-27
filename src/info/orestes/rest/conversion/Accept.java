package info.orestes.rest.conversion;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * {@link Converter}s that are annotated with this annotation indicates that
 * they can handle the specified {@link MediaType}s representation of an entity
 * 
 * @author Florian
 * 
 */
@Documented
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Inherited
public @interface Accept {
	String[] value();
}
