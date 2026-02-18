# 📄 XmlLoom XML API

XmlLoom provides a high-performance, annotation-driven Java library for XML parsing and writing. It offers a 
declarative 
way to map Java objects to XML structures using simple annotations, supporting both small and large XML files through efficient streaming (SAX/Stax).

---

## 🚀 Quick Start

### Parsing XML (`XMLReader`)

The `XMLReader` class is used to map an XML file or stream into a Java object.

```java
XMLReader xmlReader = new XMLReader();
InputStream stream = getClass().getClassLoader().getResourceAsStream("data.xml");
MyRootObject result = xmlReader.read(stream, MyRootObject.class);
```

### Writing XML (`XMLWriter`)

The `XMLWriter` class is used to serialize a Java object into XML.

```java
XMLWriter xmlWriter = new XMLWriter().setFormatted(true);
xmlWriter.write(new FileOutputStream("output.xml"), myObject);
```

---

## 📝 Annotations Guide

XmlLoom uses four primary annotations to define the mapping between Java classes and XML.

### 1. `@XMLRoot`
Marks a class as the root element of an XML document.

*   **Usage:** Class level.
*   **Example:**
    ```java
    @XMLRoot(name = "network")
    public class NetworkProject {
        private ProjectProperties projectProperties;
    }
    ```
    *Maps to:* `<network> ... </network>`

### 2. `@XMLElement`
Marks a field as a child element. It can be used for single objects or lists of objects.

*   **Usage:** Field level.
*   **Example:**
    ```java
    public class ProjectProperties {
        @XMLElement(name = "projectPerimeters")
        private ProjectPerimeters projectPerimeters;
    }
    ```
    *Maps to:*
    ```xml
    <projectProperties projectName="name" projectVersion="1.0">
        <projectPerimeters> ... </projectPerimeters>
    </projectProperties>
    ```

### 3. `@XMLAttribute`
Maps a field to an attribute of the enclosing XML element.

*   **Usage:** Field level.
*   **Parameters:**
    *   `name`: The name of the attribute in XML.
    *   `required` (optional): Boolean indicating if the attribute must be present.
*   **Example:**
    ```java
    public class ProjectProperties {
        @XMLAttribute(name = "projectName")
        private String projectName;
        
        @XMLAttribute(name = "projectVersion", required = true)
        private String projectVersion;
    }
    ```
    *Maps to:* `<projectProperties projectName="MyProject" projectVersion="1.0">`

### 4. `@XMLValue`
Used to map the text content (body) of an element. This is particularly useful for lists of simple types or elements that contain only text.

*   **Usage:** Field level.
*   **Example (from `SecuritySystems.java`):**
    ```java
    public class SecuritySystems {
        @XMLValue(name = "SecuritySystem")
        private List<String> trainSecuritySystem;
    }
    ```
    *Maps to:*
    ```xml
    <SecuritySystems>
        <SecuritySystem>PMM</SecuritySystem>
        <SecuritySystem>MMP</SecuritySystem>
    </SecuritySystems>
    ```

---

## 🛠 Advanced Features

### Error Handling
You can retrieve warnings and issues encountered during parsing using the `XMLIssueReport`.

```java
XMLReader xmlReader = new XMLReader();
NetworkProject result = xmlReader.read(stream, NetworkProject.class);
XMLIssueReport reports = xmlReader.getWarningHandler();

if (reports != null && !reports.getIssues().isEmpty()) {
    reports.getIssues().forEach(issue -> System.out.println("Issue: " + issue.getMessage()));
}
```

### Formatted Output
When writing XML, you can enable "pretty-printing" for better readability.

```java
XMLWriter writer = new XMLWriter().setFormatted(true);
writer.write(System.out, myObject);
```

---

## 🏗 Real-World Example (from Tests)

Based on the `NetworkProject` test suite, here is how a complex structure is mapped:

**Java Model:**
```java
@XMLRoot(name = "network")
public class NetworkProject {
    @XMLElement(name = "projectProperties")
    private ProjectProperties projectProperties;
}

public class ProjectProperties {
    @XMLAttribute(name = "projectName")
    private String projectName;

    @XMLElement(name = "projectPerimeters")
    private ProjectPerimeters projectPerimeters;
}
```

**Resulting XML:**
```xml
<network>
    <projectProperties projectName="network">
        <projectPerimeters>
            <!-- ... -->
        </projectPerimeters>
    </projectProperties>
</network>
```

---

## 📦 Installation

- Maven:
  ```xml
  <dependency>
    <groupId>xmlloom</groupId>
    <artifactId>xmlloom</artifactId>
    <version>1.0.0</version>
  </dependency>
  ```
- Gradle (Kotlin DSL):
  ```kotlin
  implementation("xmlloom:xmlloom:1.0.0")
  ```
---

## ✍️ Writing Modes Explained (XMLWriter)

XmlLoom writes XML using a StAX-based streaming strategy (`StaxWritingStrategy`) behind the `XMLWriter` facade. You can control output style and destination.

### Output style
- Formatted (pretty-printed):
  ```java
  XMLWriter writer = new XMLWriter().setFormatted(true);
  writer.write(new FileOutputStream("output.xml"), myObject);
  ```
  - Pros: human-readable with indentation and newlines
  - Cons: slightly larger output size

- Compact (no pretty print, default):
  ```java
  new XMLWriter().write(System.out, myObject);
  ```
  - Pros: smallest payload
  - Cons: harder to read manually

### Output destinations
- `OutputStream` (recommended for files and large outputs):
  ```java
  try (OutputStream os = Files.newOutputStream(Path.of("out.xml"))) {
      new XMLWriter().setFormatted(true).write(os, myObject);
  }
  ```
- `Writer` (use when you must control charset or wrap in your own writer):
  ```java
  Writer w = new OutputStreamWriter(new FileOutputStream("out.xml"), StandardCharsets.UTF_8);
  new XMLWriter().write(w, myObject);
  ```

### Streaming and memory footprint
- Writing is streaming and does not build the whole XML in memory. This is suitable for very large documents.
- Avoid converting to `String` for large payloads; prefer writing directly to an `OutputStream`.

### Encoding, namespaces, and special characters
- Use a `Writer` configured with your desired charset if you need non-default encodings; otherwise the underlying StAX writer will use its defaults (commonly UTF-8).
- XML-reserved characters in text or attributes are escaped by the writer.
- Namespace prefixes and URIs can be introduced via your model and annotations (advanced usage).

### Nulls, empties, and collections
- Fields without values are omitted by default.
- `@XMLValue` collections produce repeated simple elements:
  ```java
  public class SecuritySystems {
      @XMLValue(name = "SecuritySystem")
      private List<String> securitySystem;
  }
  // => <SecuritySystems><SecuritySystem>PMM</SecuritySystem>...</SecuritySystems>
  ```

### Error handling
- Writing failures raise `XMLAPIException`. Catch and handle or let them surface to your framework:
  ```java
  try {
      new XMLWriter().write(System.out, myObject);
  } catch (XMLAPIException e) {
      // log and handle
  }
  ```

### Thread-safety
- Create one `XMLWriter` instance per concurrent write. Internally, commands are executed via an invoker, keeping instances lightweight.

### Strategy customization (advanced)
- `XMLWriter` uses `XMLWritingStrategy` under the hood. If you need a custom strategy (e.g., different StAX configuration), extend `XMLWritingStrategy` and wire it through a custom command.