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

 import com.netease.mebius.client.action.CodeAnalyze;
 import com.netease.mebius.client.enums.ExecType;
 import com.netease.mebius.client.model.MethodCallResult;
 import com.netease.mebius.client.model.MethodsCallResult;
 import com.netease.mebius.client.model.project.GitParam;
 import com.netease.mebius.client.model.project.ProjectParam;
 import lombok.extern.slf4j.Slf4j;
 import org.assertj.core.util.Lists;
 import org.junit.jupiter.api.Test;
 import java.util.List;

 import static com.netease.mebius.client.enums.GitAccessType.ACCESS_TOKEN;

 /**
  * 代码分析测试类
  */
 @Slf4j
 public class CodeAnalyzeTest {
     //git通行令牌
     final static String gitAccessToken = "********";
     //项目路径
     final static String PROJECT_PATH = "D:\\mebiuscode\\code\\newcode\\mebius-examples";
     //当前分支(需和本地分支保持一致)
     final static String currentBranch = "feature-syh-20220810";
     //对比分支
     final static String compareBranch = "master";

     /**
      * 执行代码分析(执行前，需保证本地代码已编译)
      */
     @Test
     public void codeAnalyzeResultTEST() {

         GitParam gitParam = new GitParam();
         gitParam.setGitAccessType(ACCESS_TOKEN);
         gitParam.setGitAccessToken(gitAccessToken);
         gitParam.setGitRepoDir(PROJECT_PATH);

         List<ProjectParam> projectParams = Lists.newArrayList();
         ProjectParam projectParam = new ProjectParam();
         projectParam.setProjectRootPath(PROJECT_PATH);
         projectParam.setExecType(ExecType.BRANCH_DIFF);
         projectParam.setCurrentBranch(currentBranch);
         projectParam.setCompareBranch(compareBranch);
         projectParam.setGitParam(gitParam);
         projectParams.add(projectParam);

         try {
             List<MethodsCallResult> results = CodeAnalyze.analyzeWithDiff(projectParams, null);
             //根据自身需要的信息来输出或处理
             for (MethodsCallResult result : results) {
                 for (MethodCallResult methodCallResult : result.getList()) {
                     log.debug("Top class info: " + methodCallResult.getTopClassName() + "." + methodCallResult.getMethod());
                 }
             }
         } catch (Exception e) {
             e.printStackTrace();
         }
     }
 }