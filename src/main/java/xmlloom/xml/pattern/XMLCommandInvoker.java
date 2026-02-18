package com.datashepherd.xml.pattern;

import java.util.ArrayList;
import java.util.List;

import com.datashepherd.xml.exception.XMLAPIException;

/**
 * Invoker for XML commands following the Command design pattern.
 */
public class XMLCommandInvoker {
    private final List<XMLCommand> commandQueue = new ArrayList<>();

    /**
     * Adds a command to the invoker.
     *
     * @param command the command to add.
     */
    public void addCommand(XMLCommand command) {
        commandQueue.add(command);
    }

    /**
     * Executes all added commands.
     *
     * @throws XMLAPIException if any command fails.
     */
    public void executeCommands() throws XMLAPIException {
        for (XMLCommand command : commandQueue) {
            command.execute();
        }
        commandQueue.clear();
    }
}