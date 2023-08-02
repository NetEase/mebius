# [mebius](https://g.hz.netease.com/qa-tech/mebius)

### 解析工程下所有mapping url

------

#### 用途

解析工程下所有controller层的映射地址，获取所有http接口信息

#### 参数说明

| 参数            | 类型   | 必须 | 说明     |
| --------------- | ------ | ---- | -------- |
| projectRootPath | String | 是   | 工程路径 |

#### 使用示例

```java
try{
		List<UrlMappingInfo> results = UrlMappingAnalyze.analyze(projectRootPath);
} catch (Exception e) {
		e.printStackTrace();
}
```
