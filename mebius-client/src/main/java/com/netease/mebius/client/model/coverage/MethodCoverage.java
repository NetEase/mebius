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

import java.math.BigDecimal;
import java.util.List;

/**
 * 方法覆盖率
 */
@Data
public class MethodCoverage {

    /**
     * 所在包名（入参使用）
     */
    private String pkgName;

    /**
     * 所在类名（入参使用）
     */
    private String className;

    /**
     * 方法名（入参使用）
     */
    private String methodName;

    /**
     * 参数（入参使用）
     */
    private List<String> params;

    /**
     * 覆盖行数
     */
    private int coveredCount;

    /**
     * 未覆盖行数
     */
    private int missCount;

    /**
     * 总行数
     */
    private int totalCount;

    /**
     * 行覆盖率
     */
    private BigDecimal covLinesRatio;

    /**
     * 覆盖率报告（预留，给上层使用方使用）
     */
    private String reportUrl;

    /**
     * 方法所在起始行号
     */
    private int firstLine;
}
