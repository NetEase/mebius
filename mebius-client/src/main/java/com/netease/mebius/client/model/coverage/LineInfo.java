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
import org.jacoco.core.analysis.ICounter;

/**
 * 代码行覆盖率信息
 */
@Data
public class LineInfo {

    private int index;

    private int classIndex;

    private ICounter instructionCounter;

    private ICounter branchCounter;

    public LineInfo(int index, int classIndex, ICounter instructionCounter, ICounter branchCounter) {
        this.index = index;
        this.classIndex = classIndex;
        this.instructionCounter = instructionCounter;
        this.branchCounter = branchCounter;
    }

    public LineInfo() {

    }
}