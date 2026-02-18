package xmlloom.xml.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Annotation to mark a field as an XML attribute.
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
public @interface XMLAttribute {
    /**
     * The XML attribute name corresponding to this field.
     *
     * @return attribute name.
     */
    String name();

    /**
     * The XML attribute is required.
     *
     * @return attribute is required.
     */
    boolean required() default false;
}