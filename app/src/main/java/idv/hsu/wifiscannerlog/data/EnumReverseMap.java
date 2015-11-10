package idv.hsu.wifiscannerlog.data;

import java.util.HashMap;
import java.util.Map;

public class EnumReverseMap<V extends Enum<V> & EnumConverter> {
    private Map<Integer, V> map = new HashMap<Integer, V>();
    public EnumReverseMap(Class<V> valueType) {
        for (V v : valueType.getEnumConstants()) {
            map.put(v.convert(), v);
        }
    }

    public V get(int num) {
        return map.get(Integer.valueOf(num));
    }
}
