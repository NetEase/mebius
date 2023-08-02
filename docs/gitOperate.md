# [mebius](https://g.hz.netease.com/qa-tech/mebius)

### Git代码操作

------

#### 用途

从Git上拉取代码，更新代码，获取分支信息等操作。

#### 功能清单

```
CodeOperate.cloneCode
CodeOperate.checkoutAndPull
CodeOperate.getBranchList
CodeOperate.getCommitList
CodeOperate.getCurrentRef
CodeOperate.reset
```

#### 参数说明

| 参数              | 类型    | 必须 | 说明                                                         |
| ----------------- | ------- | ---- | ------------------------------------------------------------ |
| gitRepoDir        | String  | 是   | Git工程路径(传项目所在的绝对路径)                            |
| gitAccessType     | String  | 是   | Git访问方式(如：账号密码:ACCOUNT、access token方式:ACCESS_TOKEN、SSH方式:SSH_KEY) |
| gitAccessToken    | String  | 否   | Git访问令牌                                                  |
| sshPrivateKeyPath | String  | 否   | ssh私钥文件路径                                              |
| gitUser           | String  | 否   | git用户                                                      |
| gitPassword       | String  | 否   | git用户密码                                                  |
| sshKnownHosts     | String  | 否   | ssh known_hosts文件路径                                      |
| cloneUrl          | String  | 否   | clone路径                                                    |
| branchName        | String  | 否   | 检出分支名                                                   |
| branchlist        | List    | 否   | 分支列表                                                     |
| count             | Integer | 否   | 获取commit版本个数                                           |
| ref               | String  | 否   | ref版本                                                      |

#### 使用示例

```java
GitParam gitParam = new GitParam();
gitParam.setGitAccessType(ACCESS_TOKEN);
gitParam.setGitAccessToken(gitAccessToken);
gitParam.setGitRepoDir(PROJECT_PATH);
CodeOperate operate = new CodeOperate(gitParam);

operate.cloneCode(cloneUrl,branchName);
operate.checkoutAndPull(cloneUrl, branchName);
List<String> result = operate.getBranchList(ListBranchCommand.ListMode.REMOTE);
Iterable<RevCommit> commitsresult = operate.getCommitList(branchName, 10);
Ref ref = operate.getCurrentRef();
```
供参考单测：
```
CodeAnalyzeTest
```

