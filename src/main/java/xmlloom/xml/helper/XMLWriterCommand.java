package xmlloom.xml.helper;

import java.io.OutputStream;
import java.io.Writer;

import xmlloom.xml.exception.XMLAPIException;
import xmlloom.xml.pattern.XMLCommand;
import xmlloom.xml.pattern.XMLWritingStrategy;


/**
 * Implements the XMLCommand interface to handle writing objects to XML outputs.
 * This class encapsulates the logic for writing an object to XML using a specified
 * writing strategy and supports both formatted and unformatted output.
 */
public class XMLWriterCommand implements XMLCommand {

    /**
     * The output destination for the XML writing operation.
     *
     * This variable represents the target to which the XML content will be written.
     * It can either be an instance of {@link OutputStream} for binary outputs
     * or {@link Writer} for character-based outputs. The exact type of the
     * object determines the writing strategy used during the execution of the
     * XML writing process.
     *
     * This variable is immutable and must be specified at the time of constructing
     * an {@code XMLWriterCommand} instance.
     */
    private final Object output;
    /**
     * The object to be written to XML output.
     * This variable represents the data or entity to be serialized into an XML format.
     * It is used in conjunction with a defined writing strategy for the XML serialization process.
     * The specific representation of the object within the XML output depends on the implementation
     * of the selected {@link XMLWritingStrategy}.
     */
    private final Object objectToWrite;
    /**
     * The strategy instance used for writing XML content.
     * This variable holds a reference to an implementation of the {@link XMLWritingStrategy}
     * interface, enabling flexible and customizable XML serialization logic.
     *
     * It determines how objects are serialized into XML, including optional formatting.
     * The writing strategy encapsulates the underlying implementation details, allowing
     * the {@link XMLWriterCommand} to support different XML generation approaches, such as
     * StAX, DOM, or custom implementations.
     *
     * This variable is immutable and must be provided during initialization of the
     * {@link XMLWriterCommand}.
     */
    private final XMLWritingStrategy writingStrategy;
    /**
     * Indicates whether the XML output should be formatted (pretty-printed).
     * When set to true, the output will include indentation and line breaks
     * to enhance human readability. When false, the output will be unformatted
     * and compact.
     */
    private boolean formatted = false;

    /**
     * Constructs an XMLWriterCommand to handle writing an object to an XML output
     * using the specified writing strategy.
     *
     * @param output           the target output for the XML, which can be an OutputStream or a Writer.
     * @param objectToWrite    the object to be written to XML.
     * @param writingStrategy  the strategy to use for writing the object to XML.
     */
    public XMLWriterCommand(Object output, Object objectToWrite, XMLWritingStrategy writingStrategy) {
        this.output = output;
        this.objectToWrite = objectToWrite;
        this.writingStrategy = writingStrategy;
    }

    /**
     * Constructs an instance of the XMLWriterCommand class.
     * This constructor initializes an XMLWriterCommand object to write the given object
     * to the specified output using the provided XML writing strategy, with support for
     * both formatted and unformatted XML output.
     *
     * @param output the target output for the XML, which can be an OutputStream or a Writer.
     * @param objectToWrite the object to be serialized into XML.
     * @param writingStrategy the strategy to use for writing the XML output.
     * @param formatted true to enable formatted (pretty-printed) XML output, false for unformatted output.
     */
    public XMLWriterCommand(Object output, Object objectToWrite, XMLWritingStrategy writingStrategy, boolean formatted) {
        this(output, objectToWrite, writingStrategy);
        this.formatted = formatted;
    }

    /**
     * Executes the command to write an object to an XML output using the specified
     * writing strategy. The output can be an {@link OutputStream} or a {@link Writer},
     * and the output format can be configured as formatted or unformatted.
     *
     * @throws XMLAPIException if an error occurs during the writing process.
     * @throws UnsupportedOperationException if the specified output is not a supported type.
     */
    @Override
    public void execute() throws XMLAPIException {
        if (output instanceof OutputStream os) {
            writingStrategy.write(os, objectToWrite, formatted);
        } else if (output instanceof Writer writer) {
            writingStrategy.write(writer, objectToWrite, formatted);
        } else {
            throw new UnsupportedOperationException("Unsupported output type: " + output.getClass().getName());
        }
    }
}
