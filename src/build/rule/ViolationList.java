package build.rule;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class ViolationList {
    private Map<String, Violation> violations = new LinkedHashMap<>();
    public void add(Violation v)       { violations.put(v.rule(), v); }
    public void clear(String ruleName) { violations.remove(ruleName); }

    public List<Violation> violations() { return new ArrayList<>(violations.values()); }

    public boolean containsWarning() { return violations.values().stream().anyMatch(v -> v.severity() == Severity.WARNING); }
    public boolean containsError()   { return violations.values().stream().anyMatch(v -> v.severity() == Severity.ERROR); }

}
