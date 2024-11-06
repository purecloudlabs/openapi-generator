@Library(value='pipeline-library@master', changelog=false)
import com.genesys.jenkins.Service

final serviceBuild = new com.genesys.jenkins.Service()

pipeline {
  agent {
    node {
      label 'dev_mesos_v2'
    }
  }

  stages {
        stage('Build') {
            steps {
                script {
                    serviceBuild.runMavenGoals('clean package -Dmaven.test.skip=true -Dmaven.javadoc.skip=true -pl modules/openapi-generator-cli -am', false, "OpenJDK 17", "Maven 3.9.5")
                }
            }
        }
    }
    post{
        success{
            archiveArtifacts artifacts:'modules/openapi-generator-cli/target/openapi-generator-cli.jar' , fingerprint: true
        }
    }
}