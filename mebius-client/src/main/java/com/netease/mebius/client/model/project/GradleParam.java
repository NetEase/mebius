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
 * Gradle编译请求参数
 */
@Data
@ToString
public class GradleParam {

    /**
     * 必填：工程所在路径
     */
    private String projectPath;

    /**
     * 必填：编译命令（如compileJava）
     */
    private String[] tasks;

    /**
     * 可选：自定义参数
     */
    private String[] arguments;

    /**
     * 可选：输出的日志路径
     */
    private String logPath;

    /**
     * 可选：gradle版本（如5.4.1）
     */
    private String gradleVersion;

    /**
     * 可选：javaHome
     */
    private String javaHome;

    /**
     * 可选：distribution url
     */
    private String distribution;
}