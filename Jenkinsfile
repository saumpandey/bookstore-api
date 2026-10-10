pipeline {
    agent any

    tools {
        maven 'Maven-3.9'   // configure under Manage Jenkins > Tools
        jdk 'JDK-17'
    }

    stages {
        stage('Checkout') {
            steps { checkout scm }
        }
        stage('Unit Tests') {
            steps { sh 'mvn -B test' }
            post { always { junit 'target/surefire-reports/*.xml' } }
        }
        stage('Integration Tests') {
            steps { sh 'mvn -B verify' }
            post { always { junit 'target/failsafe-reports/*.xml' } }
        }
        stage('Package') {
            steps { archiveArtifacts artifacts: 'target/*.jar', fingerprint: true }
        }
        stage('Docker Build') {
            steps { sh 'docker build -t bookstore-api:${BUILD_NUMBER} .' }
        }
    }
}
