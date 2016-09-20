#!/usr/bin/env bash
# Updates Ruby & Jekyll environment as needed.

sudo gem update
sudo gem cleanup
sudo bundle update
sudo bundle clean --force
