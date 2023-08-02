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

 import com.netease.mebius.client.executor.diff.GitAdapter;
 import com.netease.mebius.client.model.project.PreDiffParam;
 import com.netease.mebius.client.model.project.ProjectParam;
 import lombok.extern.slf4j.Slf4j;
 import org.apache.commons.lang.StringUtils;
 import org.assertj.core.util.Lists;
 import org.eclipse.jgit.api.Git;
 import org.eclipse.jgit.diff.DiffEntry;
 import org.eclipse.jgit.lib.Ref;
 import org.eclipse.jgit.treewalk.AbstractTreeIterator;
 import org.jacoco.core.internal.diff.ClassInfo;

 import java.io.IOException;
 import java.util.List;
 import java.util.Map;

 /**
  * 分支对比处理器
  */
 @Slf4j
 public class BranchDiffHandler extends AbstractDiffHandler implements DiffHandler {

     @Override
     public List<ClassInfo> diff(ProjectParam param, Map<String, List<String>> excludes) throws Exception {
         return diffMethods(param.getProjectRootPath(), param.getCurrentBranch(), param.getCompareBranch(), param.getCurrentCommit(), excludes);
     }

     @Override
     public ClassInfo parseClassInfo(GitAdapter gitAdapter, PreDiffParam param, DiffEntry diffEntry, Map<String, List<String>> excludes) throws IOException {
         String newClassContent = gitAdapter.getBranchSpecificFileContent(param.getNewBranch(), diffEntry.getNewPath());
         String oldClassContent = gitAdapter.getBranchSpecificFileContent(param.getOldBranch(), diffEntry.getOldPath());
         return parseClassFromAstTree(diffEntry, getDiffFormatter(gitAdapter.getGit()), newClassContent, oldClassContent, excludes);
     }


     /**
      * @param gitPath
      * @param newBranchName
      * @param oldBranchName
      * @param newBranchCommit
      * @param excludes
      * @return
      * @throws Exception
      */
     private static List<ClassInfo> diffMethods(String gitPath, String newBranchName, String oldBranchName,
                                                String newBranchCommit, Map<String, List<String>> excludes) throws Exception {
         //获取本地分支
         GitAdapter gitAdapter = new GitAdapter(gitPath);
         Git git = gitAdapter.getGit();
         Ref localBranchRef = gitAdapter.getRepository().exactRef(REF_HEADS + newBranchName);
         Ref localMasterRef = gitAdapter.getRepository().exactRef(REF_HEADS + oldBranchName);
         //更新本地分支
         gitAdapter.checkOutAndPull(localMasterRef, oldBranchName);
         gitAdapter.checkOutAndPull(localBranchRef, newBranchName);

         //最终要追踪到当前分支，为了防止代码不一致问题
         gitAdapter.checkOut(newBranchName);
         gitAdapter.reset(newBranchCommit);

         if (localBranchRef == null) {
             localBranchRef = gitAdapter.getRepository().exactRef(REF_HEADS + newBranchName);
         }
         if (localMasterRef == null) {
             localMasterRef = gitAdapter.getRepository().exactRef(REF_HEADS + oldBranchName);
         }
         //获取分支信息
         AbstractTreeIterator newTreeParser = gitAdapter.prepareTreeParser(localBranchRef);
         AbstractTreeIterator oldTreeParser = gitAdapter.prepareTreeParser(localMasterRef);
         //对比差异
         List<DiffEntry> diffs = git.diff()
                 .setOldTree(oldTreeParser)
                 .setNewTree(newTreeParser)
                 .setShowNameAndStatusOnly(true)
                 .call();
         diffs = renameDetector(git, diffs);
         //排除submodule
         List<DiffEntry> filterDiffs = Lists.newArrayList();
         for (DiffEntry diffEntry : diffs) {
             if (StringUtils.endsWith(diffEntry.getNewPath(), ".java") ||
                     StringUtils.endsWith(diffEntry.getOldPath(), ".java")) {
                 filterDiffs.add(diffEntry);
                 continue;
             }
             if (StringUtils.contains(diffEntry.getNewPath(), "/") &&
                     StringUtils.contains(diffEntry.getOldPath(), "/")) {
                 filterDiffs.add(diffEntry);
             }
         }
         //多线程处理解析class信息
         return batchPrepareDiffMethod(gitAdapter,
                 PreDiffParam.getBranchPreDiff(newBranchName, oldBranchName), filterDiffs, excludes);
     }
 }