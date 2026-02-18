package xmlloom.xml.pattern;

import java.io.OutputStream;
import java.io.Writer;

import xmlloom.xml.exception.XMLAPIException;

/**
 * Strategy interface for XML writing.
 */
public interface XMLWritingStrategy {
    /**
     * Writes an object to an XML output stream.
     *
     * @param output the output stream to write to.
     * @param object the object to write.
     * @throws XMLAPIException if writing fails.
     */
    void write(OutputStream output, Object object) throws XMLAPIException;
    void write(OutputStream output, Object object, boolean formatted) throws XMLAPIException;

    /**
     * Writes an object to an XML writer.
     *
     * @param writer the writer to write to.
     * @param object the object to write.
     * @throws XMLAPIException if writing fails.
     */
    void write(Writer writer, Object object) throws XMLAPIException;
    void write(Writer writer, Object object, boolean formatted) throws XMLAPIException;
}
