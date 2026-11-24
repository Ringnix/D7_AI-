---
name: purchase-request-frontend-list-form-detail
description: 基于 CTable/useTable + Ant Design Vue 生成单据列表、主子表表单、详情与动作弹框页面，含状态化操作按钮、国际化与 Vite 代理。触发：中台前端项目新增单据类页面时使用。
---

# 前端列表/表单/详情页面生成 Skill

## 触发条件

- 项目使用 mp-cli 中台脚手架、CTable/useTable、Ant Design Vue、i18n。
- 后端已提供分页/详情/动作接口。

## 输入

- 接口清单与 VO 字段、状态枚举、错误码
- 现有页面模板（如订单管理）

## 核心判断

1. 列表列自定义渲染必须给列配置 `slotName`，否则 CTable 不渲染插槽。
2. 动作按钮按单据状态显隐；删除用红色并走 `deleteConfirm(content, false)`，成功后再提示。
3. 编辑弹框先拉详情拿明细与 version；表单校验 + 明细行校验 + 合计预览。
4. 语言包模块名会被脚手架转 kebab-case，key 使用 `purchase-request.*`。
5. 本地代理必须为 `/api/<模块>` 增加规则并放在 `/api` 兜底之前。

## 工作流程

1. service/types；2. 字段配置；3. 列表 + 详情 + 表单 + 动作弹框；4. 路由与 locale；5. lint/build；6. 浏览器 UI 联调。

## 产出

- `src/service/**`、`src/views/**`、`src/router/**`、locale 文件、vite 代理
- UI 截图与断言输出

## 验证方法

- ESLint/Stylelint/Prettier/生产构建通过；
- 页面无原始 i18n key；
- 列表状态/按钮显隐、详情字段、表单合计与接口一致；
- 浏览器 console 无业务异常。

## 不适用范围

- 需要复杂表格编辑（合并单元格、批量导入）或独立页面路由的场景；
- 需要接入中台权限码/菜单资源的场景（需补充授权配置）。
