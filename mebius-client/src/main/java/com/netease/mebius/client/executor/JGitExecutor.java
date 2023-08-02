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
package com.netease.mebius.client.executor;

import com.netease.mebius.client.constant.ConstantVar;
import com.netease.mebius.client.exception.MebiusException;
import com.netease.mebius.client.executor.helper.GitHelper;
import com.netease.mebius.client.model.project.GitParam;
import com.netease.mebius.client.model.project.JGitParam;
import com.netease.mebius.client.utils.FileUtils;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.assertj.core.util.Lists;
import org.eclipse.jgit.api.CloneCommand;
import org.eclipse.jgit.api.CreateBranchCommand;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.ListBranchCommand;
import org.eclipse.jgit.api.PullCommand;
import org.eclipse.jgit.api.ResetCommand;
import org.eclipse.jgit.api.SubmoduleUpdateCommand;
import org.eclipse.jgit.api.TransportCommand;
import org.eclipse.jgit.api.errors.CheckoutConflictException;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.api.errors.JGitInternalException;
import org.eclipse.jgit.api.errors.RefNotAdvertisedException;
import org.eclipse.jgit.api.errors.RefNotFoundException;
import org.eclipse.jgit.api.errors.WrongRepositoryStateException;
import org.eclipse.jgit.lib.ConfigConstants;
import org.eclipse.jgit.lib.Constants;
import org.eclipse.jgit.lib.Ref;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.revwalk.RevCommit;
import org.eclipse.jgit.submodule.SubmoduleWalk;
import org.eclipse.jgit.transport.SshSessionFactory;
import org.eclipse.jgit.transport.SshTransport;
import org.eclipse.jgit.transport.UsernamePasswordCredentialsProvider;

import java.io.File;
import java.io.IOException;
import java.util.List;

/**
 * JGit执行器
 */
@Slf4j
public class JGitExecutor {

    public static ThreadLocal<JGitParam> threadLocal = new ThreadLocal<>();

    /**
     * 构造方法
     *
     * @param gitParam
     * @return
     */
    public JGitExecutor(GitParam gitParam) {
        JGitParam jgitParam = new JGitParam();
        jgitParam.setProjectBasePath(gitParam.getGitRepoDir());
        jgitParam.setUsernamePasswordCredentialsProvider(GitHelper.getCredentialsProvider(gitParam));
        jgitParam.setSshSessionFactory(GitHelper.getSshSessionFactory(gitParam));
        threadLocal.set(jgitParam);
    }

    /**
     * 设置git认证信息和ssh连接配置
     *
     * @param childCommand
     */
    public void setCredentialsProviderOrTransportConfigCallback(TransportCommand childCommand) {
        SshSessionFactory sshSessionFactory = threadLocal.get().getSshSessionFactory();
        UsernamePasswordCredentialsProvider usernamePasswordCredentialsProvider = threadLocal.get().getUsernamePasswordCredentialsProvider();
        if (sshSessionFactory != null) {
            childCommand.setTransportConfigCallback(transport -> ((SshTransport) transport).setSshSessionFactory(sshSessionFactory));
        } else if (usernamePasswordCredentialsProvider != null) {
            childCommand.setCredentialsProvider(usernamePasswordCredentialsProvider);
        }
    }

    /**
     * git clone代码到本地
     *
     * @param cloneUrl
     * @param branchName
     * @return
     */
    public void cloneCode(String cloneUrl, String branchName) throws MebiusException {
        String projectBasePath = threadLocal.get().getProjectBasePath();
        log.debug("JGit clone start: {}, {}, {}", projectBasePath, cloneUrl, branchName);
        if (StringUtils.isBlank(branchName)) {
            branchName = ConstantVar.DEFAULT_BRANCH;
        }
        try {
            //检查目标文件夹是否存在
            File file = new File(projectBasePath);
            File[] fs = file.listFiles();
            if (fs != null && fs.length > 0) {
                throw new MebiusException("目标目录不为空时不能clone代码，先清空目录：" + projectBasePath);
            }
            CloneCommand cloneCommand = Git.cloneRepository()
                    .setURI(cloneUrl)
                    .setDirectory(file)
                    .setBranch(branchName)
                    .setCloneSubmodules(true);
            setCredentialsProviderOrTransportConfigCallback(cloneCommand);
            cloneCommand.call();
            subModuleUpdate(Git.open(file), branchName);
            log.debug("JGit clone end: {} {}", projectBasePath, branchName);
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            throw new MebiusException("Git clone exception:" + e.getMessage());
        }
    }

    /**
     * 获取git分支列表
     *
     * @param node
     * @return
     */
    public List<String> getBranchList(ListBranchCommand.ListMode node) {
        List<String> branchList = Lists.newArrayList();
        String projectBasePath = threadLocal.get().getProjectBasePath();
        try {
            final Git git = Git.open(new File(projectBasePath));
            PullCommand pullCommand = git.pull();
            setCredentialsProviderOrTransportConfigCallback(pullCommand);
            pullCommand.call();
            List<Ref> refs = git.branchList().setListMode(node).call();
            for (Ref ref : refs) {
                if (!ref.isSymbolic()) {
                    branchList.add(ref.getName().substring(ref.getName().lastIndexOf("/") + 1));
                }
            }
        } catch (Exception e) {
            log.error(e.getMessage(), e);
        }
        return branchList;
    }

    /**
     * 获取git分支commit记录
     *
     * @param branchName
     * @param count
     * @return
     */
    public Iterable<RevCommit> getCommitList(String branchName, Integer count) {
        Iterable<RevCommit> commitList = Lists.newArrayList();
        String projectBasePath = threadLocal.get().getProjectBasePath();
        if (count == null) {
            count = 20;
        }
        try {
            //初始化git仓库
            final Git git = Git.open(new File(projectBasePath));
            //检查并拉取代码分支到本地
            doCheckoutAndPull(git, branchName);
            //查询记录
            commitList = git.log().setMaxCount(count).call();
        } catch (Exception e) {
            log.error(e.getMessage(), e);
        }
        return commitList;
    }

    /**
     * 分支checkout和pull
     *
     * @param cloneUrl
     * @param branchName
     * @return
     */
    public void checkoutAndPull(String cloneUrl, String branchName) throws MebiusException {
        String projectBasePath = threadLocal.get().getProjectBasePath();
        log.debug("JGit branch checkout and pull start: {} {}", projectBasePath, branchName);
        if (StringUtils.isBlank(branchName)) {
            branchName = ConstantVar.DEFAULT_BRANCH;
        }
        try {
            final Git git = Git.open(new File(projectBasePath));
            doCheckoutAndPull(git, branchName);
            //submodule处理
            subModuleUpdate(git, branchName);
            log.debug("JGit branch checkout and pull end: {} {}", projectBasePath, branchName);
        } catch (CheckoutConflictException | RefNotAdvertisedException | JGitInternalException | WrongRepositoryStateException e) {
            log.debug("JGit branch checkout conflict or ref exception.");
            //冲突处理，删除目录，重新拉代码
            File file = new File(projectBasePath.split(FileUtils.GIT_FOLDER)[0]);
            if (file.exists()) {
                FileUtils.deleteDirectory(file);
            }
            cloneCode(cloneUrl, branchName);
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            throw new MebiusException("Git checkout and pull exception:" + e.getMessage());
        }
    }

    /**
     * 获取当前工程的ref
     *
     * @return
     */
    public Ref getCurrentRef() {
        try {
            final Git git = Git.open(new File(threadLocal.get().getProjectBasePath()));
            if (git == null || git.getRepository() == null) {
                return null;
            }
            String currentBranch = git.getRepository().getBranch();

            if (StringUtils.isBlank(currentBranch)) {
                return null;
            }
            List<Ref> refs = git.branchList().call();
            for (Ref ref : refs) {
                if (StringUtils.equals(ref.getName(), "refs/heads/" + currentBranch)) {
                    return ref;
                }
            }
        } catch (Exception e) {
            log.error(e.getMessage(), e);
        }
        return null;
    }

    /**
     * 检查commit是否存在
     *
     * @param commitId
     * @return
     */
    public boolean checkCommitIsExist(String branchName, String commitId) {
        try {
            //初始化git仓库
            final Git git = Git.open(new File(threadLocal.get().getProjectBasePath()));
            //检查并拉取代码分支到本地
            doCheckoutAndPull(git, branchName);
            //查询记录是否存在
            Iterable<RevCommit> revCommits = git.log().call();
            for (RevCommit revCommit : revCommits) {
                if (revCommit.getName().startsWith(commitId)) {
                    return true;
                }
            }
        } catch (Exception e) {
            log.error(e.getMessage(), e);
        }
        return false;
    }

    /**
     * reset
     *
     * @param ref
     * @return
     */
    public void reset(String ref) throws MebiusException {
        if (StringUtils.isBlank(ref)) {
            return;
        }
        String projectBasePath = threadLocal.get().getProjectBasePath();
        log.debug("JGit reset start: {}, {}", projectBasePath, ref);
        try {
            final Git git = Git.open(new File(projectBasePath));
            git.reset().setMode(ResetCommand.ResetType.HARD).setRef(ref).call();
            log.debug("JGit reset end: {}, {}", projectBasePath, ref);
        } catch (GitAPIException | IOException e) {
            log.error(e.getMessage(), e);
            throw new MebiusException("Git reset exception:" + e.getMessage());
        }
    }


    /**
     * 执行代码更新
     *
     * @param git
     * @param branchName
     * @throws GitAPIException
     */
    private void doCheckoutAndPull(Git git, String branchName) throws GitAPIException, IOException {
        if (branchNameExist(git, branchName)) {
            //如果分支在本地已存在，直接checkout即可。
            git.checkout().setCreateBranch(false).setName(branchName).call();
            //然后更新下代码
            PullCommand pullCommand = git.pull();
            setCredentialsProviderOrTransportConfigCallback(pullCommand);
            pullCommand.call();
        } else {
            String fullBranch = git.getRepository().getFullBranch();
            //先更新代码，否则会拉不到远程分支
            if (fullBranch.startsWith(Constants.R_HEADS)) {
                PullCommand pullCommand = git.pull();
                setCredentialsProviderOrTransportConfigCallback(pullCommand);
                pullCommand.call();
            }
            //如果分支在本地不存在，需要创建这个分支，并追踪到远程分支上面。
            git.checkout()
                    .setCreateBranch(true)
                    .setName(branchName)
                    .setStartPoint("origin/" + branchName)
                    .setUpstreamMode(CreateBranchCommand.SetupUpstreamMode.SET_UPSTREAM)
                    .call();
        }
    }

    /**
     * 判断本地分支名是否存在
     *
     * @param git
     * @param branchName
     * @return
     * @throws GitAPIException
     */
    private boolean branchNameExist(Git git, String branchName) throws GitAPIException {
        List<Ref> refs = git.branchList().call();
        for (Ref ref : refs) {
            if (ref.getName().contains("refs/heads/") && ref.getName().split("refs/heads/")[1].equals(branchName)) {
                return true;
            }
        }
        return false;
    }

    /**
     * @param git
     * @param branchName
     * @throws IOException
     * @throws GitAPIException
     */
    private void subModuleUpdate(Git git, String branchName) throws Exception {
        //兼容逻辑
        if (git.getRepository().toString().contains("yanxuan-distribution-openapi")
                || git.getRepository().toString().contains("yanxuan-distribution-api")
                || git.getRepository().toString().contains("yanxuan-distribution-admin")) {
            modifySubModuleInfo("yanxuan-distribution-common",
                    "http://git.yx.hz.infra.mail/distribution/yanxuan-distribution-common.git");
        }
        SubmoduleWalk walk = SubmoduleWalk.forIndex(git.getRepository());
        while (walk.next()) {
            Repository submoduleRepository = walk.getRepository();
            if (submoduleRepository == null) {
                continue;
            }
            try {
                doCheckoutAndPull(new Git(submoduleRepository), branchName);
            } catch (RefNotFoundException e) {
                log.warn("submodule ref is not found: {} " + e.getMessage());
                doCheckoutAndPull(new Git(submoduleRepository), "master");
            }
            submoduleRepository.close();
        }
        walk.close();
    }

    /**
     * 修改submodule信息
     *
     * @param subsection
     * @param gitUrl
     * @return
     */
    public void modifySubModuleInfo(String subsection, String gitUrl) throws Exception {
        try {
            String projectBasePath = threadLocal.get().getProjectBasePath();
            final Git git = Git.open(new File(projectBasePath));
            git.getRepository().getConfig().setString(ConfigConstants.CONFIG_SUBMODULE_SECTION,
                    subsection, ConfigConstants.CONFIG_KEY_URL, gitUrl);
            SubmoduleUpdateCommand submoduleUpdateCommand = git.submoduleUpdate();
            setCredentialsProviderOrTransportConfigCallback(submoduleUpdateCommand);
            submoduleUpdateCommand.call();
        } catch (GitAPIException e) {
            log.error(e.getMessage(), e);
            throw new Exception("工程submodule信息处理失败:" + e.getMessage());
        }
    }

}