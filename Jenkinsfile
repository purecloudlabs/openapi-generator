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
                    serviceBuild.runMavenGoals("clean install -Dmaven.test.skip=true -Dmaven.javadoc.skip=true -pl modules/openapi-generator-cli -am", true, "OpenJDK 1.8.0 (latest)", "Maven 3.5.3")
                }
            }
        }
    }
    post{
        success{
            archiveArtifacts artifacts:'modules/openapi-generator-cli/target/openapi-generator-cli.jar' , fingerprint: true
                mail to: 'developertooling@genesys.com',
                subject: "Build Success: ${env.JOB_NAME}", 
                body: "Build Successful ${env.JOB_NAME} \n build: ${env.BUILD_NUMBER}\n\nView the logs at:\n ${env.BUILD_URL}\n\nBlue Ocean:\n${env.RUN_DISPLAY_URL}"
        }
         failure {
              mail to: 'developertooling@genesys.com',
                subject: "Build Failed: ${env.JOB_NAME}", 
                body: "Build failed ${env.JOB_NAME} \n build: ${env.BUILD_NUMBER}.\n\nView the logs at:\n ${env.BUILD_URL}\n\nBlue Ocean:\n${env.RUN_DISPLAY_URL}"
        }
    }
}
