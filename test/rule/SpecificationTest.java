package rule;

import main.domain.model.PcBuilder;
import main.domain.model.attribute.ComponentType;
import main.domain.rule.specification.BuildSpecification;
import main.domain.rule.specification.PredicateSpecification;
import org.junit.jupiter.api.Test;

import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/** Operatore implies del pattern Specification, su cui si basano tutte le regole con guardia. */
class SpecificationTest {

    PcBuilder build = new PcBuilder("test");

    // specifiche banali (sempre vera / sempre falsa): si prova solo la logica, non le regole
    BuildSpecification alwaysTrue = new PredicateSpecification(b -> true, Set.of(ComponentType.CPU));
    BuildSpecification alwaysFalse = new PredicateSpecification(b -> false, Set.of(ComponentType.GPU));

    @Test
    void impliesIsFalseOnlyWhenTheGuardIsTrueAndTheCheckIsFalse() {
        assertTrue(alwaysTrue.implies(alwaysTrue).isSatisfiedBy(build));
        assertFalse(alwaysTrue.implies(alwaysFalse).isSatisfiedBy(build));
        assertTrue(alwaysFalse.implies(alwaysTrue).isSatisfiedBy(build));
        assertTrue(alwaysFalse.implies(alwaysFalse).isSatisfiedBy(build));   // guardia falsa: vera "a vuoto"
    }
}
