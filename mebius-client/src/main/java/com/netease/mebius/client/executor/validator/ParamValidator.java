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
package com.netease.mebius.client.executor.validator;

import com.alibaba.fastjson.JSONObject;
import com.netease.mebius.client.enums.ExecType;
import com.netease.mebius.client.enums.GitAccessType;
import com.netease.mebius.client.exception.MebiusException;
import com.netease.mebius.client.model.project.ProjectParam;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;

import java.io.File;
import java.util.List;

/**
 * 参数校验器
 */
@Slf4j
public class ParamValidator {

    /**
     * 项目参数校验(多个项目)
     *
     * @param projectParams
     * @throws MebiusException
     */
    public static void projectValidator(List<ProjectParam> projectParams) throws MebiusException {
        for (ProjectParam projectParam : projectParams) {
            projectValidator(projectParam);
        }
    }

    /**
     * 项目参数校验
     *
     * @param projectParam
     * @throws MebiusException
     */
    public static void projectValidator(ProjectParam projectParam) throws MebiusException {

        log.debug("ProjectParam:{}", JSONObject.toJSONString(projectParam));

        if (StringUtils.isBlank(projectParam.getProjectRootPath())) {
            throw new MebiusException("ProjectRootPath can not null.");
        }
        if (!new File(projectParam.getProjectRootPath()).exists()) {
            throw new MebiusException("Project is not exist: " + projectParam.getProjectRootPath());
        }
        if (ExecType.BRANCH_DIFF.equals(projectParam.getExecType()) && StringUtils.isBlank(projectParam.getCurrentBranch())) {
            throw new MebiusException("Branch diff currentBranch can not null.");
        }
        if (ExecType.BRANCH_DIFF.equals(projectParam.getExecType()) && StringUtils.isBlank(projectParam.getCompareBranch())) {
            throw new MebiusException("Branch diff compareBranch can not null.");
        }
        if (ExecType.COMMIT_DIFF.equals(projectParam.getExecType()) && StringUtils.isBlank(projectParam.getCurrentBranch())) {
            throw new MebiusException("Commit diff currentBranch can not null.");
        }
        if (ExecType.COMMIT_DIFF.equals(projectParam.getExecType()) && StringUtils.isBlank(projectParam.getCurrentCommit())) {
            throw new MebiusException("Commit diff currentCommit can not null.");
        }
        if (ExecType.COMMIT_DIFF.equals(projectParam.getExecType()) && StringUtils.isBlank(projectParam.getCompareCommit())) {
            throw new MebiusException("Commit diff compareCommit can not null.");
        }
        if (ExecType.TAG_DIFF.equals(projectParam.getExecType()) && StringUtils.isBlank(projectParam.getCurrentBranch())) {
            throw new MebiusException("Commit diff currentBranch can not null.");
        }
        if (ExecType.TAG_DIFF.equals(projectParam.getExecType()) && StringUtils.isBlank(projectParam.getCurrentTag())) {
            throw new MebiusException("Commit diff currentTag can not null.");
        }
        if (ExecType.TAG_DIFF.equals(projectParam.getExecType()) && StringUtils.isBlank(projectParam.getCompareTag())) {
            throw new MebiusException("Commit diff compareTag can not null.");
        }
        if (projectParam.getGitParam().getGitAccessType() == null) {
            throw new MebiusException("GitAccessType can not null.");
        }
        if (GitAccessType.ACCOUNT.equals(projectParam.getGitParam().getGitAccessType()) && StringUtils.isBlank(projectParam.getGitParam().getGitUser())) {
            throw new MebiusException("Git account mode gitUser can not null.");
        }
        if (GitAccessType.ACCOUNT.equals(projectParam.getGitParam().getGitAccessType()) && StringUtils.isBlank(projectParam.getGitParam().getGitPassword())) {
            throw new MebiusException("Git account mode gitPassword can not null.");
        }
        if (GitAccessType.ACCESS_TOKEN.equals(projectParam.getGitParam().getGitAccessType()) && StringUtils.isBlank(projectParam.getGitParam().getGitAccessToken())) {
            throw new MebiusException("Git access token mode gitAccessToken can not null.");
        }
    }
}