# 两个服务的 JAR 打包与双击运行指南

本工程有两个独立的 Spring Boot 启动模块：

| 服务 | Maven 模块 | 默认端口 |
| --- | --- | --- |
| A | `backend-demo-server` | `8081` |
| B | `backend-demo-consumer-server` | `8082` |

两个模块都配置了 `spring-boot-maven-plugin` 的 `repackage`，构建结果是可独立运行的 Fat JAR，已经包含应用依赖。

## 1. 最终目录

执行打包后，会得到：

```text
backend-demo/
  build-deploy.bat
  deploy/
    backend-demo-server.jar
    backend-demo-consumer-server.jar
    start-a.bat
    start-b.bat
    start-all.bat
    README.md
```

`deploy` 目录可以直接压缩后复制到另一台 Windows 机器。

## 2. 打包前检查

确认系统已经安装：

- JDK 17
- Maven 3.9+

确认系统变量：

```text
JAVA_HOME
MAVEN_HOME
```

`JAVA_HOME` 应指向 JDK 根目录，不是 `bin` 目录。

如果 A、B 正在运行，先关闭对应两个服务窗口。Windows 会锁定正在运行的 JAR，导致 Maven 重命名或覆盖失败。

## 3. 双击打包

回到项目根目录，双击：

```text
build-deploy.bat
```

脚本会：

1. 找到 Maven。
2. 检查旧服务是否仍占用目标 JAR。
3. 执行 `mvn clean package`。
4. 执行 JUnit 测试。
5. 生成 A、B 的 Spring Boot 可执行 JAR。
6. 复制到 `deploy` 并改成稳定名称。

成功后会显示两个文件及大小。

## 4. 手工命令打包

也可以打开 PowerShell，进入项目根目录：

```powershell
cd C:\Users\CyberUser\Desktop\backend-demo
mvn -ntp clean package
```

构建出的原始文件：

```text
backend-demo-server/target/backend-demo-server-0.0.1-SNAPSHOT.jar
backend-demo-consumer-server/target/backend-demo-consumer-server-0.0.1-SNAPSHOT.jar
```

手工复制到部署目录并改名：

```powershell
Copy-Item `
  backend-demo-server/target/backend-demo-server-0.0.1-SNAPSHOT.jar `
  deploy/backend-demo-server.jar -Force

Copy-Item `
  backend-demo-consumer-server/target/backend-demo-consumer-server-0.0.1-SNAPSHOT.jar `
  deploy/backend-demo-consumer-server.jar -Force
```

## 5. 为什么不直接双击 JAR

Windows 对 `.jar` 的默认打开程序可能不是 JDK 17，也可能使用 `javaw.exe` 而没有控制台。

直接双击还可能遇到：

- 使用了错误版本的 Java。
- 看不到应用日志。
- 不知道 A、B 应该使用哪个端口。
- 环境变量缺失时窗口直接关闭。
- 两个服务使用同一个端口。

因此推荐双击 `start-a.bat` 和 `start-b.bat`，或者在 `deploy` 目录双击 `start-all.bat`。

## 6. 部署要求

目标机器需要：

1. JDK 17。
2. Nacos 2.5.3。
3. MySQL 8。
4. 已创建数据库 `java_simulation_project`。
5. 已创建命名空间 ID 为 `local` 的 Nacos 命名空间。

系统变量：

```text
NACOS_ADDR
NACOS_NAMESPACE
NACOS_USERNAME
NACOS_PASSWORD
DB_URL
DB_USERNAME
DB_PASSWORD
```

这些变量放在 Windows“系统变量”中。BAT 和源码中不保存实际账号密码。

新增或修改系统变量后，要重新打开资源管理器窗口或重新登录桌面，再双击 BAT，确保新进程继承变量。

## 7. 正确启动顺序

```text
Nacos
  -> MySQL
  -> A：backend-demo-server:8081
  -> B：backend-demo-consumer-server:8082
```

A 启动时会注册到 Nacos，并通过 Liquibase 执行数据库变更。B 启动后也会注册到 Nacos。

## 8. 双击运行 A

进入 `deploy`，双击：

```text
start-a.bat
```

脚本会检查 `NACOS_*` 和 `DB_*`，检查 JAR 和 Java，然后执行：

```bat
"%JAVA_HOME%\bin\java.exe" -jar backend-demo-server.jar --server.port=8081
```

窗口会保留 Spring Boot 日志。看到类似下面的内容后再启动 B：

```text
Tomcat started on port 8081
Started BackendDemoApplication
```

浏览器打开：

```text
http://127.0.0.1:8081/swagger-ui.html
```

## 9. 双击运行 B

双击：

```text
start-b.bat
```

B 不需要数据库变量，只会检查 Nacos 变量并执行：

```bat
"%JAVA_HOME%\bin\java.exe" -jar backend-demo-consumer-server.jar --server.port=8082
```

看到类似下面的内容表示启动完成：

```text
Tomcat started on port 8082
Started BackendDemoConsumerApplication
```

测试 B 调 A：

```powershell
Invoke-RestMethod "http://127.0.0.1:8082/consumer/orders/910000000000000002"
```

返回订单 `DEMO-ORDER-2026111802`，说明 B 已通过 Nacos/Feign 找到 A。

## 10. 一次打开两个服务

双击：

```text
start-all.bat
```

它会：

1. 打开 A 的窗口。
2. 等待 5 秒。
3. 打开 B 的窗口。

两个窗口必须保持开启。关闭窗口会停止对应 Java 进程；按 `Ctrl+C` 也可以停止。

## 11. 部署到另一台机器

1. 在开发机执行 `build-deploy.bat`。
2. 将整个 `deploy` 目录复制到目标机器。
3. 在目标机器准备 JDK 17、Nacos、MySQL。
4. 在目标机器设置系统变量。
5. 先启动 Nacos 和 MySQL。
6. 双击 `start-a.bat`。
7. 再双击 `start-b.bat`。

JAR 不需要目标机器安装 Maven。Maven 只用于构建；运行只需要 JDK。

## 12. 确认两个 JAR 可执行

可以在部署目录打开 PowerShell 执行：

```powershell
& "$env:JAVA_HOME\bin\java.exe" -jar .\backend-demo-server.jar --server.port=8081
& "$env:JAVA_HOME\bin\java.exe" -jar .\backend-demo-consumer-server.jar --server.port=8082
```

这与 BAT 中的启动命令一致，但 BAT 还会提前检查变量和文件是否存在。

## 13. 常见问题

| 现象 | 原因与处理 |
| --- | --- |
| 提示变量未设置 | 变量不在系统环境或当前桌面会话尚未刷新，重新打开终端 |
| 提示找不到 Java | `JAVA_HOME` 配置错误，应指向 JDK 17 根目录 |
| 端口被占用 | 已有一个 A/B 正在运行，先关闭旧窗口 |
| Maven 无法覆盖 JAR | 旧服务仍占用 JAR，先停止服务再打包 |
| Nacos 看不到服务 | 控制台选择 `local` 命名空间，并检查实例是否健康 |
| B 返回连接失败 | A 是否启动并注册到同一个 Nacos 命名空间 |
| 关闭 BAT 窗口服务停止 | 当前是前台运行方式，需要窗口保持开启 |

如果希望关闭窗口后仍运行，或开机自动启动，可以进一步把 JAR 注册为 Windows 服务；这比双击部署涉及更多权限和运维配置。
