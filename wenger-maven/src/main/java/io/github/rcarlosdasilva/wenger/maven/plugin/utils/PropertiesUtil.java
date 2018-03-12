package io.github.rcarlosdasilva.wenger.maven.plugin.utils;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;

import java.util.*;

public class PropertiesUtil {

  private static Stack<String> keys;

  public static synchronized Map<String, String> fromMap(Map<String, Object> map) {
    if (map == null) {
      return null;
    }

    keys = new Stack<>();
    return flat(map);
  }

  public static Map<String, String> getByKeyStartsWith(Properties prop, String start) {
    Map<String, String> result = Maps.newHashMap();
    for (Map.Entry<Object, Object> entry : prop.entrySet()) {
      String key = entry.getKey().toString();
      if (key.startsWith(start)) {
        result.put(key, entry.getValue().toString());
      }
    }
    return result;
  }

  public static List<String> getMultiKeysByStartsWith(Properties prop, String prefix) {
    Map<String, String> results = getByKeyStartsWith(prop, prefix);
    Set<String> keys = Sets.newHashSet();
    for (Map.Entry<String, String> entry : results.entrySet()) {
      String key = entry.getKey();
      int ind = key.indexOf("].", prefix.length());
      keys.add(entry.getKey().substring(0, ind + 1));
    }
    List<String> orderedKyes = Lists.newArrayList(keys);
    Collections.sort(orderedKyes);
    return orderedKyes;
  }

  public static Map<String, String> getByKeyStartsAndEndsWith(Properties prop, String start, String end) {
    Map<String, String> result = Maps.newHashMap();
    for (Map.Entry<Object, Object> entry : prop.entrySet()) {
      String key = entry.getKey().toString();
      if (key.startsWith(start) && key.endsWith(end) && (start.length() + end.length()) < key.length()) {
        result.put(key, entry.getValue().toString());
      }
    }
    return result;
  }

  @SuppressWarnings("unchecked")
  private static Map<String, String> flat(Map<String, Object> map) {
    Map<String, String> props = Maps.newHashMap();

    for (Map.Entry<String, Object> entry : map.entrySet()) {
      String key;
      if (keys.empty()) {
        key = entry.getKey();
      } else {
        key = keys.peek() + "." + entry.getKey();
      }
      keys.push(key);

      Object value = entry.getValue();
      if (value instanceof Map) {
        Map<String, Object> mv = (Map<String, Object>) value;
        props.putAll(flat(mv));
      } else if (value instanceof List) {
        List<Object> lv = (List<Object>) value;
        props.putAll(flat(lv));
      } else {
        props.put(key, String.valueOf(value));
      }

      keys.pop();
    }
    return props;
  }

  @SuppressWarnings("unchecked")
  private static Map<String, String> flat(List<Object> list) {
    Map<String, String> props = Maps.newHashMap();

    for (int i = 0; i < list.size(); i++) {
      String key = keys.peek() + "[" + i + "]";
      keys.push(key);

      Object value = list.get(i);
      if (value instanceof Map) {
        Map<String, Object> mv = (Map<String, Object>) value;
        props.putAll(flat(mv));
      } else {
        props.put(key, String.valueOf(value));
      }

      keys.pop();
    }

    return props;
  }

  public static boolean keyStartsWith(Map<String, ?> map, String prefix) {
    for (Map.Entry<String, ?> entry : map.entrySet()) {
      if (entry.getKey() != null && entry.getKey().startsWith(prefix)) {
        return true;
      }
    }
    return false;
  }

  public static int multiKeyCount(Map<String, ?> map, String prefix) {
    int count = 0;
    for (Map.Entry<String, ?> entry : map.entrySet()) {
      if (entry.getKey() != null && entry.getKey().startsWith(prefix)) {
        if (entry.getKey().length() > prefix.length() && entry.getKey().charAt(prefix.length()) == '[') {
          count++;
        }
      }
    }
    return count;
  }

}
