package build;

import build.rule.Severity;
import model.component.Component;
import specification.Specification;

import java.util.List;

public record BuildRule ( Specification<PcBuilder> specification,
                          Severity severity,
                          String message,
                          List<Component> InvolvedComponents) {

}
