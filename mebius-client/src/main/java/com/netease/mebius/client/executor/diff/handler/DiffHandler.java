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
package com.netease.mebius.client.executor.diff.handler;

import com.netease.mebius.client.exception.MebiusException;
import com.netease.mebius.client.executor.diff.GitAdapter;
import com.netease.mebius.client.model.project.PreDiffParam;
import com.netease.mebius.client.model.project.ProjectParam;
import org.eclipse.jgit.diff.DiffEntry;
import org.jacoco.core.internal.diff.ClassInfo;

import java.io.IOException;
import java.util.List;
import java.util.Map;

/**
 * Diff处理接口类
 */
public interface DiffHandler {

    /**
     * diff执行
     *
     * @param param
     * @param excludes
     * @return
     */
    List<ClassInfo> diff(ProjectParam param, Map<String, List<String>> excludes) throws Exception;


    /**
     * @param gitAdapter
     * @param param
     * @param diffEntry
     * @param excludes
     * @return
     */
    ClassInfo parseClassInfo(GitAdapter gitAdapter, PreDiffParam param, DiffEntry diffEntry, Map<String, List<String>> excludes) throws MebiusException, IOException;

}