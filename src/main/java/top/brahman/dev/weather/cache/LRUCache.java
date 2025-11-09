package top.brahman.dev.weather.cache;

import top.brahman.dev.weather.util.Util;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.function.Consumer;

import static top.brahman.dev.weather.util.Util.CACHE_CAPACITY;
/**
 * A thread-safe implementation of an LRU (Least Recently Used) cache.
 * <p>
 * This cache automatically removes the least recently accessed entry
 * once the number of elements exceeds the defined capacity {@link Util#CACHE_CAPACITY}.
 * It is backed by a {@link LinkedHashMap} configured for access order,
 * meaning that every {@link #get} or {@link  #put} operation updates
 * the order of entries.
 * </p>
 *
 * <h2>Usage in the Weather SDK</h2>
 * <ul>
 *   <li>Used to store recent weather information for requested cities.</li>
 *   <li>Helps prevent unnecessary API calls if data is still valid.</li>
 *   <li>Ensures the SDK stores no more than {@value Util#CACHE_CAPACITY} cities at a time.</li>
 * </ul>
 *
 * <h3>Thread Safety</h3>
 * <p>
 * All public methods are synchronized, making this cache safe for use
 * across multiple threads.
 * </p>
 *
 * @param <K> the type of keys maintained by this cache (e.g., city name)
 * @param <V> the type of cached values (e.g., weather data object)
 */
public class LRUCache<K, V> {
    private final Map<K, V> map;

    public LRUCache() {
        this.map = new LinkedHashMap<>(CACHE_CAPACITY + 1, 1f, true) {
            @Override
            protected boolean removeEldestEntry(Map.Entry<K, V> eldest) {
                return size() > CACHE_CAPACITY;
            }
        };
    }

    public synchronized V get(K key) {
        return map.get(key);
    }

    public synchronized void put(K key, V value) {
        map.put(key, value);
    }

    public synchronized boolean containsKey(K key) {
        return map.containsKey(key);
    }

    public synchronized int size() {
        return map.size();
    }

    public synchronized void forEachValue(Consumer<V> consumer) {
        map.values().forEach(consumer);
    }
}
