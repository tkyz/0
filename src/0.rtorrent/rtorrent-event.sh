#!/bin/bash

set -o errexit
set -o nounset
set -o pipefail

{

  echo "${@}"

} &>> "/.rtorrent/rtorrent.log"
