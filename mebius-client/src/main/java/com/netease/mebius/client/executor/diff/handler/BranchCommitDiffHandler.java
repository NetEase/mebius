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
 import org.eclipse.jgit.api.Git;
 import org.eclipse.jgit.diff.DiffEntry;
 import org.eclipse.jgit.lib.Ref;
 import org.eclipse.jgit.revwalk.RevCommit;
 import org.eclipse.jgit.treewalk.AbstractTreeIterator;
 import org.jacoco.core.internal.diff.ClassInfo;

 import java.io.IOException;
 import java.util.List;
 import java.util.Map;

 /**
  * 分支和版本对比处理器
  */
 @Slf4j
 public class BranchCommitDiffHandler extends AbstractDiffHandler implements DiffHandler {

     @Override
     public List<ClassInfo> diff(ProjectParam param, Map<String, List<String>> excludes) throws Exception {
         return diffBranchAndCommitMethods(param.getProjectRootPath(),
                 param.getCurrentBranch(), param.getCompareBranch(),
                 param.getCurrentCommit(), param.getCompareCommit(), excludes);
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
      * @param compareBranchCommit
      * @param excludes
      * @return
      * @throws Exception
      */
     private static List<ClassInfo> diffBranchAndCommitMethods(String gitPath, String newBranchName, String oldBranchName,
                                                               String newBranchCommit, String compareBranchCommit,
                                                               Map<String, List<String>> excludes) throws Exception {
         //获取本地分支
         GitAdapter gitAdapter = new GitAdapter(gitPath);
         Git git = gitAdapter.getGit();
         Ref localMasterRef = gitAdapter.getRepository().exactRef(REF_HEADS + oldBranchName);

         //更新本地分支
         gitAdapter.checkOutAndPullAllWay(localMasterRef, oldBranchName);

         RevCommit oldRevCommit = null;
         Iterable<RevCommit> oldCommits = git.log().call();
         for (RevCommit commit : oldCommits) {
             if (commit.getName().startsWith(compareBranchCommit)) {
                 oldRevCommit = commit;
             }
         }

         Ref localBranchRef = gitAdapter.getRepository().exactRef(REF_HEADS + newBranchName);
         gitAdapter.checkOutAndPullAllWay(localBranchRef, newBranchName);
         RevCommit newRevCommit = null;
         Iterable<RevCommit> newCommits = git.log().call();
         for (RevCommit commit : newCommits) {
             if (commit.getName().startsWith(newBranchCommit)) {
                 newRevCommit = commit;
             }
         }
         //最终要追踪到当前版本，为了防止代码不一致问题
         gitAdapter.reset(newBranchCommit);

         //获取分支信息
         AbstractTreeIterator newTreeParser = gitAdapter.prepareTreeParser(newRevCommit);
         AbstractTreeIterator oldTreeParser = gitAdapter.prepareTreeParser(oldRevCommit);
         //对比差异
         List<DiffEntry> diffs = git.diff().setOldTree(oldTreeParser).setNewTree(newTreeParser).setShowNameAndStatusOnly(true).call();
         diffs = renameDetector(git, diffs);
         //多线程处理解析class信息
         return batchPrepareDiffMethod(gitAdapter,
                 PreDiffParam.getBranchPreDiff(newBranchName, oldBranchName), diffs, excludes);
     }
 }