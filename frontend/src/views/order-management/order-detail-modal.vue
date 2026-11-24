<template>
  <c-modal v-model:visible="visible" :title="t('order-management.detail.title')" :width="760">
    <a-spin :spinning="loading">
      <a-descriptions bordered :column="2" size="middle">
        <a-descriptions-item :label="t('order-management.detail.orderNo')" :span="2">
          {{ detail.orderNo || '--' }}
        </a-descriptions-item>
        <a-descriptions-item :label="t('order-management.detail.customerId')">
          {{ detail.customerId || '--' }}
        </a-descriptions-item>
        <a-descriptions-item :label="t('order-management.detail.status')">
          <order-status-tag v-if="detail.status" :status="detail.status" />
          <span v-else>--</span>
        </a-descriptions-item>
        <a-descriptions-item :label="t('order-management.detail.amount')" :span="2">
          <order-amount :value="detail.amount" />
        </a-descriptions-item>
        <a-descriptions-item :label="t('order-management.detail.createdAt')" :span="2">
          {{ formatDate(detail.createdAt) || '--' }}
        </a-descriptions-item>
        <a-descriptions-item :label="t('order-management.detail.updatedAt')" :span="2">
          {{ formatDate(detail.updatedAt) || '--' }}
        </a-descriptions-item>
      </a-descriptions>
    </a-spin>
    <template #footer>
      <a-button @click="visible = false">{{ t('global.close') }}</a-button>
    </template>
  </c-modal>
</template>

<script lang="ts">
  export default {
    name: 'OrderDetailModal',
  };
</script>

<script setup lang="ts">
  import { ref } from 'vue';
  import { t } from 'mp-cli/i18n';
  import { formatDate } from 'mp-cli/utils/date';
  import useMessage from 'mp-cli/hooks/use-message';
  import OrderAmount from '@/components/order-amount.vue';
  import OrderStatusTag from '@/components/order-status-tag.vue';
  import { getOrder } from '@/service/order';
  import type { OrderDetail } from '@/service/order/types';
  import { getServerErrorCode, isErrorPromptedByFramework } from '@/service/request-error';

  const { errorTip } = useMessage();

  const visible = ref(false);
  const loading = ref(false);
  const detail = ref<Partial<OrderDetail>>({});

  /**
   * 打开订单详情弹框并加载最新数据。
   * @param id 订单主键
   */
  async function open(id: string) {
    visible.value = true;
    loading.value = true;
    detail.value = {};
    try {
      detail.value = await getOrder(id);
    } catch (error) {
      if (getServerErrorCode(error) === '90000102') {
        errorTip(t('order-management.message.orderNotFound'));
      } else if (!isErrorPromptedByFramework(error)) {
        errorTip(t('order-management.message.detailFailed'));
      }
      visible.value = false;
    } finally {
      loading.value = false;
    }
  }

  defineExpose({ open });
</script>
