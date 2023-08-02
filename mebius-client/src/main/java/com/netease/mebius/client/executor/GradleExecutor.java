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
import com.netease.mebius.client.model.project.GradleParam;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.gradle.tooling.BuildLauncher;
import org.gradle.tooling.GradleConnector;
import org.gradle.tooling.ProjectConnection;

import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.net.URI;

/**
 * Gradle执行器
 */
@Slf4j
public class GradleExecutor {

    public GradleExecutor() {

    }

    /**
     * 执行gradle命令
     *
     * @param gradleParam
     * @return
     */
    public boolean invoker(GradleParam gradleParam) throws MebiusException {

        log.debug("Gradle invoker param: {}", gradleParam.toString());
        File project = new File(gradleParam.getProjectPath());
        if (project == null || !project.exists()) {
            log.error("Project is not exits: {}", gradleParam.getProjectPath());
            return false;
        }
        if (gradleParam.getTasks().length == 0) {
            log.error("Gradle task is null: {}", gradleParam.getTasks());
            return false;
        }

        boolean result = false;
        ProjectConnection connection = null;
        try {
            GradleConnector gradleConnector = GradleConnector.newConnector()
                    .forProjectDirectory(new File(gradleParam.getProjectPath()));
            if (StringUtils.isNotBlank(gradleParam.getGradleVersion())) {
                gradleConnector.useGradleVersion(gradleParam.getGradleVersion());
            }
            if (StringUtils.isNotBlank(gradleParam.getDistribution())) {
                gradleConnector.useDistribution(new URI(gradleParam.getDistribution()));
            }
            connection = gradleConnector.connect();
            BuildLauncher buildLauncher = connection.newBuild();
            buildLauncher.forTasks(gradleParam.getTasks());
            if (gradleParam.getArguments() != null && gradleParam.getArguments().length > 0) {
                buildLauncher.withArguments(gradleParam.getArguments());
            }
            if (StringUtils.isNotBlank(gradleParam.getJavaHome())) {
                buildLauncher.setJavaHome(new File(gradleParam.getJavaHome()));
            }
            OutputStream fos = new FileOutputStream(gradleParam.getLogPath());
            buildLauncher.setStandardOutput(fos);
            buildLauncher.setStandardError(fos);
            buildLauncher.setColorOutput(true);
            buildLauncher.run();
            result = true;
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            throw new MebiusException(e.getMessage());
        } finally {
            if (connection != null) {
                connection.close();
            }
        }
        return result;
    }

}