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
 package com.netease.mebius.client.action;

 import com.netease.mebius.client.exception.MebiusException;
 import com.netease.mebius.client.executor.CallMethodExecutor;
 import com.netease.mebius.client.executor.ChangeMethodExecutor;
 import com.netease.mebius.client.executor.analyze.ClassInfoAssembler;
 import com.netease.mebius.client.executor.analyze.InvokeChainGenerator;
 import com.netease.mebius.client.executor.validator.ParamValidator;
 import com.netease.mebius.client.model.CallMethodNode;
 import com.netease.mebius.client.model.MethodsCallResult;
 import com.netease.mebius.client.model.ProjectAnalyzeResult;
 import com.netease.mebius.client.model.SpecifyParam;
 import com.netease.mebius.client.model.project.ProjectParam;
 import com.netease.mebius.client.utils.AnnotationUtils;
 import lombok.extern.slf4j.Slf4j;
 import org.apache.commons.collections.CollectionUtils;
 import org.assertj.core.util.Lists;
 import org.jacoco.core.internal.diff.ClassInfo;

 import java.util.List;
 import java.util.Map;

 /**
  * 工程的调用依赖关系分析
  */
 @Slf4j
 public class CodeAnalyze {

     /**
      * 多工程代码diff分析，结果根据url分组
      *
      * @param projectParams 代码diff请求参数，多工程
      * @param annotations   指定需要筛选的注解，为空则为系统默认的注解
      * @return 多工程下变更方法对应的调用关系
      * @throws MebiusException
      */
     public static List<MethodsCallResult> analyzeWithDiff(List<ProjectParam> projectParams, List<String> annotations)
             throws MebiusException {

         //参数校验
         ParamValidator.projectValidator(projectParams);

         //计算diff的classInfo信息
         List<ClassInfo> classInfos = ClassInfoAssembler.calDiffClassInfo(projectParams);
         if (CollectionUtils.isEmpty(classInfos)) {
             return Lists.newArrayList();
         }

         //生成工程调用链关系map
         Map<String, Map<String, CallMethodNode>> invokeMap = InvokeChainGenerator.generateInvokeMap(projectParams);

         //根据diff的class来分析代码，多线程处理
         return CallMethodExecutor.calCallChains(invokeMap, classInfos, AnnotationUtils.init(annotations), null);
     }

     /**
      * 多工程下指定类指定方法的代码分析，结果根据url分组
      *
      * @param projectParams 代码diff请求参数，多工程
      * @param specifyParams 指定类和方法参数
      * @param annotations   指定需要筛选的注解
      * @return 返回指定方法对应的调用关系
      * @throws MebiusException
      */
     public static List<MethodsCallResult> analyzeWithSpecify(List<ProjectParam> projectParams, List<SpecifyParam> specifyParams,
                                                              List<String> annotations) throws MebiusException {

         //参数校验
         ParamValidator.projectValidator(projectParams);

         //构造用户指定的classInfo信息
         List<ClassInfo> classInfos = ClassInfoAssembler.calSpecifyClassInfo(projectParams, specifyParams);
         if (CollectionUtils.isEmpty(classInfos)) {
             return Lists.newArrayList();
         }

         //生成工程调用链关系map
         Map<String, Map<String, CallMethodNode>> invokeMap = InvokeChainGenerator.generateInvokeMap(projectParams);

         //根据指定的classInfo来分析代码，多线程处理
         return CallMethodExecutor.calCallChains(invokeMap, classInfos, AnnotationUtils.init(annotations), null);
     }


     /**
      * 分析工程维度（diff模式）
      *
      * @param projectParams
      * @param annotations
      * @return
      * @throws MebiusException
      */
     public static ProjectAnalyzeResult analyzeProjectWithDiff(List<ProjectParam> projectParams, List<String> annotations)
             throws MebiusException {

         //参数校验
         ParamValidator.projectValidator(projectParams);

         //计算diff的classInfo信息
         List<ClassInfo> classInfos = ClassInfoAssembler.calDiffClassInfo(projectParams);
         if (CollectionUtils.isEmpty(classInfos)) {
             return new ProjectAnalyzeResult();
         }

         //生成工程调用链关系map
         Map<String, Map<String, CallMethodNode>> invokeMap = InvokeChainGenerator.generateInvokeMap(projectParams);
         //填充变更方法信息
         ProjectAnalyzeResult projectAnalyzeResult = ChangeMethodExecutor.fillChangeMethod(classInfos, invokeMap);
         //计算调用链
         projectAnalyzeResult.setCallResults(CallMethodExecutor.calCallChains(invokeMap, classInfos,
                 AnnotationUtils.init(annotations), projectParams.get(0).getServletMappings()));
         return projectAnalyzeResult;
     }

     /**
      * 分析工程维度（指定模式）
      *
      * @param projectParams
      * @param annotations
      * @return
      * @throws MebiusException
      */
     public static ProjectAnalyzeResult analyzeProjectWithSpecify(List<ProjectParam> projectParams, List<SpecifyParam> specifyParams,
                                                                  List<String> annotations) throws MebiusException {
         //参数校验
         ParamValidator.projectValidator(projectParams);

         //计算diff的classInfo信息
         List<ClassInfo> classInfos = ClassInfoAssembler.calSpecifyClassInfo(projectParams, specifyParams);
         if (CollectionUtils.isEmpty(classInfos)) {
             return new ProjectAnalyzeResult();
         }

         //生成工程调用链关系map
         Map<String, Map<String, CallMethodNode>> invokeMap = InvokeChainGenerator.generateInvokeMap(projectParams);
         //填充变更方法信息
         ProjectAnalyzeResult projectAnalyzeResult = ChangeMethodExecutor.fillChangeMethod(classInfos, invokeMap);
         //计算调用链
         projectAnalyzeResult.setCallResults(CallMethodExecutor.calCallChains(invokeMap, classInfos,
                 AnnotationUtils.init(annotations), projectParams.get(0).getServletMappings()));
         return projectAnalyzeResult;
     }
 }
