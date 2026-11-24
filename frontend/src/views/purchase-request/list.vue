<template>
  <a-space class="purchase-request-list" direction="vertical">
    <c-search-panel
      :key="locale"
      :title="t('purchase-request.list.searchTitle')"
      :fields="searchFields"
      :label-width="90"
      :is-del-empty="true"
      @query="handleQuery"
      @reset="handleReset"
    />
    <c-table
      :title="t('purchase-request.list.tableTitle')"
      :columns="columns"
      :data-source="dataState.tableData"
      :loading="loading"
      :pagination="pagination"
      :actions="tableActions"
      row-key="id"
      :scroll="{ x: 1500 }"
      @change="didTableChange"
    >
      <template #totalAmount="{ record }">
        ¥ {{ Number(record.totalAmount ?? 0).toFixed(2) }}
      </template>
      <template #status="{ record }">
        <purchase-request-status-tag :status="record.status" />
      </template>
      <template #action="{ record }">
        <a-space :size="4" wrap>
          <a-button type="link" size="small" @click="openDetail(record.id)">
            {{ t('global.details') }}
          </a-button>
          <a-button
            v-if="record.status === 'DRAFT'"
            type="link"
            size="small"
            @click="openForm(record)"
          >
            {{ t('global.edit') }}
          </a-button>
          <a-button
            v-if="record.status === 'DRAFT'"
            type="link"
            size="small"
            @click="handleSubmit(record)"
          >
            {{ t('purchase-request.action.submit') }}
          </a-button>
          <a-button
            v-if="record.status === 'DRAFT' || record.status === 'SUBMITTED'"
            type="link"
            size="small"
            @click="openAction(record, 'cancel')"
          >
            {{ t('purchase-request.action.cancel') }}
          </a-button>
          <a-button
            v-if="record.status === 'SUBMITTED'"
            type="link"
            size="small"
            @click="handleWithdraw(record)"
          >
            {{ t('purchase-request.action.withdraw') }}
          </a-button>
          <a-button
            v-if="record.status === 'SUBMITTED'"
            type="link"
            size="small"
            @click="openAction(record, 'approve')"
          >
            {{ t('purchase-request.action.approve') }}
          </a-button>
          <a-button
            v-if="record.status === 'SUBMITTED'"
            type="link"
            size="small"
            danger
            @click="openAction(record, 'reject')"
          >
            {{ t('purchase-request.action.reject') }}
          </a-button>
          <a-button
            v-if="record.status === 'DRAFT'"
            type="link"
            size="small"
            danger
            @click="handleDelete(record)"
          >
            {{ t('global.delete') }}
          </a-button>
        </a-space>
      </template>
    </c-table>
    <purchase-request-form-modal ref="formModalRef" @saved="handleSaved" />
    <purchase-request-detail-modal ref="detailModalRef" />
    <purchase-request-action-modal ref="actionModalRef" @saved="getTableData()" />
  </a-space>
</template>

<script lang="ts">
  export default {
    name: 'PurchaseRequestList',
  };
</script>

<script setup lang="ts">
  import { computed, onMounted, reactive, ref } from 'vue';
  import { useI18n } from 'vue-i18n';
  import { t } from 'mp-cli/i18n';
  import useMessage from 'mp-cli/hooks/use-message';
  import useTable from 'cyberway-middle-ui-components/packages/CTable/hooks/useTable';
  import type { ISearchState } from 'cyberway-middle-ui-components/packages/CTable/hooks/useTable';
  import type { CTableAction } from 'cyberway-middle-ui-components/lib/CTable';
  import {
    deletePurchaseRequest,
    queryPurchaseRequests,
    submitPurchaseRequest,
    withdrawPurchaseRequest,
  } from '@/service/purchase-request';
  import type {
    PurchaseRequestListItem,
    PurchaseRequestStatus,
  } from '@/service/purchase-request/types';
  import { getServerErrorCode, isErrorPromptedByFramework } from '@/service/request-error';
  import { useConfig } from './use-config';
  import PurchaseRequestActionModal from './purchase-request-action-modal.vue';
  import PurchaseRequestDetailModal from './purchase-request-detail-modal.vue';
  import PurchaseRequestFormModal from './purchase-request-form-modal.vue';
  import PurchaseRequestStatusTag from './purchase-request-status-tag.vue';

  const { successTip, errorTip, deleteConfirm, baseConfirm } = useMessage();
  const { locale } = useI18n();
  const { columns, searchFields } = useConfig();

  const searchData = reactive<Record<string, any>>({});
  const formModalRef = ref<{ open: (record?: PurchaseRequestListItem) => void } | null>(null);
  const detailModalRef = ref<{ open: (id: string) => void } | null>(null);
  const actionModalRef = ref<{
    open: (record: PurchaseRequestListItem, mode: 'cancel' | 'reject' | 'approve') => void;
  } | null>(null);

  const tableActions = computed<CTableAction[]>(() => [
    {
      title: t('purchase-request.list.add'),
      key: 'add',
      type: 'primary',
      onClick: () => formModalRef.value?.open(),
    },
  ]);

  /** 组装分页查询参数；useTable 的 current 从 0 开始，后端从 1 开始。 */
  async function fetchTableData({ current, pageSize }: ISearchState) {
    try {
      const { datas, total } = await queryPurchaseRequests({
        current: current + 1,
        pageSize,
        requestNoLike: searchData.requestNoLike || undefined,
        titleLike: searchData.titleLike || undefined,
        status: (searchData.status as PurchaseRequestStatus) || undefined,
      });
      return { tableData: datas, total: Number(total) };
    } catch {
      return { tableData: [], total: 0 };
    }
  }

  const { loading, pagination, dataState, didTableChange, getTableData } = useTable({
    request: fetchTableData,
    config: { showTotal: true, defaultPageSize: 20 },
  });

  function handleQuery(formModel: Record<string, any>) {
    Object.keys(searchData).forEach((key) => delete searchData[key]);
    Object.assign(searchData, formModel);
    getTableData(true);
  }

  function handleReset() {
    Object.keys(searchData).forEach((key) => delete searchData[key]);
    getTableData(true);
  }

  function openDetail(id: string) {
    detailModalRef.value?.open(id);
  }

  function openForm(record?: PurchaseRequestListItem) {
    formModalRef.value?.open(record);
  }

  function openAction(record: PurchaseRequestListItem, mode: 'cancel' | 'reject' | 'approve') {
    actionModalRef.value?.open(record, mode);
  }

  /** 业务错误统一提示，避免与框架拦截器重复弹窗。 */
  function handleBusinessError(error: unknown) {
    const errorCode = getServerErrorCode(error);
    if (errorCode === '90000202') {
      errorTip(t('purchase-request.message.notFound'));
    } else if (errorCode === '90000203') {
      errorTip(t('purchase-request.message.statusInvalid'));
    } else if (errorCode === '90000206') {
      errorTip(t('purchase-request.message.versionConflict'));
    } else if (!isErrorPromptedByFramework(error)) {
      errorTip(t('purchase-request.message.operationFailed'));
    }
  }

  function handleSubmit(record: PurchaseRequestListItem) {
    baseConfirm(
      t('purchase-request.message.submitConfirm', { requestNo: record.requestNo }),
      async () => {
        try {
          await submitPurchaseRequest(record.id, record.version);
          successTip(t('purchase-request.message.submitSuccess'));
          getTableData();
        } catch (error) {
          handleBusinessError(error);
        }
      },
    );
  }

  function handleWithdraw(record: PurchaseRequestListItem) {
    baseConfirm(
      t('purchase-request.message.withdrawConfirm', { requestNo: record.requestNo }),
      async () => {
        try {
          await withdrawPurchaseRequest(record.id, record.version);
          successTip(t('purchase-request.message.withdrawSuccess'));
          getTableData();
        } catch (error) {
          handleBusinessError(error);
        }
      },
    );
  }

  async function handleDelete(record: PurchaseRequestListItem) {
    const content = t('purchase-request.message.deleteConfirm', { requestNo: record.requestNo });
    const confirmed = await deleteConfirm(content, false);
    if (!confirmed) {
      return;
    }
    try {
      await deletePurchaseRequest(record.id);
      successTip(t('purchase-request.message.deleteSuccess'));
      getTableData();
    } catch (error) {
      handleBusinessError(error);
    }
  }

  function handleSaved({ isCreate }: { isCreate: boolean }) {
    getTableData(isCreate);
  }

  onMounted(() => {
    getTableData(true);
  });
</script>

<style scoped lang="less">
  .purchase-request-list {
    width: 100%;
  }
</style>
