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

import com.netease.mebius.client.model.project.ProjectParam;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.StringUtils;
import org.jacoco.core.internal.diff.ClassInfo;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.stream.Collectors;

/**
 * FileUtils
 */
@Slf4j
public class FileUtils {

    /**
     * git文件夹
     */
    public static final String GIT_FOLDER = ".git";

    /**
     * 获取文件大小，M为单位
     *
     * @param file
     * @return
     */
    public static String getFileSize(File file) {
        return BigDecimal.valueOf(file.length())
                .divide(BigDecimal.valueOf(1024))
                .setScale(1, BigDecimal.ROUND_HALF_UP)
                .toString() + "K";
    }


    /**
     * 根据文件路径组装成file List
     *
     * @param dir
     * @return
     */
    public static List<File> fileSets(String dir) {
        List<File> fileSetList = new ArrayList<File>();
        File path = new File(dir);
        if (!path.exists()) {
            log.error("No path name is :" + dir);
            return null;
        }
        File[] files = path.listFiles();
        try {
            if (files == null || files.length == 0) {
                return null;
            }
        } catch (NullPointerException npe) {
            npe.printStackTrace();
        }
        for (File file : files) {
            if (file.getName().contains(".exec")) {
                fileSetList.add(file);
            }
        }
        fileSetList = fileSetList.stream().sorted(Comparator.comparing(File::getName).reversed())
                .collect(Collectors.toList());
        return fileSetList;
    }

    /**
     * 删除文件夹
     *
     * @param file
     */
    public static void deleteDirectory(File file) {
        if (file.isFile()) {
            file.delete();//清理文件
        } else {
            File list[] = file.listFiles();
            if (list != null) {
                for (File f : list) {
                    deleteDirectory(f);
                }
                file.delete();//清理目录
            }
        }
    }

    /**
     * class文件包含或排除处理
     *
     * @param projectParam
     * @param classInfos
     * @return
     */
    public static void classFileExcludeAndRelation(ProjectParam projectParam, List<ClassInfo> classInfos) {
        Iterator<ClassInfo> it = classInfos.iterator();
        while (it.hasNext()) {
            ClassInfo classInfo = it.next();
            //在依赖的包中
            if (CollectionUtils.isNotEmpty(projectParam.getRelationSubPkg()) &&
                    !projectParam.getRelationSubPkg().contains(classInfo.getClassFile().split("/")[0])) {
                it.remove();
            }
            //在排除的包中
            if (CollectionUtils.isNotEmpty(projectParam.getExcludeSubPkg()) &&
                    projectParam.getExcludeSubPkg().contains(classInfo.getClassFile().split("/")[0])) {
                it.remove();
            }
        }
    }


    /**
     * 判断class文件是否排除（true:排除，false：否）
     *
     * @param filePath
     * @param relationSubPkg
     * @param excludeSubPkg
     * @return
     */
    public static boolean checkClassFileIsExclude(String filePath, String projectBasePath, List<String> relationSubPkg,
                                                  List<String> excludeSubPkg) {

        if (StringUtils.equals(filePath, projectBasePath)) {
            return false;
        }
        if (!projectBasePath.endsWith("/")) {
            projectBasePath = projectBasePath + "/";
        }

        filePath = filePath.split(projectBasePath)[1];
        if (StringUtils.isBlank(filePath)) {
            return false;
        }
        //在依赖的包中
        if (CollectionUtils.isNotEmpty(relationSubPkg) &&
                !relationSubPkg.contains(filePath.split("/")[0])) {
            return true;
        }
        //在排除的包中
        if (CollectionUtils.isNotEmpty(excludeSubPkg) &&
                excludeSubPkg.contains(filePath.split("/")[0])) {
            return true;
        }
        return false;
    }

    /**
     * @param className
     * @param directory
     * @return
     */
    public static String searchJavaFile(String className, String directory) {
        File folder = new File(directory);
        File[] files = folder.listFiles();
        if (files != null) {
            for (File file : files) {
                if (file.isDirectory()) {
                    String result = searchJavaFile(className, file.getAbsolutePath());
                    if (result != null) {
                        return result;
                    }
                } else if (file.getName().endsWith(".java")) {
                    if (StringUtils.equals(file.getName().split("\\.")[0], className)) {
                        return file.getAbsolutePath();
                    }
                }
            }
        }
        return null;
    }

    /**
     * 读取java文件内容
     *
     * @param path
     * @return
     * @throws IOException
     */
    public static String readFile(String path) throws IOException {
        InputStream is = new FileInputStream(path);
        BufferedReader in = new BufferedReader(new InputStreamReader(is));
        StringBuffer buffer = new StringBuffer();
        String line = "";
        while ((line = in.readLine()) != null) {
            buffer.append(line).append("\n");
        }
        return buffer.toString();
    }
}
