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
    stage('Dev PR') {
      when {
        changeRequest target: 'dev'
        expression { env.ENVIRONMENT == 'dev' }
      }

      stages {
        stage('build'){
          steps {
            script {
              gitCommit = apptentiveGetReleaseCommit()
              imageName = apptentiveDockerBuild('build', gitCommit)
            }
          }
        }

        stage('verification') {
          parallel {
            stage('test') {
              steps {
                script {
                  container('docker') {
                    sh "docker run ${imageName} ./gradlew test"
                  }
                }
              }
            }

            stage('lint') {
              steps {
                script {
                  container('docker') {
                    sh "docker run ${imageName} ./gradlew lint"
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
