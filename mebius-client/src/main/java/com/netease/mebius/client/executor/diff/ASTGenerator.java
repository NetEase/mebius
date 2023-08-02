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
 package com.netease.mebius.client.executor.diff;

 import com.netease.mebius.client.executor.diff.visitor.EnumVisitor;
 import com.netease.mebius.client.executor.diff.visitor.FieldVisitor;
 import com.netease.mebius.client.model.AccClassInfo;
 import com.netease.mebius.client.model.AccImportInfo;
 import com.netease.mebius.client.model.FieldProperty;
 import lombok.Data;
 import org.eclipse.jdt.core.JavaCore;
 import org.eclipse.jdt.core.dom.ASTParser;
 import org.eclipse.jdt.core.dom.AbstractTypeDeclaration;
 import org.eclipse.jdt.core.dom.CompilationUnit;
 import org.eclipse.jdt.core.dom.ImportDeclaration;
 import org.eclipse.jdt.core.dom.MethodDeclaration;
 import org.eclipse.jdt.core.dom.PackageDeclaration;
 import org.eclipse.jdt.core.dom.QualifiedName;
 import org.eclipse.jdt.core.dom.TypeDeclaration;
 import org.jacoco.core.internal.diff.ClassInfo;
 import org.jacoco.core.internal.diff.MethodInfo;

 import java.io.UnsupportedEncodingException;
 import java.security.MessageDigest;
 import java.security.NoSuchAlgorithmException;
 import java.util.ArrayList;
 import java.util.Base64;
 import java.util.List;
 import java.util.Map;
 import java.util.function.Function;
 import java.util.stream.Collectors;

 /**
  * AST编译java源文件
  */
 @Data
 public class ASTGenerator {

     private String javaText;
     private CompilationUnit compilationUnit;

     public ASTGenerator(String javaText) {
         this.javaText = javaText;
         this.initCompilationUnit();
     }

     /**
      * 获取AST编译单元,首次加载很慢
      */
     private void initCompilationUnit() {
         //  AST编译
         final ASTParser astParser = ASTParser.newParser(8);
         final Map<String, String> options = JavaCore.getOptions();
         JavaCore.setComplianceOptions(JavaCore.VERSION_1_8, options);
         astParser.setCompilerOptions(options);
         astParser.setKind(ASTParser.K_COMPILATION_UNIT);
         astParser.setResolveBindings(true);
         astParser.setBindingsRecovery(true);
         astParser.setStatementsRecovery(true);
         astParser.setSource(javaText.toCharArray());
         compilationUnit = (CompilationUnit) astParser.createAST(null);
     }

     /**
      * 获取java类包
      *
      * @return
      */
     public String getPackageName() {
         if (compilationUnit == null) {
             return "";
         }
         PackageDeclaration packageDeclaration = compilationUnit.getPackage();
         if (packageDeclaration == null) {
             return "";
         }
         String packageName = packageDeclaration.getName().toString();
         return packageName;
     }

     /**
      * 获取java类包名
      *
      * @return
      */
     public List<ImportDeclaration> getImports() {
         if (compilationUnit == null) {
             return null;
         }
         List<ImportDeclaration> imports = compilationUnit.imports();
         return imports;
     }


     public AbstractTypeDeclaration getTypes() {
         if (compilationUnit == null) {
             return null;
         }
         AbstractTypeDeclaration abstractTypeDeclaration = null;
         final List<?> types = compilationUnit.types();
         for (final Object type : types) {
             if (type instanceof AbstractTypeDeclaration) {
                 abstractTypeDeclaration = (AbstractTypeDeclaration) type;
                 break;
             }
         }
         return abstractTypeDeclaration;
     }

     /**
      * 获取普通类单元
      *
      * @return
      */
     public TypeDeclaration getJavaClass() {
         if (compilationUnit == null) {
             return null;
         }
         TypeDeclaration typeDeclaration = null;
         final List<?> types = compilationUnit.types();
         for (final Object type : types) {
             if (type instanceof TypeDeclaration) {
                 typeDeclaration = (TypeDeclaration) type;
                 break;
             }
         }
         return typeDeclaration;
     }

     /**
      * 获取java类中所有方法
      *
      * @return 类中所有方法
      */
     public MethodDeclaration[] getMethods() {
         TypeDeclaration typeDec = getJavaClass();
         if (typeDec == null) {
             return new MethodDeclaration[]{};
         }
         MethodDeclaration[] methodDec = typeDec.getMethods();
         return methodDec;
     }

     /**
      * 获取新增类中的所有方法信息
      *
      * @return
      */
     public List<MethodInfo> getMethodInfoList() {
         MethodDeclaration[] methodDeclarations = getMethods();
         List<MethodInfo> methodInfoList = new ArrayList<MethodInfo>();
         for (MethodDeclaration method : methodDeclarations) {
             MethodInfo methodInfo = new MethodInfo();
             setMethodInfo(methodInfo, method);
             methodInfoList.add(methodInfo);
         }
         return methodInfoList;
     }

     /**
      * 获取修改类型的类的信息以及其中的所有方法，排除接口类
      *
      * @param methodInfos
      * @param addLines
      * @param delLines
      * @return
      */
     public ClassInfo getClassInfo(List<MethodInfo> methodInfos, List<int[]> addLines, List<int[]> delLines,
                                   List<MethodInfo> newMethodInfos) {
         TypeDeclaration typeDec = getJavaClass();
         if (typeDec == null || typeDec.isInterface()) {
             return null;
         }
         ClassInfo classInfo = new ClassInfo();
         classInfo.setClassName(getJavaClass().getName().toString());
         classInfo.setPackages(getPackageName());
         classInfo.setMethodInfos(methodInfos);
         classInfo.setNewMethodInfos(newMethodInfos);
         classInfo.setAddLines(addLines);
         classInfo.setDelLines(delLines);
         classInfo.setType("REPLACE");
         return classInfo;
     }

     /**
      * 获取修改类型的类的信息以及其中的所有方法，排除接口类
      *
      * @param methodInfos
      * @return
      */
     public ClassInfo getClassInfo(List<MethodInfo> methodInfos) {
         TypeDeclaration typeDec = getJavaClass();
         if (typeDec == null) {
             return null;
         }
         ClassInfo classInfo = new ClassInfo();
         classInfo.setClassName(getJavaClass().getName().toString());
         classInfo.setPackages(getPackageName());
         classInfo.setMethodInfos(methodInfos);
         classInfo.setType("REPLACE");
         return classInfo;
     }

     /**
      * 获取修改类型的类的信息以及其中的所有方法，排除接口类
      *
      * @param methodInfos
      * @param addLines
      * @return
      */
     public ClassInfo getClassInfo(List<MethodInfo> methodInfos, List<int[]> addLines) {
         TypeDeclaration typeDec = getJavaClass();
         if (typeDec == null) {
             return null;
         }
         ClassInfo classInfo = new ClassInfo();
         classInfo.setClassName(getJavaClass().getName().toString());
         classInfo.setPackages(getPackageName());
         classInfo.setMethodInfos(methodInfos);
         classInfo.setType("REPLACE");
         if (addLines != null) {
             classInfo.setAddLines(addLines);
         }
         return classInfo;
     }

     /**
      * 获取新增类型的类的信息以及其中的所有方法，排除接口类
      *
      * @return
      */
     public ClassInfo getClassInfo() {
         TypeDeclaration typeDec = getJavaClass();
         if (typeDec == null || typeDec.isInterface()) {
             return null;
         }
         MethodDeclaration[] methodDeclarations = getMethods();
         ClassInfo classInfo = new ClassInfo();
         classInfo.setClassName(getJavaClass().getName().toString());
         classInfo.setPackages(getPackageName());
         classInfo.setType("ADD");
         List<MethodInfo> methodInfoList = new ArrayList<MethodInfo>();
         for (MethodDeclaration method : methodDeclarations) {
             MethodInfo methodInfo = new MethodInfo();
             setMethodInfo(methodInfo, method);
             methodInfoList.add(methodInfo);
         }
         classInfo.setMethodInfos(methodInfoList);
         classInfo.setNewMethodInfos(methodInfoList);
         return classInfo;
     }

     /**
      * 获取新增类型的类的信息以及其中的所有方法，排除接口类
      *
      * @return
      */
     public AccClassInfo getAccClassInfo() {
         AbstractTypeDeclaration typeDec = getTypes();
         if (typeDec == null) {
             return null;
         }
         MethodDeclaration[] methodDeclarations = getMethods();
         AccClassInfo classInfo = new AccClassInfo();
         classInfo.setClassName(getTypes().getName().toString());
         classInfo.setPackages(getPackageName());
         classInfo.setType("ADD");
         List<MethodInfo> methodInfoList = new ArrayList<MethodInfo>();
         for (MethodDeclaration method : methodDeclarations) {
             MethodInfo methodInfo = new MethodInfo();
             setMethodInfo(methodInfo, method);
             methodInfoList.add(methodInfo);
         }
         classInfo.setMethodInfos(methodInfoList);
         List<AccImportInfo> imports = new ArrayList<>();
         for (ImportDeclaration importDeclaration : this.getImports()) {
             AccImportInfo importInfo = new AccImportInfo();
//            info.setClassName(importDeclaration.getName());
//            info.setPackages(getPackageName());
             if (importDeclaration.getName() != null) {
                 if (importDeclaration.getName() instanceof QualifiedName) {
                     QualifiedName qualifiedName = (QualifiedName) importDeclaration.getName();
                     if (importDeclaration.isStatic()) {
                         qualifiedName = (QualifiedName) qualifiedName.getQualifier();
                     }
                     importInfo.setQualifier(qualifiedName.getQualifier().getFullyQualifiedName());
                     importInfo.setName(qualifiedName.getName().getFullyQualifiedName());
                 }
                 importInfo.setImportName(importDeclaration.getName().getFullyQualifiedName());
             } else {
                 System.out.println("import error : " + this.getPackageName());
             }
             imports.add(importInfo);
         }
         classInfo.setImports(imports);
         return classInfo;
     }

     /**
      * 获取新增类型的类的信息以及其中的所有方法，排除指定方法
      *
      * @return
      */
     public ClassInfo getClassInfoExcludesMethod(List<String> excludesMethods) {
         TypeDeclaration typeDec = getJavaClass();
         if (typeDec == null || typeDec.isInterface()) {
             return null;
         }

         Map<String, Object> excludesMethodsap = excludesMethods.stream().distinct()
                 .collect(Collectors.toMap(Function.identity(), s -> 0, (v1, v2) -> v2));

         MethodDeclaration[] methodDeclarations = getMethods();
         ClassInfo classInfo = new ClassInfo();
         classInfo.setClassName(getJavaClass().getName().toString());
         classInfo.setPackages(getPackageName());
         classInfo.setType("ADD");
         List<MethodInfo> methodInfoList = new ArrayList<MethodInfo>();
         for (MethodDeclaration method : methodDeclarations) {
             if (!excludesMethodsap.containsKey(method.getName().toString())) {
                 MethodInfo methodInfo = new MethodInfo();
                 setMethodInfo(methodInfo, method);
                 methodInfoList.add(methodInfo);
             }
         }
         classInfo.setMethodInfos(methodInfoList);
         return classInfo;
     }

     /**
      * 获取修改中的方法
      *
      * @param methodDeclaration
      * @return
      */
     public MethodInfo getMethodInfo(MethodDeclaration methodDeclaration) {
         MethodInfo methodInfo = new MethodInfo();
         setMethodInfo(methodInfo, methodDeclaration);
         return methodInfo;
     }

     private void setMethodInfo(MethodInfo methodInfo, MethodDeclaration methodDeclaration) {

         methodInfo.setMd5(MD5Encode(methodDeclaration.toString()));
         methodInfo.setMethodName(methodDeclaration.getName().toString());
         methodInfo.setParameters(methodDeclaration.parameters().toString());
         if (methodDeclaration.getReturnType2() != null) {
             methodInfo.setReturnType(methodDeclaration.getReturnType2().toString());
         }
         if (methodDeclaration.getBody() != null) {
             int startLine = compilationUnit.getLineNumber(methodDeclaration.getBody().getStartPosition()) - 1;
             int endLine = compilationUnit.getLineNumber(methodDeclaration.getBody().getStartPosition()
                     + methodDeclaration.getBody().getLength());
             methodInfo.setStartLine(startLine);
             methodInfo.setEndLine(endLine);
         }
     }

     /**
      * 计算方法的MD5的值
      *
      * @param s
      * @return
      */
     public static String MD5Encode(String s) {
         String MD5String = "";
         try {
             MessageDigest md5 = MessageDigest.getInstance("MD5");
             MD5String = Base64.getEncoder().encodeToString(md5.digest(s.getBytes("utf-8")));
         } catch (NoSuchAlgorithmException e) {
             e.printStackTrace();
         } catch (UnsupportedEncodingException e) {
             e.printStackTrace();
         }
         return MD5String;
     }

     /**
      * 判断方法是否存在
      *
      * @param method     新分支的方法
      * @param methodsMap master分支的方法
      * @return
      */
     public static boolean isMethodExist(final MethodDeclaration method,
                                         final Map<String, MethodDeclaration> methodsMap) {
         // 方法名+参数一致才一致
         if (!methodsMap.containsKey(method.getName().toString() + method.parameters().toString())) {
             return false;
         }
         return true;
     }

     /**
      * 判断方法是否一致
      *
      * @param method1
      * @param method2
      * @return
      */
     public static boolean isMethodTheSame(final MethodDeclaration method1, final MethodDeclaration method2) {
         if (MD5Encode(method1.toString()).equals(MD5Encode(method2.toString()))) {
             return true;
         }
         return false;
     }

     /**
      * 获取字段列表
      *
      * @return
      */
     public List<FieldProperty> getFieldList(){
         FieldVisitor fieldVisitor = new FieldVisitor();
         this.compilationUnit.accept(fieldVisitor);
         return fieldVisitor.getFieldList();
     }

     /**
      * 获取枚举列表
      *
      * @return
      */
     public List<String> getEnumList(){
         EnumVisitor enumVisitor = new EnumVisitor();
         this.compilationUnit.accept(enumVisitor);
         return enumVisitor.getEnumValues();
     }
 }
