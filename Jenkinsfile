pipeline {
	agent any

    tools {
		maven 'Maven'
        jdk 'JDK'
    }

    environment {
		SONAR_SCANNER_HOME = tool 'SonarQubeScanner'
        SONAR_TOKEN = credentials('SonarQube_token')
    }

    stages {

		// Étape 1 : Vérification de l'environnement
        stage('Vérification Environnement') {
			steps {
				script {
					if (isUnix()) {
						sh 'mvn --version'
                        sh 'java --version'
                    } else {
						bat 'mvn --version'
                        bat 'java --version'
                    }
                }
            }
        }

        // Étape 2 : Analyse SonarQube
        stage('SonarQube Analysis') {
			steps {
				withSonarQubeEnv('SonarQube') {
					script {
						def scannerHome = tool 'SonarQubeScanner'
                        withCredentials([string(credentialsId: 'SonarQube_token', variable: 'SONAR_TOKEN_SECURE')]) {
							if (isUnix()) {
								sh """
                                    ${scannerHome}/bin/sonar-scanner \
                                    -Dsonar.projectKey=TP-security \
                                    -Dsonar.projectName='TP-security' \
                                    -Dsonar.java.binaries=target/classes \
                                    -Dsonar.sources=src/main/java \
                                    -Dsonar.tests=src/test/java \
                                    -Dsonar.junit.reportPaths=target/surefire-reports \
                                    -Dsonar.token=$SONAR_TOKEN_SECURE \
                                    -Dsonar.host.url=http://host.docker.internal:9000
                                """
                            } else {
								bat """
                                    ${scannerHome}/bin/sonar-scanner \
                                    -Dsonar.projectKey=TP-security \
                                    -Dsonar.projectName='TP-security' \
                                    -Dsonar.java.binaries=target/classes \
                                    -Dsonar.sources=src/main/java \
                                    -Dsonar.tests=src/test/java \
                                    -Dsonar.junit.reportPaths=target/surefire-reports \
                                    -Dsonar.token=$SONAR_TOKEN_SECURE \
                                    -Dsonar.host.url=http://host.docker.internal:9000
                                """
                            }
                        }
                    }
                }
            }
        }

        //Etape 3: Sonarqube Quality
         stage('SonarQube Publish') {
			steps {
				sonarQubePublish()
          }
        }
        stage('Quality Gate') {
			steps {
				// Vérifiez les règles de qualité
        		// En cas d'échec, bloquez le build
			script {
				if (sonarQubeQualityGate('http://localhost:9000', $SONAR_TOKEN_SECURE, 'TP-security')) {
					echo 'Quality gate passed'
			  } else {
					echo 'Quality gate failed'
					error('Build failed due to SonarQube Quality Gate')
			  }
			}
		  }
		}

        // Étape 4 : Compilation et tests
        stage('Compilation et Tests') {
			steps {
				script {
					if (isUnix()) {
						sh 'mvn clean package'
                    } else {
						bat 'mvn clean package'
                    }
                }
            }
        }
        // Étape 5 : Publication des rapports de test
        stage('Publication des Rapports de Test') {
			steps {
				script {
					if (isUnix()) {
						sh 'mvn surefire-report:report-only'
                    } else {
						bat 'mvn surefire-report:report-only'
                    }
                }
            }
        }
        // Étape 6 : Nettoyage
        stage('Nettoyage') {
			steps {
				script {
					if (isUnix()) {
						sh 'mvn clean'
                    } else {
						bat 'mvn clean'
                    }
                }
            }
        }

    }

    
}
