#!/groovy
@Library('immutable-shared') _

pipeline {
  agent {
    kubernetes {
      yamlFile './_cri/KubernetesBuildPod.yaml'
    }
  }

  options {
    timeout(time: 20, unit: 'MINUTES')
  }

  stages {
    stage('Staging Merged') {
      when {
        expression { env.ENVIRONMENT == 'shared-dev' }
      }

      stages {
        stage('release') {
          steps {
            script {
              gitCommit = apptentiveGetReleaseCommit()
              imageName = apptentiveDockerBuild('build', gitCommit)
              container('docker') {
                sh 'docker run ${imageName} ./gradlew assembleRelease && ./gradlew tag && ./gradlew pushTag && ./gradlew githubRelease'
              }
            }
          }
        }
      }
    }

    stage('Dev PR') {
      when {
        changeRequest target: 'develop'
        expression { env.ENVIRONMENT == 'dev' }
      }

      stages {
        stage('verification') {
          parallel {
            stage('test') {
              steps {
                script {
                  gitCommit = apptentiveGetReleaseCommit()
                  imageName = apptentiveDockerBuild('build', gitCommit)
                  container('docker') {
                    sh "docker run ${imageName} ./gradlew test"
                  }
                }
              }
            }

            stage('lint') {
              steps {
                script {
                  gitCommit = apptentiveGetReleaseCommit()
                  imageName = apptentiveDockerBuild('build', gitCommit)
                  container('docker') {
                    //sh "docker run ${imageName} ./gradlew lint"
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