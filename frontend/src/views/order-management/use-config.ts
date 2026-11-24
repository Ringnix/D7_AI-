import { computed } from 'vue';
import { useI18n } from 'vue-i18n';
import type { FormItemSchema } from 'cyberway-middle-ui-components/lib/CDynamicForm';
import type { CTableCellAction, CTableColumn } from 'cyberway-middle-ui-components/lib/CTable';
import type { OrderStatus } from '@/service/order/types';

/** 订单状态选项，与后端枚举保持一致。 */
const ORDER_STATUS_LIST: OrderStatus[] = ['CREATED', 'PAID', 'CANCELLED'];

/**
 * 订单管理列表、查询面板和表单的字段配置。
 * 配置使用 computed 生成，切换语言后表格列、查询项和表单项会同步更新。
 */
export function useConfig() {
  const { t } = useI18n();

  const statusOptions = computed(() =>
    ORDER_STATUS_LIST.map((status) => ({
      value: status,
      label: t(`order-management.status.${status}`),
    })),
  );

  const columns = computed<CTableColumn[]>(() => [
    {
      title: t('order-management.list.orderNo'),
      dataIndex: 'orderNo',
      key: 'orderNo',
      slotName: 'orderNo',
      width: 220,
      fixed: 'left',
    },
    {
      title: t('order-management.list.customerId'),
      dataIndex: 'customerId',
      key: 'customerId',
      width: 120,
    },
    {
      title: t('order-management.list.amount'),
      dataIndex: 'amount',
      key: 'amount',
      slotName: 'amount',
      width: 150,
      align: 'right',
    },
    {
      title: t('order-management.list.status'),
      dataIndex: 'status',
      key: 'status',
      slotName: 'status',
      width: 120,
    },
    {
      title: t('order-management.list.createdAt'),
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
      width: 180,
      fixed: 'right',
    },
  ]);

  const cellActions = computed<CTableCellAction[]>(() => [
    {
      title: t('global.details'),
      key: 'details',
    },
    {
      title: t('global.edit'),
      key: 'edit',
    },
    {
      title: t('global.delete'),
      key: 'delete',
      // 删除操作需要红色文字，使用插槽自定义渲染
      slot: 'deleteAction',
    },
  ]);

  const searchFields = computed<FormItemSchema[]>(() => [
    {
      field: 'orderNoLike',
      label: t('order-management.list.orderNo'),
      component: 'Input',
      colProps: { span: 8 },
      componentProps: {
        maxlength: 32,
        allowClear: true,
        placeholder: t('order-management.form.orderNoPlaceholder'),
      },
    },
    {
      field: 'customerId',
      label: t('order-management.list.customerId'),
      component: 'InputNumber',
      colProps: { span: 8 },
      componentProps: {
        min: 1,
        precision: 0,
        placeholder: t('order-management.form.customerIdPlaceholder'),
        style: { width: '100%' },
      },
    },
    {
      field: 'status',
      label: t('order-management.list.status'),
      component: 'Select',
      colProps: { span: 8 },
      componentProps: {
        allowClear: true,
        options: statusOptions.value,
        placeholder: t('order-management.form.statusPlaceholder'),
      },
    },
  ]);

  /**
   * 校验订单金额：必填、大于 0、最多两位小数。
   * @param value 输入框中的金额
   */
  function validateAmount(_rule: unknown, value: number | string | null): Promise<void> {
    if (value === null || value === undefined || value === '') {
      return Promise.reject(new Error(t('order-management.form.amountRequired')));
    }
    const amount = Number(value);
    if (Number.isNaN(amount) || amount <= 0) {
      return Promise.reject(new Error(t('order-management.form.amountInvalid')));
    }
    if (!/^\d+(\.\d{1,2})?$/.test(String(value))) {
      return Promise.reject(new Error(t('order-management.form.amountScale')));
    }
    return Promise.resolve();
  }

  const formFields = computed<FormItemSchema[]>(() => [
    {
      field: 'orderNo',
      label: t('order-management.form.orderNo'),
      component: 'Input',
      colProps: { span: 24 },
      componentProps: {
        maxlength: 32,
        showCount: true,
        placeholder: t('order-management.form.orderNoPlaceholder'),
      },
      rules: [
        { required: true, whitespace: true, message: t('order-management.form.orderNoRequired') },
        { max: 32, message: t('order-management.form.orderNoMax') },
      ],
    },
    {
      field: 'customerId',
      label: t('order-management.form.customerId'),
      component: 'InputNumber',
      colProps: { span: 12 },
      componentProps: {
        min: 1,
        precision: 0,
        style: { width: '100%' },
        placeholder: t('order-management.form.customerIdPlaceholder'),
      },
      rules: [{ required: true, message: t('order-management.form.customerIdRequired') }],
    },
    {
      field: 'amount',
      label: t('order-management.form.amount'),
      component: 'InputNumber',
      colProps: { span: 12 },
      componentProps: {
        min: 0.01,
        precision: 2,
        step: 0.01,
        style: { width: '100%' },
        placeholder: t('order-management.form.amountPlaceholder'),
      },
      rules: [{ required: true, validator: validateAmount, trigger: 'blur' }],
    },
    {
      field: 'status',
      label: t('order-management.form.status'),
      component: 'Select',
      colProps: { span: 24 },
      componentProps: {
        options: statusOptions.value,
        placeholder: t('order-management.form.statusPlaceholder'),
      },
      rules: [{ required: true, message: t('order-management.form.statusRequired') }],
    },
  ]);

  return {
    columns,
    cellActions,
    searchFields,
    formFields,
  };
}
