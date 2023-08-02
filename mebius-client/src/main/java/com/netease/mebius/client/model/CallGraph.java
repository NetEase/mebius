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
package com.netease.mebius.client.model;

import jdk.internal.org.objectweb.asm.tree.ClassNode;

import java.util.HashMap;
import java.util.Map;

/**
 * 存储方法调用关系
 */
public class CallGraph {
    /**
     * className <methodSignature,node>
     * methodSignature = methodName + # + desc
     */
    private Map<String, Map<String, CallMethodNode>> invokeMap = new HashMap<>();
    private Map<String, ClassNode> classMap = new HashMap<>();
    private Map<String, Map<String, CallMethodNode>> virtualInvokeMap = new HashMap<>();

    public Map<String, Map<String, CallMethodNode>> getInvokeMap() {
        return invokeMap;
    }

    public void putClass(String className) {
        invokeMap.put(className, new HashMap<>());
    }

    public void putMethod(String className, String methodSig, CallMethodNode methodNode) {
        if (!invokeMap.containsKey(className)) {
            putClass(className);
        }
        invokeMap.get(className).put(methodSig, methodNode);
    }

    public void putVirtualMethod(String className, String methodSig, CallMethodNode methodNode) {
        if (!virtualInvokeMap.containsKey(className)) {
            virtualInvokeMap.put(className, new HashMap<>());
        }
        virtualInvokeMap.get(className).put(methodSig, methodNode);
    }

    public void putClasSNode(String className, ClassNode classNode) {
        classMap.put(className, classNode);
    }

    public ClassNode getClassNode(String className) {
        return classMap.get(className);
    }

    public Map<String, ClassNode> getClassMap() {
        return classMap;
    }

    public void setClassMap(Map<String, ClassNode> classMap) {
        this.classMap = classMap;
    }
}
