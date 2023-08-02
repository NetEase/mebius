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
package com.netease.mebius.client.utils;

import com.netease.mebius.client.enums.Annotation;
import org.apache.commons.collections.CollectionUtils;
import org.assertj.core.util.Lists;

import java.util.List;

/**
 * 注解工具类
 */
public class AnnotationUtils {

    /**
     * 初始化需要的筛选的类注解
     *
     * @param annotations
     * @return
     */
    public static List<String> init(List<String> annotations) {
        if (CollectionUtils.isEmpty(annotations)) {
            //暂时只加了接口
            annotations = Lists.newArrayList();
            annotations.add(Annotation.Controller.name());
            annotations.add(Annotation.RestController.name());
            annotations.add(Annotation.FeignClient.name());
            annotations.add(Annotation.Service.name());
            annotations.add(Annotation.Component.name());
            annotations.add(Annotation.RpcService.name());
            annotations.add(Annotation.RemoteMethod.name());
        }
        return annotations;
    }
}