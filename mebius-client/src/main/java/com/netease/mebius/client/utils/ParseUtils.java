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
 package com.netease.mebius.client.utils;

 import com.netease.mebius.client.model.CallMethodNode;
 import com.netease.mebius.client.model.ChangeClassInfo;
 import com.netease.mebius.client.model.ChangeMethodInfo;
 import jdk.internal.org.objectweb.asm.tree.AnnotationNode;
 import org.apache.commons.lang.StringUtils;
 import org.assertj.core.util.Lists;
 import org.jacoco.core.internal.diff.ClassInfo;
 import org.jacoco.core.internal.diff.MethodInfo;

 import java.util.ArrayList;
 import java.util.List;
 import java.util.regex.Matcher;
 import java.util.regex.Pattern;

 /**
  * ParseUtils
  */
 public class ParseUtils {

     private static final Pattern angle_brackets_pattern = Pattern.compile("\\<[^<>]*\\>");


     private static final String[] CLASS_SUFFIX = new String[]{"vo", "dto", "po", "bo", "to", "do", "bean"};


     /**
      * 从methodInfo中解析参数
      *
      * @param methodParameters
      * @return
      */
     public static List<String> parseParamTypesFromMethodInfo(String methodParameters) {
         List<String> result = Lists.newArrayList();
         //String tmp = StringUtils.substringBetween(methodParameters, "[", "]");
         String tmp = methodParameters.substring(1, methodParameters.length() - 1);
         if (tmp != null) {
             if (tmp.indexOf("<") > 0) {
                 Matcher m = angle_brackets_pattern.matcher(tmp);
                 while (m.find()) {
                     tmp = m.replaceAll("");
                     m = angle_brackets_pattern.matcher(tmp);
                 }
             }
             String[] parameters = tmp.split(",");
             if (parameters != null && parameters.length > 0) {
                 for (String p : parameters) {
                     String[] paramSplit = p.trim().split(" ");
                     if (paramSplit.length < 2) {
                         continue;
                     }
                     result.add(paramSplit[paramSplit.length - 2]);
                 }
             }
         }
         return result;
     }

     public static List<String> parseParamTypesFromMethodInfo(String methodParameters, boolean replace) {
         List<String> result = Lists.newArrayList();
         //String tmp = StringUtils.substringBetween(methodParameters, "[", "]");
         String tmp = methodParameters.substring(1, methodParameters.length() - 1);
         if (tmp != null) {
             if (tmp.indexOf("<") > 0) {
                 Matcher m = angle_brackets_pattern.matcher(tmp);
                 while (m.find()) {
                     tmp = m.replaceAll("");
                     m = angle_brackets_pattern.matcher(tmp);
                 }
             }
             String newParams = tmp;
             if (replace) {
                 String regex = "\\@RequestParam\\([^\\)]+\\)";
                 newParams = tmp.replaceAll(regex, "").trim();
             }

             String[] parameters = newParams.split(",");
             if (parameters != null && parameters.length > 0) {
                 for (String p : parameters) {
                     String[] paramSplit = p.trim().split(" ");
                     if (paramSplit.length < 2) {
                         continue;
                     }
                     result.add(paramSplit[paramSplit.length - 2]);
                 }
             }
         }
         return result;
     }

     /**
      * 从methodSig中进行jni转换
      *
      * @param methodSig
      * @return
      */
     public static List<String> parseJNIParamTypesFromMethodSig(String methodSig) {
         List<String> result = Lists.newArrayList();
         String tmp = StringUtils.substringBetween(methodSig, "(", ")");
         if (StringUtils.isBlank(tmp)) {
             return result;
         }
         Matcher m = Pattern.compile("(L.*?;|\\[{0,2}L.*?;|[ZCBSIFJD]|\\[{0,2}[ZCBSIFJD]{1})").matcher(tmp);
         while (m.find())
             result.add(m.group(1));
         return result;
     }

     /**
      * 获取simple名
      *
      * @param fullName
      * @return
      */
     public static String getSimpleName(String fullName) {
         String[] arr = fullName.split("/");
         if (arr[arr.length - 1].contains(";")) {
             return arr[arr.length - 1].split(";")[0];
         } else {
             return arr[arr.length - 1];
         }
     }

     /**
      * 规范mapping
      *
      * @param mappingUrl
      * @return
      */
     public static String standardMappingUrl(String mappingUrl) {
         if (!StringUtils.startsWith(mappingUrl, "/")) {
             mappingUrl = "/" + mappingUrl;
         }
         return mappingUrl;
     }


     /**
      * 解析成严选rpc的url格式
      *
      * @param path
      * @param methodName
      * @return
      */
     public static String parseYanxuanRpcUrl(String path, String methodName) {
         return "RPC:" + path + "?m=" + methodName;
     }

     /**
      * 解析变更类信息
      *
      * @param classInfos
      * @return
      */
     public static List<ChangeClassInfo> parseChangeClassInfo(List<ClassInfo> classInfos) {
         List<ChangeClassInfo> changeClassInfos = Lists.newArrayList();
         for (ClassInfo classInfo : classInfos) {
             List<String> newMethods = Lists.newArrayList();
             List<String> delMethods = Lists.newArrayList();
             ChangeClassInfo changeClassInfo = new ChangeClassInfo();
             changeClassInfo.setClassName(classInfo.getClassName());
             changeClassInfo.setFilePath(classInfo.getClassFile());
             changeClassInfo.setPackageName(classInfo.getPackages());
             if (StringUtils.equals(classInfo.getType(), "ADD")) {
                 changeClassInfo.setNew(true);
             }
             if (classInfo.getNewMethodInfos() != null) {
                 classInfo.getNewMethodInfos().stream().map(methodInfo -> methodInfo.getMethodName() + methodInfo.getParameters()).forEach(newMethods::add);
             }
             if (classInfo.getDelMethodInfos() != null) {
                 classInfo.getDelMethodInfos().stream().map(methodInfo -> methodInfo.getMethodName() + methodInfo.getParameters()).forEach(delMethods::add);
             }
             if (classInfo.getAddLines() != null) {
                 changeClassInfo.setAddLines(classInfo.getAddLines());
                 classInfo.getAddLines().forEach(addLine -> changeClassInfo.setAddLineCount(changeClassInfo.getAddLineCount() + addLine[1] - addLine[0]));
             }
             if (classInfo.getDelLines() != null) {
                 changeClassInfo.setDelLines(classInfo.getDelLines());
                 classInfo.getDelLines().forEach(delLine -> changeClassInfo.setDelLineCount(changeClassInfo.getDelLineCount() + delLine[1] - delLine[0]));
             }
             for (MethodInfo methodInfo : classInfo.getMethodInfos()) {
                 ChangeMethodInfo changeMethodInfo = new ChangeMethodInfo();
                 changeMethodInfo.setMethodName(methodInfo.getMethodName());
                 changeMethodInfo.setReturnType(methodInfo.getReturnType());
                 changeMethodInfo.setParameters(methodInfo.getParameters()
                         .replace("[", "(").replace("]", ")"));
                 changeMethodInfo.setStartLine(methodInfo.getStartLine());
                 changeMethodInfo.setEndLine(methodInfo.getEndLine());
                 if (newMethods.contains(methodInfo.getMethodName() + methodInfo.getParameters())) {
                     changeMethodInfo.setNew(true);
                 }
                 changeClassInfo.getChangeMethods().add(changeMethodInfo);
             }
             //有方法变更的才添加
             if (!changeClassInfo.getChangeMethods().isEmpty()) {
                 changeClassInfos.add(changeClassInfo);
             }
         }
         return changeClassInfos;
     }


     /**
      * 判断是否是GET/SET方法
      *
      * @param callMethodNode
      * @return
      */
     public static boolean isGetSetMethod(CallMethodNode callMethodNode) {

         //特定注解直接过滤
         List<AnnotationNode> annotationNodes = callMethodNode.getClassNode().visibleAnnotations;
         if (annotationNodes != null) {
             for (AnnotationNode annotationNode : annotationNodes) {
                 if (StringUtils.equals(StringUtils.substringAfterLast(annotationNode.desc, "/"), "Data") ||
                         StringUtils.equals(StringUtils.substringAfterLast(annotationNode.desc, "/"), "Getter") ||
                         StringUtils.equals(StringUtils.substringAfterLast(annotationNode.desc, "/"), "Setter") ||
                         StringUtils.equals(StringUtils.substringAfterLast(annotationNode.desc, "/"), "EnableAutoUpdateApolloConfig")) {
                     return true;
                 }
             }
         }
         //枚举排除
         if (StringUtils.equals(callMethodNode.getClassNode().superName, "java/lang/Enum")) {
             return true;
         }
         boolean isGetSetPrefix = false;
         if (StringUtils.startsWith(callMethodNode.getMethodNode().name, "get") ||
                 StringUtils.startsWith(callMethodNode.getMethodNode().name, "set")) {
             isGetSetPrefix = true;
         }

         //类名后缀判断
         for (int i = 0; i < CLASS_SUFFIX.length; i++) {
             String suffix = CLASS_SUFFIX[i];
             if (StringUtils.endsWithIgnoreCase(callMethodNode.getClassName(), suffix)
                     && isGetSetPrefix) {
                 return true;
             }
         }
         return false;
     }


     /**
      * accessToModifier
      *
      * @param access
      * @return
      */
     public static String accessToModifier(int access) {
         List<String> modifiers = new ArrayList<String>();
         if ((access & 0x0001) != 0) {
             modifiers.add("public");
         }
         if ((access & 0x0002) != 0) {
             modifiers.add("private");
         }
         if ((access & 0x0004) != 0) {
             modifiers.add("protected");
         }
         if ((access & 0x0008) != 0) {
             modifiers.add("static");
         }
         if ((access & 0x0010) != 0) {
             modifiers.add("final");
         }
         if ((access & 0x0020) != 0) {
             modifiers.add("synchronized");
         }
         if ((access & 0x0040) != 0) {
             modifiers.add("volatile");
         }
         if ((access & 0x0080) != 0) {
             modifiers.add("transient");
         }
         if ((access & 0x0100) != 0) {
             modifiers.add("native");
         }
         if ((access & 0x0400) != 0) {
             modifiers.add("abstract");
         }
         if ((access & 0x0800) != 0) {
             modifiers.add("strictfp");
         }
         if (!modifiers.isEmpty()) {
             return StringUtils.join(modifiers, " ");
         } else {
             return null;
         }
     }
 }