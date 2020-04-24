SCRIPT_DIR="${BASH_SOURCE%/*}"

: "${HOST_CONFIG_FOLDER:=${HOME}/codes}"
IMAGE_NAME="birdview"
CMD="${1}"

docker run -v "${HOST_CONFIG_FOLDER}":/config "${IMAGE_NAME}" "${CMD}"
