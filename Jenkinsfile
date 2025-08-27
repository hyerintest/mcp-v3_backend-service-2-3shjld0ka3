pipeline {
    agent any
    tools {
        jdk 'jdk17'
        gradle 'gradle7.6.2'
    }
    environment {
        COMMIT_ID = ''
    }
    stages {
        stage('Setup Gradle Wrapper') {
            steps {
                git branch: "${BRANCH}", url: "https://${SOURCE_REPO_URL}/${GROUP_NAME}_${SERVICE_NAME}.git", credentialsId: "${CREDENTIAL_ID}"
                script {
                    echo "Checking gradle-wrapper.jar integrity..."
                    def isValid = sh(script: "unzip -t gradle/wrapper/gradle-wrapper.jar >/dev/null 2>&1 && echo OK || echo BROKEN", returnStdout: true).trim()

                    if (isValid == 'BROKEN') {
                        echo "Detected broken gradle-wrapper.jar. Re-generating..."
                        sh "rm -rf gradle/wrapper gradlew gradlew.bat"
                        sh "gradle wrapper --gradle-version=8.10.2"
                        sh "chmod +x ./gradlew"

                        echo "Committing regenerated wrapper files to Git..."
                        sh 'git config user.email "ci@yourcompany.com"'
                        sh 'git config user.name "jenkins-ci"'
                        sh 'git add gradlew gradlew.bat gradle/wrapper/'
                        sh 'git commit -m "Fix: Re-generated broken gradle-wrapper.jar via Jenkins" || echo "No changes to commit"'
                        withCredentials([usernamePassword(credentialsId: "${CREDENTIAL_ID}", usernameVariable: 'GIT_USER', passwordVariable: 'GIT_PWD')]) {
                            sh 'git push https://${GIT_USER}:${GIT_PWD}@${SOURCE_REPO_URL}/${GROUP_NAME}_${SERVICE_NAME}.git HEAD:${BRANCH}'
                        }
                    } else {
                        echo "gradle-wrapper.jar is valid. Skipping regeneration."
                    }
                }
            }
        }
        stage('Sonarqube Build') {
            steps {
                git branch: "${BRANCH}", url: "https://${SOURCE_REPO_URL}/${GROUP_NAME}_${SERVICE_NAME}.git", credentialsId: "${CREDENTIAL_ID}"
                script {
                    if (env.IS_SONAR == "true") {
                        echo "SonarQube analysis..."
                        sh "chmod +x ./gradlew"
                        sh "./gradlew sonar -Dsonar.host.url=$SONAR_HOST_URL -Dsonar.projectKey=$PROJECT_KEY -Dsonar.projectName=$PROJECT_KEY -Dsonar.token=$SONAR_TOKEN"
                        sh "sleep 60"
                        sh "curl -u ${SONAR_ID}:${SONAR_PWD} ${SONAR_HOST_URL}/api/qualitygates/project_status?projectKey=${PROJECT_KEY} > result.json"
                        def qualityGates = readJSON(file: 'result.json').projectStatus.status
                        echo "Quality Gates Status: ${qualityGates}"

                        if (qualityGates == "ERROR") {
                            error("SonarQube Quality Gate failed")
                        }
                    } else {
                        echo "SonarQube analysis skipped (IS_SONAR != true)"
                    }
                }
            }
        }
        stage('Build') {
            steps {
                git branch: "${BRANCH}", url: "https://${SOURCE_REPO_URL}/${GROUP_NAME}_${SERVICE_NAME}.git", credentialsId: "${CREDENTIAL_ID}"
                script {
                    COMMIT_ID = sh(script: 'git rev-parse --short HEAD', returnStdout: true).trim()
                    echo "Commit ID: ${COMMIT_ID}"

                    echo "Gradle Build ing..."
                    sh "mkdir -p logs"
                    sh "chmod +x ./gradlew"
                    sh "./gradlew clean build jib -PspringProfile=${SPRING_PROFILES_ACTIVE} -PdockerRegistry=${IMAGE_REPO_NAME} -PdockerUser=${HARBOR_USER} -PdockerPassword=${HARBOR_PASSWORD} -PserviceName=${ARGO_APPLICATION} -PcommitRev=${COMMIT_ID}"
                }
            }
        }
        stage('Deploy') {
            steps {
                git (branch: "master", url: "https://${SOURCE_REPO_URL}/${GROUP_NAME}_HelmChart.git", credentialsId: "${CREDENTIAL_ID}")
                dir ("${STAGE}/${SERVICE_NAME}") {
                    sh "find ./ -name values.yaml -type f -exec sed -i \'s/^\\(\\s*tag\\s*:\\s*\\).*/\\1\"\'${ARGO_APPLICATION}-${COMMIT_ID}\'\"/\' {} \\;"
                    sh 'git config --global user.email "info@twolinecode.com"'
                    sh 'git config --global user.name "jenkins-runner"'
                    sh 'git add ./values.yaml'
                    sh "git commit --allow-empty -m \"Pushed Helm Chart: ${ARGO_APPLICATION}-${COMMIT_ID}\""
                    withCredentials([gitUsernamePassword(credentialsId: "${CREDENTIAL_ID}", gitToolName: 'git-tool')]) {
                        sh '''
                        while :
                        do
                            git pull --rebase origin master
                            if git push origin master
                            then
                                break
                            fi
                        done
                        '''
                    }
                }
                dir("${STAGE}/Common") {
                    script {
                        echo "Helm & Kubectl apply..."
                        sh '''
                            helm template . > ./common.yaml
                            kubectl --kubeconfig ../$KUBECONFIG apply -f common.yaml
                            kubectl --kubeconfig ../$KUBECONFIG get secret argocd-initial-admin-secret -n tlc-support -o jsonpath='{.data.password}' | base64 -d > argocd-password.txt
                        '''
                        def PASSWORD = readFile("argocd-password.txt").trim()
                        echo "Sync ArgoCD ing..."
                        sh "argocd login ${ARGO_ENDPOINT}:80 --grpc-web-root-path argocd --username admin --password ${PASSWORD} --plaintext --insecure"
                        sh "argocd app get ${ARGO_APPLICATION} --refresh"
                        sh "argocd app sync ${ARGO_APPLICATION}"
                    }
                }
            }
        }
    }
}
