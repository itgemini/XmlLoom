package xmlloom.xml.helper;

import java.io.File;
import java.io.InputStream;

import xmlloom.xml.exception.XMLAPIException;
import xmlloom.xml.pattern.XMLCommand;
import xmlloom.xml.pattern.XMLParsingStrategy;

/**
 * A command for reading and parsing XML data into an object of the specified type.
 * This class uses a provided parsing strategy to process the XML input.
 * Implements the XMLCommand interface, making it suitable for invocation in a command pattern.
 *
 * @param <T> the type of the resulting mapped object after parsing
 */
public class XMLReaderCommand<T> implements XMLCommand {

    /**
     * The input source for the XML operation. This can accept various types such as:
     * - String: Representing the path to an XML file.
     * - File: Direct reference to an XML file.
     * - InputStream: For reading XML data from a stream source.
     *
     * This variable determines the source of data to be processed during execution,
     * and its actual type is resolved and utilized by the corresponding implementation.
     */
    private final Object input;
    /**
     * The class type representing the target object to which XML data will be mapped.
     * Used by the XMLReaderCommand to specify the expected type of the parsed result.
     *
     * @param <T> the type of the mapped object after the XML is parsed.
     */
    private final Class<T> clazz;
    /**
     * The strategy used for parsing XML data into an object of type T.
     * This field represents an implementation of the {@link XMLParsingStrategy} interface
     * and defines the mechanism for converting various XML source formats, such as file paths,
     * input streams, or files, into an object of the specified type.
     *
     * It allows flexibility in how XML data can be interpreted and mapped based on the needs
     * of the application, supporting different input types and custom mapping logic.
     *
     * @param <T> the type of the resulting mapped object.
     */
    private final XMLParsingStrategy<T> parsingStrategy;
    /**
     * Stores the result of the XML parsing operation performed by the command.
     * The type of the result is defined by the generic type parameter <T> of the
     * containing class. This variable holds the parsed object or null if the command
     * has not been executed or the parsing failed.
     */
    private T result;

    /**
     * Constructs a new XMLReaderCommand with the specified input, target class, and parsing strategy.
     *
     * @param input           the XML input, which can be a String (file path), File, or InputStream.
     *                        It determines the source of the XML data to parse.
     * @param clazz           the class type to map the XML data to.
     * @param parsingStrategy the XML parsing strategy to use for processing the input.
     * @param <T>             the type parameter indicating the resulting object's type.
     */
    public XMLReaderCommand(Object input, Class<T> clazz, XMLParsingStrategy<T> parsingStrategy) {
        this.input = input;
        this.clazz = clazz;
        this.parsingStrategy = parsingStrategy;
    }

    /**
     * Executes the command to parse XML data using the specified input and
     * parsing strategy. The input can be a file path, a File object, or an
     * InputStream. The parsed result is stored and can be retrieved via the
     * {@code getResult()} method.
     *
     * @throws XMLAPIException if an error occurs during parsing.
     * @throws UnsupportedOperationException if the input type is unsupported or null.
     */
    @Override
    public void execute() throws XMLAPIException {
        switch (input) {
            case String path -> result = parsingStrategy.parse(path, clazz);
            case File file -> result = parsingStrategy.parse(file, clazz);
            case InputStream inputStream -> result = parsingStrategy.parse(inputStream, clazz);
            case null, default -> throw new UnsupportedOperationException();
        }
    }

    /**
     * Retrieves the result of the XML parsing operation.
     *
     * @return the parsed result of type T, or null if the operation has not been executed or failed.
     */
    public T getResult() {
        return result;
    }
}