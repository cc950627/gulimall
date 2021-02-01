// git仓库凭证
def git_voucher = "34ba31b6-2cb7-4ad4-b222-fefb39b5eaec"
// git仓库地址
def git_url = "git@github.com:cc950627/gulimall.git"
// 获取当前选择的项目名称
def projectInfos = "${project_infos}".split(",");

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
                        echo "${projectInfos}---------------------------------"
                        echo "${projectInfo}---------------------------------"
                        echo "${projectName}---------------------------------"
                        echo "${projectProt}---------------------------------"
                        script {
                            scannerHome = tool 'sonar-scanner'
                        }
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
    }
}
