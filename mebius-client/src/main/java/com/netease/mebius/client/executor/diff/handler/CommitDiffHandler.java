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
 import org.eclipse.jgit.lib.Repository;
 import org.eclipse.jgit.revwalk.RevCommit;
 import org.eclipse.jgit.treewalk.AbstractTreeIterator;
 import org.eclipse.jgit.util.StringUtils;
 import org.jacoco.core.internal.diff.ClassInfo;

 import java.io.IOException;
 import java.util.ArrayList;
 import java.util.List;
 import java.util.Map;

 /**
  * Commit版本对比处理器
  */
 @Slf4j
 public class CommitDiffHandler extends AbstractDiffHandler implements DiffHandler {

     @Override
     public List<ClassInfo> diff(ProjectParam param, Map<String, List<String>> excludes) throws Exception {
         return diffCommitMethods(param.getProjectRootPath(), param.getCurrentBranch(), param.getCurrentCommit(), param.getCompareCommit(), excludes);
     }

     @Override
     public ClassInfo parseClassInfo(GitAdapter gitAdapter, PreDiffParam param, DiffEntry diffEntry, Map<String, List<String>> excludes) throws IOException {
         String newClassContent = gitAdapter.getCommitRevisionSpecificFileContent(param.getNewRevCommit(), diffEntry.getNewPath());
         String oldClassContent = gitAdapter.getCommitRevisionSpecificFileContent(param.getOldRevCommit(), diffEntry.getOldPath());
         return parseClassFromAstTree(diffEntry, getDiffFormatter(gitAdapter.getGit()), newClassContent, oldClassContent, excludes);
     }


     /**
      * @param gitPath
      * @param branchName
      * @param newCommit
      * @param oldCommit
      * @param excludes
      * @return
      * @throws Exception
      */
     private static List<ClassInfo> diffCommitMethods(String gitPath, String branchName, String newCommit, String oldCommit,
                                                      Map<String, List<String>> excludes) throws Exception {
         //init local repository
         GitAdapter gitAdapter = new GitAdapter(gitPath);
         Git git = gitAdapter.getGit();
         Repository repo = gitAdapter.getRepository();
         Ref localBranchRef = repo.exactRef(REF_HEADS + branchName);

         //update local repository
         gitAdapter.checkOutAndPull(localBranchRef, branchName);

         RevCommit newRevCommit = null;
         RevCommit oldRevCommit = null;
         if (!StringUtils.isEmptyOrNull(newCommit) && !StringUtils.isEmptyOrNull(oldCommit)) {
             Iterable<RevCommit> commits = git.log().call();
             for (RevCommit commit : commits) {
                 if (commit.getName().startsWith(newCommit)) {
                     newRevCommit = commit;
                 }
                 if (commit.getName().startsWith(oldCommit)) {
                     oldRevCommit = commit;
                 }
             }
         } else {
             //不传则默认取最新的两次比较
             List<RevCommit> commitList = new ArrayList<RevCommit>();
             Iterable<RevCommit> commits = git.log().setMaxCount(2).call();
             for (RevCommit commit : commits) {
                 commitList.add(commit);
             }
             newRevCommit = commitList.get(0);
             oldRevCommit = commitList.get(1);
         }

         //最终要追踪到当前版本，为了防止代码不一致问题
         gitAdapter.reset(newCommit);

         //获取分支信息
         AbstractTreeIterator newTreeParser = gitAdapter.prepareTreeParser(newRevCommit);
         AbstractTreeIterator oldTreeParser = gitAdapter.prepareTreeParser(oldRevCommit);
         //对比差异
         List<DiffEntry> diffs = git.diff().setOldTree(oldTreeParser).setNewTree(newTreeParser).setShowNameAndStatusOnly(true).call();
         diffs = renameDetector(git, diffs);

         //多线程处理解析class信息
         return batchPrepareDiffMethod(gitAdapter,
                 PreDiffParam.getCommitPreDiff(newRevCommit, oldRevCommit), diffs, excludes);

     }

 }