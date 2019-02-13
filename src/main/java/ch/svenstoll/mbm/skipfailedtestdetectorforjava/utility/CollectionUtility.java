package ch.svenstoll.mbm.skipfailedtestdetectorforjava.utility;

import java.util.Collection;

public class CollectionUtility {

  public static boolean isNullOrEmpty(Collection collection) {
    return collection == null || collection.isEmpty();
  }
}
