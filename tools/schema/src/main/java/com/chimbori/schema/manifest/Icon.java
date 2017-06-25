package com.chimbori.schema.manifest;

public class Icon {
  public String src;

  public Icon(String src) {
    this.src = src;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;

    Icon icon = (Icon) o;

    return src != null ? src.equals(icon.src) : icon.src == null;
  }

  @Override
  public int hashCode() {
    return src != null ? src.hashCode() : 0;
  }
}
