pipeline {
    agent docker
    environment {
        COMPOSE_FILE = 'compose.yml'
        DOCKER_REGISTRY = 'kartikeyadhub'  // Optional for pushing images
    }
    stages {
        stage('Checkout') {
            steps {
                git branch: 'main', credentialsId: 'git', url: 'https://github.com/kartikeya8/demopro.git'
            }
        }
        
        stage('Build Images') {
            steps {
                script {
                    // Build each service separately to ensure proper ordering
                    sh 'docker compose -f ${COMPOSE_FILE} build db'
                    sh 'docker compose -f ${COMPOSE_FILE} up -d db'
                    
                    // Wait for MySQL to be fully ready
                    sh '''
                        while ! docker compose -f ${COMPOSE_FILE} exec db mysqladmin ping -uroot -prootpass --silent; do
                            echo "Waiting for MySQL..."
                            sleep 5
                        done
                    '''
                    
                    // Build application services after DB is ready
                    sh 'docker compose -f ${COMPOSE_FILE} build app'
                }
            }
        }
        
       
    }
}