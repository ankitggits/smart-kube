def label = "pipeline-${UUID.randomUUID().toString()}"
podTemplate(label: label,
  containers: [
        containerTemplate(name: 'maven', image: 'maven:3.3.9-jdk-8-alpine', command: 'cat', ttyEnabled: true),
        containerTemplate(name: 'docker', image: 'docker', ttyEnabled: true, command: 'cat')
  ],
  volumes: [
      persistentVolumeClaim(mountPath: '/root/.m2', claimName: 'maven-repo', readOnly: false),
      hostPathVolume(mountPath: '/var/run/docker.sock', hostPath: '/var/run/docker.sock')]
  ) {

  def image = "docker.for.mac.localhost:5000/smart-kube/api:latest"
  node(label) {
    stage('Source pull and build') {
        git 'https://github.com/ankitggits/smart-kube.git'
        container('maven') {
            sh 'mvn -B clean install'
        }
    }
    stage('Docker Image Building') {
        container('docker') {
            sh "docker build -t ${image} ."
            sh "docker push ${image}"
        }
    }
    stage('Pods Rollout') {
        container('docker') {
            def ret = sh(script: 'docker ps -a -q --filter="name=smart-kube"', returnStdout: true).split( '\n' )
            sh "echo Trying to stop pods ${ret}"
            for (int i = 0; i < ret.size(); i++) {
                sh "echo Trying to stop pod ${ret[i]}"
                try{
                    sh "docker rm -f ${ret[i]}"
                }catch(Exception ex){
                    sh "echo Could not delete ${ret[i]}"
                }
            }
        }
    }
  }
}