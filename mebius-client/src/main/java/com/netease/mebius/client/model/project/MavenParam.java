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
package com.netease.mebius.client.model.project;

import lombok.Data;
import lombok.ToString;

/**
 * maven构建参数
 */
@Data
@ToString
public class MavenParam {

    /**
     * 必填：maven所在安装路径（如：/home/maven）
     */
    private String mavenPath;

    /**
     * 必填：工程pom路径
     */
    private String pomPath;

    /**
     * 可选：执行的编译命令（如:compile、install、package）
     */
    private String command;

    /**
     * 可选：javaHome路径
     */
    private String javaHome;

    /**
     * 可选：mvn输入日志的路径（如/home/logs/maven/xxxx.log）
     */
    private String logPath;

    /**
     * 可选：环境变量值
     */
    private String profile;

    /**
     * 可选：完整的maven命令（输入后，command字段失效）
     */
    private String completeCmd;

    /**
     * 项目根目录
     */
    private String projectBaseDir;
}