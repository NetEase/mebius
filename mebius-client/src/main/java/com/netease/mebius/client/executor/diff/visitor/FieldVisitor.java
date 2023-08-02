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

import com.netease.mebius.client.model.FieldProperty;
import org.apache.commons.lang.StringUtils;
import org.eclipse.jdt.core.dom.ASTVisitor;
import org.eclipse.jdt.core.dom.AbstractTypeDeclaration;
import org.eclipse.jdt.core.dom.EnumDeclaration;
import org.eclipse.jdt.core.dom.FieldDeclaration;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.Type;
import org.eclipse.jdt.core.dom.TypeDeclaration;
import org.eclipse.jdt.core.dom.VariableDeclarationFragment;

import java.util.ArrayList;
import java.util.List;

/**
 * 字段访问器
 */
public class FieldVisitor extends ASTVisitor {

    private final List<FieldProperty> fieldList = new ArrayList<>();

    @Override
    public boolean visit(FieldDeclaration node) {
        Type type = node.getType();
        for (Object o : node.fragments()) {
            if (o instanceof VariableDeclarationFragment) {
                VariableDeclarationFragment fragment = (VariableDeclarationFragment) o;
                String fieldName = fragment.getName().getIdentifier();
                FieldProperty fieldProperty = new FieldProperty();
                fieldProperty.setType(type.toString());
                fieldProperty.setName(fieldName);

                AbstractTypeDeclaration enclosingClass;
                if (node.getParent() instanceof EnumDeclaration) {
                    enclosingClass = (EnumDeclaration) node.getParent();
                } else {
                    enclosingClass = (TypeDeclaration) node.getParent();
                }

                for (Object object : enclosingClass.modifiers()) {
                    if (object != null && StringUtils.equals(object.toString(), "@Data")) {
                        fieldProperty.setHasGetter(true);
                        fieldProperty.setHasSetter(true);
                        break;
                    }
                    if (object != null && StringUtils.equals(object.toString(), "@Getter")) {
                        fieldProperty.setHasGetter(true);
                    }
                    if (object != null && StringUtils.equals(object.toString(), "@Setter")) {
                        fieldProperty.setHasSetter(true);
                    }
                }
                enclosingClass.accept(new ASTVisitor() {
                    @Override
                    public boolean visit(MethodDeclaration node) {

                        if (node.getName().getIdentifier().equalsIgnoreCase("get" + fieldName)) {
                            fieldProperty.setHasGetter(true);
                        }
                        if (node.getName().getIdentifier().equalsIgnoreCase("set" + fieldName)) {
                            fieldProperty.setHasSetter(true);
                        }
                        return super.visit(node);
                    }
                });
                fieldList.add(fieldProperty);
            }
        }
        return super.visit(node);
    }

    public List<FieldProperty> getFieldList() {
        return fieldList;
    }
}