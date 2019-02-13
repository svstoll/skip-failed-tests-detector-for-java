package ch.svenstoll.mbm.skipfailedtestdetectorforjava.utility;

import java.util.*;

public class StringUtility {

  public static boolean isNullOrEmpty(String s) {
    return s == null || s.isEmpty();
  }

  public static String concatStrings(Collection<String> strings, String delimiter) {
    if (CollectionUtility.isNullOrEmpty(strings)) {
      return "";
    }

    StringBuilder result = new StringBuilder();
    Iterator<String> iterator = strings.iterator();
    while (iterator.hasNext()) {
      String s = iterator.next();
      if (!StringUtility.isNullOrEmpty(s)) {
        result.append(s);
      }

      if (iterator.hasNext()) {
        result.append(delimiter);
      }
    }

    return result.toString();
  }

  public static List<String> fromConcatenatedStringsToList(String concatenatedStrings, String delimiter) {
    if (isNullOrEmpty(concatenatedStrings)) {
      return Collections.emptyList();
    }

    List<String> strings = new ArrayList<>();
    for (String s : concatenatedStrings.split(delimiter)) {
      if (!"".equals(s)) {
        strings.add(s);
      }
    }
    return strings;
  }
}
