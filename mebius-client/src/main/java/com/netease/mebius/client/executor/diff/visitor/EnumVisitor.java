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
package com.netease.mebius.client.executor.diff.visitor;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.StringUtils;
import org.eclipse.jdt.core.dom.ASTVisitor;
import org.eclipse.jdt.core.dom.EnumConstantDeclaration;
import org.eclipse.jdt.core.dom.EnumDeclaration;

import java.util.ArrayList;
import java.util.List;

/**
 * 枚举访问器
 */
public class EnumVisitor extends ASTVisitor {

    private final List<String> enumValues = new ArrayList<>();

    @Override
    public boolean visit(EnumDeclaration node) {
        for (Object o : node.enumConstants()) {
            if (o instanceof EnumConstantDeclaration) {
                EnumConstantDeclaration enumConstant = (EnumConstantDeclaration) o;
                String fieldName = enumConstant.getName().getIdentifier();
                List<String> fieldValues = new ArrayList<>();
                if (CollectionUtils.isNotEmpty(enumConstant.arguments())) {
                    for (Object argument : enumConstant.arguments()) {
                        fieldValues.add(argument.toString());
                    }
                }
                enumValues.add(fieldName + "(" + StringUtils.join(fieldValues, ",") + ")");
            }
        }
        return super.visit(node);
    }

    public List<String> getEnumValues() {
        return enumValues;
    }
}