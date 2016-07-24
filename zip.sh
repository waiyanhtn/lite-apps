#!/bin/bash
# Processes a tree of Hermit App Manifests in source form (manifest.json, images, locale data)
# and creates X.hermit files out of it.


# Clean all the .DS_Store files.
find . -iname ".DS_Store" -exec rm "{}" \;

# Create any missing target directories.
ROOT_DIR=$(pwd)
LITE_APPS_SRC_ROOT="${ROOT_DIR}/lite-apps-src"
LITE_APPS_ZIP_ROOT="${ROOT_DIR}/lite-apps"
mkdir -pv ${LITE_APPS_ZIP_ROOT}

for LITE_APP in ${LITE_APPS_SRC_ROOT}/*; do
  LITE_APP_NAME=$(basename ${LITE_APP})
  echo ${LITE_APP_NAME}:

  cd ${LITE_APP}  # So the path included in the zip file is just the base name of the zipped file.
  zip -r ${LITE_APP_NAME}.hermit *
  mv ${LITE_APP_NAME}.hermit ${LITE_APPS_ZIP_ROOT}
  cd ${ROOT_DIR}
done
