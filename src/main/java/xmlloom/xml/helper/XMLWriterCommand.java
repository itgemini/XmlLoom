package com.datashepherd.xml.helper;

import java.io.OutputStream;
import java.io.Writer;
import com.datashepherd.xml.exception.XMLAPIException;
import com.datashepherd.xml.pattern.XMLCommand;
import com.datashepherd.xml.pattern.XMLWritingStrategy;

/**
 * Command for writing XML using a given writing strategy.
 */
public class XMLWriterCommand implements XMLCommand {

    private final Object output;
    private final Object objectToWrite;
    private final XMLWritingStrategy writingStrategy;
    private boolean formatted = false;

    /**
     * Constructor.
     *
     * @param output          the output (OutputStream or Writer).
     * @param objectToWrite   the object to write to XML.
     * @param writingStrategy the strategy to use for writing.
     */
    public XMLWriterCommand(Object output, Object objectToWrite, XMLWritingStrategy writingStrategy) {
        this.output = output;
        this.objectToWrite = objectToWrite;
        this.writingStrategy = writingStrategy;
    }

    public XMLWriterCommand(Object output, Object objectToWrite, XMLWritingStrategy writingStrategy, boolean formatted) {
        this(output, objectToWrite, writingStrategy);
        this.formatted = formatted;
    }

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
