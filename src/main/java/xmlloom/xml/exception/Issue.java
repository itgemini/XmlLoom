package xmlloom.xml.exception;

import java.util.List;
import java.util.Map;

public class Issue {
    private IssueKey key;
    private List<Map.Entry<String, Object>> variables;
    private String message;

    public static Issue builder() {
        return new Issue();
    }

    public Issue key(IssueKey key) {
        this.key = key;
        return this;
    }

    public IssueKey getKey() {
        return key;
    }

    public Issue variables(List<Map.Entry<String, Object>> variables) {
        this.variables = variables;
        return this;
    }

    public List<Map.Entry<String, Object>> getVariables() {
        return variables;
    }

    public Issue message(String message) {
        this.message = message;
        return this;
    }

    public String getMessage() {
        return message;
    }
}
