# Lite App Manifests

This is a directory of manifest files for [Hermit](https://hermit.chimbori.com). Manifests are zipped up into `.hermit` files, and can be used to set up a new Lite App in Hermit.

[![Build Status](https://travis-ci.org/chimbori/lite-apps.svg?branch=master)](https://travis-ci.org/chimbori/lite-apps)

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
          "name": "__MSG_search_example__"
        }
      ],
      "hermit_share": [
        {
          "url": "https://example.com/share?u=%u&t=%t",
          "name": "__MSG_share_to_example__"
        }
      ]
    }

### Fields

- `manifest_version`: Must be `1`.
- `lang`: The default language to use, in case there is no localized version of strings available. Ensure that strings for this language are available under the `_locales` directory.
- `name`: The name of the Lite App, shown on the home screen & at the top of the app.
- `start_url`: The URL for the home page of the Lite App.
- `theme_color`: A hex-formatted color used as the theme color for the app.
- `secondary_color`: A hex-formatted color used for the navigation bar and in other places in the app.
- `icons`: Hermit currently only uses the first icon specified in this array.
- `hermit_settings`: A vendor-specific addition to the W3C Web Manifest format, where Hermit settings are saved. See the documentation inline in the source above.
- `hermit_bookmarks`, `hermit_search`, `hermit_share`: 3 separate arrays of Hermit Integrations, as described in the [Hermit Online Help](https://hermit.chimbori.com/help/integrations).

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

## Submitting New Lite Apps 

We welcome new additions to this Library, and enhancements to existing ones (e.g. adding new Bookmarks or Integrations).

This repository contains not only the manifests for the individual Lite App definitions, but also a set of Java unit tests that validates & packages each Lite App. Once you clone this project to your local disk, open it in Android Studio as a Gradle project. After making changes to existing Lite Apps or creating your new Lite App, re-run all the unit tests with `./gradlew check` before submitting a pull request.

### The `under_development` JSON field

To exclude any Lite Apps that are not yet ready from being published automatically, add a new boolean field to `manifest.json`. It should be named `under_development` with the value set to `true`. This will include it in internal processing, but will exclude it from being published.

When ready to publish, simply remove this field, and re-run the tests and tools.

## Questions?

Email us at [hello@chimbori.com](mailto:hello@chimbori.com) with your questions; we’ll be happy to answer. Be sure to include a link to your work-in-progress source code.
