// git仓库凭证
def git_voucher = "34ba31b6-2cb7-4ad4-b222-fefb39b5eaec"
// git仓库地址
def git_url = "git@github.com:cc950627/gulimall.git"
// 获取当前选择的项目信息
def projectInfos = "${project_infos}".split(",");
// 服务发布的机器
def hosts = "[192.168.56.10]";

pipeline {
    agent any;

    stages {
        stage(' 代码拉取 ') {
            steps {
                checkout([$class: 'GitSCM', branches: [[name: "*/${branch}"]], doGenerateSubmoduleConfigurations: false, extensions: [], submoduleCfg: [], userRemoteConfigs: [[credentialsId: "${git_voucher}", url: "${git_url}"]]])
            }
        }
        stage(' 代码审查 ') {
            steps {
                script {
                    for (projectInfo in projectInfos) {
                        def projectName = "${projectInfo}".split("@")[0];
                        def projectProt = "${projectInfo}".split("@")[1];
                        scannerHome = tool 'sonar-scanner'
                        withSonarQubeEnv('sonar') {
                            sh """
                                cd ${projectName}
                                ${scannerHome}/bin/sonar-scanner
                            """
                        }
                    }
                }
            }
        }
        //stage(' 代码安装 ') {
        //    steps {
        //       sh "mvn clean install"
        //   }
        //}
        //stage(' 代码打包 ') {
        //    steps {
        //        script {
        //            for (projectInfo in projectInfos) {
        //                def projectName = "${projectInfo}".split("@")[0];
        //                sh "mvn -f ${projectName} clean package"
        //            }
        //        }
        //    }
        //}
        stage(' 代码部署 ') {
            steps {
                script {
                     for (projectInfo in projectInfos) {
                        def projectName = "${projectInfo}".split("@")[0];
                        for (host in hosts) {
                            sshPublisher(publishers: [sshPublisherDesc(configName: "${host}", transfers: [sshTransfer(cleanRemote: false, excludes: '', execCommand: """cd /mydata/jenkins/web/${projectName}
                            rm -rf ${projectName}-0.0.1-SNAPSHOT.jar
                            ./${projectName}.sh""", execTimeout: 120000, flatten: false, makeEmptyDirs: false, noDefaultExcludes: false, patternSeparator: '[, ]+', remoteDirectory: "/mydata/jenkins/web/${projectName}", remoteDirectorySDF: false, removePrefix: "${projectName}/target/", sourceFiles: "${projectName}/target/*.jar")], usePromotionTimestamp: false, useWorkspaceInPromotion: false, verbose: false)])
                        }
                     }
                }
            }
        }
    }
}
