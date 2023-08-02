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
package com.netease.mebius.examples;

 import com.netease.mebius.client.action.ProjectCompile;
 import com.netease.mebius.client.model.project.MavenParam;
 import junit.framework.Assert;
 import lombok.extern.slf4j.Slf4j;
 import org.junit.jupiter.api.Test;

 /**
 * 工程编译测试类
 */
@Slf4j
public class ProjectCompileTest {
    // maven所在安装本地路径（如：/home/maven）
    final static String mavenPath = "D:\\newsoftware\\apache-maven-3.2.5";
    //工程pom路径
    final static String pomPath = "D:\\mebiuscode\\code\\newcode\\mebius-examples\\pom.xml";

    //执行的编译命令（如:compile、install、package）
    final static String command = "compile";
    //javaHome路径（非必填）
    //final static String javaHome = "D:\\newsoftware\\jdk\\jdk1.8.0_144\\jdk1.8.0_144";
    // mvn输入日志的路径（非必填）
    //final static String logPath = "";
    //环境变量值（非必填）
    //final static String profile = "";

    @Test
    public  void ProjectCompileResultTest() {

        MavenParam mavenParam=new MavenParam();
        mavenParam.setMavenPath(mavenPath);
        mavenParam.setPomPath(pomPath);
        mavenParam.setCommand(command);

        try {
            boolean result = ProjectCompile.mavenCompile(mavenParam);
            log.debug("代码编译结果:"+result);
            Assert.assertEquals(result,true);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
