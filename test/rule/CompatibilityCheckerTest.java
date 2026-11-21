package rule;

import main.application.DefaultRules;
import main.domain.model.PcBuilder;
import main.domain.model.attribute.ComponentType;
import main.domain.model.attribute.EfficencyRating;
import main.domain.model.attribute.FormFactor;
import main.domain.model.attribute.MemoryGen;
import main.domain.model.attribute.PsuFormFactor;
import main.domain.model.attribute.Socket;
import main.domain.model.component.Cpu;
import main.domain.model.component.Motherboard;
import main.domain.model.component.PowerSupply;
import main.domain.rule.CompatibilityChecker;
import main.domain.rule.Severity;
import main.domain.rule.Violation;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Verifica di compatibilita' con le regole vere di DefaultRules: non tutte le 40, una per categoria
 * (piattaforma, guardie, alimentazione, cardinalita'). Le violazioni si cercano per NOME della regola,
 * cosi' se cambia il testo di un messaggio i test non si rompono.
 */
class CompatibilityCheckerTest {

    PcBuilder build;
    CompatibilityChecker checker;

    @BeforeEach
    void setUp() {
        build = new PcBuilder("test");
        checker = new CompatibilityChecker(build, DefaultRules.create());
    }

    /** AM5, 105 W. */
    private static Cpu newAm5Cpu() {
        return new Cpu(1, "AMD", "Ryzen 7 7700X", 299.0, 105, Socket.AM5, 8, 16, 4.5,
                Set.of(MemoryGen.DDR5), true, false);
    }

    /** LGA1700: socket diverso dalla CPU AM5. */
    private static Cpu newLga1700Cpu() {
        return new Cpu(2, "Intel", "Core i5-13600K", 319.0, 125, Socket.LGA1700, 14, 20, 3.5,
                Set.of(MemoryGen.DDR4, MemoryGen.DDR5), true, false);
    }

    /** 40 W. */
    private static Motherboard newMotherboard(Socket socket) {
        return new Motherboard(10, "ASUS", "Scheda " + socket, 239.0, socket, "B850",
                FormFactor.ATX, MemoryGen.DDR5, 4, 192, 8000, 2, 3, 4);
    }

    private static PowerSupply newPsu(int watts) {
        return new PowerSupply(20, "Corsair", "RM" + watts, 99.0, watts, EfficencyRating.GOLD, PsuFormFactor.ATX, 3);
    }

    private Optional<Violation> violation(String ruleName) {
        return checker.violations().stream().filter(v -> v.rule().equals(ruleName)).findFirst();
    }

    @Test
    void differentSocketsProduceAnError() {
        build.addComponent(newAm5Cpu());
        build.addComponent(newMotherboard(Socket.LGA1700));

        assertEquals(Severity.ERROR, violation("SOCKET-MISMATCH").orElseThrow().severity());
    }

    @Test
    void guardedRuleIsNotCheckedWhenAComponentIsMissing() {
        build.addComponent(newAm5Cpu());

        assertTrue(violation("SOCKET-MISMATCH").isEmpty());   // senza scheda madre la guardia e' falsa
        assertTrue(violation("MISSING-MOBO").isPresent());    // la mancanza la segnala un'altra regola
    }

    @Test
    void psuWithoutTheRecommendedHeadroomProducesAWarning() {
        build.addComponent(newAm5Cpu());               // 105 W
        build.addComponent(newMotherboard(Socket.AM5)); //  40 W -> 145 W, con il 20% di margine servono 174 W
        build.addComponent(newPsu(160));

        assertTrue(violation("PSU-SUFFICIENT-WATTS").isEmpty());
        assertEquals(Severity.WARNING, violation("PSU-LOW-HEADROOM").orElseThrow().severity());
    }

    @Test
    void violationDisappearsWhenTheIncompatibleComponentIsRemoved() {
        Motherboard wrongBoard = newMotherboard(Socket.LGA1700);
        build.addComponent(newAm5Cpu());
        build.addComponent(wrongBoard);
        assertTrue(violation("SOCKET-MISMATCH").isPresent());

        build.removeComponent(wrongBoard);

        assertTrue(violation("SOCKET-MISMATCH").isEmpty());
    }

    @Test
    void aSecondCpuIsAddedButReportedAsAnError() {
        build.addComponent(newAm5Cpu());
        build.addComponent(newLga1700Cpu());

        assertEquals(2, build.getComponentNumber(ComponentType.CPU));   // l'aggiunta non viene bloccata
        assertEquals(Severity.ERROR, violation("TOO-MANY-CPUS").orElseThrow().severity());
    }
}
