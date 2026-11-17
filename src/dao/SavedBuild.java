package dao;

import build.BuildView;
import model.attribute.ComponentType;
import model.component.Component;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;

/**
 * Una build salvata nel database: id, nome, data di creazione (UTC) e componenti.
 *
 * <p>E' anche una {@link BuildView}: si legge (e si stampa con BuildPrinter) come una build in
 * memoria, e si puo' passare di nuovo a {@link BuildDao#save} o {@link BuildDao#update}.
 *
 * <p>La mappa contiene TUTTI i tipi di componente (lista vuota se non ce ne sono), come quella di
 * PcBuilder. Ogni lista ha un elemento per esemplare: due SSD uguali compaiono due volte (nel
 * database sono una riga con quantity = 2). Dentro lo stesso tipo l'ordine e' per id, non quello
 * di inserimento nella build.
 *
 * <p>Prezzo e consumo totali sono attributi derivati: si calcolano, non si salvano.
 */
public record SavedBuild(int id, String name, LocalDateTime createdAt,
                         Map<ComponentType, List<Component>> components) implements BuildView {

    public SavedBuild {
        Map<ComponentType, List<Component>> copy = new EnumMap<>(ComponentType.class);
        for (ComponentType type : ComponentType.values()) {
            copy.put(type, List.copyOf(components.getOrDefault(type, List.of())));
        }
        components = Collections.unmodifiableMap(copy);
    }

    @Override
    public List<Component> getComponents(ComponentType type) {
        return components.get(type);
    }

    @Override
    public boolean hasComponent(ComponentType type) {
        return !components.get(type).isEmpty();
    }

    @Override
    public int getComponentNumber(ComponentType type) {
        return components.get(type).size();
    }

    @Override
    public int totalWattage() {
        return allComponents().mapToInt(Component::powerDrawWatts).sum();
    }

    public double totalPrice() {
        return allComponents().mapToDouble(Component::price).sum();
    }

    private java.util.stream.Stream<Component> allComponents() {
        return components.values().stream().flatMap(List::stream);
    }
}
