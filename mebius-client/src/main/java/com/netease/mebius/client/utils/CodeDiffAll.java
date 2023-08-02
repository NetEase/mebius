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
package com.netease.mebius.client.utils;

import com.netease.mebius.client.executor.diff.ASTGenerator;
import com.netease.mebius.client.executor.diff.GitAdapter;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.diff.DiffFormatter;
import org.eclipse.jgit.diff.RawTextComparator;
import org.eclipse.jgit.lib.Ref;
import org.eclipse.jgit.treewalk.AbstractTreeIterator;
import org.jacoco.core.internal.diff.ClassInfo;
import org.jacoco.core.internal.diff.MethodInfo;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

/**
 * 代码版本比较
 */
@Slf4j
public class CodeDiffAll {
    public final static String REF_HEADS = "refs/heads/";
    public final static  String MASTER = "master";

    public static List<ClassInfo> diffAll(String gitPath, String newBranchName) {
        List<ClassInfo> classInfos = diffAllMethods(gitPath, newBranchName);
        return classInfos;
    }
    private static List<ClassInfo> diffAllMethods(String gitPath, String newBranchName) {
        try {
            //  获取本地分支
            GitAdapter gitAdapter = new GitAdapter(gitPath);
            Git git = gitAdapter.getGit();
            Ref localBranchRef = gitAdapter.getRepository().exactRef(REF_HEADS + newBranchName);
            log.info("初始化成功");
            //  更新本地分支
            gitAdapter.checkOutAndPull(localBranchRef, newBranchName);
            log.info("本地分支已全部更新完毕");

            //  获取分支信息
            AbstractTreeIterator newTreeParser = gitAdapter.prepareTreeParser(localBranchRef);
            log.info("已获取分支信息-----");

            //  对比差异
            List<String> all_file_path = new ArrayList<String>();
            getAllFile(gitPath, gitPath, all_file_path);
            //all_file_path.add("elephant_recommend/src/main/java/com/nets/elephant/recommend/controller/RecommendClientController.java");
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            DiffFormatter df = new DiffFormatter(out);
            //设置比较器为忽略空白字符对比（Ignores all whitespace）
            df.setDiffComparator(RawTextComparator.WS_IGNORE_ALL);
            df.setRepository(git.getRepository());
            List<ClassInfo> allClassInfos = batchPrepareAllMethod(gitAdapter, newBranchName, all_file_path);
            return allClassInfos;
        }catch (Exception e) {
            e.printStackTrace();
        }
        return  new ArrayList<ClassInfo>();
    }

    public static void getAllFile(String rootPath, String gitPath, List<String> allFilePath) {
        do {
            if (gitPath.isEmpty()) {
                break;
            }
            File file = new File(gitPath);
            if (!file.exists()) {
                break;
            }
            File[] files = file.listFiles();
            if (null == files || files.length == 0) {
                break;
            }
            for (File file2 : files) {
                if (file2.isDirectory()) {
                    String path = file2.getAbsolutePath();
                    if (!path.endsWith(".git") && !path.endsWith(".idea") && !path.endsWith(".settings")) {
                        getAllFile(rootPath, file2.getAbsolutePath(), allFilePath);
                    }
                } else {
                    String tmp_path = file2.getAbsolutePath();
                    System.out.println(tmp_path.substring(rootPath.length()));
                    if (tmp_path.endsWith(".java")) {
                        allFilePath.add(tmp_path.substring(rootPath.length()+1));
                    }
                }
            }
        } while (false);
    }

    /**
     * 多线程执行对比
     * @return
     */
    private static List<ClassInfo> batchPrepareAllMethod(final GitAdapter gitAdapter, final String branchName, List<String> all_file_paths) {
        int threadSize = 300;
        int dataSize = all_file_paths.size();
        int threadNum = dataSize / threadSize + 1;
        boolean special = dataSize % threadSize == 0;
        ExecutorService executorService = Executors.newFixedThreadPool(threadNum);

        List<Callable<List<ClassInfo>>> tasks = new ArrayList<Callable<List<ClassInfo>>>();
        Callable<List<ClassInfo>> task = null;
        List<String> cutList = null;
        //  分解每条线程的数据
        for (int i = 0; i < threadNum; i++) {
            if (i == threadNum - 1) {
                if (special) {
                    break;
                }
                cutList = all_file_paths.subList(threadSize * i, dataSize);
            } else {
                cutList = all_file_paths.subList(threadSize * i, threadSize * (i + 1));
            }
            final List<String> diffFileList = cutList;
            task = new Callable<List<ClassInfo>>() {
                @Override
                public List<ClassInfo> call() throws Exception {
                    List<ClassInfo> allList = new ArrayList<ClassInfo>();
                    for (String file_path: diffFileList) {
                        ClassInfo classInfo = prepareFileMethod(gitAdapter, branchName, file_path);
                        if (classInfo != null) {
                            allList.add(classInfo);
                        }
                    }
                    return allList;
                }
            };
            // 这里提交的任务容器列表和返回的Future列表存在顺序对应的关系
            tasks.add(task);
        }
        List<ClassInfo> allClassInfoList = new ArrayList<ClassInfo>();
        try {
            List<Future<List<ClassInfo>>> results = executorService.invokeAll(tasks);
            //结果汇总
            for (Future<List<ClassInfo>> future : results ) {
                allClassInfoList.addAll(future.get());
            }
        }catch (Exception e) {
            e.printStackTrace();
        }finally {
            // 关闭线程池
            executorService.shutdown();
        }
        return allClassInfoList;
    }

    /**
     * 单个差异文件对比
     * @param gitAdapter
     * @param branchName
     * @param file_path
     * @return
     */
    private synchronized static ClassInfo prepareFileMethod(GitAdapter gitAdapter, String branchName, String file_path) {
        List<MethodInfo> methodInfoList = new ArrayList<MethodInfo>();
        try {
            //  排除测试类
            if (file_path.contains("/src/test/java/")) {
                return null;
            }
            //  非java文件 和 删除类型不记录
            if (!file_path.endsWith(".java")) {
                return null;
            }
            String newClassContent = gitAdapter.getBranchSpecificFileContent(branchName, file_path);
            ASTGenerator newAstGenerator = new ASTGenerator(newClassContent);
            /*  新增类型   */
            return newAstGenerator.getClassInfo();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }
}

