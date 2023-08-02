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
 import com.netease.mebius.client.enums.GitAccessType;
 import com.netease.mebius.client.model.MethodsCallResult;
 import com.netease.mebius.client.model.project.ProjectParam;
 import org.junit.jupiter.api.Test;

 import java.util.ArrayList;
 import java.util.List;

 /**
  * 代码分析测试类
  */
 public class CodeAnalyzeTestV2 {

     @Test
     public void codeAnalyzeResultTEST() {

         List<ProjectParam> projectParams = new ArrayList<>();
         ProjectParam projectParam = new ProjectParam();
         projectParam.setProjectRootPath("/Users/xuefeifei/IdeaProjects/elephant");
         projectParam.setExecType(ExecType.BRANCH_DIFF);
         projectParam.setCurrentBranch("xff_test_1020");
         projectParam.setCompareBranch("master");
         projectParam.getGitParam().setGitUser("bjxuefeifei");
         projectParam.getGitParam().setGitPassword("xxxx");
         projectParam.getGitParam().setGitAccessType(GitAccessType.ACCOUNT);

         projectParams.add(projectParam);
         try {
             List<MethodsCallResult> result = CodeAnalyze.analyzeWithDiff(projectParams, null);
         } catch (Exception e) {
             e.printStackTrace();
         }
     }
 }