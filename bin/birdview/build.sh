SCRIPT_DIR="${BASH_SOURCE%/*}"
MODUE_DIR="${SCRIPT_DIR}/../../birdview"

mbuild() {
  mvn clean install -DskipTests -pl "${MODUE_DIR}"
}

dbuild() {
  local IMAGE_NAME="birdview"
	local CTX_DIR="${MODUE_DIR}/target/birdview-dist"
	local DOCKER_FILE="${MODUE_DIR}/docker/Dockerfile"

  mbuild
	echo "==== Building docker image ${IMAGE_NAME} ===="

	docker build -t ${IMAGE_NAME} -f "${DOCKER_FILE}" "${CTX_DIR}"
}

case "${1}" in
docker)
  dbuild
  ;;
maven)
  mbuild
  ;;
esac
