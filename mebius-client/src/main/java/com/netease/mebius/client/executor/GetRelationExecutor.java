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

import com.netease.mebius.client.enums.NodeType;
import com.netease.mebius.client.executor.analyze.ResultAssembler;
import com.netease.mebius.client.model.CallMethodNode;
import com.netease.mebius.client.model.UrlMappingInfo;
import com.netease.mebius.client.executor.analyze.InvokeChainGenerator;
import com.netease.mebius.client.utils.AnnotationUtils;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

/**
 * 获取工程下所有节点和关联关系
 */
public class GetRelationExecutor {

    /**
     * 获取工程下所有关联关系
     *
     * @param projectRootPath
     * @param projectName
     * @return
     * @throws Exception
     */
    public static Map<String, List<String>> getAllRelation(String projectRootPath, String projectName) throws Exception {
        Map<String, List<String>> getAllData = new HashMap<>();
        Map<String, Map<String, CallMethodNode>> invokeMap = InvokeChainGenerator.generateInvokeMap(projectRootPath);
        //所有方法节点list
        List<String> allMethodNodes = new ArrayList<>();
        //所有方法关系list
        List<String> allMethodRelations = new ArrayList<>();
        //遍历所有class文件
        for (String path : invokeMap.keySet()) {
            Map<String, CallMethodNode> methodsInfo = invokeMap.get(path);
            //遍历当前class文件里所有方法
            for (String methodSig : methodsInfo.keySet()) {
                //写入方法节点,附加项目名
                String methodName = methodSig.split("#")[0];
                String methodNode = path + ":" + methodName + "," + projectName;
                if (methodName.contains("toString") || methodNode.contains("Test:")) {
                    continue;
                }
                allMethodNodes.add(methodNode);
                //将该方法的所有父节点和接口写入parentNodes
                CallMethodNode nodeInfo = methodsInfo.get(methodSig);
                List<String> parentNodes = new ArrayList<>();
                HashSet<CallMethodNode> parentMethods = (HashSet<CallMethodNode>) nodeInfo.getParentMethods();
                for (CallMethodNode parentMethod : parentMethods) {
                    String parentPath = parentMethod.getClassName();
                    String parentNode = parentPath + ":" + parentMethod.getMethodSig().split("#")[0];
                    parentNodes.add(parentNode);
                }
                List<String> interfaces = nodeInfo.getClassNode().interfaces;
                for (int i = 0; i < interfaces.size(); i++) {
                    String interfaceNode = interfaces.get(i) + ":" + methodName;
                    parentNodes.add(interfaceNode);
                }
                for (int i = 0; i < parentNodes.size(); i++) {
                    allMethodRelations.add(parentNodes.get(i) + "," + methodNode);
                }

            }
        }
        //根据invokemap解析mapping url，获取接口与方法的关系
        List<UrlMappingInfo> resultMappingUrlAndMethodRlatList = new ArrayList<>();
        for (Map.Entry<String, Map<String, CallMethodNode>> entry : invokeMap.entrySet()) {
            for (Map.Entry<String, CallMethodNode> callMethodNode : entry.getValue().entrySet())
                ResultAssembler.assembleResult(callMethodNode.getValue(), resultMappingUrlAndMethodRlatList, AnnotationUtils.init(null));
        }
        //接口节点，接口和方法关系写入list
        List<String> interfaceNodes = new ArrayList<>();
        List<String> interfaceRelations = new ArrayList<>();
        for (int i = 0; i < resultMappingUrlAndMethodRlatList.size(); i++) {
            String interfaceName = resultMappingUrlAndMethodRlatList.get(i).getMappingUrl();
            if (interfaceName.contains("/health") || interfaceName.contains("/error") || interfaceName.contains("/maps") || interfaceName.contains("/dsf")) {
                continue;
            }
            String methodName = resultMappingUrlAndMethodRlatList.get(i).getClassName() + ":" + resultMappingUrlAndMethodRlatList.get(i).getMethod();
            interfaceNodes.add(interfaceName + "," + projectName);
            interfaceRelations.add(interfaceName + "," + methodName);
        }

        //将方法节点、方法关系、接口节点、接口关系写入map
        getAllData.put(NodeType.MethodNode.toString(), allMethodNodes);
        getAllData.put(NodeType.MethodRelation.toString(), allMethodRelations);
        getAllData.put(NodeType.InterfaceNode.toString(), interfaceNodes);
        getAllData.put(NodeType.InterfaceRelation.toString(), interfaceRelations);
        return getAllData;
    }

}
