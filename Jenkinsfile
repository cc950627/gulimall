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
    }
    post {
      always {
        emailext subject: '构建通知xxx', body: '构建完成'
      }
    }
}
