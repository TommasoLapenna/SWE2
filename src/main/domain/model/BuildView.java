package main.domain.model;

import main.domain.model.attribute.ComponentType;
import main.domain.model.component.Component;

import java.util.List;
import java.util.Map;


//This is needed to generate a read only version of the main.main.build
public interface BuildView {
    String name();
    int totalWattage();
    double totalPrice();
    boolean hasComponent(ComponentType t);
    List<Component> getComponents(ComponentType t);
    int getComponentNumber(ComponentType t);
    Map<ComponentType, List<Component>> components();
    //<T extends Component> T getSingleComponent(ComponentType t, Class<T> c);
}
