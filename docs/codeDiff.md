# [mebius](https://g.hz.netease.com/qa-tech/mebius)

### 代码对比

------

#### 用途

增量修改代码diff计算。

#### 参数说明

| 参数            | 类型    | 必须 | 说明                                                         |
| --------------- | ------- | ---- | ------------------------------------------------------------ |
| execType | ExecType | 是 | 执行类型 |
| projectRootPath | String  | 是   | 项目根目录路径                                               |
| currentBranch   | String | 是   | 当前分支                                    |
| compareBranch   | String | 否   | 对比分支                                    |
| currentCommit   | String | 否   | 当前commit版本                              |
| compareCommit   | String | 否   | 对比commit版本                              |
| currentTag      | String | 否   | 当前tag                                     |
| compareTag      | String | 否   | 对比tag                                     |
| gitParam | GitParam | 否   | git参数                                   |
| excludes | List | 否   | 需要排除的包名                          |
| excludesMethod | List | 否   | 需要排除的方法名                           |
| excludeSubPkg | List     | 否   | 需要排除的子工程                         |
| relationSubPkg | List | 否   | 关联的子工程                               |
| needUpdate | Boolean | 否 | 工程代码是否需要更新 |

#### 使用示例

```java
GitParam gitParam = new GitParam();
gitParam.setGitAccessType(ACCESS_TOKEN);
gitParam.setGitAccessToken(gitAccessToken);
gitParam.setGitRepoDir(PROJECT_PATH);
CodeOperate operate = new CodeOperate(gitParam);

try {
  operate.cloneCode(cloneUrl,branchName);
  operate.checkoutAndPull(cloneUrl, branchName);
  List<String> result = operate.getBranchList(ListBranchCommand.ListMode.REMOTE);
  Iterable<RevCommit> commitsresult = operate.getCommitList(branchName, 10);
  Ref ref = operate.getCurrentRef();
  operate.reset(ref.getName());
} catch (Exception e) {
  e.printStackTrace();
}
```
供参考单测：
```
CodeOperateTest
```

