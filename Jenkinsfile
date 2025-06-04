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
                                    -Dsonar.token=$SONAR_TOKEN_SECURE
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
                                    -Dsonar.token=$SONAR_TOKEN_SECURE
                                """
                            }
                        }
                    }
                }
            }
        }

        stage('Quality Gate') {
			steps {
				try {
					timeout(time: 2, unit: 'MINUTES') {
						def qg = waitForQualityGate abortPipeline: true
						echo "Quality Gate Status: ${qg.status}"
					}
				} catch (Exception e) {
					echo "Quality Gate check failed: ${e}"
					// Optional: add manual retry logic here
				}
           	}
        }

		stage('Build & Test') {
			steps {
				script {
					def buildCmd = 'mvn clean package'
					isUnix() ? sh(buildCmd) : bat(buildCmd)
				}
			}
		}

		stage('Generate Test Report') {
			steps {
				script {
					def reportCmd = 'mvn surefire-report:report-only'
					isUnix() ? sh(reportCmd) : bat(reportCmd)
				}
			}
		}

		stage('Clean Workspace') {
			steps {
				script {
					def cleanCmd = 'mvn clean'
					isUnix() ? sh(cleanCmd) : bat(cleanCmd)
				}
			}
		}
	}

	post {
		success {
			echo '✅ Build et analyse SonarQube terminés avec succès.'
		}
		failure {
			echo '❌ Une erreur est survenue. Voir les logs pour plus de détails.'
		}
		always {
			echo '📦 Pipeline terminé (succès ou échec).'
		}
	}
}
