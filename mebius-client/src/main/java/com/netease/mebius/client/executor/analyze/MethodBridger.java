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

 import com.netease.mebius.client.model.CallMethodNode;
 import jdk.internal.org.objectweb.asm.Opcodes;
 import jdk.internal.org.objectweb.asm.tree.ClassNode;
 import lombok.extern.slf4j.Slf4j;
 import org.apache.commons.lang.StringUtils;
 import org.assertj.core.util.Lists;

 import java.util.List;
 import java.util.Map;

 /**
  * 方法桥接器
  */
 @Slf4j
 public class MethodBridger {

     /**
      * 整体调用map桥接
      *
      * @param invokeMap
      */
     public static void bridge(Map<String, Map<String, CallMethodNode>> invokeMap) {

         for (String classPath : invokeMap.keySet()) {
             for (String method : invokeMap.get(classPath).keySet()) {
                 //当前方法节点
                 CallMethodNode callMethodNode = invokeMap.get(classPath).get(method);
                 //interface桥接
                 interfaceBridge(callMethodNode, invokeMap);
                 //父类是抽象类的抽象方法桥接
                 abstractSuperClassBridge(callMethodNode, invokeMap);
                 //子类实现抽象类方法桥接
                 abstractSubClassBridge(callMethodNode, invokeMap);
             }
         }
     }

     /**
      * interface桥接
      *
      * @param callMethodNode
      * @param invokeMap
      */
     public static void interfaceBridge(CallMethodNode callMethodNode, Map<String, Map<String, CallMethodNode>> invokeMap) {
         //获取classNode
         ClassNode clazz = callMethodNode.getClassNode();
         if (!clazz.interfaces.isEmpty()) {
             //去总的invokeMap中获取interface类
             for (String interfaceName : clazz.interfaces) {
                 if (isJavaMethod(interfaceName)) {
                     continue;
                 }
                 Map<String, CallMethodNode> interfaceMap = invokeMap.get(interfaceName);
                 if (interfaceMap == null) {
                     continue;
                 }
                 //interface类下遍历每个method节点
                 for (String methodOfInterface : interfaceMap.keySet()) {
                     if (StringUtils.equals(methodOfInterface, callMethodNode.getMethodSig())) {
                         callMethodNode.getParentMethods().addAll(interfaceMap.get(methodOfInterface).getParentMethods());
                         //addAllParentMethod(callMethodNode, interfaceMap.get(methodOfInterface));
                     }
                 }
             }
         }
     }

     private static void addAllParentMethod(CallMethodNode callMethodNode, CallMethodNode interfaceParentNode) {
         for (CallMethodNode cmn : interfaceParentNode.getParentMethods()) {
             if (isNotLoopInvoke(callMethodNode, cmn, 0)) {
                 callMethodNode.getParentMethods().add(cmn);
             }
         }
     }

     private static boolean isNotLoopInvoke(CallMethodNode callMethodNode, CallMethodNode candidateNode, int loop) {
         if (callMethodNode.getMethodSig().equals(candidateNode.getMethodSig())
                 && callMethodNode.getClassName().equals(candidateNode.getClassName())) {
             return false;
         }
         for (CallMethodNode cmn : candidateNode.getParentMethods()) {
             if (cmn.getMethodSig().equals(callMethodNode.getMethodSig())
                     && cmn.getClassName().equals(callMethodNode.getClassName())) {
                 return false;
             }
             loop++;
             if (!isNotLoopInvoke(callMethodNode, cmn, loop)) {
                 return false;
             }
         }
         return true;
     }

     /**
      * 抽象方法桥接
      *
      * @param callMethodNode
      * @param invokeMap
      */
     public static void abstractSuperClassBridge(CallMethodNode callMethodNode, Map<String, Map<String, CallMethodNode>> invokeMap) {
         //获取classNode
         ClassNode clazz = callMethodNode.getClassNode();
         //父类是抽象类桥接
         if (clazz.access == (Opcodes.ACC_ABSTRACT | Opcodes.ACC_PUBLIC | Opcodes.ACC_SUPER)) {
             //用于记录这个抽象方法被哪些子类实现
             List<CallMethodNode> subMethodNodeList = Lists.newArrayList();
             //遍历所有类的所有方法，如果当前父类是抽象类，则匹配全局所有方法的superName是否等于当前抽象类名
             for (String classPath : invokeMap.keySet()) {
                 for (String method : invokeMap.get(classPath).keySet()) {
                     //搜索的方法节点
                     CallMethodNode searchMethodNode = invokeMap.get(classPath).get(method);
                     ClassNode searchClassNode = searchMethodNode.getClassNode();
                     if (StringUtils.equals(searchClassNode.superName, "java/lang/Object")) {
                         continue;
                     }
                     //记录子类所有方法节点
                     subMethodNodeList.add(searchMethodNode);
                     if (StringUtils.equals(searchClassNode.superName, callMethodNode.getClassName()) &&
                             StringUtils.equals(searchMethodNode.getMethodSig(), callMethodNode.getMethodSig())) {
                         callMethodNode.getParentMethods().addAll(searchMethodNode.getParentMethods());
                         //addAllParentMethod(callMethodNode, searchMethodNode);
                     }
                 }
             }
             //抽象类又继承了接口，需要将这些接口和子类进行桥接
             if (!clazz.interfaces.isEmpty()) {
                 for (String interfaceName : clazz.interfaces) {
                     if (isJavaMethod(interfaceName)) {
                         continue;
                     }
                     Map<String, CallMethodNode> interfaceMap = invokeMap.get(interfaceName);
                     if (interfaceMap == null) {
                         continue;
                     }
                     for (String methodOfInterface : interfaceMap.keySet()) {
                         for (CallMethodNode subMethodNode : subMethodNodeList) {
                             if (StringUtils.equals(interfaceMap.get(methodOfInterface).getMethodSig(), subMethodNode.getMethodSig())) {
                                 interfaceMap.get(methodOfInterface).getParentMethods().addAll(subMethodNode.getParentMethods());
                                 //addAllParentMethod(interfaceMap.get(methodOfInterface), subMethodNode);
                             }
                         }
                     }
                 }
             }
         }
     }


     /**
      * 抽象方法子类桥接（子类实现父类的抽象方法）
      *
      * @param callMethodNode
      * @param invokeMap
      */
     public static void abstractSubClassBridge(CallMethodNode callMethodNode, Map<String, Map<String, CallMethodNode>> invokeMap) {
         //获取classNode
         ClassNode clazz = callMethodNode.getClassNode();
         if (callMethodNode.getMethodNode().access != Opcodes.ACC_PUBLIC &&
                 callMethodNode.getMethodNode().access != Opcodes.ACC_PROTECTED) {
             //非public方法无需处理忽略
             return;
         }

         if (!isJavaMethod(clazz.superName)) {
             //先找父类方法
             Map<String, CallMethodNode> superClassMap = invokeMap.get(clazz.superName);
             if (superClassMap == null) {
                 return;
             }
             ClassNode superClass = null; //父类方法节点
             for (String methodOfSuperClass : superClassMap.keySet()) {
                 superClass = superClassMap.get(methodOfSuperClass).getClassNode();
                 if (StringUtils.equals(methodOfSuperClass, callMethodNode.getMethodSig())) {
                     callMethodNode.getParentMethods().addAll(superClassMap.get(methodOfSuperClass).getParentMethods());
                 }
             }
             //父类还有继承父类的抽象方法
             if (superClass != null && !StringUtils.equals(superClass.superName, "java/lang/Object")) {
                 Map<String, CallMethodNode> superSuperClassMap = invokeMap.get(superClass.superName);
                 if (superSuperClassMap != null) {
                     for (String methodOfSuperSuperClass : superSuperClassMap.keySet()) {
                         //非抽象方法不处理
                         if (superSuperClassMap.get(methodOfSuperSuperClass).getMethodNode().access
                                 != (Opcodes.ACC_PUBLIC | Opcodes.ACC_ABSTRACT) &&
                                 superSuperClassMap.get(methodOfSuperSuperClass).getMethodNode().access
                                         != (Opcodes.ACC_PROTECTED | Opcodes.ACC_ABSTRACT)) {
                             continue;
                         }

                         if (StringUtils.equals(methodOfSuperSuperClass, callMethodNode.getMethodSig())) {
                             callMethodNode.getParentMethods().addAll(superSuperClassMap.get(methodOfSuperSuperClass).getParentMethods());
                         }
                     }
                 }
             }

             //父类方法又implements了接口
             if (superClass != null) {
                 for (String interfaceName : superClass.interfaces) {
                     if (isJavaMethod(interfaceName)) {
                         continue;
                     }
                     Map<String, CallMethodNode> interfaceMap = invokeMap.get(interfaceName);
                     if (interfaceMap == null) {
                         continue;
                     }
                     for (String methodOfInterface : interfaceMap.keySet()) {
                         if (StringUtils.equals(interfaceMap.get(methodOfInterface).getMethodSig(), callMethodNode.getMethodSig())) {
                             callMethodNode.getParentMethods().addAll(interfaceMap.get(methodOfInterface).getParentMethods());
                         }
                     }
                 }
             }
         }
     }

     /**
      * @param name
      * @return
      */
     private static boolean isJavaMethod(String name) {
         if (StringUtils.startsWith(name, "java") || StringUtils.startsWith(name, "org")) {
             return true;
         }
         return false;
     }

 }
