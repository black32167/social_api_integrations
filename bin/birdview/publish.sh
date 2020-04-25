#!/bin/bash

SCRIPT_DIR="${BASH_SOURCE%/*}"
. "${SCRIPT_DIR}/include.sh"

dpublish() {
	docker push "${IMAGE_NAME}"
}

case "${1}" in
publish)
  dpublish
  ;;
*)
  echo "Usage:"
  echo "${BASH_SOURCE} docker"
  ;;
esac
