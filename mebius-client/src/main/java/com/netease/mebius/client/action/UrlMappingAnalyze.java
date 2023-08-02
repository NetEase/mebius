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
package com.netease.mebius.client.action;

import com.netease.mebius.client.exception.MebiusException;
import com.netease.mebius.client.executor.analyze.InvokeChainGenerator;
import com.netease.mebius.client.executor.analyze.ResultAssembler;
import com.netease.mebius.client.model.CallMethodNode;
import com.netease.mebius.client.model.UrlMappingInfo;
import com.netease.mebius.client.utils.AnnotationUtils;
import org.assertj.core.util.Lists;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Controller path mapping分析
 */
public class UrlMappingAnalyze {

    /**
     * 分析工程下的url mapping
     *
     * @param projectRootPath 工程根目录绝对路径
     * @return 返回工程下所有接口url数据
     * @throws MebiusException
     */
    public static List<UrlMappingInfo> analyze(String projectRootPath) throws MebiusException {

        //先生成调用关系map
        Map<String, Map<String, CallMethodNode>> invokeMap = InvokeChainGenerator.generateInvokeMap(projectRootPath);
        if (invokeMap == null) {
            return Lists.newArrayList();
        }

        //根据invokeMap解析url mapping
        List<UrlMappingInfo> urlMappingInfoList = Lists.newArrayList();
        for (Map.Entry<String, Map<String, CallMethodNode>> entry : invokeMap.entrySet()) {
            for (Map.Entry<String, CallMethodNode> callMethodNode : entry.getValue().entrySet()) {
                ResultAssembler.assembleResult(callMethodNode.getValue(), urlMappingInfoList, AnnotationUtils.init(null));
            }
        }

        //根据url去重
        urlMappingInfoList = urlMappingInfoList.stream().collect(Collectors.collectingAndThen(Collectors.toCollection(() ->
                new TreeSet<>(Comparator.comparing(UrlMappingInfo::getMappingUrl))), ArrayList::new));

        return urlMappingInfoList;
    }
}
