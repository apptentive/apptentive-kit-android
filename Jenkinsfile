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
        anyOf {
          changeRequest target: 'develop'
          branch 'develop'
        }
        expression { env.ENVIRONMENT == 'dev' }
      }

      stages {
        stage('verification') {
          parallel {
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

          }
        }
      }
    }
    
    stage('deploy') {
      when {
        anyOf {

          // staging/shared-dev deploy
          allOf {
            branch 'staging'
            expression { env.ENVIRONMENT == 'shared-dev' }
          }
        }
      }

      steps {
        script {
          gitCommit = apptentiveGetReleaseCommit()
          imageName = apptentiveDockerBuild('build', gitCommit)
          container('docker') {
            sh "docker run ${imageName} ./gradlew :app:deploy"
          }
        }
      }
    }
  }
}