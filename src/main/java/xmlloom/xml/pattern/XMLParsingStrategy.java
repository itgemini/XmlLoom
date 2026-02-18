package xmlloom.xml.pattern;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;

import xmlloom.xml.exception.XMLAPIException;
import xmlloom.xml.exception.XMLIssueReport;

/**
 * Strategy interface for XML parsing.
 *
 * @param <T> the type parameter for the resulting mapped object.
 */
public interface XMLParsingStrategy<T> {
    /**
     * Parses an XML file and returns an object of type T.
     *
     * @param filePath the path to the XML file.
     * @param clazz    the class type to map to.
     * @return an object of type T mapped from XML.
     * @throws XMLAPIException if parsing fails.
     */
    T parse(String filePath, Class<T> clazz) throws XMLAPIException;

    /**
     * Parses an XML file and returns an object of type T.
     *
     * @param inputStream of the XML file.
     * @param clazz       the class type to map to.
     * @return an object of type T mapped from XML.
     * @throws XMLAPIException if parsing fails.
     */
    T parse(FileInputStream inputStream, Class<T> clazz) throws XMLAPIException;

    /**
     * Parses an XML file and returns an object of type T.
     *
     * @param inputStream of the XML file.
     * @param clazz       the class type to map to.
     * @return an object of type T mapped from XML.
     * @throws XMLAPIException if parsing fails.
     */
    T parse(InputStream inputStream, Class<T> clazz) throws XMLAPIException;

    /**
     * Parses an XML file and returns an object of type T.
     *
     * @param file  the XML file.
     * @param clazz the class type to map to.
     * @return an object of type T mapped from XML.
     * @throws XMLAPIException if parsing fails.
     */
    T parse(File file, Class<T> clazz) throws XMLAPIException;

    /**
     * Sets the warning handler for XML parsing.
     *
     * @return warningHandler the warning handler to set.
     */
    XMLIssueReport getWarningHandler();
}