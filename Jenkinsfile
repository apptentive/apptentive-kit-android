#!/groovy
@Library('immutable-shared') _

pipeline {
  agent {
    kubernetes(
      // Use shared function in `jenkins-shared-libs` to configure a custom Jenkins agent
      // https://github.com/apptentive/jenkins-shared-libs/blob/master/vars/apptentiveAgent.groovy
      apptentiveAgent(
        yaml: readTrusted("_cri/KubernetesBuildPod.yaml"),
        use_vault: true
      )
    )
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

          // I was not sure how you wanted to consume the secret, so took a guess
          //
          // source the vault secrets as envvars; echo the envvar
          // we want and capture as a groovy variable
          GITHUB_TOKEN = sh(returnStdout: true, script:'''
            set +x
            . /vault/secrets/env.sh
            echo -n "$GITHUB_TOKEN"
          ''')

          gitCommit = apptentiveGetReleaseCommit()
          imageName = apptentiveDockerBuild('build', gitCommit)
          container('docker') {
            sh "docker run ${imageName} \"GITHUB_TOKEN=${GITHUB_TOKEN} ./gradlew :app:deploy\""
          }
        }
      }
    }
  }
}
