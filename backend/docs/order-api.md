# 订单管理 API

## 通用响应

所有接口使用 `ResponseResult<T>`：

```json
{
  "success": true,
  "errorCode": "00000000",
  "msg": "",
  "data": {}
}
```

失败响应不包含 SQL、堆栈、数据库凭据或主机地址。

## 状态和校验

订单状态：

| 值 | 含义 |
| --- | --- |
| `CREATED` | 已创建 |
| `PAID` | 已支付 |
| `CANCELLED` | 已取消 |

字段规则：

- `orderNo`：去除首尾空格，非空，最长 32，全局唯一。
- `customerId`：必填，大于 0。
- `amount`：必填，大于 0，最多两位小数。
- `status`：必填，只允许上述枚举值。
- `current`：从 1 开始。
- `pageSize`：1 到 100。
- 不接受客户端自定义排序。

## 创建订单

`POST /orders`

```json
{
  "orderNo": "ORDER-202611180001",
  "customerId": 1001,
  "amount": 199.90,
  "status": "CREATED"
}
```

成功时 `data` 为订单 ID。订单号冲突返回 HTTP `409` 和错误码 `90000101`。

## 查询详情

`GET /orders/{id}`

订单不存在返回 HTTP `404` 和错误码 `90000102`。

## 分页查询

`GET /orders?current=1&pageSize=20&orderNoLike=DEMO&customerId=1001&status=CREATED`

分页结果遵循项目 `Page<T>`：

```json
{
  "current": 1,
  "totalPage": 1,
  "total": 3,
  "pageSize": 20,
  "datas": []
}
```

超过末页返回空 `datas`，`total` 保持不变。结果按创建时间倒序，并以 ID 倒序稳定排序。

## 完整修改

`PUT /orders/{id}`

请求体与创建请求字段一致。路径 ID 是唯一主键来源，请求体不能修改主键和创建审计字段。

## 删除订单

`DELETE /orders/{id}`

采用物理删除。删除成功后再次查询返回 `404`；重复删除同样返回 `404` 和
`90000102`。

## 错误码

| 错误码 | HTTP 状态 | 含义 |
| --- | --- | --- |
| `90000101` | `409` | 订单号已存在 |
| `90000102` | `404` | 订单不存在 |
| `00000002` | `400` | 参数绑定或格式错误 |

Bean Validation 失败由项目的统一参数错误处理返回 HTTP `400`。

## B 服务调用

B 暴露：

- `GET /consumer/orders/{id}`
- `GET /consumer/orders?current=1&pageSize=20`

B 通过 Feign Client `OrderClient` 调用服务名 `backend-demo-server`，由 Nacos 和
Spring Cloud LoadBalancer 解析实例。A 返回的业务失败会保持相同的错误码和
`404`/`409` 状态。
