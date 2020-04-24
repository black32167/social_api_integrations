#!/bin/bash
#set -x
cd $(dirname $0)
CMD=$1

export SH_SQS_QUEUE_URL="${SQS_RSHSQSQUEUE_QUEUE_URL}"
export SH_REGION="${SH_REGION:-ap-southeast-2}"
: "${CONFIG_LOCATION:=${HOME}/codes/bv.json}"
export CONFIG_LOCATION

case "${CMD}" in
    progress|done|planned|blocked)
        java ${JAVA_OPTS} \
          -Dconfig.location="${CONFIG_LOCATION}" \
          -cp "./lib/*" \
          org.social.integrations.birdview.BirdviewKt "${CMD}"
        ;;
    *)
        echo "Usage:"
        echo "${BASH_SOURCE%/*} {progress|done|planned|blocked}"
esac
