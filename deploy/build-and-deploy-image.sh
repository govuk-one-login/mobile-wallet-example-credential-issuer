#!/bin/bash

set -eu

if [ $# -ge 2 ]
then
    TAG="$1"
    AWS_PROFILE="$2"
    echo "Attempting to build docker image for the Example CRI with tag: \"$1\""
    DOCKER_IMAGE_PATH="671524980203.dkr.ecr.eu-west-2.amazonaws.com/wallet-cred-issuer-be-ecr-containerrepository-ta87rf6hloli:$TAG"
    docker build -t "$DOCKER_IMAGE_PATH" --platform Linux/X86_64 ..

    echo "Attempting to log into ECR in the dev account"
    aws ecr get-login-password --region eu-west-2 --profile $AWS_PROFILE | docker login --username AWS --password-stdin 671524980203.dkr.ecr.eu-west-2.amazonaws.com

    echo "Attempting to push image to the Example CRI image registry in ECR"
    docker push "$DOCKER_IMAGE_PATH"

    echo "Updating image path in template.yaml to point at the newly pushed image"
    sed -i "" "s|CONTAINER-IMAGE-PLACEHOLDER|$DOCKER_IMAGE_PATH|" template.yaml

    echo "Success! You can now build and deploy the SAM template."
else
    echo "Please specify a tag for the docker image and your AWS profile for the Mobile Platform dev account, e.g. ./build-and-deploy-image.sh image-tag profile-name"
fi