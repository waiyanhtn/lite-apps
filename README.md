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
      },
      "top_level": {
        "message": "Top Level Navigation",
      },
      "another_top_level": {
        "message": "Another Top Level Navigation",
      },
      "search_example": {
        "message": "Search Lite App",
      },
      "share_to_example": {
        "message": "Share to Lite App",
      }
    }

