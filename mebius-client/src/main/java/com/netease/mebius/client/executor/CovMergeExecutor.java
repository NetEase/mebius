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

import com.netease.mebius.client.constant.ConstantVar;
import com.netease.mebius.client.utils.FileUtils;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.jacoco.core.tools.ExecFileLoader;

import java.io.File;
import java.io.IOException;
import java.util.List;

/**
 * 覆盖率合并执行器
 */
@Slf4j
public class CovMergeExecutor {

    private final String path; //exec所在路径
    private final File destFile; //目标合并的exec文件

    public CovMergeExecutor(String path, String destFileName) {
        this.path = path;
        this.destFile = new File(path + ConstantVar.FILE_SEPARATOR + destFileName);
    }

    /**
     * 执行merge
     *
     * @throws
     */
    public boolean executeMerge(String ip) {
        final ExecFileLoader loader = new ExecFileLoader();
        try {
            load(loader, ip);
            save(loader);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * 加载dump文件
     *
     * @param loader
     * @throws
     */
    private void load(final ExecFileLoader loader, String ip) throws IOException {
        int i = 0;
        boolean isContainsMainExec = false;
        List<File> jacocoFiles = FileUtils.fileSets(this.path);
        for (File file : jacocoFiles) {
            if (StringUtils.contains(file.getName(), "jacoco.exec")) {
                isContainsMainExec = true;
            }
            if (StringUtils.contains(file.getName(), "his")){
                loader.getExecutionDataStore().setHasHisExecution(true);
            }
        }
        for (final File fileSet : jacocoFiles) {
            final File inputFile = new File(this.path, fileSet.getName());
            if (inputFile.isDirectory()) {
                continue;
            }
            //非本ip的不合并
            if (StringUtils.isNotBlank(ip) && !inputFile.getName().contains(ip)) {
                continue;
            }
            try {
                log.debug("Loading execution data file :{}, file size :{}", inputFile.getAbsolutePath(), FileUtils.getFileSize(inputFile));
                if (i != 0 && isContainsMainExec) {
                    log.debug("Set main execution is false: {}", fileSet.getAbsolutePath());
                    loader.getExecutionDataStore().setMainExecution(false);
                }
                loader.load(inputFile);
                i++;
            } catch (IOException e) {
                log.error(e.getMessage(), e);
                throw new IOException("Unable to read " + inputFile.getAbsolutePath(), e);
            }
        }
    }

    /**
     * 执行合并文件和保存
     *
     * @param loader
     * @throws IOException
     */
    private void save(final ExecFileLoader loader) throws IOException {
 /*       if (loader.getExecutionDataStore().getContents().isEmpty()) {
            log.debug("Skipping JaCoCo merge execution due to missing execution data files");
            return;
        }*/
        try {
            loader.save(this.destFile, true);
            log.debug("Writing merged execution data to {}, file size:{}", this.destFile.getAbsolutePath(), FileUtils.getFileSize(this.destFile));
        } catch (IOException e) {
            log.error(e.getMessage(), e);
            throw new IOException("Unable to write merged file " + this.destFile.getAbsolutePath(), e);
        }
    }


}