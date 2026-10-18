package build;

import model.attribute.ComponentType;
import model.component.Component;

import java.util.List;
import java.util.Map;


//This is needed to generate a read only version of the build
public interface BuildView {
    String name();
    int totalWattage();
    boolean hasComponent(ComponentType t);
    List<Component> getComponents(ComponentType t);
    int getComponentNumber(ComponentType t);
    Map<ComponentType, List<Component>> components();
    //<T extends Component> T getSingleComponent(ComponentType t, Class<T> c);
}
