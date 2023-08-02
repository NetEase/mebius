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
 * 变更方法信息
 */
@Data
public class ChangeMethodInfo {

    /**
     * 方法名
     */
    private String methodName;

    /**
     * 方法sig
     */
    private String methodSig;

    /**
     * 参数
     */
    private String parameters;

    /**
     * 返回类型
     */
    private String returnType;

    /**
     * 修饰符
     */
    private String modifier;

    /**
     * 复杂度
     */
    private int complexity;

    /**
     * 起始行号
     */
    private int startLine;

    /**
     * 结束行号
     */
    private int endLine;

    /**
     * 是否是新增方法
     */
    private boolean isNew;

    /**
     * 父节点列表
     */
    private List<CallMethodNode> parentNodes = Lists.newArrayList();

}