<template>
  <a-modal
    v-model:visible="visible"
    :title="modalTitle"
    :confirm-loading="saving"
    :mask-closable="false"
    @ok="handleOk"
    @cancel="visible = false"
  >
    <a-form :label-col="{ span: 5 }" :wrapper-col="{ span: 19 }">
      <a-form-item v-if="mode === 'approve'" :label="t('purchase-request.action.approverName')">
        <a-input
          v-model:value="approverName"
          :maxlength="100"
          :placeholder="t('purchase-request.action.approverNamePlaceholder')"
        />
      </a-form-item>
      <a-form-item :label="reasonLabel" :required="mode !== 'approve'">
        <a-textarea
          v-model:value="reason"
          :rows="4"
          :maxlength="500"
          show-count
          :placeholder="reasonPlaceholder"
        />
      </a-form-item>
    </a-form>
  </a-modal>
</template>

<script lang="ts">
  export default {
    name: 'PurchaseRequestActionModal',
  };
</script>

<script setup lang="ts">
  import { computed, ref } from 'vue';
  import { t } from 'mp-cli/i18n';
  import useMessage from 'mp-cli/hooks/use-message';
  import {
    approvePurchaseRequest,
    cancelPurchaseRequest,
    rejectPurchaseRequest,
  } from '@/service/purchase-request';
  import type { PurchaseRequestListItem } from '@/service/purchase-request/types';
  import { getServerErrorCode, isErrorPromptedByFramework } from '@/service/request-error';

  type ActionMode = 'cancel' | 'reject' | 'approve';

  const emit = defineEmits<{
    (e: 'saved'): void;
  }>();

  const { successTip, errorTip } = useMessage();

  const visible = ref(false);
  const saving = ref(false);
  const mode = ref<ActionMode>('cancel');
  const record = ref<PurchaseRequestListItem | null>(null);
  const reason = ref('');
  const approverName = ref('审批人');

  const modalTitle = computed(() => {
    const titleMap: Record<ActionMode, string> = {
      cancel: t('purchase-request.action.cancelTitle'),
      reject: t('purchase-request.action.rejectTitle'),
      approve: t('purchase-request.action.approveTitle'),
    };
    return titleMap[mode.value];
  });

  const reasonLabel = computed(() => {
    const labelMap: Record<ActionMode, string> = {
      cancel: t('purchase-request.action.cancelReason'),
      reject: t('purchase-request.action.rejectReason'),
      approve: t('purchase-request.action.opinion'),
    };
    return labelMap[mode.value];
  });

  const reasonPlaceholder = computed(() => {
    const placeholderMap: Record<ActionMode, string> = {
      cancel: t('purchase-request.action.cancelReasonPlaceholder'),
      reject: t('purchase-request.action.rejectReasonPlaceholder'),
      approve: t('purchase-request.action.opinionPlaceholder'),
    };
    return placeholderMap[mode.value];
  });

  /**
   * 打开取消/驳回/审批弹框。
   * @param currentRecord 当前行申请单
   * @param actionMode 操作类型
   */
  function open(currentRecord: PurchaseRequestListItem, actionMode: ActionMode) {
    record.value = currentRecord;
    mode.value = actionMode;
    reason.value = '';
    approverName.value = '审批人';
    visible.value = true;
  }

  async function handleOk() {
    if (!record.value) {
      return;
    }
    const trimmedReason = reason.value.trim();
    if (mode.value !== 'approve' && !trimmedReason) {
      errorTip(
        mode.value === 'cancel'
          ? t('purchase-request.message.cancelReasonRequired')
          : t('purchase-request.message.rejectReasonRequired'),
      );
      return;
    }
    saving.value = true;
    try {
      if (mode.value === 'cancel') {
        await cancelPurchaseRequest(record.value.id, record.value.version, trimmedReason);
        successTip(t('purchase-request.message.cancelSuccess'));
      } else if (mode.value === 'reject') {
        await rejectPurchaseRequest(record.value.id, record.value.version, trimmedReason);
        successTip(t('purchase-request.message.rejectSuccess'));
      } else {
        await approvePurchaseRequest(
          record.value.id,
          record.value.version,
          approverName.value.trim() || '审批人',
          trimmedReason || undefined,
        );
        successTip(t('purchase-request.message.approveSuccess'));
      }
      visible.value = false;
      emit('saved');
    } catch (error) {
      const errorCode = getServerErrorCode(error);
      if (errorCode === '90000206') {
        errorTip(t('purchase-request.message.versionConflict'));
      } else if (errorCode === '90000203') {
        errorTip(t('purchase-request.message.statusInvalid'));
      } else if (errorCode === '90000204') {
        errorTip(t('purchase-request.message.cancelReasonRequired'));
      } else if (errorCode === '90000205') {
        errorTip(t('purchase-request.message.rejectReasonRequired'));
      } else if (!isErrorPromptedByFramework(error)) {
        errorTip(t('purchase-request.message.operationFailed'));
      }
    } finally {
      saving.value = false;
    }
  }

  defineExpose({ open });
</script>
