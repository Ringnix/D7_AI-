# D7 AI 协同开发全流程交付 · 采购申请单模块

基于已有中台前端（Vue3 + Vite + `cyberway-middle-ui-components`）与 Spring Boot 后端（`backend-demo`）完成的采购申请单模块，覆盖：
需求分析 → 技术设计 → 任务编排 → 前后端开发 → 单元测试 → Code Review → 接口测试 → UI 联调 → 文档与 Skill 提炼。

## 仓库结构

```text
.
├── docs/                     # AI 协同开发全过程文档（00~09）
│   ├── 00-prompt/            # 总控 Prompt、Prompt 记录、项目盘点
│   ├── 01-requirement/       # 需求文档、澄清与评审
│   ├── 02-design/            # 技术设计、ER 图、表结构、接口清单、改动清单
│   ├── 03-plan/              # 改动清单、任务编排、任务看板
│   ├── 04-dev/               # 代码变更说明、单测记录、编译启动记录
│   ├── 05-test/接口测试/      # 用例、seed、响应证据、接口测试报告、日志排查
│   ├── 06-review/            # Code Review 与修复记录
│   ├── 07-ui/                # UI 用例、联调记录、UI 结果、菜单与访问地址、截图
│   ├── 08-skill/skills/      # 4 个可复用 Skill
│   └── 09-final/             # 过程记录、交付说明、完成度清单、未验证项
├── backend/                  # 后端源码（Spring Boot 多模块）
│   ├── backend-demo-api/     # 接口、DTO、VO、枚举
│   ├── backend-demo-context/ # 实体、仓储、领域/应用服务、Liquibase
│   ├── backend-demo-starter/ # Controller、异常处理
│   ├── backend-demo-server/  # 服务 A 启动模块
│   └── ...                   # consumer/feign/starter 等既有模块
└── frontend/                 # 前端源码（Vue3 + Vite 中台模板）
    ├── src/service/purchase-request/  # 接口封装与类型
    ├── src/views/purchase-request/    # 列表/表单/详情/动作弹框/状态标签
    ├── src/router/purchase-request.ts # 路由
    ├── src/locales/                   # 中英文文案
    └── scripts/serve-uc.mjs           # 本地启动用户中心（菜单/资源/角色配置）
```

## 功能与状态流转

- 主子表：采购申请单主表 + 多条采购明细
- 分页查询（申请单号/标题/状态筛选）、详情、新增/编辑/删除、提交、撤回、取消、审批通过、审批驳回
- 状态机：`DRAFT(草稿) -> SUBMITTED(待审批) -> APPROVED(通过)/REJECTED(驳回)`；草稿/待审批可取消，待审批可撤回为草稿
- 取消原因、驳回原因必填；审批仅记录状态与审批信息，不接中台审批流
- 乐观锁：主表 `version`，所有写操作条件更新，冲突返回 `90000206`
- 权限：仅按单据状态控制按钮；未接中台权限码/租户过滤（见未验证项）

## 技术栈

| 端 | 技术 |
| --- | --- |
| 前端 | Vue 3.2、Vite 5、TypeScript、Ant Design Vue 3.2、mp-cli 1.23.0、CTable/useTable |
| 后端 | Spring Boot 3.4.8、Java 17、MyBatis + 中台 DAL、Liquibase、Jakarta Validation、Swagger |
| 数据库 | MySQL 8.0（Liquibase 自动建表） |

## 本地运行

### 1. 数据库

准备 MySQL 8.0，创建空库（示例库名 `java_simulation_project`），表结构与样例由后端 Liquibase 自动执行。

### 2. 后端

```powershell
cd backend
mvn clean package -DskipTests
$env:SERVER_PORT='8081'
$env:DB_URL='jdbc:mysql://<MySQL地址>:3306/<库名>?useUnicode=true&characterEncoding=utf8&useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=Asia/Shanghai'
$env:DB_USERNAME='<用户名>'
$env:DB_PASSWORD='<密码>'
$env:NACOS_ENABLED='false'   # 本地无 Nacos 时关闭注册/配置中心
java -Dfile.encoding=UTF-8 -jar backend-demo-server/target/backend-demo-server-0.0.1-SNAPSHOT.jar
```

- 接口端口：`8081`
- Liquibase 会执行 `backend-demo_change_log/v1.3.0.xml` 创建采购申请单表

### 3. 前端

```powershell
cd frontend
yarn
yarn dev
```

- 访问：`http://localhost:3000`
- 采购申请单页面（无菜单权限时可直接访问）：`http://localhost:3000/#/purchase-request/list`
- 开发代理：`/api/purchase-requests` → `http://127.0.0.1:8081`

### 4. 用户中心（菜单/资源/角色配置，可选）

已引入公司私库包 `cyberway-mp-uc-web@1.23.0`：

```powershell
cd frontend
yarn uc:serve
```

访问 `http://127.0.0.1:3001/`，登录后配置应用菜单资源；如无授权权限，直接使用上面的页面 URL。

## 测试与验证结果

| 类型 | 结果 |
| --- | --- |
| 后端单元测试 `mvn test` | 30/30 PASS（新增 19 个） |
| 接口测试（curl.exe） | 32/32 PASS，两轮共 64 次请求 |
| 并发/乐观锁 | 同版本并发提交 200/409 |
| 后端日志 | ERROR=0（负向用例 WARN 为预期） |
| 前端 ESLint / Stylelint / 构建 | 0 error，生产构建成功 |
| UI 联调 | 列表、状态化按钮、详情、新增表单、取消原因弹框已验证（4 张截图） |

## 文档入口

- 完成度清单：[docs/09-final/完成度清单.md](docs/09-final/完成度清单.md)
- 过程记录（含提示词/返工统计）：[docs/09-final/AI协同开发过程记录.md](docs/09-final/AI协同开发过程记录.md)
- 未验证与人工确认项：[docs/09-final/人工确认与未验证项.md](docs/09-final/人工确认与未验证项.md)
- 接口测试报告：[docs/05-test/接口测试/接口测试报告.md](docs/05-test/接口测试/接口测试报告.md)
- 菜单与访问地址：[docs/07-ui/菜单与访问地址.md](docs/07-ui/菜单与访问地址.md)

## 已知限制（如实说明）

1. 前端“新增/编辑保存”完整点击链路未自动化，保留人工验证清单（接口层已覆盖）。
2. 未接中台真实权限码/租户过滤，按钮仅按单据状态显隐。
3. 容器化部署时前端 nginx 需自行反代 `/api/purchase-requests` 到后端。
4. 审批仅记录状态，不接入中台工作流。

## 作者

大数据事业部 · 钟承礼 · 2026-11
