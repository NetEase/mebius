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
 package com.netease.mebius.client.executor;

 import com.netease.mebius.client.constant.ConstantVar;
 import com.netease.mebius.client.model.CallMethodNode;
 import com.netease.mebius.client.model.MethodCallResult;
 import com.netease.mebius.client.utils.ParseUtils;
 import lombok.extern.slf4j.Slf4j;
 import org.jacoco.core.internal.diff.ClassInfo;
 import org.jacoco.core.internal.diff.MethodInfo;

 import java.util.ArrayList;
 import java.util.List;
 import java.util.Map;
 import java.util.concurrent.CompletionService;
 import java.util.concurrent.ExecutorCompletionService;
 import java.util.concurrent.ExecutorService;
 import java.util.concurrent.Executors;
 import java.util.concurrent.Future;

 /**
  * 方法调用关系执行器（多线程执行）
  */
 @Slf4j
 public class ConcurrentCallMethodExecutor {

     private ConcurrentCallMethodExecutor() {
         super();
     }

     /**
      * 多线程处理分析
      *
      * @param invokeMap
      * @param classInfos
      * @param filterAnnotations
      * @return
      * @throws Exception
      */
     public static List<MethodCallResult> execute(Map<String, Map<String, CallMethodNode>> invokeMap,
                                                  List<ClassInfo> classInfos, List<String> filterAnnotations, Object extend) throws Exception {
         // 根据实际情况动态配置线程池大小
         int poolSize = Math.min(classInfos.size() * 2, Runtime.getRuntime().availableProcessors() * 2);
         ExecutorService executor = Executors.newFixedThreadPool(poolSize);
         CompletionService<List<MethodCallResult>> completionService = new ExecutorCompletionService<>(executor);
         List<Future<List<MethodCallResult>>> futures = new ArrayList<>();
         try {
             for (ClassInfo classInfo : classInfos) {
                 for (MethodInfo methodInfo : classInfo.getMethodInfos()) {
                     String className = classInfo.getPackages().replace(".", ConstantVar.FILE_SEPARATOR)
                             + ConstantVar.FILE_SEPARATOR
                             + classInfo.getClassName();
                     // 提交任务到线程池
                     Future<List<MethodCallResult>> future = completionService.submit(() -> CallMethodExecutor.call(invokeMap, className, methodInfo,
                             ParseUtils.parseParamTypesFromMethodInfo(methodInfo.getParameters(), true), filterAnnotations, extend));
                     futures.add(future);
                 }
             }
         } catch (Exception e) {
             log.error(e.getMessage(), e);
             throw new Exception(e.getMessage());
         } finally {
             // 关闭线程池
             executor.shutdown();
         }

         //获取每个线程执行结果
         List<MethodCallResult> result = new ArrayList<>();
         for (Future<List<MethodCallResult>> future : futures) {
             try {
                 result.addAll(future.get());
             } catch (Exception e) {
                 log.error(e.getMessage(), e);
                 throw new Exception(e.getMessage());
             }
         }
         return result;
     }
 }
