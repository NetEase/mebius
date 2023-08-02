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

 import com.google.common.base.Predicate;
 import com.netease.mebius.client.enums.Annotation;
 import com.netease.mebius.client.enums.ClassType;
 import com.netease.mebius.client.executor.helper.MethodParseHelper;
 import com.netease.mebius.client.executor.parser.AnnotationParser;
 import com.netease.mebius.client.model.CallMethodNode;
 import com.netease.mebius.client.model.CallRelation;
 import com.netease.mebius.client.model.MethodCallResult;
 import com.netease.mebius.client.model.MethodsCallResult;
 import com.netease.mebius.client.model.UrlMappingInfo;
 import com.netease.mebius.client.utils.ParseUtils;
 import jdk.internal.org.objectweb.asm.tree.AnnotationNode;
 import lombok.extern.slf4j.Slf4j;
 import org.apache.commons.lang.StringUtils;
 import org.assertj.core.util.Lists;
 import org.reflections.Reflections;
 import org.reflections.scanners.FieldAnnotationsScanner;
 import org.reflections.scanners.MethodAnnotationsScanner;
 import org.reflections.scanners.MethodParameterScanner;
 import org.reflections.scanners.SubTypesScanner;
 import org.reflections.util.ConfigurationBuilder;
 import org.reflections.util.FilterBuilder;

 import java.util.Comparator;
 import java.util.List;
 import java.util.Map;
 import java.util.Set;
 import java.util.stream.Collectors;

 /**
  * 结果组装器
  */
 @Slf4j
 public class ResultAssembler {

     /**
      * 组装结果
      *
      * @param callMethodNode
      * @param methodCallDTO
      * @param result
      * @param filterAnnotations
      */
     public static void assembleResult(CallMethodNode callMethodNode, MethodCallResult methodCallDTO,
                                       List<MethodCallResult> result, List<String> filterAnnotations,
                                       List<CallRelation> callRelationList) {

         List<AnnotationNode> classAnnotations = callMethodNode.getClassNode().visibleAnnotations;
         boolean needCollect = false;
         String annotation = "";
         if (classAnnotations != null) {
             for (AnnotationNode node : classAnnotations) {
                 String tmp = ParseUtils.getSimpleName(node.desc);
                 if (filterAnnotations.contains(tmp)) {
                     needCollect = true;
                     annotation = tmp;
                 }
             }
         }
         //servlet工程
         if (checkIsServlet(callMethodNode)) {
             needCollect = true;
             annotation = Annotation.Servlet.name();
         }

         //开始匹配注解
         if (needCollect) {
             MethodCallResult methodCallResult = new MethodCallResult();
             methodCallResult.setAnnotation(annotation);
             methodCallResult.setClassName(methodCallDTO.getClassName().replaceAll("/", "."));
             methodCallResult.setMethod(methodCallDTO.getMethod());
             methodCallResult.setMethodSig(methodCallDTO.getMethodSig());
             methodCallResult.setModifier(methodCallDTO.getModifier());
             methodCallResult.setTopClassName(callMethodNode.getClassName().replaceAll("/", "."));
             methodCallResult.setTopMethod(MethodParseHelper.methodSigTrans(callMethodNode.getMethodSig()));
             methodCallResult.setTopMethodSig(callMethodNode.getMethodSig());
             methodCallResult.setTopMethodModifier(ParseUtils.accessToModifier(callMethodNode.getMethodNode().access));
             dynamicInvokerHelper(methodCallResult, callMethodNode);
             methodCallResult.setCallRelationList(callRelationList);
             if (methodCallResult.getClassType() == ClassType.Controller) {
                 callRelationList.get(callRelationList.size() - 1).setPath(methodCallResult.getTopEntry());
             }
             result.add(methodCallResult);
         } else {
             log.warn("Not in filter annotations, class: {} , method:{}", callMethodNode.getClassName(), callMethodNode.getMethodSig().split("#")[0]);
         }
     }

     /**
      * 组装MappingUrlAndMethodRlat数据结构
      *
      * @param callMethodNode
      * @param result
      * @param filterAnnotations
      */
     public static void assembleResult(CallMethodNode callMethodNode, List<UrlMappingInfo> result, List<String> filterAnnotations) {

         List<AnnotationNode> classAnnotations = callMethodNode.getClassNode().visibleAnnotations;
         boolean needCollect = false;
         String annotation = "";
         if (classAnnotations != null) {
             for (AnnotationNode node : classAnnotations) {
                 String tmp = ParseUtils.getSimpleName(node.desc);
                 if (filterAnnotations.contains(tmp)) {
                     needCollect = true;
                     annotation = tmp;
                 }
             }
         }
         //开始匹配注解
         if (needCollect) {
             UrlMappingInfo urlMappingInfo = new UrlMappingInfo();
             urlMappingInfo.setAnnotation(annotation);
             urlMappingInfo.setMethod(callMethodNode.getMethodNode().name);
             urlMappingInfo.setClassName(callMethodNode.getClassName());
             dynamicInvokerHelper(urlMappingInfo, callMethodNode);
             if (urlMappingInfo.getMappingUrl() != null) {
                 result.add(urlMappingInfo);
             }
         }
     }

     /**
      * 结果数据转换
      *
      * @param resultList
      * @return
      */
     public static List<MethodsCallResult> resultListConverter(List<MethodCallResult> resultList) {
         //结果去重和排序
         resultList = resultList.stream()
                 .distinct()
                 .sorted(Comparator.comparing(MethodCallResult::getAnnotation))
                 .collect(Collectors.toList());
         //根据入口类进行聚类
         Map<String, List<MethodCallResult>> resultMap =
                 resultList.stream().filter(s -> StringUtils.isNotBlank(s.getTopEntry())).collect(Collectors.groupingBy(MethodCallResult::getTopEntry));
         //map转list
         List<MethodsCallResult> methodsCallResultList = Lists.newArrayList();
         resultMap.entrySet().stream().map(e -> new MethodsCallResult(e.getKey(), e.getValue())).forEach(methodsCallResultList::add);
         log.info("MethodsCallResultList size:{}", methodsCallResultList.size());
         return methodsCallResultList;
     }

     /**
      * 通过反射动态调用helper实现类
      *
      * @param methodCallResult
      * @param callMethodNode
      */
     private static void dynamicInvokerHelper(MethodCallResult methodCallResult, CallMethodNode callMethodNode) {
         //接口类型解析处理
         // 扫包
         Predicate<String> filter = new FilterBuilder().include(".*\\.class");
         Reflections reflections = new Reflections(new ConfigurationBuilder()
                 .filterInputsBy(filter)
                 .forPackages("com.netease.mebius")// 指定扫描的包名
                 .addScanners(new SubTypesScanner()) // 添加子类扫描工
                 .addScanners(new FieldAnnotationsScanner()) // 添加属性注解扫描工具
                 .addScanners(new MethodAnnotationsScanner()) // 添加方法注解扫描工具
                 .addScanners(new MethodParameterScanner()) // 添加方法参数扫描工具
         );
         Set<Class<? extends AnnotationParser>> set = reflections.getSubTypesOf(AnnotationParser.class);
         for (Class c : set) {
             try {
                 Object impl = c.newInstance();
                 if (impl instanceof AnnotationParser) {
                     ((AnnotationParser) impl).parse(methodCallResult, callMethodNode);
                 }
             } catch (InstantiationException | IllegalAccessException e) {
                 log.error(e.getMessage(), e);
             }
         }
     }

     /**
      * 通过反射动态调用helper实现类
      *
      * @param urlMappingInfo
      * @param callMethodNode
      */
     private static void dynamicInvokerHelper(UrlMappingInfo urlMappingInfo, CallMethodNode callMethodNode) {
         //接口类型解析处理
         Reflections reflection = new Reflections(AnnotationParser.class.getPackage().getName());
         Set<Class<? extends AnnotationParser>> set = reflection.getSubTypesOf(AnnotationParser.class);
         for (Class c : set) {
             try {
                 Object impl = c.newInstance();
                 if (impl instanceof AnnotationParser) {
                     ((AnnotationParser) impl).parseUrl(urlMappingInfo, callMethodNode);
                 }
             } catch (InstantiationException | IllegalAccessException e) {
                 e.printStackTrace();
             }
         }
     }

     /**
      * 判断是否是Servlet
      *
      * @param callMethodNode
      * @return
      */
     private static boolean checkIsServlet(CallMethodNode callMethodNode) {
         if (StringUtils.equals(callMethodNode.getClassNode().superName, "javax/servlet/http/HttpServlet")) {
             return true;
         }
         return false;
     }
 }