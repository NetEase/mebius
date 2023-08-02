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

import com.netease.mebius.client.exception.MebiusException;
import com.netease.mebius.client.executor.analyze.ClassInfoAssembler;
import com.netease.mebius.client.model.MethodParam;
import com.netease.mebius.client.model.SpecifyParam;
import com.netease.mebius.client.model.coverage.LineInfo;
import com.netease.mebius.client.model.coverage.ProjectFile;
import com.netease.mebius.client.model.coverage.ReportDiffParam;
import com.netease.mebius.client.model.coverage.ReportParam;
import com.netease.mebius.client.model.project.ProjectParam;
import lombok.extern.slf4j.Slf4j;
import org.assertj.core.util.Lists;
import org.jacoco.core.analysis.Analyzer;
import org.jacoco.core.analysis.CoverageBuilder;
import org.jacoco.core.analysis.IBundleCoverage;
import org.jacoco.core.analysis.IClassCoverage;
import org.jacoco.core.analysis.ICounter;
import org.jacoco.core.analysis.ILine;
import org.jacoco.core.analysis.IMethodCoverage;
import org.jacoco.core.analysis.IPackageCoverage;
import org.jacoco.core.internal.analysis.CounterImpl;
import org.jacoco.core.internal.diff.ClassInfo;
import org.jacoco.core.tools.ExecFileLoader;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

/**
 * 覆盖率报告对比执行器
 */
@Slf4j
public class CovReportDiffExecutor {


    /**
     * 覆盖率报告diff
     *
     * @param projectParam
     * @param reportParam
     * @param reportDiffParam
     */
    public static IBundleCoverage diff(ProjectParam projectParam, ReportParam reportParam, ReportDiffParam reportDiffParam) throws MebiusException {

        //先加载基准的exec
        try {
            ExecFileLoader execFileLoader = new ExecFileLoader();
            execFileLoader.load(reportDiffParam.getBaseExecFile());

            CoverageBuilder coverageBuilder = CovReportExecutor.routerBuilder(projectParam);
            for (ProjectFile projectFile : reportParam.getProjectList()) {
                Analyzer analyzer = new Analyzer(
                        execFileLoader.getExecutionDataStore(), coverageBuilder);
                analyzer.analyzeAll(projectFile.getClassesDirectory(), CovReportExecutor.getExcludeAnalyzeClass(projectParam.getExcludes()));
            }
            IBundleCoverage bundleCoverage = coverageBuilder.getBundle(reportParam.getTitle());

            //先加载当前对比的exec
            ExecFileLoader compareExecFileLoader = new ExecFileLoader();
            compareExecFileLoader.load(reportDiffParam.getCompareExecFile());

            List<SpecifyParam> sps = Lists.newArrayList();
            HashMap<String, List<LineInfo>> removeLineMap = new HashMap<>();

            //方法检查，只要该方法中有一行被覆盖，则需要加入，否则则移除
            for (IPackageCoverage iPackageCoverage : bundleCoverage.getPackages()) {
                for (IClassCoverage iClassCoverage : iPackageCoverage.getClasses()) {
                    try {
                        Iterator<IMethodCoverage> it = iClassCoverage.getMethods().iterator();
                        while (it.hasNext()) {
                            IMethodCoverage iMethodCoverage = it.next();
                            if (iMethodCoverage == null) {
                                continue;
                            }
                            int firstLine = iMethodCoverage.getFirstLine();
                            int lastLine = iMethodCoverage.getLastLine();

                            //方法中有一行被覆盖，就保留，否则则移除
                            boolean methodIsCovered = false;
                            for (int i = firstLine; i <= lastLine; i++) {
                                ILine iLine = iMethodCoverage.getLine(i);
                                if (iLine.getStatus() == 2 || iLine.getStatus() == 3) {
                                    methodIsCovered = true;
                                } else if (iLine.getStatus() == 1) {
                                    String key = iClassCoverage.getName() + "|" + iMethodCoverage.getName() + "|" + iMethodCoverage.getDesc();
                                    if (removeLineMap.get(key) == null) {
                                        List<LineInfo> lines = Lists.newArrayList();
                                        lines.add(new LineInfo(i - firstLine, i, iLine.getInstructionCounter(),
                                                iLine.getBranchCounter()));
                                        removeLineMap.put(key, lines);
                                    } else {
                                        removeLineMap.get(key).add(new LineInfo(i - firstLine, i, iLine.getInstructionCounter(),
                                                iLine.getBranchCounter()));
                                    }
                                }
                            }
                            if (!methodIsCovered) {
                                it.remove();
                            }
                        }

                        if (!iClassCoverage.getMethods().isEmpty()) {
                            SpecifyParam specifyParam = new SpecifyParam();
                            specifyParam.setPkgName(iClassCoverage.getPackageName().replace("/", "."));
                            specifyParam.setClassName(iClassCoverage.getSourceFileName().replace(".java", ""));
                            List<MethodParam> methodList = Lists.newArrayList();
                            specifyParam.setMethodList(methodList);
                            for (IMethodCoverage iMethodCoverage : iClassCoverage.getMethods()) {
                                MethodParam methodParam = new MethodParam();
                                methodParam.setMethodName(iMethodCoverage.getName());
                                methodParam.setAddLines(new int[]{iMethodCoverage.getFirstLine(), iMethodCoverage.getLastLine()});
                                methodList.add(methodParam);
                            }
                            sps.add(specifyParam);
                        }
                    } catch (Exception e) {
                        log.error("Class {} error:{}", iClassCoverage.getSourceFileName(), e.getMessage(), e);
                        throw new MebiusException(e.getMessage());
                    }
                }
            }

            //解析每个class中的方法
            List<ProjectParam> projectParams = Lists.newArrayList();
            projectParams.add(projectParam);
            List<ClassInfo> classInfos = ClassInfoAssembler.assembleClassInfoMultipleMethod(projectParams, sps);
            classInfos.forEach(classInfo -> classInfo.setType("COMPARE"));

            //加载本次执行覆盖率builder
            CoverageBuilder newCoverageBuilder = new CoverageBuilder(classInfos, projectParam.getExcludes());
            for (ProjectFile projectFile : reportParam.getProjectList()) {
                Analyzer analyzer = new Analyzer(
                        compareExecFileLoader.getExecutionDataStore(), newCoverageBuilder);
                analyzer.analyzeAll(projectFile.getClassesDirectory(), CovReportExecutor.getExcludeAnalyzeClass(projectParam.getExcludes()));
            }
            IBundleCoverage newBundleCoverage = newCoverageBuilder.getBundle(reportParam.getTitle());

            //排除基准覆盖率应该要排除的行
            for (IPackageCoverage iPackageCoverage : newBundleCoverage.getPackages()) {
                Set<String> handleClassTag = new HashSet<>(); //用于记录已处理类的标记
                for (IClassCoverage iClassCoverage : iPackageCoverage.getClasses()) {
                    Set<Integer> removeTag = new HashSet<>(); //用于记录删除行号标记，防止@Data映射到同一行重复删除
                    for (IMethodCoverage iMethodCoverage : iClassCoverage.getMethods()) {
                        String key = iClassCoverage.getName() + "|" + iMethodCoverage.getName() + "|" + iMethodCoverage.getDesc();
                        List<LineInfo> removeLine = removeLineMap.get(key);
                        if (removeLine != null) {
                            try {
                                for (LineInfo lineInfo : removeLine) {
                                    //真实的覆盖情况
                                    ILine iLine = iMethodCoverage.getLine(lineInfo.getIndex() + iMethodCoverage.getFirstLine());
                                    if (iLine == null) {
                                        continue;
                                    }

                                    //已删除则不处理
                                    if (removeTag.contains(lineInfo.getClassIndex())) {
                                        continue;
                                    }

                                    //删除本行(以下3个地方要删除)
                                    iMethodCoverage.setLine(lineInfo.getClassIndex(), null);
                                    iClassCoverage.setLine(lineInfo.getClassIndex(), null);
                                    iPackageCoverage.getSourceFile(iClassCoverage.getSourceFileName()).setLine(lineInfo.getClassIndex(), null);

                                    //开始计算无需覆盖的行
                                    //方法
                                    iMethodCoverage.setBranchCounter(calCounter(iLine, iMethodCoverage.getBranchCounter(), iLine.getBranchCounter()));
                                    iMethodCoverage.setInstructionCounter(calCounter(iLine, iMethodCoverage.getInstructionCounter(), iLine.getInstructionCounter()));
                                    iMethodCoverage.setLineCounter(calLineCounter(iLine, iMethodCoverage.getLineCounter()));

                                    //类
                                    iClassCoverage.setBranchCounter(calCounter(iLine, iClassCoverage.getBranchCounter(), iLine.getBranchCounter()));
                                    iClassCoverage.setInstructionCounter(calCounter(iLine, iClassCoverage.getInstructionCounter(), iLine.getInstructionCounter()));
                                    iClassCoverage.setLineCounter(calLineCounter(iLine, iClassCoverage.getLineCounter()));

                                    //包
                                    iPackageCoverage.setBranchCounter(calCounter(iLine, iPackageCoverage.getBranchCounter(), iLine.getBranchCounter()));
                                    iPackageCoverage.setInstructionCounter(calCounter(iLine, iPackageCoverage.getInstructionCounter(), iLine.getInstructionCounter()));
                                    iPackageCoverage.setLineCounter(calLineCounter(iLine, iPackageCoverage.getLineCounter()));

                                    //sourceFile
                                    String sourceFileName = iClassCoverage.getSourceFileName();
                                    String name = iClassCoverage.getPackageName() + "/" + sourceFileName.replace(".java", "");
                                    if (!handleClassTag.contains(name)) {
                                        iPackageCoverage.getSourceFile(iClassCoverage.getSourceFileName()).setBranchCounter(
                                                calCounter(iLine, iPackageCoverage.getSourceFile(iClassCoverage.getSourceFileName()).getBranchCounter(), iLine.getBranchCounter()));
                                        iPackageCoverage.getSourceFile(iClassCoverage.getSourceFileName()).setInstructionCounter(
                                                calCounter(iLine, iPackageCoverage.getSourceFile(iClassCoverage.getSourceFileName()).getInstructionCounter(), iLine.getInstructionCounter()));
                                        iPackageCoverage.getSourceFile(iClassCoverage.getSourceFileName()).setLineCounter(
                                                calLineCounter(iLine, iPackageCoverage.getSourceFile(iClassCoverage.getSourceFileName()).getLineCounter()));

                                        newBundleCoverage.setLineCounter(calLineCounter(iLine, newBundleCoverage.getLineCounter()));
                                    }

                                    //bundle
                                    newBundleCoverage.setBranchCounter(calCounter(iLine, newBundleCoverage.getBranchCounter(), iLine.getBranchCounter()));
                                    newBundleCoverage.setInstructionCounter(calCounter(iLine, newBundleCoverage.getInstructionCounter(), iLine.getInstructionCounter()));

                                    //删除标记
                                    removeTag.add(lineInfo.getClassIndex());
                                }
                            } catch (Exception e) {
                                log.error("Class:{}, method:{}, error:{}", iClassCoverage.getName(), iMethodCoverage.getName(), e.getMessage(), e);
                                throw new MebiusException("报告分析执行出错:" + e.getMessage());
                            }
                        }
                    }
                    //标记记录
                    handleClassTag.add(iClassCoverage.getName());
                }
            }

            //生成对比报告
            CovReportExecutor.createReport(newBundleCoverage, compareExecFileLoader, reportParam);
            return newBundleCoverage;
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            throw new MebiusException("Report diff exception:" + e.getMessage());
        }
    }

    /**
     * 计算counter
     *
     * @param iLine
     * @param oriCounter
     * @param incrementCounter
     * @return
     */
    private static CounterImpl calCounter(ILine iLine, ICounter oriCounter, ICounter incrementCounter) {

        //未覆盖
        if (iLine.getStatus() == 1 && oriCounter.getMissedCount() != 0) {
            return CounterImpl.getInstance(oriCounter)
                    .increment(incrementCounter.getMissedCount() * -1, 0);
        }
        //已覆盖
        if (iLine.getStatus() == 2 && oriCounter.getCoveredCount() != 0) {
            return CounterImpl.getInstance(oriCounter)
                    .increment(0, incrementCounter.getCoveredCount() * -1);
        }
        //部分覆盖
        if (iLine.getStatus() == 3) {
            return CounterImpl.getInstance(oriCounter)
                    .increment(incrementCounter.getMissedCount() * -1, incrementCounter.getCoveredCount() * -1);
        }
        return CounterImpl.getInstance(oriCounter);
    }

    /**
     * 计算line counter
     *
     * @param iLine
     * @param oriCounter
     * @return
     */
    private static CounterImpl calLineCounter(ILine iLine, ICounter oriCounter) {
        //未覆盖
        if (iLine.getStatus() == 1 && oriCounter.getMissedCount() != 0) {
            return CounterImpl.getInstance(oriCounter)
                    .increment(-1, 0);
        }
        //已覆盖 || 部分覆盖
        if (iLine.getStatus() == 2 || iLine.getStatus() == 3) {
            return CounterImpl.getInstance(oriCounter)
                    .increment(0, -1);
        }
        return CounterImpl.getInstance(oriCounter);
    }


}