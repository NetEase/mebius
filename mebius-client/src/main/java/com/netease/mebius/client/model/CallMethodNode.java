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
import jdk.internal.org.objectweb.asm.tree.MethodNode;

import java.util.HashSet;
import java.util.Set;

/**
 * 调用方法node
 */
public class CallMethodNode {

    //methodNode之间直接维持父子关系
    private final Set<CallMethodNode> parentMethods = new HashSet<>();
    private final Set<CallMethodNode> childMethods = new HashSet<>();
    //根据方法内操作指令记录调用的方法
    private final Set<String> childSet = new HashSet<>();

    private String methodSig;
    private String className;

    private MethodNode methodNode;
    //考虑到接口,抽象类需要实现类去调用该方法,需要父子关系统计
    private ClassNode classNode;

    /**
     * 扩展信息
     */
    private Object extend;

    public CallMethodNode(String methodSig, String className) {
        this.methodSig = methodSig;
        this.className = className;
    }

    public Set<CallMethodNode> getParentMethods() {
        return parentMethods;
    }

    public Set<CallMethodNode> getChildMethods() {
        return childMethods;
    }

    public Set<String> getChildSet() {
        return childSet;
    }

    public String getMethodSig() {
        return methodSig;
    }

    public void setMethodSig(String methodSig) {
        this.methodSig = methodSig;
    }

    public String getClassName() {
        return className;
    }

    public void setClassName(String className) {
        this.className = className;
    }

    public MethodNode getMethodNode() {
        return methodNode;
    }

    public void setMethodNode(MethodNode methodNode) {
        this.methodNode = methodNode;
    }

    public ClassNode getClassNode() {
        return classNode;
    }

    public void setClassNode(ClassNode classNode) {
        this.classNode = classNode;
    }

    public Object getExtend() {
        return extend;
    }

    public void setExtend(Object extend) {
        this.extend = extend;
    }
}
