package com.chimbori.liteapps.json;

import java.util.Arrays;

public class BlockListsIndex {
  public BlockList[] blocklists;

  public static class BlockList {
    public String blocklist;
    public String name;
    public Source[] sources;

    public static class Source {
      public String url;
      public String name;

      @Override
      public String toString() {
        return "Source{" +
                "url='" + url + '\'' +
                ", name='" + name + '\'' +
                '}';
      }
    }

    @Override
    public String toString() {
      return "BlockList{" +
              "blocklist='" + blocklist + '\'' +
              ", name='" + name + '\'' +
              ", sources=" + Arrays.toString(sources) +
              '}';
    }
  }

  @Override
  public String toString() {
    return "BlockListsIndex{" +
            "blocklists=" + Arrays.toString(blocklists) +
            '}';
  }
}
