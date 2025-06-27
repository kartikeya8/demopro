pipeline {
    agent {
        label 'docker'
    }
    environment {
        COMPOSE_FILE = 'compose.yaml'
        DOCKER_REGISTRY = 'kartikeyadhub'  // Optional for pushing images
        DOCKERHUB_CREDENTIALS = credentials('dockerhub')
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
                        attempt=0
                        max_attempts=12  # 12 attempts * 5s = 60s total
                        while [ $attempt -lt $max_attempts ]; do
                            if docker compose -f ${COMPOSE_FILE} exec db mysqladmin ping -uroot -prootpass --silent; then
                                break
                            fi
                            echo "Waiting for MySQL... (Attempt $((attempt+1))/$max_attempts)";
                            sleep 5;
                            attempt=$((attempt+1));
                        done
                        if [ $attempt -eq $max_attempts ]; then
                            echo "ERROR: MySQL not ready after 1 minute"
                            exit 1  // Fail the pipeline
                        fi
                     '''
                    
                    // Build application services after DB is ready
                   sh 'docker compose -f ${COMPOSE_FILE} build server --no-cache'
                   sh 'docker compose -f ${COMPOSE_FILE} up -d server'
                }
            }
        }
        stage('Push to Docker Hub') {
            steps {
                withCredentials([usernamePassword(
                    credentialsId: 'dockerhub',
                    usernameVariable: 'DOCKER_USER',
                    passwordVariable: 'DOCKER_PASS'
                )]) {
                    sh '''
                        # Login without persisting credentials
                        echo "$DOCKER_PASS" | docker login --username $DOCKER_USER --password-stdin
                        
                        docker compose -f  ${COMPOSE_FILE} push
                        
                        # Explicitly remove credentials
                        rm -f ~/.docker/config.json 
                    '''
                }
            }
        }       
    }
}
post {
        always {
            sh 'docker compose -f ${COMPOSE_FILE} down -v'  // Cleanup
        }
}