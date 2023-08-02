# [mebius](https://g.hz.netease.com/qa-tech/mebius)

### Jacoco覆盖率操作

------

#### 用途

基于jacoco，从远程测试服务器获取代码覆盖率文件操作。

#### 功能清单

```
CoverageOperate.reset
CoverageOperate.dump
CoverageOperate.merge
```

#### 参数说明

| 参数         | 类型    | 必须 | 说明                   |
| ------------ | ------- | ---- | ---------------------- |
| ip           | String  | 是   | 目标应用所在ip         |
| port         | Integer | 是   | 目标应用的jacoco端口   |
| execFilePath | String  | 是   | 目标exec文件存放路径   |
| destFile     | String  | 是   | 合并后目标的exec文件名 |

#### 使用示例

```java
boolean resetresult = CoverageOperate.reset(ip,port);
boolean dumpresult = CoverageOperate.dump(ip,port, execFilePath);
boolean mergeresult = CoverageOperate.merge(destFile,execFilePath, ip);
```
供参考单测：
```
CoverageOperateTest
```

