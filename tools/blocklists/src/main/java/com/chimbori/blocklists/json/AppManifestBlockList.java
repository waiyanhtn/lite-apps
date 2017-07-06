package com.chimbori.blocklists.json;

import com.chimbori.hermitcrab.schema.common.SchemaDate;

import java.util.Arrays;

public class AppManifestBlockList {
  public String name;
  public SchemaDate updated;
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
