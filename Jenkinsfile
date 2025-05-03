pipeline {
    agent any
    tools {
        maven 'Maven' // Must match the name in Global Tool Configuration
        jdk 'JDK'     // Must match the name in Global Tool Configuration
    }

    environment {
        IMAGE_NAME = 'myapp:latest'
    }

    stages {
        stage('Checkout') {
            steps {
                git branch: 'devlop', url: 'https://github.com/malleswar-reddy/jenkins-kafka.git'
                // SCM is handled by the Source Code Management configuration
                 //echo 'Code checked out'
               }
        }

        stage('Build Docker Image') {

            steps {
                sh 'mvn clean package'
                sh 'docker compose -f docker-compose-prod.yaml build'
            }
        }

        stage('Deploy with Docker Compose') {
            steps {
                sh 'docker compose -f  docker-compose-prod.yaml down'
                sh 'docker compose -f docker-compose-prod.yaml up -d'
            }
        }
    }

    post {
        success {
            echo "Deployed successfully!"
        }
        failure {
            echo "Deployment failed."
        }
    }
}
