pipeline {
	agent any

    tools {
		maven 'Maven'
        jdk 'JDK'
    }

    environment {
		SONAR_SCANNER_HOME = tool 'SonarQubeScanner'
        SONAR_TOKEN = credentials('SonarQube_token')
        SONAR_HOST_URL = 'http://host.docker.internal:9000'
        PROJECT_KEY = 'TP-security'
        PROJECT_NAME = 'TP-security'
        MAVEN_OPTS = '-Dmaven.test.failure.ignore=false'
    }

    stages {

		stage('Check Environment') {
			steps {
				script {
					def cmds = isUnix() ? ['mvn --version', 'java --version'] : ['mvn --version', 'java --version']
                    cmds.each { cmd -> isUnix() ? sh(cmd) : bat(cmd) }
                }
            }
        }

        stage('SonarQube Analysis') {
			steps {
				withSonarQubeEnv('SonarQube') {
					script {
						def scanCmd = """
                            ${SONAR_SCANNER_HOME}/bin/sonar-scanner \
                            -Dsonar.projectKey=${PROJECT_KEY} \
                            -Dsonar.projectName=${PROJECT_NAME} \
                            -Dsonar.java.binaries=target/classes \
                            -Dsonar.sources=src/main/java \
                            -Dsonar.tests=src/test/java \
                            -Dsonar.junit.reportPaths=target/surefire-reports \
                            -Dsonar.token=${SONAR_TOKEN} \
                            -Dsonar.host.url=${SONAR_HOST_URL}
                        """
                        isUnix() ? sh(scanCmd) : bat(scanCmd)
                    }
                }
            }
        }

        stage('Quality Gate') {
			steps {
				timeout(time: 2, unit: 'MINUTES') {
					waitForQualityGate abortPipeline: true
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
