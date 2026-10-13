package build;

import java.util.*;
import java.util.function.Function;

// Abstract?????
public abstract class InvertedIndexMap<K extends Enum<K>, V> {

    protected final Map<K, List<V>> index;
    private final Function<V, Set<K>> keysOf;

    public InvertedIndexMap(Class<K> keyType, Function<V, Set<K>> keysOf) {
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
        // FIXED: the original version removed the key from the index when its list became empty, so a later
        // add() on that key found no list (NPE); it also returned true only when a list became empty.
        // Old (lambda) version kept below for reference, it had the same problem:
        /*
        keysOf.apply(value).forEach(k -> {
            List<V> set = index.get(k);
            if (set != null && set.remove(value) && set.isEmpty()) index.remove(k);
        });
         */
        boolean removed = false;
        Set<K> keys = keysOf.apply(value);
        for (K key : keys) {
            // Keys are pre-initialized in the constructor and never removed, so the list is always present.
            // |= -> the result is true if the value was removed from at least one key
            removed |= index.get(key).remove(value);
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
