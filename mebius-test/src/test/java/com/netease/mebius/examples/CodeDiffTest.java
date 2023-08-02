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

 import com.netease.mebius.client.action.CodeDiff;
 import com.netease.mebius.client.enums.ExecType;
 import com.netease.mebius.client.enums.GitAccessType;
 import com.netease.mebius.client.model.project.GitParam;
 import com.netease.mebius.client.model.project.ProjectParam;
 import lombok.extern.slf4j.Slf4j;
 import org.jacoco.core.internal.diff.ClassInfo;
 import org.jacoco.core.internal.diff.MethodInfo;
 import org.junit.jupiter.api.Test;

 import java.util.List;

 import static com.netease.mebius.client.enums.ExecType.BRANCH_DIFF;

 /**
  * 代码diff测试类
  */
 @Slf4j
public class CodeDiffTest {
    //执行类型(如：全量:ALL、分支对比:BRANCH_DIFF、commit、版本对比:COMMIT_DIFF、tag对比:TAG_DIFF、分支&commit对比:BRANCHANDCOMMIT_DIFF)
    final  static  ExecType execType=ExecType.BRANCH_DIFF;
    //项目根目录路径
    final static  String projectRootPath ="D:\\mebiuscode\\code\\newcode\\mebius-examples";
    //当前分支
    final static String currentBranch = "syh-feature-20220812";
    //对比分支
    final static String compareBranch = "feature-syh-20220819";
    //当前commit版本
    //final static String currentCommit = "3303405a";
    //对比commit版本
    //final static String compareCommit = "fc34b120";
    //当前tag
    final static String currentTag = "***";
    //对比tag
    final static String compareTag = "***";
    //Git参数
    final static GitParam gitParam=new GitParam();
    //git访问令牌
    final static String gitAccessToken = "**********";
    //相对路径
    final static String gitRepoDir = "mebius-examples";

    @Test
    public  void codeDiffResultTest() {
        //codeoperate中的参数
        GitParam gitParam=new GitParam();
        gitParam.setGitAccessType(GitAccessType.ACCESS_TOKEN);
        gitParam.setGitAccessToken(gitAccessToken);
        gitParam.setGitRepoDir(gitRepoDir);

        ProjectParam projectParam=new ProjectParam();
        projectParam.setProjectRootPath(projectRootPath);
        projectParam.setExecType(BRANCH_DIFF);
        projectParam.setCurrentBranch(currentBranch);
        projectParam.setCompareBranch(compareBranch);
        projectParam.setGitParam(gitParam);
        //projectParam.setCurrentCommit(currentCommit);
        //projectParam.setCompareCommit(compareCommit);

        try {
            List<ClassInfo> classInfos=CodeDiff.codeDiff(projectParam);
            classInfos.forEach(classInfo -> {
                for (MethodInfo methodInfo : classInfo.getMethodInfos()) {
                    log.debug("Diff method info:{}.{} " , classInfo.getClassName() , methodInfo.getMethodName());
                    log.debug("Diff addLine info:{} " , classInfo.getAddLines());
                    log.debug("Diff delLine info:{} " ,classInfo.getDelLines());
                }
            });
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}