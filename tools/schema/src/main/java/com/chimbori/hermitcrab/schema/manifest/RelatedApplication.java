package com.chimbori.hermitcrab.schema.manifest;

public class RelatedApplication {
  private static final String PLAY_STORE_URL_TEMPLATE = "https://play.google.com/store/apps/details?id=";
  private static final String GOOGLE_PLAY = "play";

  public String id;
  public String platform;
  public String url;

  public RelatedApplication(String appId) {
    this.id = appId;
    this.platform = GOOGLE_PLAY;
    this.url = PLAY_STORE_URL_TEMPLATE + appId;
  }
}
