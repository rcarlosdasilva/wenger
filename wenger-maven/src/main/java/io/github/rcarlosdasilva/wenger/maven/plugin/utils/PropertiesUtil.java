package io.github.rcarlosdasilva.wenger.maven.plugin.utils;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

import java.util.List;
import java.util.Map;
import java.util.Stack;

public class PropertiesUtil {

  private static Stack<String> keys;

  public static synchronized Map<String, String> fromMap(Map<String, Object> map) {
    if (map == null) {
      return null;
    }

    keys = new Stack<>();
    return flat(map);
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

  public static void main(String[] args) {
    Map<String, Object> spring = Maps.newHashMap();
    spring.put("str", "abc");
    spring.put("int", 123);

    Map<String, Object> profile = Maps.newHashMap();
    profile.put("hahha", "aaa");
    profile.put("ggagag", "bbb");
    spring.put("profiles", profile);

    List<String> active = Lists.newArrayList();
    active.add("test");
    active.add("devel");
    active.add("prod");
    profile.put("active", active);

    List<Object> odd = Lists.newArrayList();
    Map<String, Object> m1 = Maps.newHashMap();
    Map<String, Object> m2 = Maps.newHashMap();
    m1.put("a", 1);
    m1.put("b", 2);
    m1.put("c", 3);
    m2.put("x", 10);
    m2.put("y", 20);
    m2.put("z", 30);
    odd.add(m1);
    odd.add(m2);
    spring.put("odd", odd);

    Map<String, String> prop = fromMap(spring);
    System.out.println(prop);
  }

}
