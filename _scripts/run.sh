#!/usr/bin/env bash
# Runs the Jekyll server for development.
babel --presets react --extensions .jsx --watch library/ --out-dir library/ & # Run in the background.
bundle exec jekyll serve --watch --trace
