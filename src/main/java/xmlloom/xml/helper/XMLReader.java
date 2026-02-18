package com.datashepherd.xml.helper;

import com.datashepherd.xml.exception.XMLAPIException;
import com.datashepherd.xml.exception.XMLIssueReport;
import com.datashepherd.xml.pattern.SAXParsingStrategy;
import com.datashepherd.xml.pattern.XMLCommandInvoker;
import com.datashepherd.xml.pattern.XMLParsingStrategy;

/**
 * Facade class providing a simplified API for XML operations.
 */
public class XMLReader {

    private final XMLCommandInvoker invoker = new XMLCommandInvoker();
    private XMLIssueReport warningHandler;

    /**
     * Reads an XML file and maps it to an object of the specified class.
     *
     * @param input the XML file path.
     * @param clazz    the class type to map to.
     * @param <T>      the type parameter.
     * @return the mapped object.
     * @throws XMLAPIException if reading fails.
     */
    public <T> T read(Object input, Class<T> clazz) throws XMLAPIException {
        XMLParsingStrategy<T> parsingStrategy = new SAXParsingStrategy<>();
        XMLReaderCommand<T> readerCommand = new XMLReaderCommand<>(input, clazz, parsingStrategy);
        invoker.addCommand(readerCommand);
        invoker.executeCommands();
        warningHandler = parsingStrategy.getWarningHandler();
        return readerCommand.getResult();
    }

    /**
     * Gets the warning handler for XML parsing.
     *
     * @return the warning handler.
     */
    public XMLIssueReport getWarningHandler() {
        return warningHandler;
    }
}