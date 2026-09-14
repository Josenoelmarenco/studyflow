// StudyFlow — Jenkins CI/CD pipeline
//
// A declarative pipeline that builds the project, runs the JUnit suite,
// enforces the JaCoCo coverage gate and packages the application on every
// commit. The whole pipeline runs without a database or a display, because the
// tests use in-memory H2 — which is exactly what makes it CI-friendly.
//
// Requires Maven and JDK 21 to be configured in Jenkins under the names below
// (Manage Jenkins -> Tools).

pipeline {
    agent any

    tools {
        jdk 'jdk-21'
        maven 'maven-3.9'
    }

    options {
        timestamps()
        // Keep the last 10 builds so history doesn't grow without bound.
        buildDiscarder(logRotator(numToKeepStr: '10'))
    }

    stages {
        stage('Checkout') {
            steps {
                checkout scm
            }
        }

        stage('Build') {
            steps {
                // Compile main + test sources without running tests yet.
                sh 'mvn -B -DskipTests clean compile test-compile'
            }
        }

        stage('Test') {
            steps {
                // Run the JUnit suite; JaCoCo records coverage during this phase.
                sh 'mvn -B test'
            }
            post {
                always {
                    // Publish test results whether the stage passed or failed.
                    junit 'target/surefire-reports/*.xml'
                }
            }
        }

        stage('Coverage gate') {
            steps {
                // `verify` runs the JaCoCo check rule: build fails below 70%.
                sh 'mvn -B verify'
            }
            post {
                always {
                    // Archive the human-readable coverage report as a build artifact.
                    archiveArtifacts artifacts: 'target/site/jacoco/**', allowEmptyArchive: true
                }
            }
        }

        stage('Package') {
            steps {
                sh 'mvn -B -DskipTests package'
                archiveArtifacts artifacts: 'target/*.jar', allowEmptyArchive: true
            }
        }
    }

    post {
        success {
            echo 'StudyFlow pipeline succeeded: build, tests and coverage gate all green.'
        }
        failure {
            echo 'StudyFlow pipeline failed — check the stage logs above.'
        }
        always {
            cleanWs()
        }
    }
}
