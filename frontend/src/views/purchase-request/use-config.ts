import { computed } from 'vue';
import type { CTableColumn } from 'cyberway-middle-ui-components/lib/CTable';
import type { FormItemSchema } from 'cyberway-middle-ui-components/lib/CDynamicForm';
import { t } from 'mp-cli/i18n';
import type { PurchaseRequestStatus } from '@/service/purchase-request/types';

/** 采购申请单状态下拉选项。 */
const STATUS_LIST: PurchaseRequestStatus[] = [
  'DRAFT',
  'SUBMITTED',
  'APPROVED',
  'REJECTED',
  'CANCELLED',
];

/**
 * 采购申请单列表、查询面板的字段配置。
 * 使用 computed 并在语言切换时通过 locale 重新生成，保证中英文文案同步。
 */
export function useConfig() {
  const statusOptions = computed(() =>
    STATUS_LIST.map((status) => ({
      value: status,
      label: t(`purchase-request.status.${status}`),
    })),
  );

  const columns = computed<CTableColumn[]>(() => [
    {
      title: t('purchase-request.list.requestNo'),
      dataIndex: 'requestNo',
      key: 'requestNo',
      width: 220,
      fixed: 'left',
    },
    {
      title: t('purchase-request.list.title'),
      dataIndex: 'title',
      key: 'title',
      width: 220,
      ellipsis: true,
    },
    {
      title: t('purchase-request.list.applicantName'),
      dataIndex: 'applicantName',
      key: 'applicantName',
      width: 120,
    },
    {
      title: t('purchase-request.list.department'),
      dataIndex: 'department',
      key: 'department',
      width: 120,
    },
    {
      title: t('purchase-request.list.totalAmount'),
      dataIndex: 'totalAmount',
      key: 'totalAmount',
      slotName: 'totalAmount',
      width: 150,
      align: 'right',
    },
    {
      title: t('purchase-request.list.status'),
      dataIndex: 'status',
      key: 'status',
      slotName: 'status',
      width: 120,
    },
    {
      title: t('purchase-request.list.createdAt'),
      dataIndex: 'createdAt',
      key: 'createdAt',
      width: 180,
      template: {
        mode: 'datetime',
        props: {
          pattern: 'YYYY-MM-DD HH:mm:ss',
        },
      },
    },
    {
      title: t('global.operation'),
      dataIndex: 'action',
      key: 'action',
      slotName: 'action',
      width: 320,
      fixed: 'right',
    },
  ]);

  const searchFields = computed<FormItemSchema[]>(() => [
    {
      field: 'requestNoLike',
      label: t('purchase-request.list.requestNo'),
      component: 'Input',
      colProps: { span: 8 },
      componentProps: {
        maxlength: 32,
        allowClear: true,
        placeholder: t('purchase-request.form.requestNoPlaceholder'),
      },
    },
    {
      field: 'titleLike',
      label: t('purchase-request.list.title'),
      component: 'Input',
      colProps: { span: 8 },
      componentProps: {
        maxlength: 100,
        allowClear: true,
        placeholder: t('purchase-request.form.titlePlaceholder'),
      },
    },
    {
      field: 'status',
      label: t('purchase-request.list.status'),
      component: 'Select',
      colProps: { span: 8 },
      componentProps: {
        allowClear: true,
        options: statusOptions.value,
        placeholder: t('purchase-request.form.statusPlaceholder'),
      },
    },
  ]);

  return {
    columns,
    searchFields,
    statusOptions,
  };
}
