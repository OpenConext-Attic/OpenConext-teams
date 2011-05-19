package nl.surfnet.coin.teams.util;

import java.util.Collection;

public class Contains {

  private Contains() {

  }
  
  public static boolean contains(Collection<?> coll, Object o) {
    return coll.contains(o);
  }

}
