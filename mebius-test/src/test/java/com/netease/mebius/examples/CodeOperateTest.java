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

 import com.google.gson.Gson;
 import com.netease.mebius.client.action.CodeOperate;
 import com.netease.mebius.client.enums.GitAccessType;
 import com.netease.mebius.client.model.project.GitParam;
 import lombok.extern.slf4j.Slf4j;
 import org.eclipse.jgit.api.ListBranchCommand;
 import org.eclipse.jgit.lib.Ref;
 import org.eclipse.jgit.revwalk.RevCommit;
 import org.junit.jupiter.api.Test;
 import java.util.ArrayList;
 import java.util.List;
 import static com.netease.mebius.client.enums.GitAccessType.ACCESS_TOKEN;
 import static junit.framework.Assert.assertTrue;

 /**
  * 代码操作测试类
  */
 @Slf4j
public class CodeOperateTest {
    // Git工程路径(传项目所在的绝对路径)
    private final static String gitRepoDir = "D:/mebiuscode/code/newcode/mebius-examples";
    //Git访问方式(如：账号密码：ACCOUNT、access token方式：ACCESS_TOKEN、SSH方式：SSH_KEY)
    private final static GitAccessType gitAccessType = ACCESS_TOKEN;
    // Git用户名
    private final static String gitUser = "***";
    //Git用户密码
    private final static String gitPassword = "***";
    //Git访问令牌
    private final static String gitAccessToken = "*******";
    //ssh私钥文件路径
    private final static String sshPrivateKeyPath = "***";
    //ssh known_hosts文件
    private final static String sshKnownHosts = "***";
    //clone路径
    private final static String cloneUrl = "https://g.hz.netease.com/qa-tech/mebius-examples.git";
    //检出分支名
    private final static String branchName = "feature-syh-20220810";
    //分支列表
    private final static List<String> branchlist = new ArrayList<>();
    //代码操作
    private static CodeOperate operate;

    static {
        GitParam gitParam = new GitParam();
        gitParam.setGitRepoDir(gitRepoDir);
        gitParam.setGitAccessType(ACCESS_TOKEN);
        gitParam.setGitAccessToken(gitAccessToken);

        operate = new CodeOperate(gitParam);
    }

    /**
     * git clone代码--文件不为空报错,所以暂时注释掉
     */
    //@Test
    public void codeOperateCloneCodeTest() {
        try {
            operate.cloneCode(cloneUrl,branchName);
            log.debug("代码克隆成功！");

        } catch (Exception e) {
            log.error("测试失败", e);
        }
    }

    /**
     * checkout和pull代码
     */
    @Test
    public void codeOperateCheckoutAndPullTest() {
        try {
            //git clone代码--文件不为空报错
            //operate.cloneCode(cloneUrl,branchName);
            //log.debug("代码克隆成功！");

            operate.checkoutAndPull(cloneUrl, branchName);
            log.debug("检出拉取成功!");

        } catch (Exception e) {
            log.error("测试失败", e);
        }
    }

    /**
     * 获取分支列表
     */
    @Test
    public void codeOperateGetBranchListTest() {
        try {
            List<String> result = operate.getBranchList(ListBranchCommand.ListMode.REMOTE);
            log.debug("分支列表: {}", new Gson().toJson(result));
            assertTrue(new Gson().toJson(result), result.contains("master"));
        } catch (Exception e) {
            log.error("测试失败", e);
        }
    }

    /**
     * 获取git分支commit记录
     */
    @Test
    public void codeOperateGetCommitListTest() {
        try {
            Iterable<RevCommit> commitsresult = operate.getCommitList(branchName, 10);
            while (commitsresult.iterator().hasNext()) {
                log.debug("分支commit记录:{}", new Gson().toJson(commitsresult.iterator().next()));
            }

        } catch (Exception e) {
            log.error("测试失败", e);
        }
    }

    /**
     * 获取当前工程的ref版本
     */
    @Test
    public void codeOperateGetCurrentRefTest() {
        try {
            Ref ref = operate.getCurrentRef();
            log.debug("当前工程的ref:{}", ref);

        } catch (Exception e) {
            log.error("测试失败", e);
        }
    }

}

