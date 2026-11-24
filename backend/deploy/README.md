# 部署目录

此目录在打包完成后包含两个稳定名称的 Spring Boot 可执行 JAR：

```text
backend-demo-server.jar
backend-demo-consumer-server.jar
```

JAR 属于构建产物，不提交 Git。

## 打包

双击项目根目录的：

```text
build-deploy.bat
```

或在项目根目录执行：

```powershell
powershell -ExecutionPolicy Bypass -File .\scripts\build-deploy.ps1
```

脚本会执行 Maven 测试和打包，再把两个 JAR 复制到本目录并改成稳定名称。

## 启动顺序

1. 启动 Nacos。
2. 启动 MySQL。
3. 设置好 `NACOS_*`、`DB_*` Windows 系统变量。
4. 双击 `start-a.bat`。
5. 等待 A 启动成功后，双击 `start-b.bat`。

也可以直接双击 `start-all.bat`。它会先打开 A 窗口，等待 5 秒，再打开 B 窗口。

关闭服务窗口或按 `Ctrl+C` 会停止对应服务。

## 地址

- A：`http://127.0.0.1:8081`
- B：`http://127.0.0.1:8082`
- A 的 Swagger：`http://127.0.0.1:8081/swagger-ui.html`

## 要求

- 必须有 JDK 17。
- `JAVA_HOME` 应指向 JDK 17。
- Nacos 需要使用命名空间 ID `local`。
- 账号和密码只从系统环境变量读取，不写入 BAT 或 JAR。

详细说明见项目中的 `docs/jar-deployment-guide.md`。
