 /*
  * Licensed to the Apache Software Foundation (ASF) under one
  * or more contributor license agreements.  See the NOTICE file
  * distributed with this work for additional information
  * regarding copyright ownership.  The ASF licenses this file
  * to you under the Apache License, Version 2.0 (the
  * "License"); you may not use this file except in compliance
  * with the License.  You may obtain a copy of the License at
  *
  *     http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License.
  */
package com.netease.mebius.examples;

 import com.netease.mebius.client.action.CodeOperate;
 import com.netease.mebius.client.action.CoverageOperate;
 import com.netease.mebius.client.action.CoverageReport;
 import com.netease.mebius.client.action.ProjectCompile;
 import com.netease.mebius.client.enums.ExecType;
 import com.netease.mebius.client.enums.ReportType;
 import com.netease.mebius.client.model.coverage.ProjectFile;
 import com.netease.mebius.client.model.coverage.ReportParam;
 import com.netease.mebius.client.model.project.GitParam;
 import com.netease.mebius.client.model.project.MavenParam;
 import com.netease.mebius.client.model.project.ProjectParam;
 import junit.framework.Assert;
 import lombok.extern.slf4j.Slf4j;
 import org.assertj.core.util.Lists;
 import org.jacoco.core.analysis.IBundleCoverage;
 import org.jacoco.core.analysis.IPackageCoverage;
 import org.jacoco.core.analysis.ISourceFileCoverage;
 import org.junit.jupiter.api.MethodOrderer;
 import org.junit.jupiter.api.Order;
 import org.junit.jupiter.api.Test;
 import org.junit.jupiter.api.TestMethodOrder;
 import java.io.File;
 import java.util.List;

 import static com.netease.mebius.client.enums.GitAccessType.ACCESS_TOKEN;

/**
 * 覆盖率操作场景串联测试类
 */
@Slf4j
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class CoverageOperateSceneTest {
    //git通行令牌
    final static String gitAccessToken = "**********";
    //项目路径
    final static String PROJECT_PATH = "D:\\mebiuscode\\code\\newcode\\mebius-examples";
    //项目根目录
    final static String projectRootPath= "D:\\mebiuscode\\code\\newcode\\mebius-examples";
    //当前分支
    final static String currentBranch = "feature-syh-20220810";
    //对比分支
    final static String compareBranch = "master";
    //clone路径
    private final static String cloneUrl = "https://g.hz.netease.com/qa-tech/mebius-examples.git";
    //检出分支名
    private final static String branchName = "feature-syh-20220810";
    // Git工程路径(传项目所在的绝对路径)
    private final static String gitRepoDir = "D:/mebiuscode/code/newcode/mebius-examples";
    // maven所在安装本地路径（如：/home/maven）
    final static String mavenPath = "D:\\newsoftware\\apache-maven-3.2.5";
    //工程pom路径
    final static String pomPath = "D:\\mebiuscode\\code\\newcode\\mebius-examples\\pom.xml";
    //执行的编译命令（如:compile、install、package）
    final static String command = "compile";
    //目标应用所在ip
    final  static String ip="127.0.0.1";
    //目标应用的jacoco端口
    final static  Integer port =8081;
    //目标exec文件存放路径
    final  static String execFilePath="src/resources/dumpexe/execFilePath";
    //合并后目标的exec文件名
    final  static String destFile="src/resources";
    //报告title
    private final  static  String title="syhtest";
    //exec文件
    private  final  static  String execDataPath = "src/resources/dumpexe/execFilePath";
    //classes文件
    private  final  static  String classesDirectory = "target/classes";
    //源码文件
    private  final  static  String sourceDirectory = "src/main/java";
    //报告文件（多子工程模式下，reportDirectory必须相同，不支持多报告路径）
    private  final  static File reportDirectory = new File("src/test/reportFilePath");
    private  final  static  String reportDirectoryPath = "src/test/reportFilePath";
    private  final  static ReportType reportType=ReportType.HTML;
    //被分析的项目列表
    private static List<ProjectFile> projectList= Lists.newArrayList();
    //git用户名
    private final static String gitUser="***";
    //git用户密码
    private final static String gitPassword="***";

    /**
     * 拉取代码
     */
    @Test
    @Order(1)
    public void codeOperateCheckoutAndPullTest() {
        GitParam gitParam = new GitParam();
        gitParam.setGitRepoDir(gitRepoDir);
        gitParam.setGitAccessType(ACCESS_TOKEN);
        gitParam.setGitAccessToken(gitAccessToken);
        try {
            //git clone代码--文件不为空报错
            //operate.cloneCode(cloneUrl,branchName);
            //log.debug("代码克隆成功！");
            CodeOperate result=new CodeOperate(gitParam);
            result.checkoutAndPull(cloneUrl, branchName);
            log.debug("检出拉取成功!");

        } catch (Exception e) {
            log.error("测试失败", e);
        }
    }

    /**
     * maven编译
     */
    @Test
    @Order(2)
    public  void ProjectCompileResultTest() {

        MavenParam mavenParam=new MavenParam();
        mavenParam.setMavenPath(mavenPath);
        mavenParam.setPomPath(pomPath);
        mavenParam.setCommand(command);

        try {
            boolean result = ProjectCompile.mavenCompile(mavenParam);
            log.debug("代码编译结果:"+result);
            Assert.assertEquals(result,true);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    /**
     * 远程dump jacoco exec文件
     */
    @Test
    @Order(3)
    public  void CoverageOperateDumpTest() {
        try {
            boolean dumpresult = CoverageOperate.dump(ip,port, execFilePath);
            log.debug("远程dump jacoco exec文件结果:"+dumpresult);
            Assert.assertEquals(dumpresult,true);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 单工程生成覆盖率报告
     */
    @Test
    @Order(4)
    public void CoverageReportResetTest(){
        ProjectFile projectFile= new ProjectFile(projectRootPath,classesDirectory,sourceDirectory);

        projectList.add(projectFile);
        ReportParam reportParam=  new ReportParam(title, execDataPath, reportDirectoryPath, reportType, this.projectList);
        reportParam.setReportDirectory(reportDirectory);

        ProjectParam projectParam = new ProjectParam();
        projectParam.setProjectRootPath(projectRootPath);
        projectParam.setExecType(ExecType.BRANCH_DIFF);
        projectParam.setCurrentBranch(currentBranch);
        projectParam.setCompareBranch(compareBranch);

        GitParam gitParam = new GitParam();
        gitParam.setGitRepoDir(gitRepoDir);
        gitParam.setGitAccessType(ACCESS_TOKEN);
        gitParam.setGitAccessToken(gitAccessToken);
        gitParam.setGitUser(gitUser);
        gitParam.setGitPassword(gitPassword);
        projectParam.setGitParam(gitParam);

        try {
            IBundleCoverage iBundleCoverage =  CoverageReport.generate(reportParam,projectParam);
            for (IPackageCoverage aPackage : iBundleCoverage.getPackages()) {
                aPackage.getClassCounter().getCoveredCount();
                for (ISourceFileCoverage sourceFile : aPackage.getSourceFiles()) {
                    sourceFile.getPackageName();
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

    }
}