pipeline {
	agent any

    tools {
		maven 'Maven'
        jdk 'JDK'
    }

    environment {
		SONAR_SCANNER_HOME = tool 'SonarQubeScanner'
        SONAR_TOKEN = credentials('SonarQube_token')
        ZAP_REPORT = 'zap-report.html'
        TARGET_URL = 'http://host.docker.internal:8083'
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

		stage('Check Docker Access') {
            steps {
                script {
                    isUnix() ? sh('docker --version') : bat('docker --version')
                }
            }
        }


        stage('Build & Push Docker Image'){
            steps {
                script {
                 withCredentials([usernamePassword(
                    credentialsId: 'docker-hub-creds',
                    usernameVariable: 'DOCKER_USERNAME',
                    passwordVariable: 'DOCKER_PASSWORD'
                )]) {
                    sh """
                        docker build -t olacgk/tpzapsecurity:latest .
                        echo "$DOCKER_PASSWORD" | docker login -u "$DOCKER_USERNAME" --password-stdin
                        docker push olacgk/tpzapsecurity:latest
                    """
                }
                }

            }
        }

        stage('Run OWASP ZAP Scan') {
	steps {
		echo '🛡️ Lancement de OWASP ZAP baseline scan'
		script {
            def hostIp = sh(script: "hostname -I | awk '{print \$1}'", returnStdout: true).trim()
            env.TARGET_URL = "http://${hostIp}:8083"

            sh """
                docker run --rm \\
                  --user \$(id -u):\$(id -g) \\
                  --network=host \\
                  -v ${env.WORKSPACE}:/zap/wrk:rw \\
                  zaproxy/zap-stable zap-baseline.py \\
                  -t ${env.TARGET_URL} \\
                  -r zap-report.html \\
                  -J zap-report.json \\
                  -z "-config api.disablekey=true"
            """
            sh 'ls -la zap-report.*'
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
        junit 'target/surefire-reports/*.xml'
            sh '''
                mkdir -p reports
                cp zap-report.* reports/ || true
                cp target/surefire-reports/* reports/ || true
            '''
            archiveArtifacts artifacts: 'reports/**', fingerprint: true
    }
	}
}
