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

 import com.netease.mebius.client.enums.ClassType;
 import lombok.Data;

 import java.util.List;

/**
 * 方法调用结果
 */
@Data
public class MethodCallResult {

    /**
     * 注解
     */
    private String annotation;

    /**
     * class类型
     */
    private ClassType classType;

    /**
     * 原类名
     */
    private String className;

    /**
     * 原方法名
     */
    private String method;

    /**
     * 原方法sig
     */
    private String methodSig;

    /**
     * 原方法修饰符
     */
    private String modifier;

    /**
     * 最顶层方法
     */
    private String topMethod;

    /**
     * 最顶层方法sig
     */
    private String topMethodSig;

    /**
     * 最顶层类
     */
    private String topClassName;

    /**
     * 最顶层方法修饰符
     */
    private String topMethodModifier;

    /**
     * 原方法是否为新增方法
     */
    private Boolean isNew;

    /**
     * 最顶层方法是否为新增方法
     */
    private Boolean topIsNew;

    /**
     * 顶层入口
     */
    private String topEntry;

    /**
     * http请求方式
     */
    private String httpMethod;

    /**
     * 方法调用链路
     */
    private List<CallRelation> callRelationList;

}