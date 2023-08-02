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
 * 工程文件
 */
@Data
public class ProjectFile {

    /**
     * classes文件
     */
    private File classesDirectory;

    /**
     * 源码文件
     */
    private File sourceDirectory;

    /**
     * constructor method
     *
     * @param projectBasePath 项目根目录
     * @param classesPath 项目class文件
     * @param srcPath 项目源码文件
     */
    public ProjectFile(String projectBasePath, String classesPath, String srcPath) {
        File projectDirectory = new File(projectBasePath);
        this.classesDirectory = new File(projectDirectory, classesPath);
        this.sourceDirectory = new File(projectDirectory, srcPath);
    }

    public ProjectFile(){

    }
}