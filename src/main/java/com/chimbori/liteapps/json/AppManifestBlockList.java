package com.chimbori.liteapps.json;

import java.util.Arrays;

public class AppManifestBlockList {
  public String name;
  public String updated;
  public String[] hosts;

  @Override
  public String toString() {
    return "AppManifestBlockList{" +
        "name='" + name + '\'' +
        ", updated='" + updated + '\'' +
        ", hosts=" + Arrays.toString(hosts) +
        '}';
  }
}
