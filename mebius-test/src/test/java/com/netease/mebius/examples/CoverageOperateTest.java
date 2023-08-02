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

 import com.netease.mebius.client.action.CoverageOperate;
 import junit.framework.Assert;
 import lombok.extern.slf4j.Slf4j;
 import org.junit.jupiter.api.Test;

 /**
 * 覆盖率操作测试类
 */
@Slf4j
public class CoverageOperateTest {
        //目标应用所在ip
        final  static String ip="127.0.0.1";
        //目标应用的jacoco端口
        final static  Integer port =8081;
        //目标exec文件存放路径
        final  static String execFilePath="src/resources/dumpexe/execFilePath";
        //合并后目标的exec文件名
        final  static String destFile="src/resources";

        /**
         * 远程清空jacoco覆盖率操作
         */
        @Test
        public  void CoverageOperateResetTest() {
            try {
                boolean resetresult = CoverageOperate.reset(ip,port);
                log.debug("远程清空jacoco覆盖率结果:"+resetresult);
                Assert.assertEquals(resetresult,true);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        /**
         * 远程dump jacoco exec文件
         */
        @Test
        public  void CoverageOperateDumpTest() {
            try {
                boolean dumpresult = CoverageOperate.dump(ip,port, execFilePath);
                log.debug("远程dump jacoco exec文件结果:"+dumpresult);
                Assert.assertEquals(dumpresult,true);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        /**
         * exec 文件合并
         */
        @Test
        public  void CoverageOperateMergeTest() {
            try {

                boolean mergeresult = CoverageOperate.merge(destFile,execFilePath, ip);
                log.debug("远程merge jacoco exec文件结果:"+mergeresult);
                Assert.assertEquals(mergeresult,true);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

