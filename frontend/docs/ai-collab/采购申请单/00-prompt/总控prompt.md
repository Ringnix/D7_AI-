# D7 采购申请单 AI 协同开发 · 总控 Prompt

> 本文件保存导师下发的总控 Prompt（执行基线）。执行时按阶段推进，所有产物落在 `docs/ai-collab/采购申请单/`。

## 角色

开发 Agent + 测试 Agent + 文档 Agent + 交付 Agent；必须实际执行，不虚构结果。

## 工作区与占位符（自动推断结果）

| 占位符 | 实际值 | 说明 |
| --- | --- | --- |
| 项目根目录 | `C:\Users\CyberUser\Desktop\cyberway-mp-frontend-template-release-1.23.0` | 当前工作区 |
| 后端项目目录 | `C:\Users\CyberUser\Desktop\backend-demo` | 自动推断（Spring Boot 多模块） |
| 前端项目目录 | `C:\Users\CyberUser\Desktop\cyberway-mp-frontend-template-release-1.23.0` | 当前工作区 |
| 需求模板 | `C:\Users\CyberUser\Desktop\赛博威培训\D7\D7_提示词\标准完整版需求文档模板.md` | 自动搜索 |
| 本地自测 Skill | `...\赛博威培训\D7\D7_提示词\本地自测联调\SKILL.md` | 自动搜索 |
| 踩坑准则 | `...\赛博威培训\D7\D7_提示词\本地自测联调\pitfalls.md` | 自动搜索 |
| Liquibase 检查脚本 | `...\本地自测联调\scripts\check-liquibase-and-table.ps1` | 存在，本项目为 MySQL + 自定义记录表，按实际配置核对 |
| 环境解析脚本 | `...\本地自测联调\scripts\resolve-test-env.ps1` | 存在，适用 ops 平台，本项目按 bootstrap.yml 手动解析 |
| 输出目录 | `docs/ai-collab/采购申请单/` | 按默认 |
| 交付目录 | `deliverables/采购申请单_AI协同开发_交付包_20261124/` | 按默认 |

## 硬约束（执行中遵守）

1. 不写入真实客户/个人数据、账号口令、Token、内网地址到文档与测试数据；测试数据使用虚拟名称。
2. 数据库默认只读；造数只允许唯一 INSERT，执行后保留审计；禁止 DELETE/UPDATE/TRUNCATE/DROP 等清理动作。
3. 表结构变更只追加 Liquibase changeSet，由应用启动执行；不手工改表。
4. 所有 PASS/FAIL 来自实际执行；未执行项标记未验证并说明原因。
5. Windows 使用 `curl.exe`；含中文 JSON 用 UTF-8 无 BOM 文件 + `--data-binary @file`。
6. 不 commit/push/部署；不改共享环境。
7. 最小充分实现，遵循项目现有分层、命名、异常、日志、分页与前端组件规范。

## 阶段

阶段 0 盘点 → 1 需求 → 2 技术设计 → 3 任务编排 → 4 代码与单测 → 5 Review → 6 接口测试 → 7 UI 联调 → 8 文档/Skill/交付包。

每阶段产物与结论见 `09-final/AI协同开发过程记录.md`。
