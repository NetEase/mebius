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

import com.netease.mebius.client.model.CallGraph;
import com.netease.mebius.client.model.CallMethodNode;
import jdk.internal.org.objectweb.asm.tree.ClassNode;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;

import java.util.Map;

/**
 * 工程扫描-方法关联器
 *
 */
@Slf4j
public class MethodLinker {

    /**
     * 构建工程下调用和被调用关系
     *
     * @param callGraph
     * @return
     */
    public static CallGraph buildBeInvokedRelation(CallGraph callGraph) {
        Map<String, Map<String, CallMethodNode>> invokeMap = callGraph.getInvokeMap();

        invokeMap.forEach((className, classMethodMap) -> {
            //基于类
            classMethodMap.forEach((methodSig, methodParentNode) -> {
                //基于每个方法node
                methodParentNode.getChildSet().forEach((classMethodSig) -> {
                    // 包名类名#方法名#方法描述
                    String ownerName = classMethodSig.substring(0, classMethodSig.indexOf('#'));
                    String invokeMethodSig = classMethodSig.substring(classMethodSig.indexOf('#') + 1);
                    //明确是 调用了本工程的类
                    if (invokeMap.containsKey(ownerName)) {
                        CallMethodNode beInvokedNode = invokeMap.get(ownerName).get(invokeMethodSig);
                        if (beInvokedNode == null) {
                            //排除枚举的ordinal方法
                            if ("ordinal#()I".equals(invokeMethodSig)) {
                                return;
                            } else {
                                //search in superClass
                                ClassNode classNode = callGraph.getClassNode(ownerName);
                                if (classNode.superName != null
                                        && !"java/lang/Object".equals(classNode.superName)
                                        && !"java/lang/Enum".equals(classNode.superName)) {
                                    Map<String, CallMethodNode> map = invokeMap.get(classNode.superName);
                                    if (map != null) {
                                        beInvokedNode = map.get(invokeMethodSig);
                                        if (beInvokedNode == null) {
                                            //can't happen unless superClass hasn't been recorded
                                            //virtual supperClass
                                            virtualAndLinkNode(callGraph, methodParentNode, classNode.superName, invokeMethodSig);
                                            return;
                                        } else {
                                            link(methodParentNode, beInvokedNode);
                                            return;
                                        }
                                    }
                                }

                            }
                        }
                        link(methodParentNode, beInvokedNode);
                    } else {
                        //不是本工程类
                        //虚拟方法节点,例如数据流想知道工程哪里调用过List集合的add,所以虚拟该节点,但其childMethods是空.只有parent
                        virtualAndLinkNode(callGraph, methodParentNode, ownerName, invokeMethodSig);
                    }
                });
            });
        });
        return callGraph;
    }

    private static void virtualAndLinkNode(CallGraph callGraph, CallMethodNode methodParentNode, String ownerName, String invokeMethodSig) {
        CallMethodNode virtualMethod = new CallMethodNode(invokeMethodSig, ownerName);
        callGraph.putVirtualMethod(ownerName, invokeMethodSig, virtualMethod);
        link(methodParentNode, virtualMethod);
    }

    private static void link(CallMethodNode methodParentNode, CallMethodNode beInvokedNode) {

        if (beInvokedNode != null) {

            //递归的方式，自己调用自己，则不处理，防止死循环
            if (StringUtils.equals(methodParentNode.getMethodSig(), beInvokedNode.getMethodSig())
                    && (StringUtils.equals(methodParentNode.getClassName(), beInvokedNode.getClassName()))) {
                return;
            }

            //循环调用方式，判断parent的parent中的方法名是否是自己
            if (methodParentNode.getParentMethods() != null) {
                for (CallMethodNode callMethodNode : methodParentNode.getParentMethods()) {
                    if (callMethodNode != null) {
                        if (StringUtils.equals(callMethodNode.getMethodSig(), beInvokedNode.getMethodSig())) {
                            return;
                        }
                    }
                }
            }

            //父添加到子的parentMethods
            beInvokedNode.getParentMethods().add(methodParentNode);
            //子添加到父的childMethods
            methodParentNode.getChildMethods().add(beInvokedNode);
        }
    }
}
