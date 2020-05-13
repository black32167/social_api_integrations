#!/bin/bash
#set -x
cd $(dirname $0)

export SH_SQS_QUEUE_URL="${SQS_RSHSQSQUEUE_QUEUE_URL}"
export SH_REGION="${SH_REGION:-ap-southeast-2}"
: "${CONFIG_LOCATION:=${HOME}/codes}"
export CONFIG_LOCATION

java ${JAVA_OPTS} \
  -Dconfig.location="${CONFIG_LOCATION}" \
  -cp "./lib/*" \
  org.social.integrations.birdview.BirdviewKt "${@}"
