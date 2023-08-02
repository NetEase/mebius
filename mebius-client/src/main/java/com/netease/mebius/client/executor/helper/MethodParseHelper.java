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
 package com.netease.mebius.client.executor.helper;

 import com.alibaba.fastjson.JSONObject;
 import com.netease.mebius.client.model.CallMethodNode;
 import com.netease.mebius.client.model.ChangeClassInfo;
 import com.netease.mebius.client.model.ChangeMethodInfo;
 import com.netease.mebius.client.model.MethodCallResult;
 import com.netease.mebius.client.utils.ComplexityUtils;
 import com.netease.mebius.client.utils.ParseUtils;
 import jdk.internal.org.objectweb.asm.Opcodes;
 import jdk.internal.org.objectweb.asm.tree.ClassNode;
 import lombok.extern.slf4j.Slf4j;
 import org.apache.commons.collections.CollectionUtils;
 import org.apache.commons.lang.StringUtils;
 import org.assertj.core.util.Lists;

 import java.util.HashMap;
 import java.util.Iterator;
 import java.util.List;
 import java.util.Map;

 /**
  * 方法解析帮助类
  */
 @Slf4j
 public class MethodParseHelper {

     public static Map<String, String> mapping = new HashMap<>();

     static {
         mapping.put("I", "int");
         mapping.put("J", "long");
         mapping.put("B", "byte");
         mapping.put("S", "short");
         mapping.put("C", "char");
         mapping.put("F", "float");
         mapping.put("D", "double");
         mapping.put("Z", "boolean");
         mapping.put("[I", "int[]");
         mapping.put("[J", "long[]");
         mapping.put("[B", "byte[]");
         mapping.put("[S", "short[]");
         mapping.put("[C", "char[]");
         mapping.put("[F", "float[]");
         mapping.put("[D", "double[]");
         mapping.put("[Z", "boolean[]");
         mapping.put("[[I", "int[][]");
         mapping.put("[[J", "long[][]");
         mapping.put("[[B", "byte[][]");
         mapping.put("[[S", "short[][]");
         mapping.put("[[C", "char[][]");
         mapping.put("[[F", "float[][]");
         mapping.put("[[D", "double[][]");
         mapping.put("[[Z", "boolean[][]");
         mapping.put("V", "void");
     }

     /**
      * 处理抽象类
      *
      * @param superName
      * @param methodSig
      * @param invokeMap
      * @param methodCallResult
      * @return
      */
     public static CallMethodNode getAbstractCallMethod(String superName, String methodSig,
                                                        Map<String, Map<String, CallMethodNode>> invokeMap, MethodCallResult methodCallResult) {
         CallMethodNode result = null;
         if ("java/lang/Object".equals(superName)) {
             return null;
         }
         Map<String, CallMethodNode> superClassMethodMap = invokeMap.get(superName);
         if (superClassMethodMap != null && superClassMethodMap.values().size() > 0) {
             ClassNode superClass = superClassMethodMap.values().iterator().next().getClassNode();
             //父类不是抽象类
             if (superClass.access != (Opcodes.ACC_ABSTRACT | Opcodes.ACC_PUBLIC | Opcodes.ACC_SUPER)) {
                 return null;
             }
             for (Map.Entry<String, CallMethodNode> entry : superClassMethodMap.entrySet()) {
                 //TODO 校验参数是否一致，父类中的方法参数与子类参数一致，或者是子类参数的父类/接口；
                 if ((methodCallResult.getTopMethod() != null
                         && entry.getValue().getMethodSig().equals(methodCallResult.getTopMethod()))
                         || (methodCallResult.getTopMethod() == null
                         && entry.getValue().getMethodNode().name.equals(methodCallResult.getMethod()))) {
                     CallMethodNode methodNode = superClassMethodMap.get(entry.getValue().getMethodSig());
                     return methodNode;
                 }
             }
             //在父类中没有这个方法
             log.debug("does not find method {} in super class {}", methodSig, superName);
             //父类implements的接口中是否有这个方法
             if (CollectionUtils.isNotEmpty(superClass.interfaces)) {
                 log.debug("super class {} implements {}, go to find method in interfaces", superName, JSONObject.toJSONString(superClass.interfaces));
                 result = getInterfaceCallMethod(superClass.interfaces, invokeMap, methodCallResult);
             }
             if (result == null) {
                 //如果没有接口或者接口中没有此方法，则递归到父类的父类
                 log.debug("does not find method {} in super class {}'s interfaces", methodSig, superName);
                 if (StringUtils.isNotBlank(superClass.superName)) {
                     log.debug("super class extends {}, go to find method in super class", superClass.superName);
                     result = getAbstractCallMethod(superClass.superName, methodSig, invokeMap, methodCallResult);
                 }
             }
         }
         return result;
     }

     /**
      * 处理接口类
      *
      * @param interfaces
      * @param invokeMap
      * @param methodCallResult
      * @return
      */
     public static CallMethodNode getInterfaceCallMethod(List<String> interfaces,
                                                         Map<String, Map<String, CallMethodNode>> invokeMap, MethodCallResult methodCallResult) {
         for (String interfaceName : interfaces) {
             if ("java/io/Serializable".equals(interfaceName)) {
                 return null;
             }
             Map<String, CallMethodNode> map = invokeMap.get(interfaceName);
             if (map == null) {
                 log.warn("{} interface not in CallMethodNode.", interfaceName);
                 return null;
             }

             String key = methodCallResult.getTopMethod();
             for (Map.Entry<String, CallMethodNode> entry : map.entrySet()) {
                 String methodName = entry.getValue().getMethodSig();
                 log.debug("{} , {}", key, methodName);
                 if (methodName.equals(key)) {
                     if (checkParameters(entry.getValue(), methodsTypeMapping(ParseUtils.parseJNIParamTypesFromMethodSig(key)))) {
                         //if (checkParameters(entry.getValue(), ParseUtils.parseParamTypesFromMethodSig(key))) {
                         return entry.getValue();
                     }
                 }
             }
             //如果接口继承了接口，则继续递归寻找是否有匹配的方法
             Iterator<CallMethodNode> iter = map.values().iterator();
             if (iter.hasNext()) {
                 CallMethodNode cmn = iter.next();
                 if (CollectionUtils.isNotEmpty(cmn.getClassNode().interfaces)) {
                     return getInterfaceCallMethod(cmn.getClassNode().interfaces, invokeMap, methodCallResult);
                 }
             }
         }
         return null;
     }


     /**
      * 校验方法参数是否一致
      *
      * @param callMethodNode
      * @param paramTypes
      * @return
      */
     public static boolean checkParameters(CallMethodNode callMethodNode, List<String> paramTypes) {
         boolean result = true;
        /*List<String> methodParamTypes = ParseUtils.parseParamTypesFromMethodSig(callMethodNode.getMethodSig());
        if (methodParamTypes.size() != paramTypes.size()) {
            return false;
        }
        for (int i = 0; i < methodParamTypes.size(); i++) {
            result = result && methodParamTypes.get(i).equals(paramTypes.get(i));
        }*/
         String str, tmp;
         List<String> jniParamsType = ParseUtils.parseJNIParamTypesFromMethodSig(callMethodNode.getMethodSig());
         List<String> methodParamTypes = methodsTypeMapping(jniParamsType);
         if (methodParamTypes.size() != paramTypes.size()) {
             return false;
         }
         for (int i = 0; i < methodParamTypes.size(); i++) {
             str = paramTypes.get(i);
             tmp = methodParamTypes.get(i);
             //类里又定义了一个对象
             // getItemQcFile#(Lcom/netease/mail/yanxuan/supplier/ms/server/controller/qc/SupplierQcController$SupplierItem;)Lcom/netease/mail/yanxuan/supplier/ms/server/vo/AjaxResponse;
             if (tmp.contains("$")) {
                 tmp = tmp.split("\\$")[1];
             }
             //多参数
             if (str.contains("...")) {
                 str = str.substring(0, str.length() - 3) + "[]";
             }
             result = result && tmp.equals(str);
         }
         return result;
     }

     /**
      * methodSig转换
      *
      * @param methodSig
      * @return
      */
     public static String methodSigTrans(String methodSig) {
         List<String> jniParamTypes = ParseUtils.parseJNIParamTypesFromMethodSig(methodSig);
         List<String> methodParamTypes = methodsTypeMapping(jniParamTypes);
         String paramsType = "";
         for (int i = 0; i < methodParamTypes.size(); i++) {
             if (i == methodParamTypes.size() - 1) {
                 paramsType += methodParamTypes.get(i);
             } else
                 paramsType += methodParamTypes.get(i) + ", ";
         }
         String methodName = methodSig.split("#")[0];
         //String returnType = returnTypeTrans(methodSig.split("\\)")[1].split(";")[0]);
         return methodName + "(" + paramsType + ")";
     }

     /**
      * methodSig转换返回参数
      *
      * @param methodSig
      * @return
      */
     public static List<String> methodSigTransReturnParams(String methodSig) {
         List<String> jniParamTypes = ParseUtils.parseJNIParamTypesFromMethodSig(methodSig);
         return methodsTypeMapping(jniParamTypes);
     }

     /**
      * methodSig JNI数据类型转换为java数据类型
      *
      * @param methodParamTypes
      * @return
      */
     private static List<String> methodsTypeMapping(List<String> methodParamTypes) {
         for (int i = 0; i < methodParamTypes.size(); i++) {
             String type = methodParamTypes.get(i).split(";")[0];
             if (!type.contains("/"))
                 methodParamTypes.set(i, mapping.get(type));
             else {
                 if (!type.contains("["))
                     methodParamTypes.set(i, type.substring(type.lastIndexOf("/") + 1, type.length()));
                 else if (type.contains("[["))
                     methodParamTypes.set(i, type.substring(type.lastIndexOf("/") + 1, type.length()) + "[][]");
                 else
                     methodParamTypes.set(i, type.substring(type.lastIndexOf("/") + 1, type.length()) + "[]");
             }
         }
         return methodParamTypes;
     }

     /**
      * 返回值类型 JNI转换
      *
      * @param type
      * @return
      */
     private static String returnTypeTrans(String type) {
         if (!type.contains("/")) {
             if (!type.contains("["))
                 return mapping.get(type);
         } else {
             if (!type.contains("["))
                 return type.substring(type.lastIndexOf("/") + 1);
             else if (type.contains("[["))
                 return type.substring(type.lastIndexOf("/") + 1) + "[][]";
             else
                 return type.substring(type.lastIndexOf("/") + 1) + "[]";
         }
         return type;
     }

     /**
      * 变更方法信息关联父节点
      *
      * @param changeClasses
      * @param invokeMap
      */
     public static void changeMethodRelationParentNode(List<ChangeClassInfo> changeClasses,
                                                       Map<String, Map<String, CallMethodNode>> invokeMap) {

         Iterator<ChangeClassInfo> iterator = changeClasses.iterator();
         while (iterator.hasNext()) {
             ChangeClassInfo changeClassInfo = iterator.next();
             //获取类-方法节点
             String className = StringUtils.replace(changeClassInfo.getPackageName()
                     + "." + changeClassInfo.getClassName(), ".", "/");
             Map<String, CallMethodNode> callMethodNodeMap = invokeMap.get(className);
             if (callMethodNodeMap == null) {
                 log.error("Class not found in invokeMap:{}", className);
                 iterator.remove();
                 continue;
             }
             List<ChangeMethodInfo> changeMethods = Lists.newArrayList();
             for (ChangeMethodInfo methodInfo : changeClassInfo.getChangeMethods()) {
                 for (String methodSig : callMethodNodeMap.keySet()) {
                     //这边排除get/set方法
                     CallMethodNode callMethodNode = callMethodNodeMap.get(methodSig);
                     if (ParseUtils.isGetSetMethod(callMethodNode)) {
                         continue;
                     }
                     //先判断方法名是否相同
                     if (StringUtils.equals(methodSig.split("#")[0], methodInfo.getMethodName())) {
                         List<String> oriList = ParseUtils.parseParamTypesFromMethodInfo(methodInfo.getParameters());
                         List<String> compareList = methodSigTransReturnParams(methodSig);
                         if (StringUtils.equals(StringUtils.join(oriList, ","), StringUtils.join(compareList, ","))) {
                             methodInfo.setMethodSig(methodSig);
                             methodInfo.setModifier(ParseUtils.accessToModifier(callMethodNode.getMethodNode().access));
                             methodInfo.setComplexity(ComplexityUtils.calculate(callMethodNode.getMethodNode()));
                             for (CallMethodNode parentNode : callMethodNodeMap.get(methodSig).getParentMethods()) {
                                 methodInfo.getParentNodes().add(new CallMethodNode(parentNode.getMethodSig(), parentNode.getClassName()));
                             }
                             changeMethods.add(methodInfo);
                         }
                     }
                 }
             }
             changeClassInfo.setChangeMethods(changeMethods);
         }
     }
 }