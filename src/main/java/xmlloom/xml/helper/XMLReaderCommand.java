package com.datashepherd.xml.helper;

import java.io.File;
import java.io.InputStream;

import com.datashepherd.xml.exception.XMLAPIException;
import com.datashepherd.xml.pattern.XMLCommand;
import com.datashepherd.xml.pattern.XMLParsingStrategy;

/**
 * Command for reading XML using a given parsing strategy.
 *
 * @param <T> the type parameter for the mapped object.
 */
public class XMLReaderCommand<T> implements XMLCommand {

    private final Object input;
    private final Class<T> clazz;
    private final XMLParsingStrategy<T> parsingStrategy;
    private T result;

    /**
     * Constructor.
     *
     * @param input        the XML file.
     * @param clazz           the class type to map to.
     * @param parsingStrategy the strategy to use for parsing.
     */
    public XMLReaderCommand(Object input, Class<T> clazz, XMLParsingStrategy<T> parsingStrategy) {
        this.input = input;
        this.clazz = clazz;
        this.parsingStrategy = parsingStrategy;
    }

    @Override
    public void execute() throws XMLAPIException {
        if (input instanceof String path) result = parsingStrategy.parse(path, clazz);
        else if (input instanceof File file) result = parsingStrategy.parse(file, clazz);
        else if (input instanceof InputStream inputStream) result = parsingStrategy.parse(inputStream, clazz);
        else throw new UnsupportedOperationException();
    }

    /**
     * Returns the result object after execution.
     *
     * @return the mapped object.
     */
    public T getResult() {
        return result;
    }
}