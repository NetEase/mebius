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
package com.netease.mebius.client.executor;

import com.netease.mebius.client.exception.MebiusException;
import com.netease.mebius.client.model.project.MavenParam;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.apache.maven.shared.invoker.DefaultInvocationRequest;
import org.apache.maven.shared.invoker.DefaultInvoker;
import org.apache.maven.shared.invoker.InvocationRequest;
import org.apache.maven.shared.invoker.InvocationResult;
import org.apache.maven.shared.invoker.Invoker;
import org.apache.maven.shared.invoker.MavenInvocationException;
import org.assertj.core.util.Lists;

import java.io.File;
import java.util.Collections;
import java.util.List;

/**
 * maven编译器
 */
@Slf4j
public class MavenExecutor {

    private String mavenPath;

    /**
     * @param mavenPath
     */
    public MavenExecutor(String mavenPath) {
        this.mavenPath = mavenPath;
    }

    /**
     * maven命令执行
     *
     * @param mavenParam
     * @return
     */
    public boolean invoker(MavenParam mavenParam) throws MebiusException {

        mavenParam.setCommand(genCommand(mavenParam));
        boolean result = false;
        log.debug("Maven invoker param: {}", mavenParam);
        File pomFile = new File(mavenParam.getPomPath());
        if (pomFile == null || !pomFile.exists()) {
            log.error("Pom file is not exits: {}", pomFile);
            return false;
        }
        try {
            log.debug("Maven invoker, begin: {}", mavenParam.getPomPath());
            InvocationRequest request = new DefaultInvocationRequest();
            request.setPomFile(pomFile);
            request.setGoals(Collections.singletonList(mavenParam.getCommand()));
            if (StringUtils.isNotBlank(mavenParam.getJavaHome())) {
                request.setJavaHome(new File(mavenParam.getJavaHome()));
            }
            if (StringUtils.isNotBlank(mavenParam.getProfile())) {
                List<String> profiles = Lists.newArrayList();
                profiles.add(mavenParam.getProfile());
                request.setProfiles(profiles);
            }
            Invoker invoker = new DefaultInvoker();
            invoker.setMavenHome(new File(mavenPath));
            InvocationResult invocationResult = invoker.execute(request);
            if (invocationResult.getExitCode() == 0) {
                result = true;
            }
        } catch (MavenInvocationException e) {
            log.error(e.getMessage(), e);
            throw new MebiusException(e.getMessage());
        }
        log.debug("Maven invoker, end: {}, {}", mavenParam.getPomPath(), result);
        return result;
    }

    /**
     * 执行test
     * @param mavenParam
     * @return
     */
    public boolean runTest(MavenParam mavenParam) {
        mavenParam.setCommand(genTestCommand(mavenParam));
        boolean result = false;
        log.debug("Maven run test param: {}", mavenParam);
        try {
            log.debug("Maven run test, begin: {}", mavenParam.getProjectBaseDir());
            InvocationRequest request = new DefaultInvocationRequest();
            request.setGoals(Collections.singletonList(mavenParam.getCommand()));
            request.setBaseDirectory(new File(mavenParam.getProjectBaseDir()));
            if (StringUtils.isNotBlank(mavenParam.getJavaHome())) {
                request.setJavaHome(new File(mavenParam.getJavaHome()));
            }
            if (StringUtils.isNotBlank(mavenParam.getProfile())) {
                List<String> profiles = Lists.newArrayList();
                profiles.add(mavenParam.getProfile());
                request.setProfiles(profiles);
            }
            Invoker invoker = new DefaultInvoker();
            invoker.setMavenHome(new File(mavenPath));
            InvocationResult invocationResult = invoker.execute(request);
            if (invocationResult.getExitCode() == 0) {
                result = true;
            }
        } catch (MavenInvocationException e) {
            log.error(e.getMessage(), e);
        }
        log.debug("Maven run test, end: {}, {}", mavenParam.getProjectBaseDir(), result);
        return result;
    }


    /**
     * @param mavenParam
     * @return
     */
    private String genCommand(MavenParam mavenParam) {

        String allCommand = mavenParam.getCompleteCmd();
        if (StringUtils.isNotBlank(mavenParam.getCommand())) {
            allCommand = "clean " + mavenParam.getCommand() + " -T 1C -Dmaven.test.skip=true -Dmaven.compile.fork=true";
        }
        if (StringUtils.isNotBlank(mavenParam.getLogPath())) {
            allCommand = allCommand + " -l " + mavenParam.getLogPath();
        }
        return allCommand;
    }

    /**
     * @param mavenParam
     * @return
     */
    private String genTestCommand(MavenParam mavenParam) {
        String allCommand = mavenParam.getCompleteCmd();
        if (StringUtils.isNotBlank(mavenParam.getLogPath())) {
            allCommand = allCommand + " -l " + mavenParam.getLogPath();
        }
        return allCommand;
    }
}