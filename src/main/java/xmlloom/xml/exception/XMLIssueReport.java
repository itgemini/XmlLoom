package com.datashepherd.xml.exception;

import java.util.ArrayList;
import java.util.List;

public class XMLIssueReport {
    private final List<Issue> warnings = new ArrayList<>();
    private final List<Issue> errors = new ArrayList<>();


    public void addWarning(Issue issue) {
        warnings.add(issue);
    }

    public void addErrors(Issue issue) {
        errors.add(issue);
    }

    public List<Issue> getWarnings() {
        return warnings;
    }

    public List<Issue> getErrors() {
        return errors;
    }

    public boolean hasWarnings() {
        return !warnings.isEmpty();
    }

    public boolean hasErrors() {
        return !errors.isEmpty();
    }
}
