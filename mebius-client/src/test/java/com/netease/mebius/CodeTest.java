/**
 * @(#)CodeTest.java, 2023/7/18.
 * <p/>
 * Copyright 2023 Netease, Inc. All rights reserved.
 * NETEASE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package com.netease.mebius;

import com.netease.mebius.client.action.CodeDiff;
import com.netease.mebius.client.enums.ExecType;
import com.netease.mebius.client.enums.GitAccessType;
import com.netease.mebius.client.exception.MebiusException;
import com.netease.mebius.client.model.project.GitParam;
import com.netease.mebius.client.model.project.ProjectParam;
import org.eclipse.jgit.api.errors.GitAPIException;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * @author chenyiqing@corp.netease.com on 2023/7/18.
 */
public class CodeTest {

    public static void main(String[] args) throws IOException, GitAPIException, MebiusException {
        List<ProjectParam> projectParams = new ArrayList<>();
        ProjectParam projectParam = new ProjectParam();
        projectParam.setProjectRootPath("/Users/chenyiqing/temp/yx-dubhe");
        projectParam.setExecType(ExecType.BRANCH_DIFF);
        projectParam.setCurrentBranch("feature-AIcodeReview");
        projectParam.setCompareBranch("master");
        GitParam gitParam = new GitParam();
        gitParam.setGitAccessType(GitAccessType.ACCESS_TOKEN);
        gitParam.setGitAccessToken("ryrJy12SfyqNwFi1RVxs");
        projectParam.setGitParam(gitParam);
        projectParams.add(projectParam);
        CodeDiff.codeDiff(projectParam);
    }


}