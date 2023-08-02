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

import com.netease.mebius.client.constant.ConstantVar;
import com.netease.mebius.client.enums.Annotation;
import com.netease.mebius.client.enums.ClassType;
import com.netease.mebius.client.model.CallMethodNode;
import com.netease.mebius.client.model.MethodCallResult;
import com.netease.mebius.client.model.UrlMappingInfo;
import com.netease.mebius.client.utils.ParseUtils;
import jdk.internal.org.objectweb.asm.tree.AnnotationNode;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.assertj.core.util.Lists;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Controller解析器
 */
@Slf4j
public class ControllerParser implements AnnotationParser {


    /**
     * 接口解析
     *
     * @param resultCallMethod
     * @param callMethodNode
     */
    @Override
    public void parse(MethodCallResult resultCallMethod, CallMethodNode callMethodNode) {

        if (!StringUtils.equals(resultCallMethod.getAnnotation(), Annotation.Controller.name()) &&
                !StringUtils.equals(resultCallMethod.getAnnotation(), Annotation.RestController.name()) &&
                !StringUtils.equals(resultCallMethod.getAnnotation(), Annotation.FeignClient.name())) {
            return;
        }
        //先从class注解中获取根mapping路径
        ArrayList<String> baseRequestMappingList = Lists.newArrayList();
        for (AnnotationNode node : callMethodNode.getClassNode().visibleAnnotations) {
            if ((StringUtils.equals(ParseUtils.getSimpleName(node.desc), Annotation.RequestMapping.name()) ||
                    StringUtils.equals(ParseUtils.getSimpleName(node.desc), Annotation.FeignClient.name())) && node.values != null) {
                for (int i = 0; i < node.values.size(); i++) {
                    if (node.values.get(i).equals("value") || node.values.get(i).equals("path")) {
                        if (node.values.get(i + 1) instanceof String) {
                            String baseRequestMapping = node.values.get(i + 1).toString();
                            if (baseRequestMapping.endsWith("/")) {
                                baseRequestMapping = baseRequestMapping.substring(0, baseRequestMapping.length() - 1);
                                baseRequestMappingList.add(baseRequestMapping);
                            } else
                                baseRequestMappingList.add(baseRequestMapping);
                        } else {
                            ArrayList<String> oldBaseRequestMappingList = (ArrayList) node.values.get(i + 1);
                            if (oldBaseRequestMappingList != null && oldBaseRequestMappingList.size() != 0) {
                                for (String baseRequestMapping : oldBaseRequestMappingList) {
                                    if (baseRequestMapping.endsWith("/")) {
                                        baseRequestMapping = baseRequestMapping.substring(0, baseRequestMapping.length() - 1);
                                        baseRequestMappingList.add(baseRequestMapping);
                                    } else
                                        baseRequestMappingList.add(baseRequestMapping);
                                }
                            }
                        }
                    }
                }
            }
        }

        //获取方法所有注解（主要获取有没@RequestBody）
        List<AnnotationNode> annotationNodes = callMethodNode.getMethodNode().visibleAnnotations;

        //再获取方法注解
        if (annotationNodes != null) {
            for (AnnotationNode annotationNode : annotationNodes) {

                if (!Arrays.asList(ConstantVar.INTERFACE_ANNOTATIONS).contains(ParseUtils.getSimpleName(annotationNode.desc))) {
                    continue;
                }

                ArrayList<String> mappingUrlList = Lists.newArrayList();

                if (annotationNode.values != null) {
                    for (int i = 0; i < ((ArrayList) annotationNode.values).size(); i++) {
                        if (((ArrayList) annotationNode.values).get(i).equals("value") || ((ArrayList) annotationNode.values).get(i).equals("path")) {
                            ArrayList<String> values = (ArrayList) annotationNode.values.get(i + 1);
                            if (values != null) {
                                for (String subMapping : values) {
                                    if (baseRequestMappingList != null && baseRequestMappingList.size() != 0) {
                                        for (String baseRequestMapping : baseRequestMappingList) {
                                            String mappingUrl = ParseUtils.standardMappingUrl(baseRequestMapping) + ParseUtils.standardMappingUrl(subMapping);
                                            mappingUrlList.add(mappingUrl);
                                        }
                                    } else {
                                        String mappingUrl = ParseUtils.standardMappingUrl(subMapping);
                                        mappingUrlList.add(mappingUrl);
                                    }
                                }
                            }
                        }
                    }
                }
                if (mappingUrlList.size() == 0) {
                    for (String baseRequestMapping : baseRequestMappingList) {
                        String mappingUrl = ParseUtils.standardMappingUrl(baseRequestMapping);
                        mappingUrlList.add(mappingUrl);
                    }
                }
                resultCallMethod.setTopEntry(mappingUrlList.stream().collect(Collectors.joining(",")));
                if (StringUtils.equals(ParseUtils.getSimpleName(annotationNode.desc), Annotation.PostMapping.name())) {
                    resultCallMethod.setHttpMethod("POST");
                }

                if (StringUtils.equals(ParseUtils.getSimpleName(annotationNode.desc), Annotation.GetMapping.name())) {
                    resultCallMethod.setHttpMethod("GET");
                }

                if (StringUtils.equals(ParseUtils.getSimpleName(annotationNode.desc), Annotation.RequestMapping.name())) {
                    resultCallMethod.setHttpMethod("GET");
                    for (AnnotationNode an : annotationNodes) {
                        if (StringUtils.equals(Annotation.ResponseBody.name(), ParseUtils.getSimpleName(an.desc))) {
                            resultCallMethod.setHttpMethod("POST");
                            break;
                        }
                    }
                }
                resultCallMethod.setClassType(ClassType.Controller);
            }
        }
    }

    @Override
    public void parseUrl(UrlMappingInfo urlMappingInfo, CallMethodNode callMethodNode) {

        if (!StringUtils.equals(urlMappingInfo.getAnnotation(), Annotation.Controller.name()) &&
                !StringUtils.equals(urlMappingInfo.getAnnotation(), Annotation.RestController.name()) &&
                !StringUtils.equals(urlMappingInfo.getAnnotation(), Annotation.FeignClient.name())) {
            return;
        }
        //先从class注解中获取根mapping路径
        String baseRequestMapping = "";
        for (AnnotationNode node : callMethodNode.getClassNode().visibleAnnotations) {
            if ((StringUtils.equals(ParseUtils.getSimpleName(node.desc), Annotation.RequestMapping.name()) ||
                    StringUtils.equals(ParseUtils.getSimpleName(node.desc), Annotation.FeignClient.name())) && node.values != null) {
                for (int i = 0; i < node.values.size(); i++) {
                    if (node.values.get(i).equals("value") || node.values.get(i).equals("path")) {
                        if (node.values.get(i + 1) instanceof String) {
                            baseRequestMapping = node.values.get(i + 1).toString();
                            if (baseRequestMapping.endsWith("/")) {
                                baseRequestMapping = baseRequestMapping.substring(0, baseRequestMapping.length() - 1);
                            }
                        } else {
                            ArrayList<String> values = (ArrayList) node.values.get(i + 1);
                            if (values != null) {
                                baseRequestMapping = values.get(0);
                                if (baseRequestMapping.endsWith("/")) {
                                    baseRequestMapping = baseRequestMapping.substring(0, baseRequestMapping.length() - 1);
                                }
                            }
                        }
                    }
                }
            }
        }

        //获取方法所有注解（主要获取有没@RequestBody）
        List<AnnotationNode> annotationNodes = callMethodNode.getMethodNode().visibleAnnotations;

        //再获取方法注解
        if (annotationNodes != null) {
            for (AnnotationNode annotationNode : annotationNodes) {
                if (!Arrays.asList(ConstantVar.INTERFACE_ANNOTATIONS).contains(ParseUtils.getSimpleName(annotationNode.desc))) {
                    continue;
                }
                String subMapping = "";
                for (int i = 0; i < ((ArrayList) annotationNode.values).size(); i++) {
                    if (((ArrayList) annotationNode.values).get(i).equals("value") || ((ArrayList) annotationNode.values).get(i).equals("path")) {
                        ArrayList<String> values = (ArrayList) annotationNode.values.get(i + 1);
                        subMapping = ParseUtils.standardMappingUrl(values.get(0));
                    }
                }
                urlMappingInfo.setMappingUrl(baseRequestMapping + subMapping);
            }
        }
    }
}