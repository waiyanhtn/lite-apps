#!/usr/bin/env bash
# Installs the Jekyll server for development.

bundle config build.nokogiri --use-system-libraries
gem install jekyll bundler
bundle install
