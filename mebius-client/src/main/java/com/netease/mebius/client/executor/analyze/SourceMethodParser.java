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
import com.netease.mebius.client.utils.FileUtils;
import jdk.internal.org.objectweb.asm.ClassReader;
import jdk.internal.org.objectweb.asm.Handle;
import jdk.internal.org.objectweb.asm.tree.AbstractInsnNode;
import jdk.internal.org.objectweb.asm.tree.ClassNode;
import jdk.internal.org.objectweb.asm.tree.InnerClassNode;
import jdk.internal.org.objectweb.asm.tree.InvokeDynamicInsnNode;
import jdk.internal.org.objectweb.asm.tree.MethodInsnNode;
import jdk.internal.org.objectweb.asm.tree.MethodNode;
import lombok.extern.slf4j.Slf4j;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.ListIterator;

import static jdk.internal.org.objectweb.asm.tree.AbstractInsnNode.INVOKE_DYNAMIC_INSN;
import static jdk.internal.org.objectweb.asm.tree.AbstractInsnNode.METHOD_INSN;

/**
 * 解析class文件 获得一个方法及其之中的真实调用
 *
 */
@Slf4j
public class SourceMethodParser {

    /**
     * 传入一个工程路径，遍历下面的class，读成流发给解析类
     *
     * @param currFilePath 当前文件路径
     * @param projectRoot 项目根路径
     * @param callGraph
     * @param relationSubPkg
     * @param excludeSubPkg
     */
    public static void buildSource(String currFilePath, String projectRoot, CallGraph callGraph,
                                   List<String> relationSubPkg,
                                   List<String> excludeSubPkg) {
        File root = new File(currFilePath);
        if (!root.exists()) {
            return;
        }

        if (root.isDirectory()) {
            String path = root.getAbsolutePath();
            if(isOsWindows()){
                path = path.replaceAll("\\\\","/");
                projectRoot = projectRoot.replaceAll("\\\\","/");

            }
            //根据依赖或排除的class来加载文件目录
            if (FileUtils.checkClassFileIsExclude(path, projectRoot, relationSubPkg, excludeSubPkg)) {
                return;
            }
            if (!path.endsWith(".git") && !path.endsWith(".idea") && !path.endsWith(".settings")
                    && !path.endsWith("resources") && !path.contains("src/test/java")) {
                //递归
                for (String child : root.list()) {
                    buildSource(path + File.separator + child, projectRoot, callGraph, relationSubPkg, excludeSubPkg);
                }
            }
        } else {
            if (root.getAbsolutePath().endsWith(".class")) {
                try {
                    FileInputStream in = new FileInputStream(root);
                    parseSourceClass(in, callGraph);
                    in.close();
                } catch (Exception e) {
                    log.error(e.getMessage(), e);
                }
            }
        }
    }

    /**
     * 解析class
     *
     * @param in
     */
    public static void parseSourceClass(InputStream in, CallGraph callGraph) {
        ClassNode cn = new ClassNode();
        try {
            ClassReader cr = new ClassReader(in);
            cr.accept(cn, 0);
        } catch (IOException e) {
            e.printStackTrace();
        }

        //记录该类
        callGraph.putClass(cn.name);
        callGraph.putClasSNode(cn.name, cn);
        if (cn.methods != null && cn.methods.size() > 0) {
            for (MethodNode mn : cn.methods) {

                //不记录类加载方法
                if ("<init>".equals(mn.name)) {
                    continue;
                }

                if ("<clinit>".equals(mn.name)) {
                    continue;
                }

                //内部类需要解析
                //如果是类加载器先加载全部jar和class,就只需要以内部类的name去loadClass,只是方式不同.取决于如何读取class
                //如果是直接每个class文件遍历读,内部类则已经读取了.
                //parseInnerClass(cn);
                //记录该类的method
                CallMethodNode cMethodNode = new CallMethodNode(mn.name + "#" + mn.desc, cn.name);
                callGraph.putMethod(cn.name, mn.name + "#" + mn.desc, cMethodNode);
                cMethodNode.setMethodNode(mn);
                cMethodNode.setClassNode(cn);

                //抽象方法就不记录更多
                if (mn.instructions.size() > 0) {
                    ListIterator<AbstractInsnNode> iterator = mn.instructions.iterator();
                    while (iterator.hasNext()) {
                        AbstractInsnNode insnNode = iterator.next();
                        switch (insnNode.getType()) {
                            case METHOD_INSN:
                                //储存该方法内 方法调用指令节点
                                MethodInsnNode methodisInsnNode = ((MethodInsnNode) insnNode);
                                String methodOwner = methodisInsnNode.owner;
                                String methodName = methodisInsnNode.name;
                                String methodDesc = methodisInsnNode.desc;
                                cMethodNode.getChildSet().add(methodOwner + "#" + methodName + "#" + methodDesc);
                                break;
                            case INVOKE_DYNAMIC_INSN:
                                InvokeDynamicInsnNode node = (InvokeDynamicInsnNode) insnNode;
                                getChildFromDynamicInvoke(cMethodNode, node);
                                break;
                            default:
                                break;
                        }
                    }
                }
            }
        }

    }

    private static void getChildFromDynamicInvoke(CallMethodNode cMethodNode, InvokeDynamicInsnNode node) {
        Handle handle = node.bsm;
        //从lambda表达式中获取调用关系
        if (handle.getOwner().equals("java/lang/invoke/LambdaMetafactory") && node.bsmArgs.length == 3) {
            Handle handle2 = (Handle) node.bsmArgs[1];
            cMethodNode.getChildSet().add(handle2.getOwner() + "#" + handle2.getName() + "#" + handle2.getDesc());
        }
    }

    private static void parseInnerClass(ClassNode cn) {
        List<InnerClassNode> innerClasses = cn.innerClasses;
        for (InnerClassNode innerClass : innerClasses) {
            if (innerClass.name.equals(cn.innerClasses)) {
                //添加新方法 parse
            }
        }
    }

    /** 判断是不是Windows系统.
     *
     * @return    返回是不是Windows系统.
     */
    public static boolean isOsWindows() {
        String osname = System.getProperty("os.name").toLowerCase();
        boolean rt = osname.startsWith("windows");
        return rt;
    }
}
