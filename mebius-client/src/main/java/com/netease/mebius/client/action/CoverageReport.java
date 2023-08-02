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
import com.netease.mebius.client.executor.CovReportDiffExecutor;
import com.netease.mebius.client.executor.CovReportExecutor;
import com.netease.mebius.client.executor.validator.ParamValidator;
import com.netease.mebius.client.model.coverage.MethodCoverage;
import com.netease.mebius.client.model.coverage.ReportDiffParam;
import com.netease.mebius.client.model.coverage.ReportParam;
import com.netease.mebius.client.model.project.ProjectParam;
import org.jacoco.core.analysis.IBundleCoverage;

import java.util.List;

/**
 * 覆盖率报告生成
 */
public class CoverageReport {

    /**
     * 生成覆盖率报告
     *
     * @param reportParam  覆盖率报告参数
     * @param projectParam 工程参数
     * @return
     * @throws MebiusException
     */
    public static IBundleCoverage generate(ReportParam reportParam, ProjectParam projectParam) throws MebiusException {
        //参数校验
        ParamValidator.projectValidator(projectParam);
        //生成覆盖率数据
        return CovReportExecutor.create(reportParam, projectParam);
    }

    /**
     * 输出指定方法调用链上的的覆盖率数据
     *
     * @param methodInfos 方法信息
     * @return
     * @throws MebiusException
     */
    public static List<MethodCoverage> genForMethodLink(IBundleCoverage bundleCoverage,
                                                        List<MethodCoverage> methodInfos) {
        //计算方法覆盖率数据
        return CovReportExecutor.calMethodCoverageData(bundleCoverage, methodInfos);
    }


    /**
     * 两份覆盖率报告对比计算，生成对比报告
     *
     * @param projectParam
     * @param reportParam
     * @param reportDiffParam
     * @return
     * @throws MebiusException
     */
    public static IBundleCoverage genForReportDiff(ProjectParam projectParam, ReportParam reportParam,
                                                   ReportDiffParam reportDiffParam) throws MebiusException {
        //参数校验
        ParamValidator.projectValidator(projectParam);
        //执行覆盖率报告diff
        return CovReportDiffExecutor.diff(projectParam, reportParam, reportDiffParam);
    }

}