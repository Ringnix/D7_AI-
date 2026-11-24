/** 采购申请单状态，与后端 PurchaseRequestStatus 枚举保持一致。 */
export type PurchaseRequestStatus = 'DRAFT' | 'SUBMITTED' | 'APPROVED' | 'REJECTED' | 'CANCELLED';

/** 采购申请单明细项。金额由后端计算并返回。 */
export interface PurchaseRequestItem {
  id?: string;
  itemName: string;
  spec?: string;
  unit?: string;
  quantity: string | number;
  unitPrice: string | number;
  amount?: string;
  sortNo?: number;
}

/** 采购申请单列表项，主键和金额由后端以字符串返回，避免精度丢失。 */
export interface PurchaseRequestListItem {
  id: string;
  requestNo: string;
  title: string;
  applicantName: string;
  department: string;
  totalAmount: string;
  status: PurchaseRequestStatus;
  version: number;
  createdAt: string;
}

/** 采购申请单详情。 */
export interface PurchaseRequestDetail extends PurchaseRequestListItem {
  remark?: string;
  cancelReason?: string;
  cancelledAt?: string;
  approvalUserName?: string;
  approvalTime?: string;
  approvalOpinion?: string;
  updatedAt?: string;
  items: PurchaseRequestItem[];
}

/** 分页查询条件，current 从 1 开始。 */
export interface PurchaseRequestQuery {
  current: number;
  pageSize: number;
  requestNoLike?: string;
  titleLike?: string;
  status?: PurchaseRequestStatus;
}

/** 新增/修改采购申请单的提交字段。 */
export interface PurchaseRequestFormData {
  title: string;
  applicantName: string;
  department: string;
  remark?: string;
  items: Array<{
    itemName: string;
    spec?: string;
    unit?: string;
    quantity: number;
    unitPrice: number;
  }>;
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
