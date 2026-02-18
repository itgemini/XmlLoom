package xmlloom.xml.pattern;

import xmlloom.xml.exception.XMLAPIException;

/**
 * Factory class to create instances of annotated XML classes using reflection.
 */
public class XMLObjectFactory {
    private XMLObjectFactory() {
    }

    /**
     * Creates an instance of the specified class.
     *
     * @param clazz the class to instantiate.
     * @param <T>   the type parameter.
     * @return a new instance of the class.
     * @throws XMLAPIException if instantiation fails.
     */
    public static <T> T createInstance(Class<T> clazz) throws XMLAPIException {
        try {
            return clazz.getDeclaredConstructor().newInstance();
        } catch (Exception e) {
            throw new XMLAPIException("Failed to create instance of " + clazz.getName(), e);
        }
    }
}
