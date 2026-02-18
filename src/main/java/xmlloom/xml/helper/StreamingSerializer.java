package com.datashepherd.xml.helper;

import java.lang.reflect.Field;
import java.util.Collection;
import java.util.Optional;
import javax.xml.stream.XMLEventFactory;
import javax.xml.stream.XMLEventWriter;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.events.Attribute;
import javax.xml.stream.events.StartElement;
import javax.xml.stream.events.EndElement;
import javax.xml.stream.events.Characters;
import javax.xml.namespace.QName;

import com.datashepherd.xml.annotation.XMLAttribute;
import com.datashepherd.xml.annotation.XMLElement;
import com.datashepherd.xml.annotation.XMLRoot;

/**
 * Serializer that converts Java objects to XML using StAX.
 */
public class StreamingSerializer {

    private final XMLEventWriter eventWriter;
    private final XMLEventFactory eventFactory;
    private boolean formatted = false;
    private int indentLevel = 0;
    private static final String INDENT = "    ";

    public StreamingSerializer(XMLEventWriter eventWriter) {
        this.eventWriter = eventWriter;
        this.eventFactory = XMLEventFactory.newInstance();
    }

    public StreamingSerializer(XMLEventWriter eventWriter, boolean formatted) {
        this(eventWriter);
        this.formatted = formatted;
    }

    public void serialize(Object object) throws XMLStreamException {
        if (object == null) return;
        eventWriter.add(eventFactory.createStartDocument());
        if (formatted) eventWriter.add(eventFactory.createCharacters("\n"));
        serializeObject(object, null);
        if (formatted) eventWriter.add(eventFactory.createCharacters("\n"));
        eventWriter.add(eventFactory.createEndDocument());
    }

    private void serializeObject(Object object, String overrideName) throws XMLStreamException {
        if (object == null) return;

        Class<?> clazz = object.getClass();
        String tagName = overrideName;

        if (tagName == null) {
            XMLRoot rootAnno = clazz.getAnnotation(XMLRoot.class);
            if (rootAnno != null) {
                tagName = rootAnno.name();
            } else {
                tagName = clazz.getSimpleName();
            }
        }

        if (formatted) writeIndent();
        // Start element
        StartElement startElement = eventFactory.createStartElement(new QName(tagName), null, null);
        eventWriter.add(startElement);
        indentLevel++;

        // Attributes
        for (Field field : clazz.getDeclaredFields()) {
            XMLAttribute attrAnno = field.getAnnotation(XMLAttribute.class);
            if (attrAnno != null) {
                field.setAccessible(true);
                try {
                    Object val = field.get(object);
                    if (val != null) {
                        Attribute attribute = eventFactory.createAttribute(attrAnno.name(), val.toString());
                        eventWriter.add(attribute);
                    }
                } catch (IllegalAccessException e) {
                    throw new XMLStreamException("Error accessing field " + field.getName(), e);
                }
            }
        }

        // Elements
        boolean hasChildElements = false;
        for (Field field : clazz.getDeclaredFields()) {
            XMLElement elemAnno = field.getAnnotation(XMLElement.class);
            if (elemAnno != null) {
                field.setAccessible(true);
                try {
                    Object val = field.get(object);
                    if (val != null) {
                        if (!hasChildElements && formatted) {
                            eventWriter.add(eventFactory.createCharacters("\n"));
                        }
                        hasChildElements = true;
                        if (val instanceof Collection<?> collection) {
                            for (Object item : collection) {
                                serializeObject(item, elemAnno.name());
                                if (formatted) eventWriter.add(eventFactory.createCharacters("\n"));
                            }
                        } else if (isPrimitiveOrWrapper(val.getClass())) {
                            writeSimpleElement(elemAnno.name(), val.toString());
                            if (formatted) eventWriter.add(eventFactory.createCharacters("\n"));
                        } else {
                            serializeObject(val, elemAnno.name());
                            if (formatted) eventWriter.add(eventFactory.createCharacters("\n"));
                        }
                    }
                } catch (IllegalAccessException e) {
                    throw new XMLStreamException("Error accessing field " + field.getName(), e);
                }
            }
        }

        indentLevel--;
        if (hasChildElements && formatted) writeIndent();
        // End element
        EndElement endElement = eventFactory.createEndElement(new QName(tagName), null);
        eventWriter.add(endElement);
    }

    private void writeSimpleElement(String tagName, String content) throws XMLStreamException {
        if (formatted) writeIndent();
        eventWriter.add(eventFactory.createStartElement(new QName(tagName), null, null));
        eventWriter.add(eventFactory.createCharacters(content));
        eventWriter.add(eventFactory.createEndElement(new QName(tagName), null));
    }

    private void writeIndent() throws XMLStreamException {
        eventWriter.add(eventFactory.createCharacters(INDENT.repeat(indentLevel)));
    }

    private boolean isPrimitiveOrWrapper(Class<?> type) {
        return type.isPrimitive() || 
               type == Double.class || type == Float.class || type == Long.class ||
               type == Integer.class || type == Short.class || type == Byte.class ||
               type == Boolean.class || type == Character.class || type == String.class;
    }
}
