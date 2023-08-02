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

 import com.netease.mebius.client.exception.MebiusException;
 import com.netease.mebius.client.executor.analyze.ResultAssembler;
 import com.netease.mebius.client.executor.helper.MethodParseHelper;
 import com.netease.mebius.client.model.CallMethodNode;
 import com.netease.mebius.client.model.CallRelation;
 import com.netease.mebius.client.model.MethodCallResult;
 import com.netease.mebius.client.model.MethodsCallResult;
 import com.netease.mebius.client.utils.CopyUtils;
 import com.netease.mebius.client.utils.ParseUtils;
 import jdk.internal.org.objectweb.asm.tree.AnnotationNode;
 import lombok.extern.slf4j.Slf4j;
 import org.apache.commons.collections.CollectionUtils;
 import org.apache.commons.lang.StringUtils;
 import org.assertj.core.util.Lists;
 import org.jacoco.core.internal.diff.ClassInfo;
 import org.jacoco.core.internal.diff.MethodInfo;

 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 import java.util.Map.Entry;

 /**
  * 方法调用关系执行器
  */
 @Slf4j
 public class CallMethodExecutor {

     private CallMethodExecutor() {
         super();
     }

     /**
      * 多线程计算完整链路拓扑
      *
      * @param invokeMap
      * @param classInfos
      * @param filterAnnotations
      * @return
      */
     public static List<MethodsCallResult> calCallChains(Map<String, Map<String, CallMethodNode>> invokeMap,
                                                         List<ClassInfo> classInfos, List<String> filterAnnotations, Object extend) throws MebiusException {
         //多线程处理代码分析
         try {
             List<MethodCallResult> result = ConcurrentCallMethodExecutor.execute(invokeMap, classInfos, filterAnnotations, extend);
             //标记是否为新增的顶层节点
             markMethodNew(classInfos, result);
             //结果组装处理
             return ResultAssembler.resultListConverter(result);
         } catch (Exception e) {
             throw new MebiusException(e.getMessage());
         }
     }


     /**
      * call
      *
      * @param invokeMap
      * @param className
      * @param paramTypes
      * @param filterAnnotations
      * @return
      */
     public static List<MethodCallResult> call(Map<String, Map<String, CallMethodNode>> invokeMap, String className, MethodInfo methodInfo,
                                               List<String> paramTypes, List<String> filterAnnotations, Object extend) {
         List<MethodCallResult> result = Lists.newArrayList();
         String methodName = methodInfo.getMethodName();
         Map<String, CallMethodNode> callMethodNodeMap = invokeMap.get(className);
         if (callMethodNodeMap != null) {
             CallMethodNode callMethodNode = null;
             //模糊匹配方法名
             for (Entry<String, CallMethodNode> entry : callMethodNodeMap.entrySet()) {
                 if (entry.getKey().split("#")[0].equals(methodName)) {
                     if (MethodParseHelper.checkParameters(entry.getValue(), paramTypes)) {
                         callMethodNode = entry.getValue();
                         break;
                     }
                 }
             }
             //没有在全局调用链map的排除
             if (callMethodNode == null) {
                 log.warn("{}.{} is not in callMethodNode.", className, methodName);
                 return Lists.newArrayList();
             }
             //get/set方法不分析排除
             if (ParseUtils.isGetSetMethod(callMethodNode)) {
                 return Lists.newArrayList();
             }
             //初始化原始值
             MethodCallResult methodCallDTO = new MethodCallResult();
             methodCallDTO.setClassName(callMethodNode.getClassName().replaceAll("/", "."));
             methodCallDTO.setMethodSig(callMethodNode.getMethodSig());
             methodCallDTO.setModifier(ParseUtils.accessToModifier(callMethodNode.getMethodNode().access));
             if (methodInfo != null && StringUtils.isNotBlank(methodInfo.getParameters())) {
                 String params = methodInfo.getParameters().replace("[", "(").replace("]", ")");
                 methodCallDTO.setMethod(methodName + params);
             }
             //调用关系list（先添加起始节点）
             List<CallRelation> callRelationList = Lists.newArrayList();
             //定义个map，根据class名称和方法签名，暂存调用关系list
             Map<String, List<CallRelation>> callRelationMap = new HashMap<>();
             //防死循环校验map
             Map<String, Object> checkMap = new HashMap<>();
             //递归遍历寻找方法调用关系
             getMethodTopInvoker(callMethodNode, methodCallDTO, result, filterAnnotations, callRelationList, callRelationMap, checkMap, true, extend);
         } else {
             log.error("{} not in callMethodNodeMap.", className);
         }
         return result;
     }

     /**
      * 递归处理分析
      *
      * @param callMethodNode
      * @param methodCallDTO
      * @param result
      * @param filterAnnotations
      * @param callRelationList
      * @param callRelationMap
      */
     public static void getMethodTopInvoker(CallMethodNode callMethodNode, MethodCallResult methodCallDTO, List<MethodCallResult> result,
                                            List<String> filterAnnotations, List<CallRelation> callRelationList, Map<String, List<CallRelation>> callRelationMap,
                                            Map<String, Object> checkMap, boolean first, Object extend) {

         //循环调用防止递归死循环
         checkMap.put(callMethodNode.getClassName() + callMethodNode.getMethodSig(), callMethodNode);
         //唯一标识key
         String key = callMethodNode.getClassName() + "." + callMethodNode.getMethodSig();
         //第一次需要加入起始节点
         if (first) {
             callRelationList.add(assembleCallRelation(callMethodNode));
         }
         //如果只有自己没有父节点则不处理
         //if (first && callMethodNode.getParentMethods().isEmpty()) {
         //    return;
         //}
         //没有父类就结束
         if (callMethodNode.getParentMethods().isEmpty()) {
             callMethodNode.setExtend(extend);
             ResultAssembler.assembleResult(callMethodNode, methodCallDTO, result, filterAnnotations, callRelationList);
             return;
         }
         //下一级处理
         for (CallMethodNode parentNode : callMethodNode.getParentMethods()) {
             if (!checkMap.containsKey(parentNode.getClassName() + parentNode.getMethodSig())) {
                 methodCallDTO.setTopMethod(parentNode.getMethodSig());
                 methodCallDTO.setTopMethodSig(parentNode.getMethodSig());
                 methodCallDTO.setTopClassName(parentNode.getClassName().replace("/", "."));
                 callRelationList = getRelationList(callRelationMap, callRelationList, key);
                 callRelationList.add(assembleCallRelation(parentNode));
                 //递归
                 getMethodTopInvoker(parentNode, methodCallDTO, result, filterAnnotations, callRelationList, callRelationMap, checkMap, false, extend);
             }
         }
         callRelationMap.remove(key);
     }


     /**
      * 递归parent方法时的调用关系列表获取，如果已存在直接返回，不存在则放入map，下次递归调用时可获取。
      *
      * @param callRelationMap
      * @param callRelationList
      * @param targetName
      * @return
      */
     private static List<CallRelation> getRelationList(Map<String, List<CallRelation>> callRelationMap, List<CallRelation> callRelationList, String targetName) {

         List<CallRelation> callNewRelationList;

         if (callRelationMap.containsKey(targetName)) {
             Map<String, List<CallRelation>> callRelationMapClone = CopyUtils.clone(callRelationMap);
             callNewRelationList = callRelationMapClone.get(targetName);
             return callNewRelationList;
         } else {
             try {
                 List<CallRelation> callRelationListCopy = CopyUtils.clone(callRelationList);
                 callRelationMap.put(targetName, callRelationListCopy);
             } catch (Exception e) {
                 log.error(e.getMessage(), e);
             }
         }

         return callRelationList;
     }

     /**
      * 解析是否为新增节点，如果是新增打上标签
      *
      * @param classInfos 变更方法信息
      * @param result     初级调用关系拓扑
      * @return
      */
     private static void markMethodNew(List<ClassInfo> classInfos, List<MethodCallResult> result) {
         for (MethodCallResult methodResult : result) {
             if (methodResult.getIsNew() != null && methodResult.getTopIsNew() != null) {
                 continue;
             }
             for (ClassInfo classInfo : classInfos) {
                 //格式：com.netease.mail.yanxuan.distribution.wolverine.api.to.overseas.config.ExpressCompanyTo
                 String className = classInfo.getPackages() + "." + classInfo.getClassName();
                 if (CollectionUtils.isEmpty(classInfo.getNewMethodInfos())) {
                     continue;
                 }
                 //遍历每个新增方法
                 for (MethodInfo newMethod : classInfo.getNewMethodInfos()) {
                     //当前节点是否命中
                     if (StringUtils.equals(className, methodResult.getClassName()) &&
                             newMethod.getMethodName().equals(methodResult.getMethodSig().split("#")[0])) {
                         methodResult.setIsNew(true);
                     }
                     //TOP节点是否命中
                     if (StringUtils.equals(className, methodResult.getTopClassName()) &&
                             newMethod.getMethodName().equals(methodResult.getTopMethodSig().split("#")[0])) {
                         methodResult.setTopIsNew(true);
                     }
                 }
             }

             //设置默认值
             if (methodResult.getIsNew() == null) {
                 methodResult.setIsNew(false);
             }
             if (methodResult.getTopIsNew() == null) {
                 methodResult.setTopIsNew(false);
             }
         }
     }

     /**
      * 组装调用关系
      *
      * @param node
      * @return
      */
     private static CallRelation assembleCallRelation(CallMethodNode node) {
         CallRelation callRelation = new CallRelation();
         callRelation.setClassName(node.getClassName().replace("/", "."));
         callRelation.setMethodSig(node.getMethodSig());
         callRelation.setMethod(MethodParseHelper.methodSigTrans(node.getMethodSig()));
         List<String> annotations = Lists.newArrayList();
         if (node.getClassNode().visibleAnnotations != null) {
             for (AnnotationNode annotationNode : node.getClassNode().visibleAnnotations) {
                 annotations.add(StringUtils.substringAfterLast(annotationNode.desc, "/").replace(";", ""));
             }
         }
         if (!annotations.isEmpty()) {
             callRelation.setAnnotations(annotations);
         }
         callRelation.setModifier(ParseUtils.accessToModifier(node.getMethodNode().access));
         return callRelation;
     }
 }
