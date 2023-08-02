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

import org.eclipse.jdt.core.dom.CompilationUnit;
import org.jacoco.core.internal.diff.ClassInfo;

import java.util.List;

/**
 * class info
 */
public class AccClassInfo extends ClassInfo {

    /**
     * 文件路径
     */
    private String filePath;

    private CompilationUnit compilationUnit;

    /**
     * imports 依赖
     */
    private List<AccImportInfo> imports;

    public String getFilePath() {
        return filePath;
    }

    public void setFilePath(String filePath) {
        this.filePath = filePath;
    }

    public CompilationUnit getCompilationUnit() {
        return compilationUnit;
    }

    public void setCompilationUnit(CompilationUnit compilationUnit) {
        this.compilationUnit = compilationUnit;
    }

    public List<AccImportInfo> getImports() {
        return imports;
    }

    public void setImports(List<AccImportInfo> imports) {
        this.imports = imports;
    }
}
