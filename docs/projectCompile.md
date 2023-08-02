# [mebius](https://g.hz.netease.com/qa-tech/mebius)

### 工程编译

------

#### 用途

编译工程，支持maven和Gradle。

#### 功能清单

```
ProjectCompile.mavenCompile
ProjectCompile.gradleCompile
```

#### 参数说明

| 参数          | 类型     | 必须 | 说明                                           |
| ------------- | -------- | ---- | ---------------------------------------------- |
| mavenPath     | String   | 是   | maven所在安装本地路径                          |
| pomPath       | String   | 是   | 工程pom路径                                    |
| command       | String   | 是   | 执行的编译命令（如:compile、install、package） |
| javaHome      | String   | 否   | javaHome路径                                   |
| logPath       | String   | 否   | mvn输入日志的路径                              |
| profile       | String   | 否   | 环境变量值                                     |
| completeCmd   | String   | 否   | 完整的maven命令（输入后，command字段失效）     |
| projectPath   | String   | 是   | 工程所在路径                                   |
| tasks         | String[] | 是   | 编译命令（如compileJava）                      |
| arguments     | String[] | 否   | 自定义参数                                     |
| gradleVersion | String   | 否   | gradle版本                                     |
| distribution  | String   | 否   | distribution url                               |

#### 使用示例

```java
MavenParam mavenParam=new MavenParam();
mavenParam.setMavenPath(mavenPath);
mavenParam.setPomPath(pomPath);
mavenParam.setCommand(command);

boolean result = ProjectCompile.mavenCompile(mavenParam);
```
供参考单测：
```
ProjectCompileTest
```
