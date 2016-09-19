package com.chimbori.liteapps;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

/**
 * Scrapes a web site using JSoup to identify elements such as the name of a site, a dominant theme
 * color, and a list of likely top-level bookmarks. This may be used to bootstrap the manifest for
 * a Lite App.
 */
public class Scraper {
  private static final int FETCH_TIMEOUT_MS = 10000;
  private static final String CHROME_MOBILE_USER_AGENT = "Mozilla/5.0 (Linux; Android 6.0; Nexus 5 Build/MRA58N) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/48.0.2564.23 Mobile Safari/537.36";
  private static final String GOOGLE_COM_HOME_PAGE = "http://www.google.com";

  private final String url;
  private Document doc;

  public static class SiteMetadata {
    public String title = "";
    public String themeColor = "";
    public String iconUrl = "";
    public Collection<Bookmark> bookmarks;

    @Override
    public String toString() {
      return "SiteMetadata{" +
          "title='" + title + '\'' +
          ", themeColor='" + themeColor + '\'' +
          ", iconUrl='" + iconUrl + '\'' +
          ", bookmarks=" + bookmarks +
          '}';
    }
  }

  public static class Bookmark {
    public String url = "";
    public String title = "";

    @Override
    public boolean equals(Object o) {
      if (this == o) {
        return true;
      }
      if (o == null || getClass() != o.getClass()) {
        return false;
      }
      Bookmark bookmark = (Bookmark) o;
      return url.equals(bookmark.url);
    }

    @Override
    public int hashCode() {
      return url.hashCode();
    }

    public Bookmark url(String url) {
      this.url = url;
      return this;
    }

    public Bookmark title(String title) {
      this.title = title;
      return this;
    }

    @Override
    public String toString() {
      return "Bookmark{" +
          "url='" + url + '\'' +
          ", title='" + title + '\'' +
          '}';
    }
  }

  public static SiteMetadata scrape(String url) {
    return new Scraper(url).fetch().parse();
  }

  public Scraper(String url) {
    this.url = url;
  }

  private Scraper fetch() {
    try {
      System.out.println(String.format("Fetching %s…", url));
      doc = Jsoup.connect(url)
          .ignoreContentType(true)
          .userAgent(CHROME_MOBILE_USER_AGENT)
          .referrer(GOOGLE_COM_HOME_PAGE)
          .timeout(FETCH_TIMEOUT_MS)
          .followRedirects(true)
          .execute().parse();
    } catch (IOException e) {
      System.err.println(e.getMessage());
    }
    return this;
  }

  public SiteMetadata parse() {
    SiteMetadata metadata = new SiteMetadata();
    if (doc == null) {  // Fetch failed or never fetched.
      metadata.bookmarks = new ArrayList<>();  // So callers need not check for null-ness.
      return metadata;  // NonNull {@code SiteMetadata}, but empty fields.
    }

    // First try to get the Site’s name (not just the current page’s title) via OpenGraph tags.
    metadata.title = doc.select("meta[property=og:site_name]").attr("content");
    if (metadata.title == null || metadata.title.isEmpty()) {
      // But if the page isn’t using OpenGraph tags, then fallback to using the current page’s title.
      metadata.title = doc.select("title").text();
    }
    metadata.themeColor = doc.select("meta[name=theme-color]").attr("content");
    // The "abs:" prefix is a JSoup shortcut that converts this into an absolute URL.
    metadata.iconUrl = doc.select("link[rel=icon]").attr("abs:href");

    metadata.bookmarks = findBookmarkableLinks();

    return metadata;
  }

  private Collection<Bookmark> findBookmarkableLinks() {
    // Use a Map so we can ensure that we only keep one Bookmark per URL if the same URL appears
    // more than once in the navigation links.
    Map<String, Bookmark> bookmarkableLinks = new HashMap<>();

    Elements ariaRoleNavigation = doc.select("*[role=navigation]").select("a[href]");
    for (Element navLink : ariaRoleNavigation) {
      String linkUrl = navLink.attr("abs:href");
      bookmarkableLinks.put(linkUrl, new Bookmark().url(linkUrl).title(navLink.text()));
    }

    if (bookmarkableLinks.isEmpty()) {
      Elements likelyNavigationLinks = doc.select("nav, .nav, #nav, .navbar, #navbar, .navigation, #navigation").select("a[href]");
      for (Element navLink : likelyNavigationLinks) {
        String linkUrl = navLink.attr("abs:href");
        bookmarkableLinks.put(linkUrl, new Bookmark().url(linkUrl).title(navLink.text()));
      }
    }

    return bookmarkableLinks.values();
  }
}
