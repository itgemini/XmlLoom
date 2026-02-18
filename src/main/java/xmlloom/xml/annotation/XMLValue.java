package com.datashepherd.xml.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Annotation for mapping an element's direct value to a field.
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
public @interface XMLValue {
    /**
     * The XML element name corresponding to this field.
     *
     * @return element name.
     */
    String name();
}