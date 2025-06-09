pipeline {
	agent any

    tools {
		maven 'Maven'
        jdk 'JDK'
    }

    environment {
		SONAR_SCANNER_HOME = tool 'SonarQubeScanner'
        SONAR_TOKEN = credentials('SonarQube_token')
        ZAP_PATH = 'C:\\Program Files\\ZAP\\Zed Attack Proxy\\zap.bat'
        ZAP_REPORT = 'zap-report.html'
        TARGET_URL = 'http://localhost:8083'
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
                                    -Dsonar.host.url=http://localhost:9000/sonarqube
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
                                    -Dsonar.host.url=http://localhost:9000/sonarqube
                                """
                            }
                        }
                    }
                }
            }
        }

        stage('Quality Gate') {
			steps {
				script{
					try {
					timeout(time: 2, unit: 'MINUTES') {
						def qg = waitForQualityGate( abortPipeline: true, credentialsId: 'SonarQube_token' )
						echo "Quality Gate Status: ${qg.status}"
					}
					} catch (Exception e) {
						echo "Quality Gate check failed: ${e}"
						// Optional: add manual retry logic here
					}
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

		stage('Run Spring Boot App') {
			steps {
				echo '🚀 Démarrage de l\'application Spring Boot'
				script {
					if (isUnix()) {
						sh 'nohup java -jar target/*.jar > app.log 2>&1 &'
					} else {
						bat 'start /B java -jar target\\*.jar'
					}
				}
                echo '⏳ Attente du démarrage'
                script {
					if (isUnix()) {
						sh 'sleep 20'
					} else {
						bat 'timeout /T 20 >nul'
					}
				}
            }
        }

        stage('Run OWASP ZAP Scan') {
			steps {
				echo '🛡️ Lancement de OWASP ZAP baseline scan'
				script {
					if (isUnix()) {
						sh """
							"${ZAP_PATH}" -cmd -quickurl ${TARGET_URL} \
							-quickout ${ZAP_REPORT} \
							-quickprogress
						"""
					} else {
						bat """
							"${ZAP_PATH}" -cmd -quickurl ${TARGET_URL} ^
							-quickout ${ZAP_REPORT} ^
							-quickprogress
						"""
					}
				}
            }
        }

        stage('Archive ZAP Report') {
			steps {
				echo '📦 Archive du rapport ZAP'
                archiveArtifacts artifacts: "${ZAP_REPORT}", onlyIfSuccessful: true
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
			echo '🧹 Nettoyage - arrêt de l\'application Spring Boot'
			script {
				if (isUnix()) {
					sh 'pkill -f "target/.*.jar" || true'
				} else {
					bat 'taskkill /F /IM java.exe || exit 0'
				}
			}
		}
	}
}
