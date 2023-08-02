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

import com.netease.mebius.client.enums.ReportType;
import lombok.Data;

import java.io.File;
import java.util.List;

/**
 * 覆盖率报告参数
 */
@Data
public class ReportParam {

    /**
     * 报告标题
     */
    private String title;

    /**
     * exec文件
     */
    private File execDataFile;

    /**
     * 报告存放目录
     */
    private File reportDirectory;

    /**
     * 报告类型(为空默认为HTML)
     */
    private ReportType reportType;

    /**
     * 被分析的项目列表
     */
    private List<ProjectFile> projectList;

    /**
     * constructor method
     *
     * @param title
     * @param execDataPath
     * @param reportDirectoryPath
     * @param reportType
     */
    public ReportParam(String title, String execDataPath, String reportDirectoryPath, ReportType reportType, List<ProjectFile> projectList) {
        this.title = title;
        this.execDataFile = new File(execDataPath);
        this.reportDirectory = new File(reportDirectoryPath);
        this.projectList = projectList;
        if (reportType == null) {
            this.reportType = ReportType.HTML;
        } else {
            this.reportType = reportType;
        }
    }

    public ReportParam() {

    }
}