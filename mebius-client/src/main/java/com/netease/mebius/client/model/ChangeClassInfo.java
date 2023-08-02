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
package com.netease.mebius.client.model;

import lombok.Data;
import org.assertj.core.util.Lists;

import java.util.List;

/**
 * 变更类信息
 */
@Data
public class ChangeClassInfo {

    /**
     * 文件路径
     */
    private String filePath;

    /**
     * 类名
     */
    private String className;

    /**
     * 包名
     */
    private String packageName;

    /**
     * 是否新增
     */
    private boolean isNew;

    /**
     * 新增行信息
     */
    private List<int[]> addLines = Lists.newArrayList();

    /**
     * 删除行信息
     */
    private List<int[]> delLines = Lists.newArrayList();

    /**
     * 新增行数量
     */
    private int addLineCount;

    /**
     * 删除行数量
     */
    private int delLineCount;

    /**
     * 本次变更方法信息
     */
    private List<ChangeMethodInfo> changeMethods = Lists.newArrayList();
}