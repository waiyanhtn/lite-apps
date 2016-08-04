#!/bin/bash
# Processes a tree of Hermit App Manifests in source form (manifest.json, images, locale data)
# and creates X.hermit files out of it.


function remove_ds_store() {
  find . -iname ".DS_Store" -exec rm "{}" \;
}

function zip_manifests() {
  # Create any missing target directories.
  ROOT_DIR=$(pwd)
  LITE_APPS_SRC_ROOT="${ROOT_DIR}/lite-apps-src"
  LITE_APPS_ZIP_ROOT="${ROOT_DIR}/lite-apps"
  mkdir -pv "${LITE_APPS_ZIP_ROOT}"

  for LITE_APP in ${LITE_APPS_SRC_ROOT}/*; do
    LITE_APP_NAME=$(basename "${LITE_APP}")
    echo ${LITE_APP_NAME}:
    MANIFEST_JSON="${LITE_APP}/manifest.json"

    check_if_exists ".manifest_version"
    check_if_exists ".lang"
    check_if_exists ".name"
    check_if_exists ".start_url"
    check_if_exists ".theme_color"
    check_if_exists ".secondary_color"
    check_if_exists ".icons[0].src"
    check_languages "${LITE_APP}"

    cd "${LITE_APP}"  # So the path included in the zip file is just the base name of the zipped file.
    zip -r "${LITE_APP_NAME}.hermit" *
    mv "${LITE_APP_NAME}.hermit" "${LITE_APPS_ZIP_ROOT}"
    cd "${ROOT_DIR}"
  done
}

function check_if_exists() {
  jq -e "$1" "${MANIFEST_JSON}" > /dev/null
  if [ $? -ne 0 ]; then
    echo "  Required field '$1' not found."
    exit 1
  fi
}

function check_languages() {
  if [ -f "$1/_locales/en/messages.json" ]; then
    jq "." "$1/_locales/en/messages.json" > /dev/null
    if [ $? -ne 0 ]; then
      echo "  Malformed localization."
      exit 1
    fi
  fi
}


remove_ds_store
zip_manifests
