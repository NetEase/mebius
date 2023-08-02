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
package com.netease.mebius.client.action;

import com.netease.mebius.client.executor.CovMergeExecutor;
import com.netease.mebius.client.executor.CovOperateExecutor;

/**
 * Jacoco覆盖率操作
 */
public class CoverageOperate {

    /**
     * 远程清空jacoco覆盖率操作
     *
     * @param ip   目标应用所在ip
     * @param port 目标应用的jacoco端口
     * @return
     */
    public static boolean reset(String ip, Integer port) {
        return CovOperateExecutor.reset(ip, port);
    }


    /**
     * 远程dump jacoco exec文件
     *
     * @param ip           目标应用所在ip
     * @param port         目标应用的port
     * @param execFilePath 目标exec文件存放路径
     * @return
     */
    public static boolean dump(String ip, Integer port, String execFilePath) {
        return CovOperateExecutor.dump(ip, port, execFilePath);
    }


    /**
     * exec文件合并
     * 1、用于多台应用实例覆盖率需要合并的场景
     * 2、用于历史和新的覆盖率数据合并场景
     *
     * @param execPath 需要合并exec所在路径
     * @param destFile 合并后目标的exec文件名
     * @param ip       应用ip（exec文件名中带有相同ip标识的会做合并操作，为空则不指定）
     * @return
     */
    public static boolean merge(String execPath, String destFile, String ip) {
        CovMergeExecutor covMergeExecutor = new CovMergeExecutor(execPath, destFile);
        return covMergeExecutor.executeMerge(ip);
    }
}