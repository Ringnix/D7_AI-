<template>
  <a-modal v-model:visible="visible" :title="t('purchase-request.detail.title')" :width="900">
    <a-spin :spinning="loading">
      <a-descriptions bordered :column="2" size="middle">
        <a-descriptions-item :label="t('purchase-request.detail.requestNo')" :span="2">
          {{ detail.requestNo || '--' }}
        </a-descriptions-item>
        <a-descriptions-item :label="t('purchase-request.detail.title')" :span="2">
          {{ detail.title || '--' }}
        </a-descriptions-item>
        <a-descriptions-item :label="t('purchase-request.detail.applicantName')">
          {{ detail.applicantName || '--' }}
        </a-descriptions-item>
        <a-descriptions-item :label="t('purchase-request.detail.department')">
          {{ detail.department || '--' }}
        </a-descriptions-item>
        <a-descriptions-item :label="t('purchase-request.detail.status')">
          <purchase-request-status-tag v-if="detail.status" :status="detail.status" />
          <span v-else>--</span>
        </a-descriptions-item>
        <a-descriptions-item :label="t('purchase-request.detail.totalAmount')">
          ¥ {{ Number(detail.totalAmount ?? 0).toFixed(2) }}
        </a-descriptions-item>
        <a-descriptions-item :label="t('purchase-request.detail.remark')" :span="2">
          {{ detail.remark || '--' }}
        </a-descriptions-item>
        <a-descriptions-item :label="t('purchase-request.detail.createdAt')">
          {{ formatDate(detail.createdAt) || '--' }}
        </a-descriptions-item>
        <a-descriptions-item :label="t('purchase-request.detail.updatedAt')">
          {{ formatDate(detail.updatedAt) || '--' }}
        </a-descriptions-item>
        <a-descriptions-item :label="t('purchase-request.detail.cancelReason')" :span="2">
          {{ detail.cancelReason || '--' }}
        </a-descriptions-item>
        <a-descriptions-item :label="t('purchase-request.detail.approvalUserName')">
          {{ detail.approvalUserName || '--' }}
        </a-descriptions-item>
        <a-descriptions-item :label="t('purchase-request.detail.approvalTime')">
          {{ formatDate(detail.approvalTime) || '--' }}
        </a-descriptions-item>
        <a-descriptions-item :label="t('purchase-request.detail.approvalOpinion')" :span="2">
          {{ detail.approvalOpinion || '--' }}
        </a-descriptions-item>
      </a-descriptions>

      <div class="detail-items-title">{{ t('purchase-request.detail.items') }}</div>
      <a-table
        :columns="itemColumns"
        :data-source="detail.items ?? []"
        :pagination="false"
        size="small"
        bordered
        row-key="id"
      >
        <template #bodyCell="{ column, record }">
          <template v-if="column.key === 'amount'">
            ¥ {{ Number(record.amount ?? 0).toFixed(2) }}
          </template>
        </template>
      </a-table>
    </a-spin>
    <template #footer>
      <a-button @click="visible = false">{{ t('global.close') }}</a-button>
    </template>
  </a-modal>
</template>

<script lang="ts">
  export default {
    name: 'PurchaseRequestDetailModal',
  };
</script>

<script setup lang="ts">
  import { computed, ref } from 'vue';
  import { t } from 'mp-cli/i18n';
  import { formatDate } from 'mp-cli/utils/date';
  import useMessage from 'mp-cli/hooks/use-message';
  import { getPurchaseRequest } from '@/service/purchase-request';
  import type { PurchaseRequestDetail } from '@/service/purchase-request/types';
  import { getServerErrorCode, isErrorPromptedByFramework } from '@/service/request-error';
  import PurchaseRequestStatusTag from './purchase-request-status-tag.vue';

  const { errorTip } = useMessage();

  const visible = ref(false);
  const loading = ref(false);
  const detail = ref<Partial<PurchaseRequestDetail>>({});

  const itemColumns = computed(() => [
    { title: t('purchase-request.detail.itemName'), dataIndex: 'itemName', key: 'itemName' },
    { title: t('purchase-request.detail.spec'), dataIndex: 'spec', key: 'spec' },
    { title: t('purchase-request.detail.unit'), dataIndex: 'unit', key: 'unit', width: 80 },
    {
      title: t('purchase-request.detail.quantity'),
      dataIndex: 'quantity',
      key: 'quantity',
      width: 100,
    },
    {
      title: t('purchase-request.detail.unitPrice'),
      dataIndex: 'unitPrice',
      key: 'unitPrice',
      width: 120,
    },
    { title: t('purchase-request.detail.amount'), dataIndex: 'amount', key: 'amount', width: 120 },
  ]);

  /**
   * 打开详情弹框并加载最新数据。
   * @param id 申请单主键
   */
  async function open(id: string) {
    visible.value = true;
    loading.value = true;
    detail.value = {};
    try {
      detail.value = await getPurchaseRequest(id);
    } catch (error) {
      if (getServerErrorCode(error) === '90000202') {
        errorTip(t('purchase-request.message.notFound'));
      } else if (!isErrorPromptedByFramework(error)) {
        errorTip(t('purchase-request.message.detailFailed'));
      }
      visible.value = false;
    } finally {
      loading.value = false;
    }
  }

  defineExpose({ open });
</script>

<style scoped lang="less">
  .detail-items-title {
    margin: 16px 0 8px;
    font-weight: 600;
  }
</style>
