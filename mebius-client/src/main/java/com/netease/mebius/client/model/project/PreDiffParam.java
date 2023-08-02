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
package com.netease.mebius.client.model.project;

import com.netease.mebius.client.enums.ExecType;
import lombok.Data;
import org.eclipse.jgit.revwalk.RevCommit;

/**
 * diff请求参数
 */
@Data
public class PreDiffParam {

    private ExecType execType;

    private RevCommit newRevCommit;

    private RevCommit oldRevCommit;

    private String newBranch;

    private String oldBranch;

    private String newCommit;

    private String oldCommit;

    private String newTag;

    private String oldTag;


    /**
     * @param newBranch
     * @param oldBranch
     * @return
     */
    public static PreDiffParam getBranchPreDiff(String newBranch, String oldBranch) {
        PreDiffParam preDiffParam = new PreDiffParam();
        preDiffParam.setExecType(ExecType.BRANCH_DIFF);
        preDiffParam.setNewBranch(newBranch);
        preDiffParam.setOldBranch(oldBranch);
        return preDiffParam;
    }

    /**
     * @param newRevCommit
     * @param oldRevCommit
     * @return
     */
    public static PreDiffParam getCommitPreDiff(RevCommit newRevCommit, RevCommit oldRevCommit) {
        PreDiffParam preDiffParam = new PreDiffParam();
        preDiffParam.setExecType(ExecType.COMMIT_DIFF);
        preDiffParam.setNewRevCommit(newRevCommit);
        preDiffParam.setOldRevCommit(oldRevCommit);
        return preDiffParam;
    }


    /**
     * @param newTag
     * @param oldTag
     * @return
     */
    public static PreDiffParam getTagPreDiff(String newTag, String oldTag) {
        PreDiffParam preDiffParam = new PreDiffParam();
        preDiffParam.setExecType(ExecType.TAG_DIFF);
        preDiffParam.setNewTag(newTag);
        preDiffParam.setOldTag(oldTag);
        return preDiffParam;
    }

}