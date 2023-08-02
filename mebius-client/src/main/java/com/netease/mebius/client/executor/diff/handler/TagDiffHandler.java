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
 import org.eclipse.jgit.lib.ObjectId;
 import org.eclipse.jgit.lib.ObjectReader;
 import org.eclipse.jgit.lib.Ref;
 import org.eclipse.jgit.lib.Repository;
 import org.eclipse.jgit.treewalk.CanonicalTreeParser;
 import org.jacoco.core.internal.diff.ClassInfo;

 import java.io.IOException;
 import java.util.List;
 import java.util.Map;

 /**
  * Tag对比处理器
  */
 @Slf4j
 public class TagDiffHandler extends AbstractDiffHandler implements DiffHandler {

     @Override
     public List<ClassInfo> diff(ProjectParam param, Map<String, List<String>> excludes) throws Exception {
         return diffTagMethods(param.getProjectRootPath(), param.getCurrentBranch(),
                 param.getCurrentTag(), param.getCompareTag(), param.getCurrentCommit(), excludes);
     }

     @Override
     public ClassInfo parseClassInfo(GitAdapter gitAdapter, PreDiffParam param, DiffEntry diffEntry, Map<String, List<String>> excludes) throws IOException {
         String newClassContent = gitAdapter.getTagRevisionSpecificFileContent(param.getNewTag(), diffEntry.getNewPath());
         String oldClassContent = gitAdapter.getTagRevisionSpecificFileContent(param.getOldTag(), diffEntry.getOldPath());
         return parseClassFromAstTree(diffEntry, getDiffFormatter(gitAdapter.getGit()), newClassContent, oldClassContent, excludes);
     }


     /**
      * @param gitPath
      * @param branchName
      * @param newTag
      * @param oldTag
      * @param newCommit
      * @param excludes
      * @return
      * @throws Exception
      */
     private static List<ClassInfo> diffTagMethods(String gitPath, String branchName, String newTag, String oldTag, String newCommit,
                                                   Map<String, List<String>> excludes) throws Exception {
         //init local repository
         GitAdapter gitAdapter = new GitAdapter(gitPath);
         Git git = gitAdapter.getGit();
         Repository repo = gitAdapter.getRepository();
         Ref localBranchRef = repo.exactRef(REF_HEADS + branchName);

         //update local repository
         gitAdapter.checkOutAndPull(localBranchRef, branchName);

         ObjectId head = repo.resolve(newTag + "^{tree}");
         ObjectId previousHead = repo.resolve(oldTag + "^{tree}");

         //Instantiation a reader to read the data from the Git database
         ObjectReader reader = repo.newObjectReader();
         //Create the tree iterator for each commit
         CanonicalTreeParser oldTreeIter = new CanonicalTreeParser();
         oldTreeIter.reset(reader, previousHead);
         CanonicalTreeParser newTreeIter = new CanonicalTreeParser();
         newTreeIter.reset(reader, head);

         //最终要追踪到当前版本，为了防止代码不一致问题
         gitAdapter.reset(newCommit);

         //对比差异
         List<DiffEntry> diffs = git.diff().setOldTree(oldTreeIter).setNewTree(newTreeIter).setShowNameAndStatusOnly(true).call();
         diffs = renameDetector(git, diffs);

         //多线程处理解析class信息
         return batchPrepareDiffMethod(gitAdapter,
                 PreDiffParam.getTagPreDiff(newTag, oldTag), diffs, excludes);
     }
 }