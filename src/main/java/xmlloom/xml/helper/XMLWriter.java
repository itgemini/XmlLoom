package com.datashepherd.xml.helper;

import java.io.OutputStream;
import java.io.Writer;
import com.datashepherd.xml.exception.XMLAPIException;
import com.datashepherd.xml.pattern.StaxWritingStrategy;
import com.datashepherd.xml.pattern.XMLCommandInvoker;
import com.datashepherd.xml.pattern.XMLWritingStrategy;

/**
 * Facade class providing a simplified API for XML writing operations.
 */
public class XMLWriter {
    private final XMLCommandInvoker invoker = new XMLCommandInvoker();
    private boolean formatted = false;

    /**
     * Sets whether the output XML should be formatted (pretty-printed).
     *
     * @param formatted true to format the XML, false otherwise.
     * @return this XMLWriter instance.
     */
    public XMLWriter setFormatted(boolean formatted) {
        this.formatted = formatted;
        return this;
    }

    /**
     * Writes an object to XML.
     *
     * @param output the output (OutputStream or Writer).
     * @param object the object to write.
     * @throws XMLAPIException if writing fails.
     */
    public void write(Object output, Object object) throws XMLAPIException {
        XMLWritingStrategy writingStrategy = new StaxWritingStrategy();
        XMLWriterCommand writerCommand = new XMLWriterCommand(output, object, writingStrategy, formatted);
        invoker.addCommand(writerCommand);
        invoker.executeCommands();
    }
}
