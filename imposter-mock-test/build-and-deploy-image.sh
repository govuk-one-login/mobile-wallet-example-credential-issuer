#!/bin/bash

set -eu

if [ $# -ge 2 ]
then
    TAG="$1"
    AWS_PROFILE="$2"
    echo "Building docker image for Imposter Mock with tag: \"$TAG\""
    DOCKER_IMAGE_PATH="671524980203.dkr.ecr.eu-west-2.amazonaws.com/imposter-mock-test:$TAG"
    docker build -t "$DOCKER_IMAGE_PATH" --platform Linux/X86_64 .

    echo "Logging into ECR"
    aws ecr get-login-password --region eu-west-2 --profile $AWS_PROFILE | docker login --username AWS --password-stdin 671524980203.dkr.ecr.eu-west-2.amazonaws.com

    echo "Pushing image to ECR"
    docker push "$DOCKER_IMAGE_PATH"

    echo "Updating image path in template.yaml"
    sed -i "" "s|CONTAINER-IMAGE-PLACEHOLDER|$DOCKER_IMAGE_PATH|" template.yaml

    echo "Success! You can now build and deploy the SAM template."
else
    echo "Please specify a tag for the docker image and your AWS profile, e.g. ./build-and-deploy-image.sh image-tag profile-name"
fi
