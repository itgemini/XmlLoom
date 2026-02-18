package com.datashepherd.xml.helper;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Deque;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.PriorityQueue;
import java.util.Queue;
import java.util.Set;
import java.util.concurrent.ConcurrentLinkedDeque;
import java.util.concurrent.locks.ReentrantLock;
import java.util.stream.Stream;
import javax.xml.stream.Location;
import javax.xml.stream.XMLEventReader;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.events.Attribute;
import javax.xml.stream.events.EndElement;
import javax.xml.stream.events.StartElement;
import javax.xml.stream.events.XMLEvent;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import com.datashepherd.xml.annotation.XMLAttribute;
import com.datashepherd.xml.annotation.XMLElement;
import com.datashepherd.xml.annotation.XMLRoot;
import com.datashepherd.xml.annotation.XMLValue;
import com.datashepherd.xml.exception.Issue;
import com.datashepherd.xml.exception.XMLAPIException;
import com.datashepherd.xml.exception.XMLIssueReport;
import com.datashepherd.xml.pattern.XMLObjectFactory;

import static com.datashepherd.xml.exception.IssueKey.ATTRIBUTES_ERROR;
import static com.datashepherd.xml.exception.IssueKey.FIELD;
import static com.datashepherd.xml.exception.IssueKey.MISSING_ATTRIBUTES;
import static com.datashepherd.xml.exception.IssueKey.ROOT_OBJECT;
import static com.datashepherd.xml.exception.IssueKey.VALUE;
import static com.datashepherd.xml.exception.IssueKey.XML_PARSING_ERROR;

public class ConcurrentProcessor<T> {

    private final Deque<StackFrame> stack = new ConcurrentLinkedDeque<>();
    private final ReentrantLock stackLock = new ReentrantLock(true);
    private final Class<T> rootClass;
    private final XMLEventReader eventReader;
    private final XMLIssueReport report = new XMLIssueReport();
    private Location location;
    private T result;

    public ConcurrentProcessor(Class<T> rootClass, XMLEventReader eventReader) {
        this.rootClass = rootClass;
        this.eventReader = eventReader;
    }

    /**
     * Converts text to the field type, throwing XMLAPIException instead of generic exceptions.
     */
    private static Object convertType(Field field, String typeName, String text) throws XMLAPIException {
        try {
            return switch (typeName) {
                case "java.lang.Integer", "int" -> Integer.parseInt(text);
                case "java.lang.Double", "double" -> Double.parseDouble(text);
                case "java.lang.Float", "float" -> Float.parseFloat(text);
                case "java.lang.Long", "long" -> Long.parseLong(text);
                case "java.lang.Boolean", "boolean" -> Boolean.parseBoolean(text);
                case "java.time.LocalDate" -> LocalDate.parse(text);
                case "java.time.LocalDateTime" -> LocalDateTime.parse(text);
                case "java.lang.String" -> text;
                default -> throw new XMLAPIException("Unsupported field type: " + field.getType().getName());
            };
        } catch (NumberFormatException e) {
            throw new XMLAPIException("Invalid number format for " + field.getName() + ": " + text, e);
        }
    }

    /**
     * Initiates parsing.
     *
     * @return this Processor for fluent usage
     */
    public ConcurrentProcessor<T> start() {
        parseDocument();
        return this;
    }

    /**
     * @return The root object after parsing is complete
     */
    public T build() {
        return result;
    }

    public XMLIssueReport getReport() {
        return report;
    }

    /**
     * Main loop over XMLEvents with a single break for end of document.
     */
    private void parseDocument() {
        while (eventReader.hasNext()) {
            try {
                XMLEvent event = eventReader.peek();
                location = event.getLocation();
                if (event.isEndDocument()) break;
                handleXmlEvent(event);
            } catch (XMLStreamException streamEx) {
                String message = String.format("XML parsing error, Line number %s Column number %s, cause %s ", streamEx.getLocation().getLineNumber(), streamEx.getLocation().getColumnNumber(), streamEx.getMessage());
                report.addErrors(Issue.builder().key(XML_PARSING_ERROR).variables(List.of(Map.entry("line_number", streamEx.getLocation().getLineNumber()), Map.entry("column_number", streamEx.getLocation().getLineNumber()), Map.entry("message", streamEx.getMessage()))).message(message));
                consumeProblemEvent();
                break;
            }
        }
    }

    /**
     * Handles the current event based on its type, then consumes it if needed.
     */
    private void handleXmlEvent(XMLEvent event) throws XMLStreamException {
        if (event.isStartDocument()) {
            eventReader.nextEvent();
            return;
        }
        if (event.isStartElement()) {
            handleStartElement(event.asStartElement());
            return;
        }
        if (event.isEndElement()) {
            handleEndElement(event.asEndElement());
            return;
        }
        eventReader.nextEvent();
    }

    /**
     * Safely consumes a problematic event to avoid infinite loops.
     */
    private void consumeProblemEvent() {
        try {
            eventReader.nextEvent();
        } catch (XMLStreamException e) {
            String message = String.format("XML parsing error, Line number %s Column number %s, cause %s ", e.getLocation().getLineNumber(), e.getLocation().getColumnNumber(), e.getMessage());
            report.addErrors(Issue.builder().key(XML_PARSING_ERROR).variables(List.of(Map.entry("line_number", e.getLocation().getLineNumber()), Map.entry("column_number", e.getLocation().getLineNumber()), Map.entry("message", e.getMessage()))).message(message));
        }
    }

    /**
     * Handles a StartElement: either root or nested element.
     */
    private void handleStartElement(StartElement startEl) throws XMLStreamException {
        eventReader.nextEvent();

        if (stackIsEmpty()) {
            handleRootElement(startEl);
        } else {
            handleNestedElement(startEl);
        }
    }

    /**
     * Handles root-level element (checking @XMLRoot match).
     */
    private void handleRootElement(StartElement startEl) throws XMLStreamException {
        if (!rootClass.isAnnotationPresent(XMLRoot.class)) {
            skipElementText();
            return;
        }
        String rootName = Objects.requireNonNull(rootClass.getAnnotation(XMLRoot.class), "Missing name in XMLRoot annotation").name();
        if (!rootName.equals(startEl.getName().getLocalPart())) {
            skipElementText();
            return;
        }
        try {
            Object rootObj = XMLObjectFactory.createInstance(rootClass);
            applyAttributes(rootClass, rootObj, startEl.getAttributes());
            pushFrame(startEl.getName().getLocalPart(), rootObj);
            result = rootClass.cast(rootObj);
        } catch (XMLAPIException e) {
            String message = String.format("Cannot create root object cause %s ", e.getMessage());
            report.addErrors(Issue.builder().message(message).key(ROOT_OBJECT).variables(List.of(Map.entry("message", e.getMessage()))));
            skipElementText();
            throw new XMLStreamException(message);
        }
    }

    /**
     * Handles nested StartElement under a known parent.
     */
    private void handleNestedElement(StartElement startEl) throws XMLStreamException {
        StackFrame parentFrame = peekFrame();
        if (parentFrame == null) {
            skipElementText();
            return;
        }
        String localName = startEl.getName().getLocalPart();

        Optional<Field> matched = findMatchingField(parentFrame.instance.getClass(), localName);

        if (matched.isEmpty()) {
            skipElementText();
            return;
        }
        Field field = matched.get();
        try {
            if (Collection.class.isAssignableFrom(field.getType())) {
                handleCollectionField(field, parentFrame, startEl);
            } else {
                handleSingleField(field, parentFrame, startEl);
            }
        } catch (ReflectiveOperationException | XMLAPIException e) {
            skipElementText();
            String message = String.format("Error handling field name %s cause %s ", field.getName(), e.getMessage());
            report.addErrors(Issue.builder().message(message).variables(List.of(Map.entry("field_name", field.getName()), Map.entry("message", e.getMessage()))));
            throw new XMLStreamException(message);
        }
    }

    /**
     * Handles an EndElement: pop if the top matches the element name.
     */
    private void handleEndElement(EndElement endEl) throws XMLStreamException {
        eventReader.nextEvent();
        StackFrame top = peekFrame();
        if (top != null && top.elementName.equals(endEl.getName().getLocalPart())) {
            popFrame();
        }
    }

    /**
     * If the field is a single object or textual value.
     */
    private void handleSingleField(Field field, StackFrame parentFrame, StartElement startEl) throws ReflectiveOperationException, XMLAPIException, XMLStreamException {
        if (field.isAnnotationPresent(XMLValue.class)) {
            String text = readElementText();
            Object converted = convertType(field, field.getType().getName(), text);
            invokeSetter(parentFrame.instance.getClass(), field, parentFrame.instance, converted);
        } else {
            Object child = ensureObjectInitialized(parentFrame.instance, field);
            applyAttributes(child.getClass(), child, startEl.getAttributes());
            pushFrame(startEl.getName().getLocalPart(), child);
        }
    }

    /**
     * If the field is a collection of objects or repeated values.
     */
    private void handleCollectionField(Field field, StackFrame parentFrame, StartElement startEl) throws ReflectiveOperationException, XMLAPIException, XMLStreamException {
        Collection<Object> collection = ensureCollectionInitialized(parentFrame.instance, field);
        Class<Object> genericType = getGenericType(field);

        if (field.isAnnotationPresent(XMLValue.class)) {
            String text = readElementText();
            Object val = convertType(field, genericType.getName(), text);
            collection.add(val);
            readRepeatedValues(field, genericType, collection);
        } else {
            Object item = XMLObjectFactory.createInstance(genericType);
            applyAttributes(genericType, item, startEl.getAttributes());
            collection.add(item);
            pushFrame(startEl.getName().getLocalPart(), item);
        }
    }

    /**
     * Recursively tries to read more text for repeated @XMLValue fields.
     * Single break statement by consolidating conditions into one check.
     */
    private void readRepeatedValues(Field field, Class<Object> genericType, Collection<Object> collection) {
        while (shouldContinueReading()) {
            try {
                if (!eventReader.peek().isStartElement()) return;
                String text = readElementText();
                Object val = convertType(field, genericType.getName(), text);
                collection.add(val);
            } catch (XMLStreamException ex) {
                String message = String.format("Failed to read value, Line number %s Column number %s, cause %s ", ex.getLocation().getLineNumber(), ex.getLocation().getColumnNumber(), ex.getMessage());
                report.addWarning(Issue.builder().message(message).key(VALUE).variables(List.of(Map.entry("line_number", ex.getLocation().getLineNumber()), Map.entry("column_number", ex.getLocation().getLineNumber()), Map.entry("message", ex.getMessage()))));
                break;
            } catch (XMLAPIException ex) {
                String message = String.format("Failed to read repeated value: %s", ex.getMessage());
                report.addWarning(Issue.builder().message(message).key(VALUE).variables(List.of(Map.entry("message", ex.getMessage()))));
                break;
            }
        }
    }

    /**
     * Decides whether we can read another chunk of text
     * or if we should stop. This ensures only one break in the loop.
     */
    private boolean shouldContinueReading() {
        XMLEvent peeked;
        try {
            peeked = eventReader.peek();
        } catch (XMLStreamException ex) {
            String message = String.format("Error peeking event, Line number %s Column number %s, cause %s ", ex.getLocation().getLineNumber(), ex.getLocation().getColumnNumber(), ex.getMessage());
            report.addErrors(Issue.builder().message(message).key(XML_PARSING_ERROR).variables(List.of(Map.entry("line_number", ex.getLocation().getLineNumber()), Map.entry("column_number", ex.getLocation().getLineNumber()), Map.entry("message", ex.getMessage()))));
            return false;
        }
        if (peeked == null) {
            return false;
        }
        return !(peeked.isStartElement() || peeked.isEndElement() || peeked.isEndDocument());
    }

    /**
     * Reads text up to the matching EndElement.
     */
    private String readElementText() throws XMLStreamException {
        return eventReader.getElementText();
    }

    /**
     * If we don't recognize an element, skip it entirely (including nested).
     */
    private void skipElementText() throws XMLStreamException {
        while (eventReader.hasNext()) {
            XMLEvent e = eventReader.peek();
            if (e.isStartElement() || e.isEndElement() || e.isEndDocument()) {
                return;
            }
            eventReader.nextEvent();
        }
    }

    /**
     * Applies attributes to fields annotated with @XMLAttribute.
     */
    private void applyAttributes(Class<?> clazz, Object instance, Iterator<Attribute> attrs) {
        List<Attribute> attrList = new ArrayList<>();
        attrs.forEachRemaining(attrList::add);

        Stream.of(clazz.getDeclaredFields())
                .filter(f -> f.isAnnotationPresent(XMLAttribute.class))
                .forEach(field -> {
                    XMLAttribute attribute = Objects.requireNonNull(field.getAnnotation(XMLAttribute.class));
                    String expectedName = attribute.name();
                    Optional<Attribute> attributeOptional = attrList.stream()
                            .filter(a -> a.getName().getLocalPart().equals(expectedName))
                            .findFirst();
                    if (attribute.required() && attributeOptional.isEmpty()) {
                        Optional.ofNullable(location).ifPresentOrElse(localization -> report.addErrors(Issue.builder().key(MISSING_ATTRIBUTES).variables(List.of(Map.entry("line_number", localization.getLineNumber()), Map.entry("column_number", localization.getColumnNumber())))), () -> {
                            report.addErrors(Issue.builder().key(ATTRIBUTES_ERROR).variables(List.of(Map.entry("element_number", stack.size()), Map.entry("element", clazz.getSimpleName()), Map.entry("attribute_name", expectedName))));
                        });
                    }
                    attributeOptional
                            .ifPresent(a -> {
                                try {
                                    Object val = convertType(field, field.getType().getName(), a.getValue());
                                    invokeSetter(clazz, field, instance, val);
                                } catch (ReflectiveOperationException | XMLAPIException ex) {
                                    String message = String.format("Failed setting attribute %s %s", expectedName, ex.getMessage());
                                    report.addErrors(Issue.builder().key(FIELD).message(message).variables(List.of(Map.entry("message", message))));
                                }
                            });
                });
    }

    /**
     * Finds a field annotated with @XMLElement or @XMLValue matching the given name.
     */
    private Optional<Field> findMatchingField(Class<?> clazz, String name) {
        return Stream.of(clazz.getDeclaredFields())
                .filter(f -> isMatchingField(f, name))
                .findFirst();
    }

    /**
     * Checks if a field has the correct annotation name for the given XML element.
     */
    private boolean isMatchingField(Field field, String xmlName) {
        if (field.isAnnotationPresent(XMLElement.class)) {
            return Objects.requireNonNull(field.getAnnotation(XMLElement.class)).name().equals(xmlName);
        } else if (field.isAnnotationPresent(XMLValue.class)) {
            return Objects.requireNonNull(field.getAnnotation(XMLValue.class)).name().equals(xmlName);
        }
        return false;
    }

    /**
     * Ensures a nested object field is initialized.
     */
    private Object ensureObjectInitialized(Object parent, Field field) throws ReflectiveOperationException, XMLAPIException {
        Class<?> parentClass = parent.getClass();
        String getter = "get" + StringUtils.capitalize(field.getName());
        Object existing;
        try {
            existing = parentClass.getDeclaredMethod(getter).invoke(parent);
        } catch (NoSuchMethodException | IllegalAccessException | InvocationTargetException e) {
            throw new XMLAPIException("Failed to invoke getter: " + getter, e);
        }

        if (existing == null) {
            Object newObj;
            try {
                newObj = field.getType().getDeclaredConstructor().newInstance();
            } catch (InstantiationException e) {
                throw new XMLAPIException("Cannot instantiate " + field.getType().getName(), e);
            }
            invokeSetter(parentClass, field, parent, newObj);
            return newObj;
        }
        return existing;
    }

    /**
     * Ensures a Collection field is not null/empty.
     */
    @SuppressWarnings("unchecked")
    private Collection<Object> ensureCollectionInitialized(Object parent, Field field) throws ReflectiveOperationException, XMLAPIException {
        Class<?> parentClass = parent.getClass();
        String getter = "get" + StringUtils.capitalize(field.getName());
        Collection<Object> coll;
        try {
            coll = (Collection<Object>) parentClass.getDeclaredMethod(getter).invoke(parent);
        } catch (NoSuchMethodException | IllegalAccessException | InvocationTargetException e) {
            throw new XMLAPIException("Failed to invoke getter: " + getter, e);
        }

        if (CollectionUtils.isEmpty(coll)) {
            coll = createCollection(field);
            invokeSetter(parentClass, field, parent, coll);
        }
        return coll;
    }

    /**
     * Creates an appropriate Collection instance based on the field type.
     */
    private Collection<Object> createCollection(Field field) {
        if (List.class.isAssignableFrom(field.getType())) {
            return new ArrayList<>();
        } else if (Set.class.isAssignableFrom(field.getType())) {
            return new HashSet<>();
        } else if (Queue.class.isAssignableFrom(field.getType())) {
            return new PriorityQueue<>();
        } else if (Deque.class.isAssignableFrom(field.getType())) {
            return new ArrayDeque<>();
        }
        return new ArrayList<>();
    }

    /**
     * Reflection-based setter for the field: calls setXxx on the parent object.
     */
    private void invokeSetter(Class<?> clazz, Field field, Object target, Object value) throws ReflectiveOperationException, XMLAPIException {
        String setter = "set" + StringUtils.capitalize(field.getName());
        try {
            clazz.getDeclaredMethod(setter, field.getType()).invoke(target, value);
        } catch (NoSuchMethodException e) {
            throw new XMLAPIException("Setter not found: " + setter, e);
        } catch (IllegalAccessException | InvocationTargetException e) {
            throw new XMLAPIException("Error calling setter: " + setter, e);
        }
    }

    /**
     * Reads the generic parameter from a Collection field.
     */
    @SuppressWarnings("unchecked")
    private Class<Object> getGenericType(Field field) throws XMLAPIException {
        try {
            return (Class<Object>) ((java.lang.reflect.ParameterizedType) field.getGenericType())
                    .getActualTypeArguments()[0];
        } catch (ClassCastException e) {
            throw new XMLAPIException("Failed to read generic type for field: " + field.getName(), e);
        }
    }

    /**
     * @return true if the stack is empty (thread-safe).
     */
    private boolean stackIsEmpty() {
        stackLock.lock();
        try {
            return stack.isEmpty();
        } finally {
            stackLock.unlock();
        }
    }

    /**
     * Pushes a new frame onto our LIFO stack.
     */
    private void pushFrame(String elementName, Object instance) {
        stackLock.lock();
        try {
            stack.push(new StackFrame(elementName, instance));
        } finally {
            stackLock.unlock();
        }
    }

    /**
     * Pops the top frame from our stack, or null if empty.
     */
    private void popFrame() {
        stackLock.lock();
        try {
            if (!stack.isEmpty()) {
                stack.pop();
            }
        } finally {
            stackLock.unlock();
        }
    }

    /**
     * @return The top frame, or null if empty.
     */
    private StackFrame peekFrame() {
        stackLock.lock();
        try {
            return stack.isEmpty() ? null : stack.peek();
        } finally {
            stackLock.unlock();
        }
    }

    private record StackFrame(String elementName, Object instance) {
    }
}