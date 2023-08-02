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

import com.netease.mebius.client.utils.FileUtils;
import lombok.extern.slf4j.Slf4j;
import org.jacoco.core.tools.ExecDumpClient;
import org.jacoco.core.tools.ExecFileLoader;

import java.io.File;

/**
 * 覆盖率操作执行器
 */
@Slf4j
public class CovOperateExecutor {

    /**
     * 远程清空覆盖率操作
     *
     * @param ip
     * @param port
     */
    public static boolean reset(String ip, int port) {
        log.debug("Jacoco reset begin: {}:{}", ip, port);
        try {
            ExecDumpClient client = new ExecDumpClient();
            client.setReset(true);
            client.setDump(false);
            ExecFileLoader execFileLoader = client.dump(ip, port);
            if (execFileLoader == null || execFileLoader.getExecutionDataStore() == null || !execFileLoader.getExecutionDataStore().getContents().isEmpty()) {
                log.error("Jacoco reset fail, execFileLoader is null.");
                return false;
            }
            log.debug("Jacoco reset end: {}:{}", ip, port);
            return true;
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            return false;
        }
    }


    /**
     * 远程dump exec文件
     *
     * @param ip
     * @param port
     * @param execFilePath
     */
    public static boolean dump(String ip, int port, String execFilePath) {

        log.debug("Jacoco dump exec begin: {}:{}", ip, port);
        try {
            ExecDumpClient client = new ExecDumpClient();
            client.setDump(true);
            ExecFileLoader execFileLoader = client.dump(ip, port);
            File file = new File(execFilePath);
            File fileParent = file.getParentFile();
            if (!fileParent.exists()) {
                fileParent.mkdirs();
            }
            //每次删除源文件并生成新文件
            if (file.exists()) {
                file.delete();
            }
            file.createNewFile();
            execFileLoader.save(file, true);

            if (file.length() == 0) {
                log.error("Jacoco dump fail, file is empty.");
                return false;
            }
            log.debug("Jacoco dump exec end: {}:{} ,file size:{}", ip, port, FileUtils.getFileSize(file));
            return true;
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            return false;
        }
    }
}