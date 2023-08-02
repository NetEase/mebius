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
package com.netease.mebius.client.model.coverage;

import lombok.Data;

import java.io.File;

/**
 * 覆盖率报告对比参数
 */
@Data
public class ReportDiffParam {

    /**
     * 基准的exec文件
     */
    private File baseExecFile;

    /**
     * 对比的exec文件
     */
    private File compareExecFile;

    public ReportDiffParam(String baseExecFile, String compareExecFile) {
        this.baseExecFile = new File(baseExecFile);
        this.compareExecFile = new File(compareExecFile);
    }
}