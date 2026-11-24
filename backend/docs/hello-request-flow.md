# `/demo/hello` 请求流转

请求：

```http
GET /demo/hello?name=World
```

完整流转顺序：

```text
HTTP 请求
  -> Tomcat
  -> Spring MVC DispatcherServlet
  -> HelloController#hello
  -> DemoApi Bean
  -> DemoApplicationService#hello
  -> DemoService Bean#hello
  -> DemoHelloVo
  -> ResponseResult.successResult
  -> Jackson 序列化为 JSON
  -> HTTP 响应
```

各层职责：

1. `HelloController` 映射 `/demo/hello`，将查询参数绑定为 `DemoHelloDto` 并执行 Bean Validation。
2. `DemoApi` 是 Controller 依赖的接口，运行时实例为 `DemoApplicationService`。
3. `BackendDemoContextConfiguration` 使用 `@Bean` 创建 `DemoService` 和 `DemoApplicationService`。
4. `DemoService` 负责生成欢迎消息，名称为空时使用 `World`，并生成响应时间。
5. `DemoHelloVo` 隔离内部对象和公开响应。
6. Controller 使用统一 `ResponseResult` 包装成功结果。

成功响应示例：

```json
{
  "success": true,
  "errorCode": "00000000",
  "msg": "",
  "data": {
    "message": "Hello, World!",
    "timestamp": "2026-11-18T15:30:00+08:00"
  }
}
```

`name` 超过 50 个字符时，在 Controller 参数校验阶段返回 HTTP `400`，不会进入 Service。
