def mvnCmd(String cmd) {
    sh 'mvn -B -s .mvn/settings.xml ' + cmd
}

pipeline {
    agent {
        node {
            label 'carbonio-agent-v1'
        }
    }
    parameters {
        booleanParam defaultValue: false, description: 'Whether to upload the packages in playground repositories', name: 'PLAYGROUND'
    }
    environment {
        ARTIFACTORY_ACCESS = credentials('artifactory-jenkins-gradle-properties-splitted')
        BUILD_PROPERTIES_PARAMS = ' -Dartifactory_user=$ARTIFACTORY_ACCESS_USR -Dartifactory_password=$ARTIFACTORY_ACCESS_PSW'
    }
    options {
        buildDiscarder(logRotator(numToKeepStr: '25'))
        timeout(time: 2, unit: 'HOURS')
        skipDefaultCheckout()
    }
    stages {

        stage('Checkout') {
            steps {
                checkout scm
            }
        }

        stage('Build') {
            steps {
                mvnCmd("$BUILD_PROPERTIES_PARAMS -Dmaven.test.skip package")
                sh 'mkdir staging'
                sh 'cp -r package pom.xml src target staging'
                stash includes: 'staging/**', name: 'staging'
            }
            post {
                always {
                    archiveArtifacts artifacts: 'target/*.jar', fingerprint: true
                }
            }
        }

        stage('Publish to maven') {
            when {
                buildingTag()
            }
            steps {
                mvnCmd("$BUILD_PROPERTIES_PARAMS deploy")
            }
        }

        stage('Build deb/rpm') {
            stages {
                stage('pacur') {
                    parallel {
                        stage('Ubuntu 20.04') {
                            agent {
                                node {
                                    label 'pacur-agent-ubuntu-20.04-v1'
                                }
                            }
                            steps {
                                unstash 'staging'
                                sh 'cp -r staging /tmp'
                                sh 'sudo pacur build ubuntu-focal /tmp/staging/package'
                                stash includes: 'artifacts/', name: 'artifacts-ubuntu-focal'
                            }
                            post {
                                always {
                                    archiveArtifacts artifacts: 'artifacts/*.deb', fingerprint: true
                                }
                            }
                        }
                        stage('Rocky 8') {
                            agent {
                                node {
                                    label 'pacur-agent-rocky-8-v1'
                                }
                            }
                            steps {
                                unstash 'staging'
                                sh 'cp -r staging /tmp'
                                sh 'sudo pacur build rocky-8 /tmp/staging/package'
                                stash includes: 'artifacts/', name: 'artifacts-rocky-8'
                            }
                            post {
                                always {
                                    archiveArtifacts artifacts: 'artifacts/*.rpm', fingerprint: true
                                }
                            }
                        }
                    }
                }
            }
        }
        stage('Upload To Playground') {
            when {
                anyOf {
                    branch 'playground/*'
                    expression {
                        params.PLAYGROUND == true
                    }
                }
            }
            steps {
                unstash 'artifacts-ubuntu-focal'
                unstash 'artifacts-rocky-8'

                script {
                    def server = Artifactory.server 'zextras-artifactory'
                    def buildInfo
                    def uploadSpec
                    buildInfo = Artifactory.newBuildInfo()
                    uploadSpec ='''{
                        "files": [{
                            "pattern": "artifacts/*focal*.deb",
                            "target": "ubuntu-playground/pool/",
                            "props": "deb.distribution=focal;deb.component=main;deb.architecture=amd64"
                        },
                        {
                            "pattern": "artifacts/(carbonio-consul-client)-(*).rpm",
                            "target": "centos8-playground/zextras/{1}/{1}-{2}.rpm",
                            "props": "rpm.metadata.arch=x86_64;rpm.metadata.vendor=zextras"
                        }]
                    }'''
                    server.upload spec: uploadSpec, buildInfo: buildInfo, failNoOp: false
                }
            }
        }
        stage('Upload & Promotion Config') {
            when {
                anyOf {
                    branch 'release/*'
                    buildingTag()
                }
            }
            steps {
                unstash 'artifacts-ubuntu-focal'
                unstash 'artifacts-rocky-8'

                script {
                    def server = Artifactory.server 'zextras-artifactory'
                    def buildInfo
                    def uploadSpec
                    def config

                    //ubuntu
                    buildInfo = Artifactory.newBuildInfo()
                    buildInfo.name += '-ubuntu'
                    uploadSpec = '''{
                        "files": [{
                            "pattern": "artifacts/*focal*.deb",
                            "target": "ubuntu-rc/pool/",
                            "props": "deb.distribution=focal;deb.component=main;deb.architecture=amd64"
                        }]
                    }'''
                    server.upload spec: uploadSpec, buildInfo: buildInfo, failNoOp: false
                    config = [
                        'buildName': buildInfo.name,
                        'buildNumber': buildInfo.number,
                        'sourceRepo': 'ubuntu-rc',
                        'targetRepo': 'ubuntu-release',
                        'comment': 'Do not change anything! Just press the button',
                        'status': 'Released',
                        'includeDependencies': false,
                        'copy': true,
                        'failFast': true
                    ]
                    Artifactory.addInteractivePromotion server: server, promotionConfig: config, displayName: "Ubuntu Promotion to Release"
                    server.publishBuildInfo buildInfo

                    //rocky8
                    buildInfo = Artifactory.newBuildInfo()
                    buildInfo.name += '-centos8'
                    uploadSpec = '''{
                        "files": [{
                            "pattern": "artifacts/(carbonio-consul-client)-(*).rpm",
                            "target": "centos8-rc/zextras/{1}/{1}-{2}.rpm",
                            "props": "rpm.metadata.arch=x86_64;rpm.metadata.vendor=zextras"
                        }]
                    }'''
                    server.upload spec: uploadSpec, buildInfo: buildInfo, failNoOp: false
                    config = [
                        'buildName': buildInfo.name,
                        'buildNumber': buildInfo.number,
                        'sourceRepo': 'centos8-rc',
                        'targetRepo': 'centos8-release',
                        'comment': 'Do not change anything! Just press the button',
                        'status': 'Released',
                        'includeDependencies': false,
                        'copy': true,
                        'failFast': true
                    ]
                    Artifactory.addInteractivePromotion server: server, promotionConfig: config, displayName: 'Centos8 Promotion to Release'
                    server.publishBuildInfo buildInfo
                }
            }
        }
    }
}