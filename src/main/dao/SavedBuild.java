package main.dao;

import main.domain.model.component.Component;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Una build salvata nel database: id, nome, data di creazione (UTC) e componenti.
 *
 * <p>La lista contiene un elemento per ogni esemplare: due SSD uguali compaiono due volte
 * (nel database sono una riga con quantity = 2). L'ordine e' fisso: per tipo di componente
 * (nell'ordine dell'enum ComponentType) e poi per id.
 *
 * <p>Prezzo e consumo totali sono attributi derivati: si calcolano, non si salvano.
 */
public record SavedBuild(int id, String name, LocalDateTime createdAt, List<Component> components) {

    public SavedBuild {
        components = List.copyOf(components);
    }

    public double totalPrice() {
        return components.stream().mapToDouble(Component::price).sum();
    }

    public int totalPowerDrawWatts() {
        return components.stream().mapToInt(Component::powerDrawWatts).sum();
    }
}
