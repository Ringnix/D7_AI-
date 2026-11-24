/** 订单状态，取值与后端 OrderStatus 枚举保持一致。 */
export type OrderStatus = 'CREATED' | 'PAID' | 'CANCELLED';

/** 订单列表项。主键和金额由后端以字符串返回，避免 Long/BigDecimal 精度丢失。 */
export interface OrderListItem {
  id: string;
  orderNo: string;
  customerId: string;
  amount: string;
  status: OrderStatus;
  createdAt: string;
}

/** 订单详情，在列表项基础上增加最后修改时间。 */
export interface OrderDetail extends OrderListItem {
  updatedAt: string;
}

/** 订单分页查询条件，current 从 1 开始。 */
export interface OrderQuery {
  current: number;
  pageSize: number;
  orderNoLike?: string;
  customerId?: number;
  status?: OrderStatus;
}

/** 新增、修改订单的提交字段。 */
export interface OrderFormData {
  orderNo: string;
  customerId: number;
  amount: number;
  status: OrderStatus;
}

/** 后端统一响应包装。 */
export interface ApiResult<T> {
  success: boolean;
  errorCode: string;
  msg: string;
  data: T;
}

/** 后端分页结果。 */
export interface PageResult<T> {
  current: number;
  totalPage: number | string;
  total: number | string;
  pageSize: number;
  datas: T[];
}
