package ch.svenstoll.mbm.skipfailedtestdetectorforjava.utility;

public class BooleanUtility {

  public static boolean nvl(Boolean bool) {
    return bool != null && bool;
  }
}
