pipeline {
    agent any

    stages {
        stage(' 代码拉取 ') {
            steps {
                checkout([$class: 'GitSCM', branches: [[name: '*/master']], doGenerateSubmoduleConfigurations: false, extensions: [], submoduleCfg: [], userRemoteConfigs: [[credentialsId: '2011ac9f-ebe5-4ced-b82f-5ac4b4c9b195', url: 'git@192.168.56.11:gulimall_group/gulimall.git']]])
            }
        }
        stage(' 代码清理 ') {
            steps {
                sh 'mvn clean'
            }
        }
        stage(' 代码编译 ') {
            steps {
                sh 'mvn compile'
            }
        }
        stage(' 代码安装 ') {
            steps {
                sh 'mvn install'
            }
        }
        stage(' 代码打包 ') {
            steps {
                sh 'mvn package'
            }
        }
        stage(' 代码部署 ') {
            steps {
                sshPublisher(publishers: [sshPublisherDesc(configName: '192.168.56.10', transfers: [sshTransfer(cleanRemote: false, excludes: '', execCommand: '''cd /mydata/jenkins/web/gateway
                ./gateway.sh''', execTimeout: 120000, flatten: false, makeEmptyDirs: false, noDefaultExcludes: false, patternSeparator: '[, ]+', remoteDirectory: '/mydata/jenkins/web/gateway', remoteDirectorySDF: false, removePrefix: 'gulimall-gateway/target/', sourceFiles: 'gulimall-gateway/target/*.jar'), sshTransfer(cleanRemote: false, excludes: '', execCommand: '''cd /mydata/jenkins/web/product
                ./product.sh''', execTimeout: 120000, flatten: false, makeEmptyDirs: false, noDefaultExcludes: false, patternSeparator: '[, ]+', remoteDirectory: '/mydata/jenkins/web/product', remoteDirectorySDF: false, removePrefix: 'gulimall-product/target/', sourceFiles: 'gulimall-product/target/*.jar')], usePromotionTimestamp: false, useWorkspaceInPromotion: false, verbose: false)])
            }
        }
    }
    post {
      always {
        emailext subject: '构建通知：${PROJECT_NAME} - Build # ${BUILD_NUMBER} - ${BUILD_STATUS}！', body: '构建完成'
      }
    }
}
