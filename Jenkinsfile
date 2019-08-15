#!/groovy
@Library('immutable-shared') _

pipeline {
  agent {
    kubernetes {
      yamlFile './_cri/KubernetesBuildPod.yaml'
    }
  }

  stages {
    stage('PR') {
      when {
        changeRequest target: 'dev'
        expression { env.ENVIRONMENT == 'dev' }
      }

      stages {
        load "./_cri/pullRequest.groovy"
      }
    }
  }
}
