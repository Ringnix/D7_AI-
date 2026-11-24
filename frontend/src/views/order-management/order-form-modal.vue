<template>
  <c-modal-form
    ref="modalRef"
    :key="locale"
    v-model:show="visible"
    :title="modalTitle"
    :fields="formFields"
    :form-config="formConfig"
    :width="720"
    :destroy-on-close="false"
    force-render
    :async-fn="handleSave"
    @cancel="handleCancel"
  />
</template>

<script lang="ts">
  export default {
    name: 'OrderFormModal',
  };
</script>

<script setup lang="ts">
  import { computed, nextTick, ref } from 'vue';
  import { useI18n } from 'vue-i18n';
  import { t } from 'mp-cli/i18n';
  import useMessage from 'mp-cli/hooks/use-message';
  import { createOrder, modifyOrder } from '@/service/order';
  import type { OrderFormData, OrderListItem } from '@/service/order/types';
  import { getServerErrorCode, isErrorPromptedByFramework } from '@/service/request-error';
  import { useConfig } from './use-config';

  /** 弹框内动态表单对外暴露的方法。 */
  interface DynamicFormInstance {
    resetFields: () => void | Promise<void>;
  }

  /** c-modal-form 对外暴露的方法。 */
  interface ModalFormInstance {
    formRef?: DynamicFormInstance;
    setFieldsValue: (values: Record<string, any>) => void | Promise<void>;
  }

  const emit = defineEmits<{
    (e: 'saved', payload: { isCreate: boolean }): void;
  }>();

  const { successTip, errorTip } = useMessage();
  const { locale } = useI18n();
  const { formFields } = useConfig();

  const formConfig = {
    labelWidth: 110,
  };

  const modalRef = ref<ModalFormInstance | null>(null);
  const visible = ref(false);
  const editingId = ref('');

  const modalTitle = computed(() =>
    editingId.value ? t('order-management.form.editTitle') : t('order-management.form.addTitle'),
  );

  /**
   * 打开弹框。传入订单记录时为编辑，不传时为新增。
   * @param record 当前行订单数据
   */
  async function open(record?: OrderListItem) {
    editingId.value = record?.id ?? '';
    await nextTick();
    modalRef.value?.formRef?.resetFields();
    modalRef.value?.setFieldsValue({
      orderNo: record?.orderNo ?? '',
      customerId: record ? Number(record.customerId) : undefined,
      amount: record ? Number(record.amount) : undefined,
      status: record?.status ?? 'CREATED',
    });
    visible.value = true;
  }

  /**
   * 提交订单表单，校验通过后调用新增或修改接口。
   * @param formModel 弹框校验后的表单数据
   */
  async function handleSave(formModel: OrderFormData) {
    const payload: OrderFormData = {
      orderNo: String(formModel.orderNo ?? '').trim(),
      customerId: Number(formModel.customerId),
      amount: Number(formModel.amount),
      status: formModel.status,
    };
    try {
      if (editingId.value) {
        await modifyOrder(editingId.value, payload);
        successTip(t('order-management.message.editSuccess'));
      } else {
        await createOrder(payload);
        successTip(t('order-management.message.createSuccess'));
      }
      visible.value = false;
      emit('saved', { isCreate: !editingId.value });
    } catch (error) {
      const errorCode = getServerErrorCode(error);
      if (errorCode === '90000101') {
        errorTip(t('order-management.message.orderNoExists'));
      } else if (errorCode === '90000102') {
        errorTip(t('order-management.message.orderNotFound'));
      } else if (!isErrorPromptedByFramework(error)) {
        errorTip(t('order-management.message.submitFailed'));
      }
    }
  }

  function handleCancel() {
    visible.value = false;
  }

  defineExpose({ open });
</script>
