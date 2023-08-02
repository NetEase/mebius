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

import com.netease.mebius.client.exception.MebiusException;
import com.netease.mebius.client.model.CallGraph;
import com.netease.mebius.client.model.CallMethodNode;
import com.netease.mebius.client.model.project.ProjectParam;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;

import java.util.List;
import java.util.Map;

/**
 * 调用链分析器
 */
@Slf4j
public class InvokeChainGenerator {

    /**
     * 生成调用关系map(适用单工程)
     *
     * @param projectRoot
     * @return
     * @throws Exception
     */
    public static Map<String, Map<String, CallMethodNode>> generateInvokeMap(String projectRoot) throws MebiusException {

        if (StringUtils.isBlank(projectRoot)) {
            return null;
        }
        CallGraph callGraph = new CallGraph();
        SourceMethodParser.buildSource(projectRoot, projectRoot, callGraph, null, null);
        return generator(callGraph);
    }

    /**
     * 生成调用关系map（适用于多工程）
     *
     * @param projectParams 工程参数
     * @return
     */
    public static Map<String, Map<String, CallMethodNode>> generateInvokeMap(List<ProjectParam> projectParams) throws MebiusException {
        CallGraph callGraph = new CallGraph();
        projectParams.forEach(projectParam -> SourceMethodParser.buildSource(
                projectParam.getProjectRootPath(),
                projectParam.getProjectRootPath(),
                callGraph,
                projectParam.getRelationSubPkg(),
                projectParam.getExcludeSubPkg()));
        Map<String, Map<String, CallMethodNode>> resultMap = generator(callGraph);
        log.debug("Project invoke map size:{}", resultMap.size());
        return resultMap;
    }

    /**
     * 生成调用链
     *
     * @param callGraph
     * @return
     * @throws Exception
     */
    private static Map<String, Map<String, CallMethodNode>> generator(CallGraph callGraph) throws MebiusException {
        //最终建立被调用的关系
        callGraph = MethodLinker.buildBeInvokedRelation(callGraph);
        Map<String, Map<String, CallMethodNode>> invokeMap = callGraph.getInvokeMap();
        if (invokeMap.isEmpty()) {
            throw new MebiusException("解析失败，请确认工程是否存在或是否已完成编译");
        }
        //桥接interface和实现类的关系
        MethodBridger.bridge(invokeMap);
        return invokeMap;
    }
}