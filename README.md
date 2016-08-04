# Lite App Manifests

This is a directory of manifest files for [Hermit](https://hermit.chimbori.com).

# Syntax

Hermit Lite Apps are zip files, with the extension `.hermit`, with the following structure.

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

## _locales/en/messages.json

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
