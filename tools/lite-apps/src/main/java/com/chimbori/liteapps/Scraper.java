package com.chimbori.liteapps;


import com.chimbori.hermitcrab.schema.manifest.Endpoint;
import com.chimbori.hermitcrab.schema.manifest.Icon;
import com.chimbori.hermitcrab.schema.manifest.Manifest;
import com.chimbori.hermitcrab.schema.manifest.RelatedApplication;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.util.ArrayList;
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
  private static final String GOOGLE_COM_HOME_PAGE = "https://www.google.com";

  private final String url;
  private Document doc;

  public static Manifest scrape(String url) {
    return new Scraper(url).fetch().parse();
  }

  private Scraper(String url) {
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

  private Manifest parse() {
    Manifest manifest = new Manifest();
    if (doc == null) {  // Fetch failed or never fetched.
      return manifest;  // NonNull {@code Manifest}, but empty fields.
    }

    // First try to get the Site’s name (not just the current page’s title) via OpenGraph tags.
    manifest.name = doc.select("meta[property=og:site_name]").attr("content");
    if (manifest.name == null || manifest.name.isEmpty()) {
      // But if the page isn’t using OpenGraph tags, then fallback to using the current page’s title.
      manifest.name = doc.select("title").text();
    }
    manifest.theme_color = doc.select("meta[name=theme-color]").attr("content");
    // The "abs:" prefix is a JSoup shortcut that converts this into an absolute URL.
    String iconUrl = doc.select("link[rel=apple-touch-icon]").attr("abs:href");
    if (iconUrl == null || iconUrl.isEmpty()) {
      iconUrl = doc.select("link[rel=apple-touch-icon-precomposed]").attr("abs:href");
    }
    if (iconUrl != null) {
      Icon icon = new Icon();
      icon.src = iconUrl;
      manifest.addIcon(icon);
    }

    manifest.hermit_bookmarks = findBookmarkableLinks();
    manifest.hermit_feeds = findAtomAndRssFeeds();
    manifest.related_applications = findRelatedApps();

    return manifest;
  }

  private List<RelatedApplication> findRelatedApps() {
    List<RelatedApplication> relatedApps = new ArrayList<>();
    Elements playStoreLinks = doc.select("a[href*=play.google.com]");
    for (Element playStoreLink : playStoreLinks) {
      String playStoreUrl = playStoreLink.attr("href");
      System.out.println(playStoreUrl);
      Matcher matcher = Pattern.compile("id=([^&]+)").matcher(playStoreUrl);
      while (matcher.find()) {
        relatedApps.add(new RelatedApplication(matcher.group(1)));
      }
    }
    return relatedApps;
  }

  private List<Endpoint> findAtomAndRssFeeds() {
    List<Endpoint> feeds = new ArrayList<>();
    Elements atomOrRssFeeds = doc.select("link[type=application/rss+xml], link[type=application/atom+xml]");
    for (Element feed : atomOrRssFeeds) {
      feeds.add(new Endpoint()
          .url(feed.attr("abs:href"))
          .title(feed.attr("title")));
    }
    return feeds;
  }

  private List<Endpoint> findBookmarkableLinks() {
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

    return new ArrayList<>(bookmarkableLinks.values());
  }
}
