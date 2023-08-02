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
package com.netease.mebius.client.executor.diff;

import com.netease.mebius.client.exception.MebiusException;
import com.netease.mebius.client.executor.diff.handler.AbstractDiffHandler;
import com.netease.mebius.client.executor.helper.GitHelper;
import com.netease.mebius.client.model.coverage.MethodCoverage;
import com.netease.mebius.client.model.project.ProjectParam;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.jacoco.core.internal.diff.ClassInfo;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 代码diff计算执行器
 */
@Slf4j
public class CalDiffExecutor {

    /**
     * 计算diff classInfo
     *
     * @param param
     * @return
     * @throws Exception
     */
    public static List<ClassInfo> cal(ProjectParam param) throws MebiusException {
        //git用户授权
        GitHelper.setAuthorization(param);
        //转换需要排除的方法
        Map<String, List<String>> excludesMap = convertExcludesMap(param);
        //执行diff
        try {
            return AbstractDiffHandler.initDiffHandler(param.getExecType()).diff(param, excludesMap);
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            throw new MebiusException("Code diff exception:" + e.getMessage());
        }
    }

    /**
     * convert excludes map
     *
     * @param param
     * @return
     */
    private static Map<String, List<String>> convertExcludesMap(ProjectParam param) {
        // 排除的class和method转换成map
        Map<String, List<String>> excludesMap = param.getExcludesMethod().stream().collect(Collectors.toMap(MethodCoverage::getClassName,
                p -> {
                    List<String> methodsList = new ArrayList<>();
                    if (StringUtils.isNotBlank(p.getMethodName())) {
                        methodsList.add(p.getMethodName());
                    }
                    return methodsList;
                },
                (List<String> value1, List<String> value2) -> {
                    value1.addAll(value2);
                    return value1;
                }
        ));
        return excludesMap;
    }
}