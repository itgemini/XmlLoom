package com.datashepherd.xml.pattern;

import com.datashepherd.xml.exception.XMLAPIException;

/**
 * Command interface representing an operation in the XML API.
 */
public interface XMLCommand {
    /**
     * Executes the command.
     *
     * @throws XMLAPIException if an error occurs during execution.
     */
    void execute() throws XMLAPIException;
}