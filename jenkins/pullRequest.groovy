stage('image') {
  container('docker') {
    aws.dockerLogin(env.AWS_ACCESS_KEY_ID, env.AWS_SECRET_ACCESS_KEY)

    proj.build(dockerRepository)
    // push before verification so reviewers can pull down in progress work
    proj.push(dockerRepository)
  }
}

stage('verify') {
  parallel(
    test: {
      container('docker') {
        proj.test(dockerRepository)
      }
    },
    lint: {
      container('docker') {
        proj.lint(dockerRepository)
      }
    }
  )
}
