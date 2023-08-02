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

 import com.netease.mebius.client.action.CoverageReport;
 import com.netease.mebius.client.enums.ExecType;
 import com.netease.mebius.client.enums.ReportType;
 import com.netease.mebius.client.model.coverage.ProjectFile;
 import com.netease.mebius.client.model.coverage.ReportParam;
 import com.netease.mebius.client.model.project.GitParam;
 import com.netease.mebius.client.model.project.ProjectParam;
 import org.assertj.core.util.Lists;
 import org.jacoco.core.analysis.IBundleCoverage;
 import org.jacoco.core.analysis.IPackageCoverage;
 import org.jacoco.core.analysis.ISourceFileCoverage;
 import org.junit.jupiter.api.Test;

 import java.io.File;
 import java.util.List;

 import static com.netease.mebius.client.enums.ExecType.ALL;
 import static com.netease.mebius.client.enums.GitAccessType.ACCESS_TOKEN;

 /**
  * 覆盖率报告测试类
  */
public class CoverageReportTest {

    private final  static  String title="syhtest"; //报告title
    //exec文件
    private  final  static  String execDataPath = "src/resources/dumpexe/execFilePath";
    //classes文件
    private  final  static  String classesDirectory = "target/classes";
    //源码文件
    //private  final  static  File sourceDirectory = new File("src/main/java");
    private  final  static  String sourceDirectory = "src/main/java";
    //报告文件（多子工程模式下，reportDirectory必须相同，不支持多报告路径）
    private  final  static  File reportDirectory = new File("src/test/reportFilePath");
    private  final  static  String reportDirectoryPath = "src/test/reportFilePath";
    private  final  static ReportType reportType=ReportType.HTML;



    //执行类型
    private final static ExecType execType = ALL;
    //项目根目录路径
    private final static String projectRootPath="D:\\mebiuscode\\code\\newcode\\mebius-examples";
    //当前分支
    private final static String currentBranch="syh-feature-20220812";
    //对比分支
    final static String compareBranch = "feature-syh-20220810";
    //当前commit版本
    //final static String currentCommit = "22bf1130";
    //对比commit版本
    //final static String compareCommit = "2c726cda";
    //当前tag
    final static String currentTag = "***";
    //对比tag
    final static String compareTag = "***";
    // Git工程路径(传项目所在的绝对路径)----疑问？
    //    //相对路径，"../../mebius"
    private final static String gitRepoDir = "D:\\mebiuscode\\code\\newcode\\mebius-example";
    //git access token
    private final static String gitAccessToken = "***********";
    //git用户名
    private final static String gitUser="***";
    //git用户密码
    private final static String gitPassword="***";
    //被分析的项目列表
    private static List<ProjectFile> projectList= Lists.newArrayList();

    //Git参数
    //final static GitParam gitParam=new GitParam();

    /**
     * 单工程生成覆盖率报告
     */
    @Test
    public void CoverageReportResetTest(){
        ProjectFile projectFile= new ProjectFile(projectRootPath,classesDirectory,sourceDirectory);

        projectList.add(projectFile);
        ReportParam  reportParam=  new ReportParam(title, execDataPath, reportDirectoryPath, reportType, this.projectList);
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
