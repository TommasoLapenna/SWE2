package build;

import java.util.*;
import java.util.function.Function;

public abstract class InvertedIndexEnumMap<K extends Enum<K>, V> {

    protected final Map<K, List<V>> index;
    private final Function<V, Set<K>> keysOf;

    public InvertedIndexEnumMap(Class<K> keyType, Function<V, Set<K>> keysOf) {
        this.index = new EnumMap<>(keyType);
        this.keysOf = keysOf;
        for(K key : keyType.getEnumConstants()) {
            index.put(key, new ArrayList<>());
        }
    }

    public void add(V value) {
        /*
        keysOf.apply(value).forEach(k ->
                index.computeIfAbsent(k, x -> new ArrayList<>()).add(value));
         */

        Set<K> keys = keysOf.apply(value);
        for (K key : keys) {
            index.get(key).add(value);
        }

    }

    public boolean remove(V value) {
        //TODO REwrite, not correct
        /*
        keysOf.apply(value).forEach(k -> {
            List<V> set = index.get(k);
            if (set != null && set.remove(value) && set.isEmpty()) index.remove(k);
        });
         */
        boolean removed = false;
        Set<K> keys = keysOf.apply(value);
        for (K key : keys) {
            List<V> set = index.get(key);
            if (set != null && set.remove(value) && set.isEmpty()) {
                index.remove(key);
                removed = true;
            }
        }

        return removed;
    }

    public Set<V> lookup(Set<K> keys) {
        // VERY Important that this is a set, we don't want repeting values coming from different keys
        Set<V> result = new LinkedHashSet<>();
        keys.forEach(k -> result.addAll(index.getOrDefault(k, List.of())));
        return result;
    }

}
