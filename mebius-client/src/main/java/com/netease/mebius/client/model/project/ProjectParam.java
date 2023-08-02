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

 import com.netease.mebius.client.enums.ExecType;
 import com.netease.mebius.client.model.ServletMapping;
 import com.netease.mebius.client.model.coverage.MethodCoverage;
 import lombok.Data;
 import lombok.ToString;
 import org.assertj.core.util.Lists;

 import java.util.List;

/**
 * 项目执行参数
 */
@Data
@ToString
public class ProjectParam {

    /**
     * 执行类型
     */
    private ExecType execType;

    /**
     * 项目根目录路径
     */
    private String projectRootPath;

    /**
     * 当前分支
     */
    private String currentBranch;

    /**
     * 对比分支
     */
    private String compareBranch;

    /**
     * 当前commit版本
     */
    private String currentCommit;

    /**
     * 对比commit版本
     */
    private String compareCommit;

    /**
     * 当前tag
     */
    private String currentTag;

    /**
     * 对比tag
     */
    private String compareTag;

    /**
     * Git参数
     */
    private GitParam gitParam = new GitParam();

    /**
     * 需要覆盖率排除的包名,支持如：
     * 过滤整个包:com.netease.xxx.abc.*
     * 过滤单个文件:com.netease.xxx.abc.main
     * 模糊匹配包:com.netease.xxx.abc.*Test*.*
     * 模糊匹配文件:com.netease.xxx.abc.*Test*
     */
    private List<String> excludes = Lists.newArrayList();

    /**
     * 需要覆盖率排除的方法名
     */
    private List<MethodCoverage> excludesMethod = Lists.newArrayList();

    /**
     * 排除的子工程包（为空则不排除）
     */
    private List<String> excludeSubPkg = Lists.newArrayList();

    /**
     * 关联的子工程包（为空则工程下所有目录参与分析）
     */
    private List<String> relationSubPkg = Lists.newArrayList();

    /**
     * 工程代码是否需要更新
     */
    private Boolean needUpdate = true;

    /**
     * servlet映射
     */
    private List<ServletMapping> servletMappings;

    /**
     * constructor method
     */
    public ProjectParam() {

    }

    /**
     * constructor method
     *
     * @param projectRootPath
     */
    public ProjectParam(String projectRootPath) {
        this.projectRootPath = projectRootPath;
    }

}
