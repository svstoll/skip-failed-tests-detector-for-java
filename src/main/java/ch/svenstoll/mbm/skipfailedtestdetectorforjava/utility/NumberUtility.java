package ch.svenstoll.mbm.skipfailedtestdetectorforjava.utility;

public class NumberUtility {

  public static Integer parseIntegerSafely(String s) {
    if (StringUtility.isNullOrEmpty(s)) {
      return null;
    }
    try {
      return Integer.parseInt(s);
    }
    catch (NumberFormatException e) {
      return null;
    }
  }

  public static Long parseLongSafely(String s) {
    if (StringUtility.isNullOrEmpty(s)) {
      return null;
    }
    try {
      return Long.parseLong(s);
    }
    catch (NumberFormatException e) {
      return null;
    }
  }

  public static Integer calculateDelta(Integer n1, Integer n2) {
    if (n1 == null || n2 == null) {
      return null;
    }
    return n2 - n1;
  }
}
