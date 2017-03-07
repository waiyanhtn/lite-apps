# Submitting New Lite Apps

We welcome new additions to this Library and enhancements to existing ones (e.g. adding new Bookmarks or Integrations). Clone this repo, make your changes, run the tests, and send us a pull request.

## Scaffolder: Generate new Lite App manifests automatically

The Scaffolder tool can parse and generate a basic Lite App from the information available on a web site. Use it quickly generate a skeleton from scratch, using just the URL of a site. In one step, it will:

- fetch the root page,
- create a brand new manifest file,
- locate and download an icon, (may or may not be successful)
- attempt to infer theme colors from the downloaded icon,
- locate feed URLs, (if any are present)
- build skeleton navigation (bookmarks), (if any top-level navigation items are found on the page)
- infer any related Android native apps, etc.

The output is a skeleton manifest ready for manual customization. You must review everything that was automatically generated and manually customize it before submitting. After you confirm that all the tests work fine, submit a pull request via GitHub.

## Tests

This repository contains not only the manifests for the individual Lite App definitions, but also a set of Java unit tests that validates & packages each Lite App. Once you clone this project to your local disk, open it in Android Studio as a Gradle project. After making changes to existing Lite Apps or creating your new Lite App, re-run all the unit tests with `./gradlew check` before submitting a pull request.

# Step by Step

1. Run the Scaffolder for the site `https://example.com/` with title `Example`. To include spaces in the title of the app, type an underscore instead of a space. E.g. `New_York_Times` instead of `New York Times`.

    ```
    ./gradlew run -Pargs="--url https://example.com/ --title Example"
    ```

1. Manually inspect the generated `manifest.json`, modify any fields as required. [See the full syntax](README.md).

1. Check if `icon.png` is the appropriate size: should be 300×300.

1. If an icon could not be automatically downloaded, or is not high quality, locate an alternate high quality icon. Here is a sample query for obtaining a 300×300 icon from a specific sample domain:

    ```
    https://www.google.com/search?q=example+site:play.google.com&espv=2&tbm=isch&source=lnt&tbs=isz:ex,iszw:300,iszh:300
    ```

1. Run all the tests:

  ```
  ./gradlew check --info
  ```

1. As a side effect of running the tests, your JSON files will be auto-formatted, and a new `*.hermit` file will be generated. This generated file must be included in your commit.

1. `git commit`, `git push`, etc.

1. Send a pull request, with the Title and URL of the new Lite App in the commit message.

1. Submit a separate pull request for each new Lite App.
