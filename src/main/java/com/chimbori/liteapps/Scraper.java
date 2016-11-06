package com.chimbori.liteapps;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

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
    public Collection<Endpoint> bookmarks;
    public Collection<Endpoint> feeds;
    public Collection<String> relatedApps;

    @Override
    public String toString() {
      return "SiteMetadata{" +
          "title='" + title + '\'' +
          ", themeColor='" + themeColor + '\'' +
          ", iconUrl='" + iconUrl + '\'' +
          ", bookmarks=" + bookmarks +
          ", feeds=" + feeds +
          ", relatedApps=" + relatedApps +
          '}';
    }
  }

  public static class Endpoint {
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

    public Endpoint title(String title) {
      this.title = title;
      return this;
    }

    @Override
    public String toString() {
      return "Endpoint{" +
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
      metadata.feeds = new ArrayList<>();  // So callers need not check for null-ness.
      metadata.relatedApps = new ArrayList<>();  // So callers need not check for null-ness.
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
    metadata.iconUrl = doc.select("link[rel=apple-touch-icon]").attr("abs:href");
    if (metadata.iconUrl == null || metadata.iconUrl.isEmpty()) {
      metadata.iconUrl = doc.select("link[rel=apple-touch-icon-precomposed]").attr("abs:href");
    }

    metadata.bookmarks = findBookmarkableLinks();
    metadata.feeds = findAtomAndRssFeeds();
    metadata.relatedApps = findRelatedApps();

    return metadata;
  }

  private Collection<String> findRelatedApps() {
    List<String> relatedApps = new ArrayList<>();
    Elements playStoreLinks = doc.select("a[href*=play.google.com]");
    for (Element playStoreLink : playStoreLinks) {
      String playStoreUrl = playStoreLink.attr("href");
      System.out.println(playStoreUrl);
      Matcher matcher = Pattern.compile("id=([^&]+)").matcher(playStoreUrl);
      while (matcher.find()) {
        relatedApps.add(matcher.group(1));
      }
    }
    return relatedApps;
  }

  private Collection<Endpoint> findAtomAndRssFeeds() {
    List<Endpoint> feeds = new ArrayList<>();
    Elements atomOrRssFeeds = doc.select("link[type=application/rss+xml], link[type=application/atom+xml]");
    for (Element feed : atomOrRssFeeds) {
      feeds.add(new Endpoint().url(feed.attr("abs:href")).title(feed.attr("title")));
    }
    return feeds;
  }

  private Collection<Endpoint> findBookmarkableLinks() {
    // Use a Map so we can ensure that we only keep one Endpoint per URL if the same URL appears
    // more than once in the navigation links.
    Map<String, Endpoint> bookmarkableLinks = new HashMap<>();

    Elements ariaRoleNavigation = doc.select("*[role=navigation]").select("a[href]");
    for (Element navLink : ariaRoleNavigation) {
      String linkUrl = navLink.attr("abs:href");
      bookmarkableLinks.put(linkUrl, new Endpoint().url(linkUrl).title(navLink.text()));
    }

    if (bookmarkableLinks.isEmpty()) {
      Elements likelyNavigationLinks = doc.select("nav, .nav, #nav, .navbar, #navbar, .navigation, #navigation").select("a[href]");
      for (Element navLink : likelyNavigationLinks) {
        String linkUrl = navLink.attr("abs:href");
        bookmarkableLinks.put(linkUrl, new Endpoint().url(linkUrl).title(navLink.text()));
      }
    }

    return bookmarkableLinks.values();
  }
}
