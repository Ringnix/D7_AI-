import { axiosInstance } from 'mp-cli/network';
import type {
  ApiResult,
  PageResult,
  PurchaseRequestDetail,
  PurchaseRequestFormData,
  PurchaseRequestListItem,
  PurchaseRequestQuery,
} from './types';

/**
 * 分页查询采购申请单。
 * @param query 分页与筛选条件，current 从 1 开始
 */
export async function queryPurchaseRequests(
  query: PurchaseRequestQuery,
): Promise<PageResult<PurchaseRequestListItem>> {
  const { data } = await axiosInstance.get<ApiResult<PageResult<PurchaseRequestListItem>>>(
    '/purchase-requests',
    { params: query },
  );
  return data.data;
}

/**
 * 查询采购申请单详情（含明细）。
 * @param id 申请单主键
 */
export async function getPurchaseRequest(id: string): Promise<PurchaseRequestDetail> {
  const { data } = await axiosInstance.get<ApiResult<PurchaseRequestDetail>>(
    `/purchase-requests/${id}`,
  );
  return data.data;
}

/**
 * 新增采购申请单（保存为草稿）。
 * @param formData 申请单提交字段
 */
export async function createPurchaseRequest(formData: PurchaseRequestFormData): Promise<string> {
  const { data } = await axiosInstance.post<ApiResult<string>>('/purchase-requests', formData);
  return data.data;
}

/**
 * 修改采购申请单（仅草稿，需携带版本号做乐观锁）。
 * @param id 申请单主键
 * @param formData 申请单提交字段
 * @param version 当前版本号
 */
export async function modifyPurchaseRequest(
  id: string,
  formData: PurchaseRequestFormData,
  version: number,
): Promise<void> {
  await axiosInstance.put<ApiResult<void>>(`/purchase-requests/${id}`, { ...formData, version });
}

/**
 * 删除采购申请单（仅草稿，物理删除）。
 * @param id 申请单主键
 */
export async function deletePurchaseRequest(id: string): Promise<void> {
  await axiosInstance.delete<ApiResult<void>>(`/purchase-requests/${id}`);
}

/** 提交采购申请单（草稿 -> 待审批）。 */
export async function submitPurchaseRequest(id: string, version: number): Promise<void> {
  await axiosInstance.post<ApiResult<void>>(`/purchase-requests/${id}/submit`, { version });
}

/** 撤回采购申请单（待审批 -> 草稿）。 */
export async function withdrawPurchaseRequest(id: string, version: number): Promise<void> {
  await axiosInstance.post<ApiResult<void>>(`/purchase-requests/${id}/withdraw`, { version });
}

/** 取消采购申请单（取消原因必填）。 */
export async function cancelPurchaseRequest(
  id: string,
  version: number,
  reason: string,
): Promise<void> {
  await axiosInstance.post<ApiResult<void>>(`/purchase-requests/${id}/cancel`, { version, reason });
}

/** 审批通过采购申请单。 */
export async function approvePurchaseRequest(
  id: string,
  version: number,
  approverName: string,
  opinion?: string,
): Promise<void> {
  await axiosInstance.post<ApiResult<void>>(`/purchase-requests/${id}/approve`, {
    version,
    approverName,
    opinion,
  });
}

/** 审批驳回采购申请单（驳回原因必填）。 */
export async function rejectPurchaseRequest(
  id: string,
  version: number,
  reason: string,
): Promise<void> {
  await axiosInstance.post<ApiResult<void>>(`/purchase-requests/${id}/reject`, { version, reason });
}
