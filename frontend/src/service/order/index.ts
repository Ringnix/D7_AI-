import { axiosInstance } from 'mp-cli/network';
import { parseJsonKeepLongAsString } from '../json';
import type {
  ApiResult,
  OrderDetail,
  OrderFormData,
  OrderListItem,
  OrderQuery,
  PageResult,
} from './types';

/**
 * 服务 B 的响应中 Long 类型字段（订单 ID、客户 ID）会以 JSON 数字返回，
 * 金额也可能以数字返回。统一转成字符串，与页面层的类型定义和后端服务 A 的返回保持一致。
 * @param item 服务 B 返回的订单数据
 */
function normalizeOrderItem<T extends OrderListItem>(item: T): T {
  return {
    ...item,
    id: String(item.id),
    customerId: String(item.customerId),
    amount: item.amount === null || item.amount === undefined ? '' : Number(item.amount).toFixed(2),
  };
}

/** 服务 B 的响应需要先做一次大整数安全解析，避免订单 ID 精度丢失。 */
function parseConsumerResponse(raw: string) {
  return parseJsonKeepLongAsString(raw);
}

/**
 * 分页查询订单。
 * 该接口经订单调用服务 B（consumer）转发到订单服务 A，用于演示 B → A 的 Feign 调用链路。
 * @param query 分页与筛选条件
 * @returns 当前页订单数据与筛选后的总条数
 */
export async function queryOrders(query: OrderQuery): Promise<PageResult<OrderListItem>> {
  const { data } = await axiosInstance.get<ApiResult<PageResult<OrderListItem>>>(
    '/consumer/orders',
    {
      params: query,
      transformResponse: [parseConsumerResponse],
    },
  );
  const page = data.data;
  return {
    ...page,
    datas: page.datas.map((item) => normalizeOrderItem(item)),
  };
}

/**
 * 查询订单详情。
 * 该接口经订单调用服务 B（consumer）转发到订单服务 A。
 * @param id 订单主键
 * @returns 订单详情
 */
export async function getOrder(id: string): Promise<OrderDetail> {
  const { data } = await axiosInstance.get<ApiResult<OrderDetail>>(`/consumer/orders/${id}`, {
    transformResponse: [parseConsumerResponse],
  });
  return normalizeOrderItem(data.data);
}

/**
 * 新增订单，直接调用订单服务 A。
 * @param formData 订单提交字段
 * @returns 新订单主键
 */
export async function createOrder(formData: OrderFormData): Promise<string> {
  const { data } = await axiosInstance.post<ApiResult<string>>('/orders', formData);
  return data.data;
}

/**
 * 完整修改订单，直接调用订单服务 A。
 * @param id 订单主键
 * @param formData 订单提交字段
 */
export async function modifyOrder(id: string, formData: OrderFormData): Promise<void> {
  await axiosInstance.put<ApiResult<void>>(`/orders/${id}`, formData);
}

/**
 * 删除订单，直接调用订单服务 A；后端采用物理删除，重复删除会返回订单不存在。
 * @param id 订单主键
 */
export async function deleteOrder(id: string): Promise<void> {
  await axiosInstance.delete<ApiResult<void>>(`/orders/${id}`);
}
