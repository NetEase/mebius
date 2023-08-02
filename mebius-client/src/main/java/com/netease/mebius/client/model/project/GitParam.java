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

import com.netease.mebius.client.enums.GitAccessType;
import lombok.Data;

/**
 * Git请求参数
 */
@Data
public class GitParam {

    /**
     * Git工程路径
     */
    private String gitRepoDir;

    /**
     * Git访问方式
     */
    private GitAccessType gitAccessType;

    /**
     * git用户名
     */
    private String gitUser;

    /**
     * git用户密码
     */
    private String gitPassword;

    /**
     * git access token
     */
    private String gitAccessToken;

    /**
     * ssh私钥文件路径
     */
    private String sshPrivateKeyPath;

    /**
     * ssh known_hosts文件
     */
    private String sshKnownHosts;


}