def build(imageName) {  
  sh """docker build \
    -t ${imageName}:${env.BUILD_ID} \
    ."""
}

def push(imageName) {
  sh "docker push ${imageName}:${env.BUILD_ID}"
}

def lint(imageName) {
  sh "docker run ${imageName}:${env.BUILD_ID} ./gradlew lint"
}

def test(imageName) {
  sh "docker run ${imageName}:${env.BUILD_ID} ./gradlew test"
}

return this
