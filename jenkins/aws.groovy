def dockerLogin(awsAcesskeyId, awsSecretAccesskey) {
  sh '''docker build  \
    -f ./jenkins/Dockerfile.publish \
    -t pika-pika-publish:latest \
    .'''

  sh """\$(docker run \
    -e AWS_ACCESS_KEY_ID=${awsAcesskeyId} \
    -e AWS_SECRET_ACCESS_KEY=${awsSecretAccesskey} \
    pika-pika-publish \
    ecr get-login --no-include-email --region us-east-1)"""
}

return this
