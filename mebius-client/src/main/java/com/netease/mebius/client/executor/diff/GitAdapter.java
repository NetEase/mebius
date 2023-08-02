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

import com.jcraft.jsch.JSch;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.Session;
import org.eclipse.jgit.api.CreateBranchCommand;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.LsRemoteCommand;
import org.eclipse.jgit.api.PullCommand;
import org.eclipse.jgit.api.ResetCommand;
import org.eclipse.jgit.api.TransportCommand;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.lib.ObjectId;
import org.eclipse.jgit.lib.ObjectLoader;
import org.eclipse.jgit.lib.ObjectReader;
import org.eclipse.jgit.lib.Ref;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.revwalk.RevCommit;
import org.eclipse.jgit.revwalk.RevTree;
import org.eclipse.jgit.revwalk.RevWalk;
import org.eclipse.jgit.transport.JschConfigSessionFactory;
import org.eclipse.jgit.transport.OpenSshConfig;
import org.eclipse.jgit.transport.SshSessionFactory;
import org.eclipse.jgit.transport.SshTransport;
import org.eclipse.jgit.transport.UsernamePasswordCredentialsProvider;
import org.eclipse.jgit.treewalk.AbstractTreeIterator;
import org.eclipse.jgit.treewalk.CanonicalTreeParser;
import org.eclipse.jgit.treewalk.TreeWalk;
import org.eclipse.jgit.util.FS;
import org.eclipse.jgit.util.StringUtils;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Collection;

/**
 * Git适配器
 */
public class GitAdapter {
    private Git git;
    private Repository repository;
    private String gitFilePath;

    //  Git授权
    private static UsernamePasswordCredentialsProvider usernamePasswordCredentialsProvider;

    //用于创建git ssh连接
    private static SshSessionFactory sshSessionFactory;

    public GitAdapter(String gitFilePath) {
        this.gitFilePath = gitFilePath;
        this.initGit(gitFilePath);
    }

    private void initGit(String gitFilePath) {
        try {
            git = Git.open(new File(gitFilePath));
            repository = git.getRepository();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public String getGitFilePath() {
        return gitFilePath;
    }

    public Git getGit() {
        return git;
    }

    public Repository getRepository() {
        return repository;
    }

    /**
     * git授权。需要设置拥有所有权限的用户
     *
     * @param username git用户名
     * @param password git用户密码
     */
    public static void setCredentialsProvider(String username, String password) {
        if (usernamePasswordCredentialsProvider == null || !usernamePasswordCredentialsProvider.isInteractive()) {
            usernamePasswordCredentialsProvider = new UsernamePasswordCredentialsProvider(username, password);
        }
    }


    /**
     * git ssh连接配置
     *
     * @param sshPrivateKeyPath git用户ssh私钥文件
     * @param knownHosts        known_hosts文件流，包含git服务器的公钥
     */
    public static void setSshSessionFactory(String sshPrivateKeyPath, FileInputStream knownHosts) {
        sshSessionFactory = new JschConfigSessionFactory() {
            @Override
            protected void configure(OpenSshConfig.Host host, Session session) {
                //nothing
            }

            @Override
            protected JSch createDefaultJSch(FS fs) throws JSchException {
                JSch jsch = super.createDefaultJSch(fs);
                jsch.setKnownHosts(knownHosts);
                jsch.addIdentity(sshPrivateKeyPath);

                return jsch;
            }
        };
    }


    /**
     * 设置git认证信息和ssh连接配置
     *
     * @param childCommand
     */
    public void setCredentialsProviderOrTransportConfigCallback(TransportCommand childCommand) {
        if (sshSessionFactory != null) {
            childCommand.setTransportConfigCallback(transport -> ((SshTransport) transport).setSshSessionFactory(sshSessionFactory));
        } else if (usernamePasswordCredentialsProvider != null) {
            childCommand.setCredentialsProvider(usernamePasswordCredentialsProvider);
        }
    }

    /**
     * 获取指定分支的指定文件内容
     *
     * @param branchName 分支名称
     * @param javaPath   文件路径
     * @return java类
     * @throws IOException
     */
    public String getBranchSpecificFileContent(String branchName, String javaPath) throws IOException {
        Ref branch = repository.exactRef("refs/heads/" + branchName);
        ObjectId objId = branch.getObjectId();
        RevWalk walk = new RevWalk(repository);
        RevTree tree = walk.parseTree(objId);
        return getFileContent(javaPath, tree, walk);
    }

    /**
     * 获取指定分支指定Tag版本的指定文件内容
     *
     * @param tagRevision Tag版本
     * @param javaPath    件路径
     * @return java类
     * @throws IOException
     */
    public String getTagRevisionSpecificFileContent(String tagRevision, String javaPath) throws IOException {
        ObjectId objId = repository.resolve(tagRevision);
        RevWalk walk = new RevWalk(repository);
        RevCommit revCommit = walk.parseCommit(objId);
        RevTree tree = revCommit.getTree();
        return getFileContent(javaPath, tree, walk);
    }


    /**
     * 获取指定分支指定commit版本的指定文件内容
     *
     * @param revCommit commit版本
     * @param javaPath  件路径
     * @return java类
     * @throws IOException
     */
    public String getCommitRevisionSpecificFileContent(RevCommit revCommit, String javaPath) throws IOException {
        RevWalk walk = new RevWalk(repository);
        RevTree tree = walk.parseTree(revCommit.getTree().getId());
        return getFileContent(javaPath, tree, walk);
    }

    /**
     * 获取指定分支指定的指定文件内容
     *
     * @param javaPath 件路径
     * @param tree     git RevTree
     * @param walk     git RevWalk
     * @return java类
     * @throws IOException
     */
    private String getFileContent(String javaPath, RevTree tree, RevWalk walk) throws IOException {
        TreeWalk treeWalk = TreeWalk.forPath(repository, javaPath, tree);
        if (treeWalk == null) {
            return "";
        }
        ObjectId blobId = treeWalk.getObjectId(0);
        ObjectLoader loader = repository.open(blobId);
        byte[] bytes = loader.getBytes();
        walk.dispose();
        return new String(bytes);
    }

    /**
     * 分析分支树结构信息
     *
     * @param localRef 本地分支
     * @return
     * @throws IOException
     */
    public AbstractTreeIterator prepareTreeParser(Ref localRef) throws IOException {
        RevWalk walk = new RevWalk(repository);
        RevCommit commit = walk.parseCommit(localRef.getObjectId());
        RevTree tree = walk.parseTree(commit.getTree().getId());
        CanonicalTreeParser treeParser = new CanonicalTreeParser();
        ObjectReader reader = repository.newObjectReader();
        treeParser.reset(reader, tree.getId());
        walk.dispose();
        return treeParser;
    }

    /**
     * 分析分支树结构信息(根据commit)
     *
     * @param commit
     * @return
     * @throws IOException
     */
    public AbstractTreeIterator prepareTreeParser(RevCommit commit) throws IOException {
        RevWalk walk = new RevWalk(repository);
        RevTree tree = walk.parseTree(commit.getTree().getId());
        CanonicalTreeParser treeParser = new CanonicalTreeParser();
        ObjectReader reader = repository.newObjectReader();
        treeParser.reset(reader, tree.getId());
        walk.dispose();
        return treeParser;
    }

    /**
     * 切换分支
     *
     * @param branchName 分支名称
     * @throws GitAPIException GitAPIException
     */
    public void checkOut(String branchName) throws GitAPIException {
        // 切换分支
        git.checkout().setCreateBranch(false).setName(branchName).call();
    }

    /**
     * 切换版本
     *
     * @param currentCommit 版本名称
     * @throws GitAPIException GitAPIException
     */
    public void reset(String currentCommit) throws GitAPIException {
        // 切换commit
        if (!StringUtils.isEmptyOrNull(currentCommit)) {
            git.reset().setMode(ResetCommand.ResetType.HARD).setRef(currentCommit).call();
        }
    }

    /**
     * 更新分支代码
     *
     * @param localRef   本地分支
     * @param branchName 分支名称
     * @throws GitAPIException GitAPIException
     */
    public void checkOutAndPull(Ref localRef, String branchName) throws GitAPIException {
        boolean isCreateBranch = localRef == null;
        if (!isCreateBranch && checkBranchNewVersion(localRef)) {
            return;
        }
        //  切换分支
        git.checkout()
                .setCreateBranch(isCreateBranch)
                .setName(branchName)
                .setStartPoint("origin/" + branchName)
                .setUpstreamMode(CreateBranchCommand.SetupUpstreamMode.SET_UPSTREAM)
                .setForced(true).call();
        //  拉取最新代码
        PullCommand pullCommand = git.pull();
        setCredentialsProviderOrTransportConfigCallback(pullCommand);
        pullCommand.call();
    }

    /**
     * 强制更新分支代码
     *
     * @param localRef   本地分支
     * @param branchName 分支名称
     * @throws GitAPIException GitAPIException
     */
    public void checkOutAndPullAllWay(Ref localRef, String branchName) throws GitAPIException {
        boolean isCreateBranch = localRef == null;
        //  切换分支
        git.checkout().setCreateBranch(isCreateBranch).setName(branchName).setStartPoint("origin/" + branchName).setUpstreamMode(CreateBranchCommand.SetupUpstreamMode.SET_UPSTREAM).call();
        //  拉取最新代码
        PullCommand pullCommand = git.pull();
        setCredentialsProviderOrTransportConfigCallback(pullCommand);
        pullCommand.call();
    }

    /**
     * 判断本地分支是否是最新版本。目前不考虑分支在远程仓库不存在，本地存在
     *
     * @param localRef 本地分支
     * @return boolean
     * @throws GitAPIException GitAPIException
     */
    private boolean checkBranchNewVersion(Ref localRef) throws GitAPIException {
        String localRefName = localRef.getName();
        String localRefObjectId = localRef.getObjectId().getName();
        //  获取远程所有分支
        LsRemoteCommand lsRemoteCommand = git.lsRemote().setHeads(true);
        setCredentialsProviderOrTransportConfigCallback(lsRemoteCommand);
        Collection<Ref> remoteRefs = lsRemoteCommand.call();
        for (Ref remoteRef : remoteRefs) {
            String remoteRefName = remoteRef.getName();
            String remoteRefObjectId = remoteRef.getObjectId().getName();
            if (remoteRefName.equals(localRefName)) {
                if (remoteRefObjectId.equals(localRefObjectId)) {
                    return true;
                }
                return false;
            }
        }
        return false;
    }
}
