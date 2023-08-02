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

import com.netease.mebius.client.enums.Annotation;
import com.netease.mebius.client.enums.ClassType;
import com.netease.mebius.client.model.CallMethodNode;
import com.netease.mebius.client.model.MethodCallResult;
import com.netease.mebius.client.model.UrlMappingInfo;
import com.netease.mebius.client.utils.ParseUtils;
import jdk.internal.org.objectweb.asm.tree.AnnotationNode;
import org.apache.commons.lang.StringUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * RPC注解解析器
 */
public class RpcParser implements AnnotationParser {

    @Override
    public void parse(MethodCallResult resultCallMethod, CallMethodNode callMethodNode) {
        if (!StringUtils.equals(resultCallMethod.getAnnotation(), Annotation.RpcService.name()) &&
                !StringUtils.equals(resultCallMethod.getAnnotation(), Annotation.RemoteMethod.name())) {
            return;
        }

        for (AnnotationNode node : callMethodNode.getClassNode().visibleAnnotations) {
            //RPC类型
            if (StringUtils.equals(ParseUtils.getSimpleName(node.desc), Annotation.RemoteMethod.name())) {
                for (int i = 0; i < node.values.size(); i++) {
                    if (node.values.get(i).equals("mapping")) {
                        if (node.values.get(i + 1) instanceof ArrayList) {
                            AnnotationNode annotationNode = (AnnotationNode) ((ArrayList) node.values.get(i + 1)).get(0);
                            for (int j = 0; j < annotationNode.values.size(); j++) {
                                if (StringUtils.equals(annotationNode.values.get(j).toString(), "path")) {
                                    resultCallMethod.setClassType(ClassType.Rpc);
                                    resultCallMethod.setTopEntry(ParseUtils.parseYanxuanRpcUrl(annotationNode.values.get(j + 1).toString(), callMethodNode.getMethodNode().name));
                                    resultCallMethod.setHttpMethod("RPC");
                                    break;
                                }
                            }
                        }
                    }
                }
            }
            if (StringUtils.equals(ParseUtils.getSimpleName(node.desc), Annotation.RpcService.name())) {
                List<Object> values = node.values;
                for (int i = 0; i < values.size(); i++) {
                    if (StringUtils.equals(values.get(i).toString(), "value")) {
                        resultCallMethod.setClassType(ClassType.Rpc);
                        resultCallMethod.setTopEntry(ParseUtils.parseYanxuanRpcUrl(values.get(i + 1).toString(), callMethodNode.getMethodNode().name));
                        resultCallMethod.setHttpMethod("RPC");
                        break;
                    }
                }
            }
        }
    }

    @Override
    public void parseUrl(UrlMappingInfo urlMappingInfo, CallMethodNode callMethodNode) {

    }
}