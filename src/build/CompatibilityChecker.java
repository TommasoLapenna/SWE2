package build;

import build.rule.*;
import observer.Observer;
import specification.Specification;


import java.util.List;

public class CompatibilityChecker implements Observer<BuildEvent> {
    private final PcBuilder build;
    private List<BuildRule> rules;

    public CompatibilityChecker(PcBuilder build, List<BuildRule> rules) {
        this.build = build;
        this.rules = rules;
    }

    public List<BuildRule> rules() {
        return List.of(
                new BuildRule(
                        new SocketMatchSpecification().and(new VoltageMatchSpecification()),
                        Severity.ERROR,
                        "CPU socket and motherboard voltage must be compatible",
                        List.of()
                )
        );
    }

    @Override
    public void update(BuildEvent event) {
        //Calls Specification Checking
    }
}
