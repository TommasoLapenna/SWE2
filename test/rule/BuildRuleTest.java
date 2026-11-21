package rule;

import main.domain.model.attribute.ComponentType;
import main.domain.rule.BuildRule;
import main.domain.rule.Severity;
import main.domain.rule.specification.BuildSpecification;
import main.domain.rule.specification.PredicateSpecification;
import org.junit.jupiter.api.Test;

import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertThrows;

/** Il messaggio di una regola puo' citare solo i componenti da cui la regola dipende. */
class BuildRuleTest {

    @Test
    void placeholderOfAComponentTheRuleDoesNotDependOnIsRejected() {
        BuildSpecification cpuOnly = new PredicateSpecification(b -> true, Set.of(ComponentType.CPU));

        assertThrows(IllegalArgumentException.class,
                () -> new BuildRule(1, "TEST", cpuOnly, Severity.ERROR, "Problema con {GPU}"));
    }
}
