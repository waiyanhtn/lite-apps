package com.chimbori.schema.manifest;

public class Endpoint {
  public String url = "";
  public String name = "";
  public String selector = "";

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    Endpoint endpoint = (Endpoint) o;
    return url.equals(endpoint.url);
  }

  @Override
  public int hashCode() {
    return url.hashCode();
  }

  public Endpoint url(String url) {
    this.url = url;
    return this;
  }

  public Endpoint name(String name) {
    this.name = name;
    return this;
  }

  @Override
  public String toString() {
    return "Endpoint{" +
        "url='" + url + '\'' +
        ", name='" + name + '\'' +
        '}';
  }
}
