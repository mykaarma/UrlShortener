def server
def buildInfo
def rtMaven
pipeline {
    agent any

    environment {
        ARTIFACTORY_ID = 'artifactory'
        RELEASE_REPO = 'libs-release-local'
        SNAPSHOT_REPO = 'libs-snapshot-local'
        JAVA_VERSION = 'Java17'
        GIT_REPO_URL = 'https://github.com/mykaarma/UrlShortener.git'
        GIT_REPO_PATH = './'
    }
    parameters{
        string(name: 'branch', defaultValue: 'main')
    }
    stages {
        stage ("Source Code Management") {
            steps{
                script {
                    checkout ([
                        $class: 'GitSCM',
                        branches: [[name: "${params.branch}"]],
                        userRemoteConfigs: [[
                            url: "${GIT_REPO_URL}",
                            credentialsId: 'git_credentials'
                        ]]
                    ])
                }
            }
        }

        stage('QA remote Trriger'){
            steps{
                script{
                    def url = env.EXTERNAL_JENKINS_URL ?: ''
                    if (!url.contains("qa-ci")) {
                        try{
                            def remoteJobUrl = "https://qa-ci-k8s.mykaarma.com/job/${env.JOB_NAME}/buildWithParameters?branch=${params.branch}"
                            withEnv(["REMOTE_JOB_URL=${remoteJobUrl}"]) {
                                withCredentials([usernameColonPassword(credentialsId: "qa-k8s-jenkins-api-token", variable: "API_TOKEN")]) {
                                    def response = sh(script: '''curl -X POST -u "$API_TOKEN" -s -o /dev/null -w '%{http_code}' "$REMOTE_JOB_URL"''', returnStdout: true).trim()
                                    echo "Remote trigger HTTP status: ${response}"
                                }
                            }
                        }catch(e){
                            echo "An error occurred: ${e}"
                        }
                    }
                }
            }
        }

        stage ('Artifactory configuration') {
            steps{
                script{
                    server = Artifactory.server env.ARTIFACTORY_ID

                    rtMaven = Artifactory.newMavenBuild()
                    rtMaven.resolver releaseRepo: 'libs-release', snapshotRepo: 'libs-snapshot', server: server
                    rtMaven.deployer releaseRepo: env.RELEASE_REPO, snapshotRepo: env.SNAPSHOT_REPO, server: server
                    rtMaven.deployer.deployArtifacts = false

                    buildInfo = Artifactory.newBuildInfo()
                    buildInfo.env.capture = true
                }
            }
        }
        
        stage ('Install') {
            steps{
                withMaven(jdk: "${env.JAVA_VERSION}", maven: 'maven'){
                    script{
                        dir("${env.GIT_REPO_PATH}"){
                            rtMaven.run pom: 'pom.xml', goals: 'clean install -DskipTests', buildInfo: buildInfo
                        }
                    }
                }
            }
        }

        stage ('Deploy') {
            steps{
                script{
                    rtMaven.deployer.deployArtifacts buildInfo
                }
            }
        }

        stage ('Publish build info') {
            steps{
                script{
                    server.publishBuildInfo buildInfo
                }
            }
        }
    }

    post {
        success {
            echo "Pipeline succeeded! Library published to Artifactory."
        }
        failure {
            echo "Pipeline failed. Check logs for details."
        }
    }
}
