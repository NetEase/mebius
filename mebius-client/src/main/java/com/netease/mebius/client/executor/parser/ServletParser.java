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

 import com.alibaba.fastjson.JSON;
 import com.alibaba.fastjson.JSONArray;
 import com.netease.mebius.client.enums.Annotation;
 import com.netease.mebius.client.enums.ClassType;
 import com.netease.mebius.client.model.CallMethodNode;
 import com.netease.mebius.client.model.MethodCallResult;
 import com.netease.mebius.client.model.ServletMapping;
 import com.netease.mebius.client.model.UrlMappingInfo;
 import lombok.extern.slf4j.Slf4j;
 import org.apache.commons.lang.StringUtils;

 import java.util.List;

 /**
  * Servlet注解解析器
  */
 @Slf4j
 public class ServletParser implements AnnotationParser {

     @Override
     public void parse(MethodCallResult resultCallMethod, CallMethodNode callMethodNode) {
         if (!StringUtils.equals(resultCallMethod.getAnnotation(), Annotation.Servlet.name())) {
             return;
         }
         if (callMethodNode.getExtend() instanceof List) {
             List<ServletMapping> servletMappings = JSONArray.parseArray(JSON.toJSONString(callMethodNode.getExtend()),
                     ServletMapping.class);
             for (ServletMapping servletMapping : servletMappings) {
                 if (StringUtils.equals(servletMapping.getServletName(),
                         StringUtils.substringAfterLast(resultCallMethod.getTopClassName(), "."))) {
                     resultCallMethod.setClassType(ClassType.Servlet);
                     resultCallMethod.setTopEntry(servletMapping.getUrl());
                     if (StringUtils.equals(resultCallMethod.getTopMethodSig().split("#")[0], "doPost")) {
                         resultCallMethod.setHttpMethod("POST");
                     }
                     if (StringUtils.equals(resultCallMethod.getTopMethodSig().split("#")[0], "doGet")) {
                         resultCallMethod.setHttpMethod("GET");
                     }
                 }
             }
         }
     }

     @Override
     public void parseUrl(UrlMappingInfo urlMappingInfo, CallMethodNode callMethodNode) {

     }
 }