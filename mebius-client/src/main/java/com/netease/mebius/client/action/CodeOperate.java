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
package com.netease.mebius.client.action;

import com.netease.mebius.client.exception.MebiusException;
import com.netease.mebius.client.executor.JGitExecutor;
import com.netease.mebius.client.model.project.GitParam;
import org.eclipse.jgit.api.ListBranchCommand;
import org.eclipse.jgit.lib.Ref;
import org.eclipse.jgit.revwalk.RevCommit;

import java.util.List;

/**
 * Git代码操作
 */
public class CodeOperate {

    private JGitExecutor jGitExecutor;

    /**
     * 构造方法
     *
     * @param gitParam git项目参数
     * @return
     */
    public CodeOperate(GitParam gitParam) {
        setjGitExecutor(new JGitExecutor(gitParam));
    }

    /**
     * git clone代码
     *
     * @param cloneUrl
     * @param branchName
     * @throws MebiusException
     */
    public void cloneCode(String cloneUrl, String branchName) throws MebiusException {
        jGitExecutor.cloneCode(cloneUrl, branchName);
    }

    /**
     * checkout和pull代码
     *
     * @param cloneUrl
     * @param branchName
     * @throws MebiusException
     */
    public void checkoutAndPull(String cloneUrl, String branchName) throws MebiusException {
        jGitExecutor.checkoutAndPull(cloneUrl, branchName);
    }

    /**
     * 获取分支列表
     *
     * @param node
     * @return
     */
    public List<String> getBranchList(ListBranchCommand.ListMode node) {
        return jGitExecutor.getBranchList(node);
    }


    /**
     * 获取git分支commit记录
     *
     * @param branchName
     * @param count
     * @return
     */
    public Iterable<RevCommit> getCommitList(String branchName, Integer count) {
        return jGitExecutor.getCommitList(branchName, count);
    }

    /**
     * 获取当前工程的ref版本
     *
     * @return
     */
    public Ref getCurrentRef() {
        return jGitExecutor.getCurrentRef();
    }

    /**
     * reset ref版本
     *
     * @param ref
     * @throws MebiusException
     */
    public void reset(String ref) throws MebiusException {
        jGitExecutor.reset(ref);
    }



    public JGitExecutor getjGitExecutor() {
        return jGitExecutor;
    }

    public void setjGitExecutor(JGitExecutor jGitExecutor) {
        this.jGitExecutor = jGitExecutor;
    }
}