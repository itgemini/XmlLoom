package com.datashepherd.xml.pattern;

import java.io.OutputStream;
import java.io.Writer;
import javax.xml.stream.XMLEventWriter;
import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamException;

import com.datashepherd.xml.exception.XMLAPIException;
import com.datashepherd.xml.helper.StreamingSerializer;

/**
 * Concrete implementation of XMLWritingStrategy using StAX.
 */
public class StaxWritingStrategy implements XMLWritingStrategy {

    @Override
    public void write(OutputStream output, Object object) throws XMLAPIException {
        write(output, object, false);
    }

    @Override
    public void write(OutputStream output, Object object, boolean formatted) throws XMLAPIException {
        try {
            XMLOutputFactory outputFactory = XMLOutputFactory.newInstance();
            XMLEventWriter eventWriter = outputFactory.createXMLEventWriter(output);
            StreamingSerializer serializer = new StreamingSerializer(eventWriter, formatted);
            serializer.serialize(object);
            eventWriter.flush();
            eventWriter.close();
        } catch (XMLStreamException e) {
            throw new XMLAPIException("Error during StAX writing", e);
        }
    }

    @Override
    public void write(Writer writer, Object object) throws XMLAPIException {
        write(writer, object, false);
    }

    @Override
    public void write(Writer writer, Object object, boolean formatted) throws XMLAPIException {
        try {
            XMLOutputFactory outputFactory = XMLOutputFactory.newInstance();
            XMLEventWriter eventWriter = outputFactory.createXMLEventWriter(writer);
            StreamingSerializer serializer = new StreamingSerializer(eventWriter, formatted);
            serializer.serialize(object);
            eventWriter.flush();
            eventWriter.close();
        } catch (XMLStreamException e) {
            throw new XMLAPIException("Error during StAX writing", e);
        }
    }
}
