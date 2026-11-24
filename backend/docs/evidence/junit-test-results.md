# JUnit 测试结果

验证日期：2026-11-18

执行命令：

```powershell
mvn -ntp test
```

| 模块 | 测试类 | 数量 | 结果 |
| --- | --- | ---: | --- |
| `backend-demo-context` | `OrderApplicationServiceTest` | 2 | 通过 |
| `backend-demo-context` | `OrderDomainServiceTest` | 7 | 通过 |
| `backend-demo-context` | `DemoServiceTest` | 2 | 通过 |
| `backend-demo-feign-starter` | `OrderFeignServiceTest` | 2 | 通过 |
| `backend-demo-server` | `BackendDemoApplicationTests` | 1 | 通过 |
| `backend-demo-server` | `OrderDtoValidationTest` | 3 | 通过 |

合计：

- 测试 `17` 项。
- 失败 `0` 项。
- 错误 `0` 项。
- 跳过 `0` 项。

覆盖场景包括：

- 创建成功及返回主键。
- 应用层重复订单号拦截。
- 数据库唯一约束冲突转换。
- 查询、修改和删除不存在的订单。
- 修改不存在时禁止继续更新。
- 删除存在的订单。
- 分页无数据和正常分页。
- DTO 字段校验及分页上限。
- Feign 成功响应与远端业务失败转换。
- `/demo/hello` 默认名称和指定名称。

本机执行后，详细 XML 报告位于各模块的 `target/surefire-reports/` 目录。该目录属于构建产物，不提交到 Git；本文件作为仓库内可审阅的结果摘要。
