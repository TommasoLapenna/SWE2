package application;

import build.rule.BuildRule;
import build.rule.Severity;
import build.specification.Specs;
import model.attribute.ComponentType;

import java.util.List;

public final class DefaultRules {
    public static List<BuildRule> create() {
        return List.of(
                new BuildRule(
                        1,
                        "MISSING-CPU",
                        Specs.atLeast(ComponentType.CPU, 1),
                        Severity.ERROR,
                        "At least one CPU is required"
                ),
                new BuildRule(
                        2,
                        "TOO-MANY-CPUS",
                        Specs.atMost(ComponentType.CPU, 1),
                        Severity.ERROR,
                        "Only one CPU is allowed, found: {CPU}"
                ),
                new BuildRule(
                        3,
                        "MISSING-MOBO",
                        Specs.atLeast(ComponentType.MOTHERBOARD, 1),
                        Severity.WARNING,
                        "At least one Motherboard is required"
                ),
                new BuildRule(
                        2,
                        "TOO-MANY-MOBOS",
                        Specs.atMost(ComponentType.MOTHERBOARD, 1),
                        Severity.ERROR,
                        "Only one motherboard is allowed, found: {MOTHERBOARD}"
                ),
                new BuildRule(
                        1,
                        "MISSING-PSU",
                        Specs.atLeast(ComponentType.POWER_SUPPLY, 1),
                        Severity.WARNING,
                        "At least one Power Supply is required"
                ),
                new BuildRule(
                        6,
                        "TOO-MANY-PSUS",
                        Specs.atMost(ComponentType.POWER_SUPPLY, 1),
                        Severity.ERROR,
                        "Only one Power Supply is allowed, found: {POWER_SUPPLY}"
                ),
                new BuildRule(
                        6,
                        "MISSING-RAM",
                        Specs.atLeast(ComponentType.RAM, 1),
                        Severity.ERROR,
                        "Ram is missing"
                ),
                new BuildRule(
                        7,
                        "MISSING-STORAGE",
                        Specs.atLeast(ComponentType.STORAGE, 1),
                        Severity.WARNING,
                        "Storage is missing"
                ),
                new BuildRule(
                        8,
                        "MISSING-CASE",
                        Specs.atLeast(ComponentType.CASE, 1),
                        Severity.ERROR,
                        "Case is missing"
                ),
                new BuildRule(
                        9,
                        "TOO-MANY-CASES",
                        Specs.atMost(ComponentType.CASE, 1),
                        Severity.ERROR,
                        "Only one case allowed, found: {CASE}"
                ),
                new BuildRule(
                        10,
                        "PSU-SUFFICIENT-WATTS",
                        Specs.exactly(ComponentType.POWER_SUPPLY, 1).implies(Specs.psuSufficientWattsSpecification),
                        Severity.ERROR,
                        "Power supply must provide sufficient watts"
                ),
                new BuildRule(
                        11,
                        "SOCKET-MISMATCH",
                        Specs.mutipleExcatclyOne(ComponentType.CPU, ComponentType.MOTHERBOARD).implies(Specs.socketMatchSpecification),
                        Severity.ERROR,
                        "CPU {CPU} and Motherboard {MOTHERBOARD} socket must match"
                ),
                new BuildRule(
                        12,
                        "FORM-FACTOR-MISMATCH",
                        Specs.mutipleExcatclyOne(ComponentType.MOTHERBOARD, ComponentType.CASE).implies(Specs.formFactorMatchSpecification),
                        Severity.ERROR,
                        "Motherboard {MOTHERBOARD} form factor must match case {CASE}"
                )
        );
    }
}
