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
        stage('awsLogin') {
          steps {
            script {
              dockerRepository = "089941056973.dkr.ecr.us-east-1.amazonaws.com/apptentive-javascript"
              proj = load "./_cri/proj.groovy"

              container('docker') {
                ecrLogin = apptentiveGetEcrLogin()
                sh ecrLogin
              }
            }
          }
        }
      

        stage('image') {
          steps {
            script {
              container('docker') {
                proj.build(dockerRepository)
                // push before verification so reviewers can pull down in progress work
                proj.push(dockerRepository)
              }
            }
          }
        }

        stage('verification') {
          parallel {
            stage('test') {
              steps {
                script {
                  container('docker') {
                    proj.test(dockerRepository)
                  }
                }
              }
            }

            stage('lint') {
              steps {
                script {
                  container('docker') {
                    proj.lint(dockerRepository)
                  }
                }
              }
            }
          }
        }
      }
    }
  }
}
