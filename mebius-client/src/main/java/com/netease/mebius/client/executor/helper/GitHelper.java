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
package com.netease.mebius.client.executor.helper;

import com.jcraft.jsch.JSch;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.Session;
import com.netease.mebius.client.constant.ConstantVar;
import com.netease.mebius.client.enums.GitAccessType;
import com.netease.mebius.client.exception.MebiusException;
import com.netease.mebius.client.executor.diff.GitAdapter;
import com.netease.mebius.client.model.project.GitParam;
import com.netease.mebius.client.model.project.ProjectParam;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.jgit.transport.JschConfigSessionFactory;
import org.eclipse.jgit.transport.OpenSshConfig;
import org.eclipse.jgit.transport.UsernamePasswordCredentialsProvider;
import org.eclipse.jgit.util.FS;

import java.io.FileInputStream;
import java.io.FileNotFoundException;

/**
 * Git操作帮助类
 */
@Slf4j
public class GitHelper {

    /**
     * Git设置授权
     *
     * @param projectParam
     */
    public static void setAuthorization(ProjectParam projectParam) throws MebiusException {

        try {
            if (GitAccessType.ACCOUNT.equals(projectParam.getGitParam().getGitAccessType())) {
                GitAdapter.setCredentialsProvider(projectParam.getGitParam().getGitUser(), projectParam.getGitParam().getGitPassword());
            }
            if (GitAccessType.ACCESS_TOKEN.equals(projectParam.getGitParam().getGitAccessType())) {
                GitAdapter.setCredentialsProvider(ConstantVar.GIT_PRIVATE_TOKEN_USER, projectParam.getGitParam().getGitAccessToken());
            }
            if (GitAccessType.SSH_KEY.equals(projectParam.getGitParam().getGitAccessType())) {
                GitAdapter.setSshSessionFactory(projectParam.getGitParam().getSshPrivateKeyPath(), new FileInputStream(projectParam.getGitParam().getSshKnownHosts()));
            }
        } catch (FileNotFoundException e) {
            throw new MebiusException(e.getMessage());
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            throw new MebiusException("Git auth fail.");
        }
    }


    /**
     * getCredentialsProvider
     *
     * @param gitParam
     * @return
     */
    public static UsernamePasswordCredentialsProvider getCredentialsProvider(GitParam gitParam) {
        if (GitAccessType.ACCOUNT.equals(gitParam.getGitAccessType())) {
            return new UsernamePasswordCredentialsProvider(gitParam.getGitUser(), gitParam.getGitPassword());
        }
        if (GitAccessType.ACCESS_TOKEN.equals(gitParam.getGitAccessType())) {
            return new UsernamePasswordCredentialsProvider(ConstantVar.GIT_PRIVATE_TOKEN_USER, gitParam.getGitAccessToken());
        }
        return null;
    }


    /**
     * getSshSessionFactory
     *
     * @param gitParam
     * @return
     */
    public static JschConfigSessionFactory getSshSessionFactory(GitParam gitParam) {
        if (!GitAccessType.SSH_KEY.equals(gitParam.getGitAccessType())) {
            return null;
        }
        JschConfigSessionFactory sshSessionFactory = new JschConfigSessionFactory() {
            @Override
            protected void configure(OpenSshConfig.Host host, Session session) {
                //nothing
            }

            @Override
            protected JSch createDefaultJSch(FS fs) throws JSchException {
                JSch jsch = super.createDefaultJSch(fs);
                jsch.setKnownHosts(gitParam.getSshKnownHosts());
                jsch.addIdentity(gitParam.getSshPrivateKeyPath());
                return jsch;
            }
        };
        return sshSessionFactory;
    }
}