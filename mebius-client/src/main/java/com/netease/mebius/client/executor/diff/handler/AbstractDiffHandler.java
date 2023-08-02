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
package com.netease.mebius.client.executor.diff.handler;

import com.netease.mebius.client.enums.ExecType;
import com.netease.mebius.client.executor.diff.ASTGenerator;
import com.netease.mebius.client.executor.diff.GitAdapter;
import com.netease.mebius.client.model.project.PreDiffParam;
import lombok.extern.slf4j.Slf4j;
import org.assertj.core.util.Lists;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.diff.DiffEntry;
import org.eclipse.jgit.diff.DiffFormatter;
import org.eclipse.jgit.diff.Edit;
import org.eclipse.jgit.diff.EditList;
import org.eclipse.jgit.diff.RawTextComparator;
import org.eclipse.jgit.diff.RenameDetector;
import org.eclipse.jgit.patch.FileHeader;
import org.jacoco.core.internal.diff.ClassInfo;
import org.jacoco.core.internal.diff.MethodInfo;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

/**
 * Diff抽象处理器
 *
 */
@Slf4j
public class AbstractDiffHandler {

    public final static String REF_HEADS = "refs/heads/";

    public final static String TEST_PATH = "/src/test/java/";

    /**
     * 获取diff formatter
     *
     * @param git
     * @return
     */
    public static DiffFormatter getDiffFormatter(Git git) {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        DiffFormatter df = new DiffFormatter(out);
        //设置比较器为忽略空白字符对比（Ignores all whitespace）
        df.setDiffComparator(RawTextComparator.WS_IGNORE_ALL);
        df.setRepository(git.getRepository());
        return df;
    }

    /**
     * 根据执行类型初始化handler
     *
     * @param execType
     * @return
     */
    public static DiffHandler initDiffHandler(ExecType execType) {
        if (execType == null) {
            return new NoDiffHandler();
        }
        switch (execType) {
            case ALL:
                return new NoDiffHandler();
            case BRANCH_DIFF:
                return new BranchDiffHandler();
            case COMMIT_DIFF:
                return new CommitDiffHandler();
            case TAG_DIFF:
                return new TagDiffHandler();
            case BRANCH_COMMIT_DIFF:
                return new BranchCommitDiffHandler();
        }
        return new NoDiffHandler();
    }


    /**
     * 从语法树中解析生成class信息
     *
     * @param excludes
     * @param diffEntry
     * @param df
     * @param newClassContent
     * @param oldClassContent
     * @return
     * @throws IOException
     */
    public static ClassInfo parseClassFromAstTree(DiffEntry diffEntry, DiffFormatter df,
                                                  String newClassContent, String oldClassContent,
                                                  Map<String, List<String>> excludes) throws IOException {
        List<MethodInfo> methodInfoList = Lists.newArrayList();
        List<MethodInfo> newMethodList = Lists.newArrayList();
        String newJavaPath = diffEntry.getNewPath();
        String oldJavaPath = diffEntry.getOldPath();
        //排除测试类
        if (newJavaPath.contains(TEST_PATH)) {
            return null;
        }
        //非java文件 和 删除类型不记录
        if (!newJavaPath.endsWith(".java") || diffEntry.getChangeType() == DiffEntry.ChangeType.DELETE) {
            return null;
        }

        ASTGenerator newAstGenerator = new ASTGenerator(newClassContent);
        ASTGenerator oldAstGenerator = new ASTGenerator(oldClassContent);
        if (newAstGenerator.getClassInfo() == null) {
            return null;
        }

        String fullClassName = newAstGenerator.getClassInfo().getPackages() + "." + newAstGenerator.getClassInfo().getClassName();
        //获取文件差异位置，从而统计差异的行数，如增加行数，减少行数
        FileHeader fileHeader = df.toFileHeader(diffEntry);
        List<int[]> addLines = new ArrayList<int[]>();
        List<int[]> delLines = new ArrayList<int[]>();
        EditList editList = fileHeader.toEditList();

        for (Edit edit : editList) {
            if (edit.getLengthA() > 0) {
                delLines.add(new int[]{edit.getBeginA(), edit.getEndA()});
            }
            if (edit.getLengthB() > 0) {
                addLines.add(new int[]{edit.getBeginB(), edit.getEndB()});
            }
        }

        //新增类型
        if (diffEntry.getChangeType() == DiffEntry.ChangeType.ADD) {
            MethodDeclaration[] newMethods = newAstGenerator.getMethods();
            for (final MethodDeclaration method : newMethods) {
                MethodInfo methodInfo = newAstGenerator.getMethodInfo(method);
                methodInfoList.add(methodInfo);
                newMethodList.add(methodInfo);
            }

            if (excludes != null && excludes.containsKey(fullClassName)) {
                List<String> excludesMethods = excludes.get(fullClassName);
                if (excludesMethods == null || excludesMethods.size() == 0) {
                    return null;
                } else {
                    ClassInfo classInfo = newAstGenerator.getClassInfoExcludesMethod(excludesMethods);
                    classInfo.setAddLines(addLines);
                    classInfo.setClassFile(newJavaPath);
                    classInfo.setMethodInfos(methodInfoList);
                    classInfo.setNewMethodInfos(newMethodList);
                    return classInfo;
                }
            }
            ClassInfo classInfo = newAstGenerator.getClassInfo();
            classInfo.setAddLines(addLines);
            classInfo.setDelLines(delLines);
            classInfo.setClassFile(newJavaPath);
            classInfo.setMethodInfos(methodInfoList);
            classInfo.setNewMethodInfos(newMethodList);
            return classInfo;
        }
        //修改类型
        MethodDeclaration[] newMethods = newAstGenerator.getMethods();
        MethodDeclaration[] oldMethods = oldAstGenerator.getMethods();
        Map<String, MethodDeclaration> methodsMap = new HashMap<String, MethodDeclaration>();
        for (int i = 0; i < oldMethods.length; i++) {
            methodsMap.put(oldMethods[i].getName().toString() + oldMethods[i].parameters().toString(), oldMethods[i]);
        }
        for (final MethodDeclaration method : newMethods) {
            if (excludes != null && excludes.containsKey(fullClassName)) {
                List<String> excludesMethods = excludes.get(fullClassName);
                if (excludesMethods == null || excludesMethods.size() == 0) {
                    return null;
                }
                if (excludesMethods.contains(method.getName().toString())) {
                    continue;
                }
            }
            // 如果方法名是新增的,则直接将方法加入List
            if (!ASTGenerator.isMethodExist(method, methodsMap)) {
                MethodInfo methodInfo = newAstGenerator.getMethodInfo(method);
                methodInfoList.add(methodInfo);
                newMethodList.add(methodInfo);
                continue;
            }
            // 如果两个版本都有这个方法,则根据MD5判断方法是否一致
            if (!ASTGenerator.isMethodTheSame(method, methodsMap.get(method.getName().toString() + method.parameters().toString()))) {
                MethodInfo methodInfo = newAstGenerator.getMethodInfo(method);
                methodInfoList.add(methodInfo);
            }
        }
        ClassInfo classInfo = newAstGenerator.getClassInfo(methodInfoList, addLines, delLines, newMethodList);
        classInfo.setClassFile(newJavaPath);
        return classInfo;
    }


    /**
     * 多线程执行对比
     *
     * @param gitAdapter
     * @param preDiffParam
     * @param diffs
     * @param excludes
     * @return
     */
    public static List<ClassInfo> batchPrepareDiffMethod(final GitAdapter gitAdapter,
                                                         final PreDiffParam preDiffParam,
                                                         List<DiffEntry> diffs,
                                                         Map<String, List<String>> excludes) throws Exception {
        int threadSize = 100;
        int dataSize = diffs.size();
        int threadNum = dataSize / threadSize + 1;
        boolean special = dataSize % threadSize == 0;
        ExecutorService executorService = Executors.newFixedThreadPool(threadNum);
        List<Callable<List<ClassInfo>>> tasks = new ArrayList<>();
        Callable<List<ClassInfo>> task;
        List<DiffEntry> cutList;
        //  分解每条线程的数据
        for (int i = 0; i < threadNum; i++) {
            if (i == threadNum - 1) {
                if (special) {
                    break;
                }
                cutList = diffs.subList(threadSize * i, dataSize);
            } else {
                cutList = diffs.subList(threadSize * i, threadSize * (i + 1));
            }
            final List<DiffEntry> diffEntryList = cutList;
            task = () -> {
                List<ClassInfo> allList = new ArrayList<>();
                for (DiffEntry diffEntry : diffEntryList) {
                    ClassInfo classInfo = initDiffHandler(preDiffParam.getExecType())
                            .parseClassInfo(gitAdapter, preDiffParam, diffEntry, excludes);
                    if (classInfo != null) {
                        allList.add(classInfo);
                    }
                }
                return allList;
            };
            // 这里提交的任务容器列表和返回的Future列表存在顺序对应的关系
            tasks.add(task);
        }
        List<ClassInfo> allClassInfoList = Lists.newArrayList();
        try {
            List<Future<List<ClassInfo>>> results = executorService.invokeAll(tasks);
            //结果汇总
            for (Future<List<ClassInfo>> future : results) {
                allClassInfoList.addAll(future.get());
            }
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            throw new Exception(e.getMessage());
        } finally {
            // 关闭线程池
            executorService.shutdown();
        }
        return allClassInfoList;
    }

    /**
     * git文件重命名检测
     *
     * @param git
     * @param diffs
     * @return
     * @throws IOException
     */
    public static List<DiffEntry> renameDetector(Git git, List<DiffEntry> diffs) throws IOException {
        RenameDetector rd = new RenameDetector(git.getRepository());
        rd.setRenameScore(40);
        rd.addAll(diffs);
        return rd.compute();
    }
}