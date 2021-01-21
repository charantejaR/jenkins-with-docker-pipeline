pipeline {
    environment {
        JAVA_TOOL_OPTIONS = "-Duser.home=/var/maven"
    }
  agent {
        docker {
            image "artifactory.guidewire.com/mulesoft-docker-release/maven-mule:0.0.4"
            registryUrl "https://artifactory.guidewire.com/"
            registryCredentialsId "jfrog_cred"
            args '--net="host"' 
            //args "--net=host -v /tmp/maven:/var/maven/.m2 -e MAVEN_CONFIG=/var/maven/.m2"
        }
    }
 stages {
  stage('GIT Checkout') {
      steps{
        git branch: "${params.gitBranch}", credentialsId: "dso-sys-devsecops-credentials", url: "${params.bitBucketURL}"      
        echo 'SourceCode Checkout'
      }
    }
    stage('Maven Build'){
        steps{
            sh "mvn clean package versions:set -DnewVersion=${params.pomVersion}-${BUILD_NUMBER}"
            echo 'Maven Build'
            //archive 'pom.xml'
            //archive 'target/*.jar'
        }
    }
    stage('Cloudhub Deployment'){
          environment {
            ANYPOINT_CREDENTIALS = credentials('anypoint.credentials')
            DEV_CRED = credentials('anypoint.bg.dev')
            }
          steps{
            sh "mvn  deploy -DmuleDeploy -Pcloudhub -Danypoint.userName=${ANYPOINT_CREDENTIALS_USR} -Danypoint.password=${ANYPOINT_CREDENTIALS_PSW}  -Dapp.name=${params.applicationName} -Denvironment.name=${params.environment} -Dworker.type=${params.workerSize} -Dworkers=${params.workers} -Dobject.store.enable=true -Dmule.businessGroup=${params.businessGroup} -Dbusiness.group.client.id=${DEV_CRED_USR} -Dbusiness.group.client.secret=${DEV_CRED_PSW} -Denv=${params.env} -Dsecure.key=${params.secureKey} -DapiId=${params.autoDiscoveryId}"
           echo 'Cloudhub Deployment'
         
      }
 }
 }
}
