---
title: README for Hermit Lite App Manifests
description: Native apps eating up battery and slowing down your phone? Switch to Lite Apps — with Hermit.
---

# Lite App Manifests

This is a directory of manifest files for [Hermit](https://hermit.chimbori.com). Manifests are zipped up into `.hermit` files, and can be used to set up a new Lite App in Hermit.

[![Build Status](https://travis-ci.org/chimbori/lite-apps.svg?branch=master)](https://travis-ci.org/chimbori/lite-apps)

## Submitting New Lite Apps

We welcome new additions to this Library, and enhancements to existing ones (e.g. adding new Bookmarks or Integrations).

Please see the [step by step instructions](CONTRIBUTING.md) and an overview of the automated tools and tests we’ve made available.

# Syntax

Hermit Lite Apps are zip files, with the extension `.hermit`. Each zip file contains multiple files that define the Lite App, how it should be installed, and default settings to be used. Only two files are required, all others are optional.

- `manifest.json` : The basic metadata about a Lite App is contained in a `manifest.json` file. This follows the [W3C Web App Manifest](https://www.w3.org/TR/appmanifest/) format with additional vendor-specific fields for Hermit that are not yet a part of the W3C standard.
- `icon.png` : This file can be named anything, as long as the same name is used in the `manifest.json` in the `icons` field. Ensure that icons are large enough (typically at least 192×192px; larger is fine too). Only one icon is required and only the first entry in the `icons` array is currently used by Hermit; all others are ignored.

All Hermit Lite Apps fully support localization, but it’s optional. So if you’re a German publication or Chinese site and only want to include one language in your Lite App definition, that’s completely fine. If included, localized strings must follow a specific directory structure (same as what Chrome extensions use.)

## Directory Structure

    Lite App.hermit
    - manifest.json  (required)
    - icon.png       (required)
    + _locales/      (optional)
      + en/
        - messages.json
      + es/
        - messages.json
      + de/
        - messages.json
      + pt_BR/
        - messages.json
      … etc.

## manifest.json

    {
      "manifest_version": 1,
      "lang": "en",
      "name": "__MSG_lite_app_title__",
      "start_url": "https://example.com",
      "theme_color": "#ff0000",
      "secondary_color": "#00ff00",
      "icons": [
        {
          "src": "icon.png"
        }
      ],
      "hermit_settings": {
        "block_malware":  true | false,
        "do_not_track":  true | false,
        "load_images":  true | false,
        "open_links": "in_app" | "browser",
        "preferred_view": "accelerated",
        "save_data":  true | false,
        "scroll_to_top":  true | false,
        "text_zoom":  true | false,
        "user_agent": "desktop" | ""
      },
      "hermit_bookmarks": [
        {
          "url": "https://example.com/top-level-navigation",
          "name": "__MSG_top_level__"
        },
        {
          "url": "https://example.com/another-top-level-navigation",
          "name": "__MSG_another_top_level__"
        }
      ],
      "hermit_search": [
        {
          "url": "https://example.com/search?q=%s",
          "name": "Integrated Search, use %s as a search query placeholder."
        }
      ],
      "hermit_share": [
        {
          "url": "https://example.com/share?u=%u&t=%t",
          "name": "Share from the native Android dialog to any Lite App"
        }
      ],
      "hermit_create": [
        {
          "url": "https://example.com/create",
          "name": "Create New Content"
        }
      ],
      "hermit_feeds": [
        {
          "url": "https://example.com/rss.xml",
          "name": "RSS feed of all new content"
        },
        {
          "url": "https://example.com/atom.xml",
          "name": "Atom feeds are supported too."
        }
      ]
    }


### Required Fields

- `manifest_version`: Integer, must be `1`. Only one manifest version is currently supported; this field is reserved for future use.
- `lang`: The default language to use, in case there are no localized strings available. Ensure that strings for this language are available under the `_locales` directory.
- `name`: The name of the Lite App, shown on the home screen & at the top of the app.
- `manifest_url`: The URL where this Lite App Manifest will be hosted. Typically, this should be `https://hermit.chimbori.com/lite-apps/YOUR_APP_NAME.hermit`. This must be explicitly specified for every Lite App, although it is not present when you create your own Lite App in Hermit and export it.
- `start_url`: The URL for the home page of the Lite App.
- `theme_color`: A hex-formatted color used as the theme color for the app.
- `secondary_color`: A hex-formatted color used for the navigation bar and in other places in the app.
- `icons`: Hermit currently only uses the first icon specified in this array.

### Optional Fields

- `hermit_settings`: A vendor-specific addition to the W3C Web Manifest format, where Hermit settings are saved. See details below.
- `hermit_bookmarks`: A list of bookmarks shown in the left sidebar in every Hermit Lite App.
- `hermit_search`: Search can be integrated into any Lite App. [See details on how to configure this](https://hermit.chimbori.com/help/integrations).
- `hermit_share`: Share text from any Android app directly (natively) into a Hermit Lite App. [See details on how to configure this](https://hermit.chimbori.com/help/integrations).
- `hermit_create`: A floating action button can be a quick shortcut to load a common action in any Lite App. [See details on how to configure this](https://hermit.chimbori.com/help/integrations).
- `hermit_feeds`: RSS or Atom feed URLs that Hermit will check regularly and notify the user about.

### Settings

- `block_malware`: Whether or not to block ads and malware. Boolean, `true` \| `false`
- `do_not_track`: Whether to send the [Do Not Track HTTP header](https://donottrack.us/). Boolean, `true` \| `false`
- `load_images`: Image loading can be disabled, e.g. on slow networks. Boolean, `true` \| `false`
- `open_links`: Choose where external links should be opened: `"in_app"` opens them inside the Lite App. `"browser"` uses the system default browser.
- `preferred_view`: `"accelerated"` will load fast Accelerated Mobile Pages instead of slow regular ones. `"original`" loads the original pages.
- `save_data`: Whether to send the [Save Data client hint](https://httpwg.org/http-extensions/client-hints.html#the-save-data-hint) on every request. Boolean, `true` \| `false`
- `scroll_to_top`: Whether to show the Scroll to Top button in the Hermit UI. Boolean, `true` \| `false`
- `pull_to_refresh`: Whether swiping down in the Lite App should refresh the page. Boolean, `true` \| `false`
- `text_zoom`: A percentage number between `0` to `200`, in steps of `20`. The default is `100`.
- `user_agent`: `"desktop"` reports the user agent of this browser as a desktop user agent, `""` to use the default mobile user agent.

## Localization

A single Lite App can include different names for each item (app name, bookmarks, etc.) in multiple languages. On installation, the correct strings will be loaded and used in Hermit, based on the user’s device language. If no strings are available for the user’s language, then strings from the default locale will be used, as specified in the `lang` field in `manifest.json`.

Hermit Lite Apps use the same JSON format as Chrome Extensions for localization. See examples below.

A string (e.g. `app_name`) can be used in `manifest.json` by referencing it as `__MSG_app_name__`. (I.e. prefix a message key with `__MSG_` and suffix with `__` to use its value in the manifest).

### _locales/en/messages.json

    {
      "lite_app_title": {
        "message": "Lite App",
        "description": "Name of this Lite App, shown in the Launcher and everywhere else as a title. Keep it short."
      },
      "top_level": {
        "message": "Top Level Navigation",
        "description": "Just what it says. Descriptions are for translators to understand the context in which a string is being used."
      },
      "another_top_level": {
        "message": "Another Top Level Navigation",
        "description": "Just what it says. Descriptions are for translators to understand the context in which a string is being used."
      },
      "search_example": {
        "message": "Search Lite App",
        "description": "Phrase shown as the text hint in the search box at the top of the Lite App"
      },
      "share_to_example": {
        "message": "Share to Lite App",
        "description": "Phrase shown in the dialog where a user can share text from another app directly to this Lite App"
      }
    }

## Questions?

Email us at [hello@chimbori.com](mailto:hello@chimbori.com) with your questions; we’ll be happy to answer. Be sure to include a link to your work-in-progress source code.
