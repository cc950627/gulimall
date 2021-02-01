// git仓库凭证
def git_voucher = "34ba31b6-2cb7-4ad4-b222-fefb39b5eaec"
// git仓库地址
def git_url = "git@github.com:cc950627/gulimall.git"

pipeline {
    agent any;
    // 获取当前选择的项目名称
    def projectInfos = "${project_infos}".split(",");

    stages {
        stage(' 代码拉取 ') {
            steps {
                checkout([$class: 'GitSCM', branches: [[name: "*/${branch}"]], doGenerateSubmoduleConfigurations: false, extensions: [], submoduleCfg: [], userRemoteConfigs: [[credentialsId: "${git_voucher}", url: "${git_url}"]]])
            }
        }
        stage(' 代码审查 ') {
            for (int i = 0; i < projectInfos.length; i++) {
                def projectInfo = projectInfos[i];
                def projectName = "${projectInfo}".split("@")[0];
                def projectProt = "${projectInfo}".split("@")[1];
                steps {
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
