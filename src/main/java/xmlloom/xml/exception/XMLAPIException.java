package xmlloom.xml.exception;

/**
 * Custom exception for XML API errors.
 */
public class XMLAPIException extends Exception {
    public XMLAPIException(String message, Throwable cause) {
        super(message, cause);
    }

    public XMLAPIException(String message) {
        super(message);
    }
}