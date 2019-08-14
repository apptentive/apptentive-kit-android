podTemplate(yaml: """
apiVersion: v1
kind: Pod
spec:
  containers:
  - name: docker
    image: docker:stable
    command: ['cat']
    tty: true
    volumeMounts:
    - name: dockersock
      mountPath: /var/run/docker.sock
  - name: compose
    image: "docker/compose:1.24.1"
    command: ['cat']
    tty: true
    volumeMounts:
    - name: dockersock
      mountPath: /var/run/docker.sock
  volumes:
  - name: dockersock
    hostPath:
      path: /var/run/docker.sock
"""
  ) {
  node(POD_LABEL) {
    sh 'env'
    checkout scm

    aws = load "./jenkins/aws.groovy"
    proj = load "./jenkins/proj.groovy"
    dockerRepository = "089941056973.dkr.ecr.us-east-1.amazonaws.com/apptentive-android-sdk"

    // Pull request time tasks
    if(env.ENVIRONMENT == 'dev' && env.CHANGE_TARGET == 'dev') {
      withCredentials([
      [
        $class: 'AmazonWebServicesCredentialsBinding',
        accessKeyVariable: 'AWS_ACCESS_KEY_ID',
        credentialsId: 'ecr',
        secretKeyVariable: 'AWS_SECRET_ACCESS_KEY']
      ]) {
        load "./jenkins/pullRequest.groovy"
      }
    }

    // Dev verify or deploy tasks
    if (env.ENVIRONMENT == 'dev' && env.BRANCH_NAME == 'dev') {
      sh 'totes deploying to dev'
    }

    // Staging verify or deploy tasks
    if (env.ENVIRONMENT == 'shared-dev' && env.BRANCH_NAME == 'staging') {
      sh 'totes deploying to staging'
    }

    // Prod verify or deploy tasks
    if (env.ENVIRONMENT == 'production' && env.BRANCH_NAME == 'prod') {
      sh 'totes deploying to production'
    }
  }
}
