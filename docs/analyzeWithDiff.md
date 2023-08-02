# [mebius](https://g.hz.netease.com/qa-tech/mebius)

### 多工程影响调用链分析

------

#### 用途

多个工程根据diff的代码来分析影响调用链，分析结果根据url进行分组。

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
| annotations | List     | 否 | 指定筛选的注解 |

#### 使用示例

```java
GitParam gitParam = new GitParam();
gitParam.setGitAccessType(ACCESS_TOKEN);
gitParam.setGitAccessToken(gitAccessToken);
gitParam.setGitRepoDir(PROJECT_PATH);

List<ProjectParam> projectParams = Lists.newArrayList();
ProjectParam projectParam = new ProjectParam();
projectParam.setProjectRootPath(projectRootPath);
projectParam.setExecType(ExecType.COMMIT_DIFF);
projectParam.setCurrentBranch(currentBranch);
projectParam.setCompareBranch(compareBranch);
projectParam.setGitParam(gitParam);
projectParams.add(projectParam);

try {
  List<MethodsCallResult> results = CodeAnalyze.analyzeWithDiff(projectParams, null);
} catch (Exception e) {
  e.printStackTrace();
}
```
供参考单测：
```
CodeAnalyzeTest
```

