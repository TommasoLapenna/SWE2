package build;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class ViolationList {
    private Map<String, Violation> violations = new LinkedHashMap<>();
    void add(Violation v)       { violations.put(v.rule(), v); }
    void clear(String ruleName) { violations.remove(ruleName); }

    public List<Violation> violations() { return new ArrayList<>(violations.values()); }
}
