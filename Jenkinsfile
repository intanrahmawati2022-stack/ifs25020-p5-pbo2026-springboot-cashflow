pipeline {
    agent any
    environment {
        IMAGE = 'ifs25020-p5-pbo2026-springboot-cashflow'
    }
    stages {
        stage('Build & Test') {
            steps { sh './mvnw -B clean verify -Pproduction' }
        }
        stage('Jacoco 100%') {
            steps { sh './mvnw -B jacoco:check' }
        }
        stage('Trivy Scan') {
            steps { sh 'trivy fs --exit-code 1 --severity CRITICAL .' }
        }
        stage('Package') {
            steps { sh './mvnw -B package -DskipTests -Pproduction' }
        }
        stage('Publish') {
            steps { archiveArtifacts artifacts: 'target/*.jar', fingerprint: true }
        }
        stage('Deploy') {
            when { branch 'main' }
            steps { echo "Deploy ${IMAGE} ke server (isi sesuai lingkungan Anda)" }
        }
    }
}
