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

import com.netease.mebius.client.enums.ExecType;
import com.netease.mebius.client.enums.ReportType;
import com.netease.mebius.client.exception.MebiusException;
import com.netease.mebius.client.executor.diff.CalDiffExecutor;
import com.netease.mebius.client.executor.helper.GitHelper;
import com.netease.mebius.client.model.coverage.MethodCoverage;
import com.netease.mebius.client.model.coverage.ProjectFile;
import com.netease.mebius.client.model.coverage.ReportParam;
import com.netease.mebius.client.model.project.ProjectParam;
import lombok.extern.slf4j.Slf4j;
import org.assertj.core.util.Lists;
import org.jacoco.core.analysis.Analyzer;
import org.jacoco.core.analysis.CoverageBuilder;
import org.jacoco.core.analysis.IBundleCoverage;
import org.jacoco.core.tools.ExecFileLoader;
import org.jacoco.report.DirectorySourceFileLocator;
import org.jacoco.report.FileMultiReportOutput;
import org.jacoco.report.IReportVisitor;
import org.jacoco.report.MultiSourceFileLocator;
import org.jacoco.report.html.HTMLFormatter;
import org.jacoco.report.xml.XMLFormatter;

import java.io.FileOutputStream;
import java.io.IOException;
import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 覆盖率报告生成执行器
 */
@Slf4j
public class CovReportExecutor {

    /**
     * Create the report.
     *
     * @param reportParam
     * @param projectParam
     * @throws IOException
     */
    public static IBundleCoverage create(ReportParam reportParam, ProjectParam projectParam) throws MebiusException {

        log.debug("Jacoco report create begin: {}", reportParam.getExecDataFile());

        try {
            // Read the jacoco.exec file. Multiple data files could be merged
            // at this point
            ExecFileLoader execFileLoader = new ExecFileLoader();
            execFileLoader.load(reportParam.getExecDataFile());

            // Run the structure analyzer on a single class folder to build up
            // the coverage model. The process would be similar if your classes
            // were in a jar file. Typically you would create a bundle for each
            // class folder and each jar you want in your report. If you have
            // more than one bundle you will need to add a grouping node to your
            // report
            CoverageBuilder coverageBuilder = routerBuilder(projectParam);

            //全量类型或classes不为0则生成
            boolean classesExist = coverageBuilder != null && coverageBuilder.getClassInfos() != null
                    && !coverageBuilder.getClassInfos().isEmpty();
            if (ExecType.ALL.equals(projectParam.getExecType()) || classesExist) {
                for (ProjectFile projectFile : reportParam.getProjectList()) {
                    Analyzer analyzer = new Analyzer(
                            execFileLoader.getExecutionDataStore(), coverageBuilder);
                    analyzer.analyzeAll(projectFile.getClassesDirectory(), getExcludeAnalyzeClass(projectParam.getExcludes()));
                }
            }

            //获取原始数据
            IBundleCoverage bundleCoverage = coverageBuilder.getBundle(reportParam.getTitle());

            //生成报告
            createReport(bundleCoverage, execFileLoader, reportParam);
            log.debug("Jacoco report create end: {}", reportParam.getExecDataFile());
            return bundleCoverage;
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            throw new MebiusException("Coverage report exception:" + e.getMessage());
        }
    }

    /**
     * 生成报告
     *
     * @param bundleCoverage
     * @param execFileLoader
     * @param reportParam
     * @throws IOException
     */
    public static void createReport(IBundleCoverage bundleCoverage, ExecFileLoader execFileLoader, ReportParam reportParam)
            throws IOException {

        // Create a concrete report visitor based on some supplied
        // configuration. In this case we use the defaults
        IReportVisitor visitor;

        if (ReportType.XML.equals(reportParam.getReportType())) {
            XMLFormatter xmlFormatter = new XMLFormatter();
            visitor = xmlFormatter.createVisitor(new FileOutputStream(reportParam.getReportDirectory() + "/report.xml"));
        } else {
            HTMLFormatter htmlFormatter = new HTMLFormatter();
            visitor = htmlFormatter.createVisitor(new FileMultiReportOutput(reportParam.getReportDirectory()));
        }

        MultiSourceFileLocator multiSourceFileLocator = new MultiSourceFileLocator(4);
        for (ProjectFile projectFile : reportParam.getProjectList()) {
            multiSourceFileLocator.add(new DirectorySourceFileLocator(projectFile.getSourceDirectory(),
                    "UTF-8", 4));
        }

        // Initialize the report with all of the execution and session
        // information. At this point the report doesn't know about the
        // structure of the report being created
        visitor.visitInfo(execFileLoader.getSessionInfoStore().getInfos(),
                execFileLoader.getExecutionDataStore().getContents());

        // Populate the report structure with the bundle coverage information.
        // Call visitGroup if you need groups in your report.
        visitor.visitBundle(bundleCoverage, multiSourceFileLocator);

        // Signal end of structure information to allow report to write all
        // information out
        visitor.visitEnd();
    }


    /**
     * routerBuilder
     *
     * @param projectParam
     * @return
     */
    public static CoverageBuilder routerBuilder(ProjectParam projectParam) throws MebiusException {
        log.debug("Coverage builder param: {}", projectParam.toString());
        GitHelper.setAuthorization(projectParam);
        CoverageBuilder coverageBuilder = new CoverageBuilder(CalDiffExecutor.cal(projectParam), projectParam.getExcludes());
        return coverageBuilder;
    }

    /**
     * 获取覆盖率分析时需要过滤的class文件(部分文件需要再分析时就排除掉)
     *
     * @param excludes
     * @return
     */
    public static List<String> getExcludeAnalyzeClass(List<String> excludes) {
        List<String> excludeClass = Lists.newArrayList();
        if (excludes == null) {
            return excludeClass;
        }
        for (String exclude : excludes) {
            if (exclude.contains("*")) {
                continue;
            }
            excludeClass.add(exclude.split("\\.")[exclude.split("\\.").length - 1] + ".class");
        }
        return excludeClass;
    }

    /**
     * 计算方法覆盖率
     *
     * @param bundleCoverage
     * @param methodInfos
     * @return
     */
    public static List<MethodCoverage> calMethodCoverageData(IBundleCoverage bundleCoverage, List<MethodCoverage> methodInfos) {
        Map<String, MethodCoverage> methodMap = new HashMap<>(methodInfos.size());
        for (MethodCoverage methodInfo : methodInfos) {
            methodMap.put(methodInfo.getPkgName() + "." + methodInfo.getClassName() + "." + methodInfo.getMethodName(), methodInfo);
        }
        bundleCoverage.getPackages().stream().
                forEach(packageCoverage -> packageCoverage.getClasses().stream().
                        forEach(classCoverage -> classCoverage.getMethods().stream().
                                forEach(methodCoverage -> {
                                    String className = classCoverage.getName().replace("/", ".");
                                    if (methodMap.containsKey(className + "." + methodCoverage.getName())) {
                                        //TODO 同名方法参数校验
                                        MethodCoverage result = methodMap.get(className + "." + methodCoverage.getName());
                                        result.setCoveredCount(result.getCoveredCount() + methodCoverage.getLineCounter().getCoveredCount());
                                        result.setMissCount(result.getMissCount() + methodCoverage.getLineCounter().getMissedCount());
                                        result.setTotalCount(result.getCoveredCount() + result.getMissCount());
                                        result.setCovLinesRatio(BigDecimal.valueOf(result.getCoveredCount())
                                                .divide(BigDecimal.valueOf(result.getTotalCount()), 4, BigDecimal.ROUND_HALF_UP));
                                        result.setFirstLine(methodCoverage.getFirstLine());
                                    }

                                    //lambda$合并处理
                                    if (methodCoverage.getName().contains("lambda$")) {
                                        String filterMethodName = methodCoverage.getName().split("lambda\\$")[1].split("\\$")[0];
                                        if (methodMap.containsKey(className + "." + filterMethodName)) {
                                            MethodCoverage result = methodMap.get(className + "." + filterMethodName);
                                            result.setCoveredCount(result.getCoveredCount() + methodCoverage.getLineCounter().getCoveredCount());
                                            result.setMissCount(result.getMissCount() + methodCoverage.getLineCounter().getMissedCount());
                                            result.setTotalCount(result.getCoveredCount() + result.getMissCount());
                                            result.setCovLinesRatio(BigDecimal.valueOf(result.getCoveredCount())
                                                    .divide(BigDecimal.valueOf(result.getTotalCount()), 4, BigDecimal.ROUND_HALF_UP));
                                        }
                                    }
                                })
                        ));
        return methodInfos;
    }
}