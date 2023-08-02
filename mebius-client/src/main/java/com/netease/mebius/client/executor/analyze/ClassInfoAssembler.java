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
 package com.netease.mebius.client.executor.analyze;

 import com.netease.mebius.client.action.CodeDiff;
 import com.netease.mebius.client.exception.MebiusException;
 import com.netease.mebius.client.executor.diff.ASTGenerator;
 import com.netease.mebius.client.executor.diff.GitAdapter;
 import com.netease.mebius.client.executor.helper.GitHelper;
 import com.netease.mebius.client.model.SpecifyParam;
 import com.netease.mebius.client.model.project.ProjectParam;
 import com.netease.mebius.client.utils.FileUtils;
 import lombok.extern.slf4j.Slf4j;
 import org.apache.commons.collections.CollectionUtils;
 import org.apache.commons.lang.BooleanUtils;
 import org.apache.commons.lang.StringUtils;
 import org.assertj.core.util.Lists;
 import org.eclipse.jdt.core.dom.MethodDeclaration;
 import org.eclipse.jgit.lib.Ref;
 import org.jacoco.core.internal.diff.ClassInfo;
 import org.jacoco.core.internal.diff.MethodInfo;

 import java.io.IOException;
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;

 import static com.netease.mebius.client.executor.analyze.SourceMethodParser.isOsWindows;

 /**
  * ClassInfo组装器
  */
 @Slf4j
 public class ClassInfoAssembler {

     public final static String REF_HEADS = "refs/heads/";

     /**
      * 计算diff的classInfo
      *
      * @param projectParams
      * @return
      */
     public static List<ClassInfo> calDiffClassInfo(List<ProjectParam> projectParams) throws MebiusException {
         List<ClassInfo> allClassInfos = Lists.newArrayList();
         for (ProjectParam projectParam : projectParams) {
             List<ClassInfo> classInfos = CodeDiff.codeDiff(projectParam);
             FileUtils.classFileExcludeAndRelation(projectParam, classInfos);
             allClassInfos.addAll(classInfos);
         }
         if (CollectionUtils.isEmpty(allClassInfos)) {
             log.warn("Not calculated diff classes.");
             return allClassInfos;
         }
         log.debug("Class info size:{}", allClassInfos.size());
         return allClassInfos;
     }


     /**
      * 计算指定的classInfo
      *
      * @param projectParams
      * @param specifyParams
      * @return
      */
     public static List<ClassInfo> calSpecifyClassInfo(List<ProjectParam> projectParams, List<SpecifyParam> specifyParams) throws MebiusException {
         List<ClassInfo> allClassInfos = Lists.newArrayList();
         for (SpecifyParam specifyParam : specifyParams) {
             List<ClassInfo> classInfos = assembleClassInfo(projectParams, specifyParam);
             allClassInfos.addAll(classInfos);
         }
         //根据class聚合
         HashMap<String, ClassInfo> classInfoHashMap = new HashMap<>();
         for (ClassInfo classInfo : allClassInfos) {
             String key = classInfo.getPackages() + "." + classInfo.getClassName();
             if (classInfoHashMap.get(key) == null) {
                 classInfoHashMap.put(key, classInfo);
             } else {
                 if (classInfo.getMethodInfos() != null) {
                     classInfoHashMap.get(key).getMethodInfos().addAll(classInfo.getMethodInfos());
                 }
                 if (classInfo.getDelMethodInfos() != null) {
                     classInfoHashMap.get(key).getDelMethodInfos().addAll(classInfo.getDelMethodInfos());
                 }
                 if (classInfo.getNewMethodInfos() != null) {
                     classInfoHashMap.get(key).getNewMethodInfos().addAll(classInfo.getNewMethodInfos());
                 }
                 if (classInfo.getAddLines() != null) {
                     classInfoHashMap.get(key).getAddLines().addAll(classInfo.getAddLines());
                 }
                 if (classInfo.getDelLines() != null) {
                     classInfoHashMap.get(key).getDelLines().addAll(classInfo.getDelLines());
                 }
             }
         }
         List<ClassInfo> resultList = Lists.newArrayList();
         classInfoHashMap.keySet().stream().map(classInfoHashMap::get).forEach(resultList::add);
         log.debug("Class info size:{}", resultList.size());
         return resultList;
     }

     public static List<ClassInfo> assembleClassInfoMultipleMethod(List<ProjectParam> projectParams, List<SpecifyParam> sps) throws MebiusException {
         List<ClassInfo> classInfoList = Lists.newArrayList();
         for (SpecifyParam sp : sps) {
             List<ClassInfo> classInfos = assembleClassInfo(projectParams, sp);
             classInfoList.addAll(classInfos);
         }
         return classInfoList;
     }


     /**
      * 单个组装classInfo
      *
      * @param projectParams
      * @param sp
      * @return
      * @throws Exception
      */
     public static List<ClassInfo> assembleClassInfo(List<ProjectParam> projectParams, SpecifyParam sp) throws MebiusException {
         List<ClassInfo> classInfos = new ArrayList<>();
         for (ProjectParam projectParam : projectParams) {
             //传入git信息则做代码的更新
             codeUpdate(projectParam);

             List<MethodInfo> methodInfoList = new ArrayList<>();
             //获取java文件绝对路径
             String javaFilePath = FileUtils.searchJavaFile(sp.getClassName(), projectParam.getProjectRootPath());
             if (StringUtils.isBlank(javaFilePath)) {
                 log.error("Java file not found:{}", sp.getClassName());
                 continue;
             }
             log.debug("Java file path:{}", javaFilePath);
             if (javaFilePath.contains("/src/test/java/") || !javaFilePath.endsWith(".java")) {
                 continue;
             }
             //获取java源码
             if (isOsWindows()) {
                 javaFilePath = javaFilePath.replaceAll("/", "\\\\");
             }
             String newClassContent;
             try {
                 newClassContent = FileUtils.readFile(javaFilePath);
             } catch (IOException e) {
                 log.error(e.getMessage(), e);
                 throw new MebiusException("Unable to find sourceCode file: " + javaFilePath);
             }
             ASTGenerator newAstGenerator = new ASTGenerator(newClassContent);
             MethodDeclaration[] newMethods = newAstGenerator.getMethods();
             Map<String, MethodDeclaration> methodsMap = new HashMap<String, MethodDeclaration>();
             //解析出文件中的所有方法
             for (int i = 0; i < newMethods.length; i++) {
                 methodsMap.put(newMethods[i].getName().toString() + newMethods[i].parameters().toString(), newMethods[i]);
             }
             if (StringUtils.isBlank(sp.getMethodName())) {
                 for (final MethodDeclaration method : newMethods) {
                     MethodInfo methodInfo = newAstGenerator.getMethodInfo(method);
                     methodInfoList.add(methodInfo);
                 }
             } else if (StringUtils.isBlank(sp.getParams())) {
                 for (String key : methodsMap.keySet()) {
                     if (key.contains(sp.getMethodName())) {
                         MethodInfo methodInfo = newAstGenerator.getMethodInfo(methodsMap.get(key));
                         methodInfoList.add(methodInfo);
                     }
                 }
             } else {
                 if (isMethodExist(sp, methodsMap)) {
                     MethodInfo methodInfo = newAstGenerator.getMethodInfo(methodsMap.get(sp.getMethodName() + sp.getParams()));
                     methodInfoList.add(methodInfo);
                 }
             }
             if (methodInfoList.size() > 0 && newAstGenerator.getClassInfo(methodInfoList) != null) {
                 ClassInfo classInfo = newAstGenerator.getClassInfo(methodInfoList, null);
                 classInfo.setClassFile(javaFilePath.split(projectParam.getProjectRootPath())[1]);
                 classInfos.add(classInfo);
             }
         }
         return classInfos;
     }

     /**
      * 判断指定方法是否存在
      *
      * @param specifyParam 新分支的方法
      * @param methodsMap   master分支的方法
      * @return
      */
     public static boolean isMethodExist(SpecifyParam specifyParam, final Map<String, MethodDeclaration> methodsMap) {
         // 方法名+参数一致才一致
         if (!methodsMap.containsKey(specifyParam.getMethodName() + specifyParam.getParams())) {
             return false;
         }
         return true;
     }

     /**
      * 代码更新
      *
      * @param projectParam
      */
     private static void codeUpdate(ProjectParam projectParam) throws MebiusException {
         if (BooleanUtils.isFalse(projectParam.getNeedUpdate())) {
             return;
         }
         //git授权
         GitHelper.setAuthorization(projectParam);
         //获取本地分支
         GitAdapter gitAdapter = new GitAdapter(projectParam.getProjectRootPath());
         try {
             Ref localBranchRef = gitAdapter.getRepository().exactRef(REF_HEADS + projectParam.getCurrentBranch());
             //更新本地分支
             gitAdapter.checkOutAndPull(localBranchRef, projectParam.getCurrentBranch());
             //最终要追踪到当前分支，为了防止代码不一致问题
             gitAdapter.checkOut(projectParam.getCurrentBranch());
             //最终要追踪到当前版本，为了防止代码不一致问题
             gitAdapter.reset(projectParam.getCurrentCommit());
         } catch (Exception e) {
             log.error(e.getMessage(), e);
             throw new MebiusException("代码更新失败:" + e.getMessage());
         }
     }
 }
