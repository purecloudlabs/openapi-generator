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
                    serviceBuild.runMavenGoals('clean package -Dmaven.test.skip=true -Dmaven.javadoc.skip=true -pl modules/openapi-generator-cli -am', true, "OpenJDK 17", "Maven 3.9.5")
                }
            }
        }
    }
    post{
        success{
            archiveArtifacts artifacts:'modules/openapi-generator-cli/target/openapi-generator-cli.jar' , fingerprint: true
                mail to: 'kavin_arasu.balasubramanian@genesys.com',
                subject: "SUCCESSFUL: Build ${env.JOB_NAME}", 
                body: "Build Successful ${env.JOB_NAME} build no: ${env.BUILD_NUMBER}\n\nView the log at:\n ${env.BUILD_URL}\n\nBlue Ocean:\n${env.RUN_DISPLAY_URL}"
        }
         failure {
              mail to: 'kavin_arasu.balasubramanian@genesys.com',
                subject: "FAILED: Build ${env.JOB_NAME}", 
                body: "Build failed ${env.JOB_NAME} build no: ${env.BUILD_NUMBER}.\n\nView the log at:\n ${env.BUILD_URL}\n\nBlue Ocean:\n${env.RUN_DISPLAY_URL}"
        }
    }
}