package build;

import build.rule.*;
import model.attribute.ComponentType;
import observer.Observer;
import specification.Specification;


import java.util.ArrayList;
import java.util.List;

public class CompatibilityChecker implements Observer<BuildEvent> {
    private final PcBuilder build;
    private List<BuildRule> rules = new ArrayList<>();

    public CompatibilityChecker(PcBuilder build, List<BuildRule> rules) {
        this.build = build;
        //this.rules = rules;
        rules.add(
          new BuildRule(Specs.atMost(ComponentType.CPU,1), )
        );
    }

    @Override
    public void update(BuildEvent event) {
        //Calls Specification Checking
    }
}
