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
package com.netease.mebius.client.executor.parser;

import com.netease.mebius.client.model.UrlMappingInfo;
import com.netease.mebius.client.model.CallMethodNode;
import com.netease.mebius.client.model.MethodCallResult;

/**
 * 注解解析器，可自行实现需要自定义的注解
 */
public interface AnnotationParser {

    /**
     * 解析
     *
     * @param resultCallMethod
     * @param callMethodNode
     */
    void parse(MethodCallResult resultCallMethod, CallMethodNode callMethodNode);

    /**
     * 解析项目所有url和method
     *
     * @param urlMappingInfo
     * @param callMethodNode
     */
    void parseUrl(UrlMappingInfo urlMappingInfo, CallMethodNode callMethodNode);
}