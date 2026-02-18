package xmlloom.xml.pattern;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import javax.xml.stream.XMLEventReader;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamException;

import xmlloom.xml.exception.XMLAPIException;
import xmlloom.xml.exception.XMLIssueReport;
import xmlloom.xml.helper.ConcurrentProcessor;


/**
 * Concrete implementation of XMLParsingStrategy using SAXParser.
 *
 * @param <T> the type parameter.
 */
public class SAXParsingStrategy<T> implements XMLParsingStrategy<T> {
    private ConcurrentProcessor<T> processor;

    @Override
    public T parse(String filePath, Class<T> clazz) throws XMLAPIException {
        XMLInputFactory xmlInputFactory = XMLInputFactory.newInstance();
        xmlInputFactory.setProperty(XMLInputFactory.IS_SUPPORTING_EXTERNAL_ENTITIES, false);
        xmlInputFactory.setProperty(XMLInputFactory.SUPPORT_DTD, false);
        try (FileInputStream fis = new FileInputStream(filePath)) {
            XMLEventReader eventReader = xmlInputFactory.createXMLEventReader(fis);
            processor = new ConcurrentProcessor<>(clazz, eventReader).start();
            return processor.build();
        } catch (Exception e) {
            throw new XMLAPIException("Error during SAX parsing", e);
        }
    }

    @Override
    public T parse(FileInputStream inputStream, Class<T> clazz) throws XMLAPIException {
        try {
            XMLInputFactory xmlInputFactory = XMLInputFactory.newInstance();
            xmlInputFactory.setProperty(XMLInputFactory.IS_SUPPORTING_EXTERNAL_ENTITIES, false);
            xmlInputFactory.setProperty(XMLInputFactory.SUPPORT_DTD, false);
            XMLEventReader eventReader = xmlInputFactory.createXMLEventReader(inputStream);
            processor = new ConcurrentProcessor<>(clazz, eventReader).start();
            return processor.build();
        } catch (XMLStreamException e) {
            throw new XMLAPIException("Error during SAX parsing", e);
        }
    }

    public T parse(InputStream inputStream, Class<T> clazz) throws XMLAPIException {
        try {
            XMLInputFactory xmlInputFactory = XMLInputFactory.newInstance();
            xmlInputFactory.setProperty(XMLInputFactory.IS_SUPPORTING_EXTERNAL_ENTITIES, false);
            xmlInputFactory.setProperty(XMLInputFactory.SUPPORT_DTD, false);
            XMLEventReader eventReader = xmlInputFactory.createXMLEventReader(inputStream);
            processor = new ConcurrentProcessor<>(clazz, eventReader).start();
            return processor.build();
        } catch (XMLStreamException e) {
            throw new XMLAPIException("Error during SAX parsing", e);
        }
    }

    public T parse(File file, Class<T> clazz) throws XMLAPIException {
        try (FileInputStream fis = new FileInputStream(file)) {
            XMLInputFactory xmlInputFactory = XMLInputFactory.newInstance();
            xmlInputFactory.setProperty(XMLInputFactory.IS_SUPPORTING_EXTERNAL_ENTITIES, false);
            xmlInputFactory.setProperty(XMLInputFactory.SUPPORT_DTD, false);
            XMLEventReader eventReader = xmlInputFactory.createXMLEventReader(fis);
            processor = new ConcurrentProcessor<>(clazz, eventReader).start();
            return processor.build();
        } catch (Exception e) {
            throw new XMLAPIException("Error during SAX parsing", e);
        }
    }

    @Override
    public XMLIssueReport getWarningHandler() {
        return processor.getReport();
    }
}