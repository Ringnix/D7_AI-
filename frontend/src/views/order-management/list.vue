<template>
  <a-space class="order-management-list" direction="vertical">
    <c-search-panel
      :key="locale"
      :title="t('order-management.list.searchTitle')"
      :fields="searchFields"
      :label-width="90"
      :is-del-empty="true"
      @query="handleQuery"
      @reset="handleReset"
    />
    <c-table
      :title="t('order-management.list.tableTitle')"
      :columns="columns"
      :data-source="dataState.tableData"
      :loading="loading"
      :pagination="pagination"
      :actions="tableActions"
      :cell-actions="cellActions"
      row-key="id"
      :scroll="{ x: 1040 }"
      @change="didTableChange"
      @did-click-cell="handleCellAction"
    >
      <template #orderNo="{ record }">
        <a-button type="link" size="small" @click="openDetail(record.id)">
          {{ record.orderNo }}
        </a-button>
      </template>
      <template #amount="{ record }">
        <order-amount :value="record.amount" />
      </template>
      <template #status="{ record }">
        <order-status-tag :status="record.status" />
      </template>
      <template #deleteAction="{ record }">
        <a class="order-management-list__delete" @click.stop="removeOrder(record)">{{
          t('global.delete')
        }}</a>
      </template>
    </c-table>
    <order-form-modal ref="formModalRef" @saved="handleSaved" />
    <order-detail-modal ref="detailModalRef" />
  </a-space>
</template>

<script lang="ts">
  export default {
    name: 'OrderManagementList',
  };
</script>

<script setup lang="ts">
  import { computed, onMounted, reactive, ref } from 'vue';
  import { useI18n } from 'vue-i18n';
  import { t } from 'mp-cli/i18n';
  import useMessage from 'mp-cli/hooks/use-message';
  import useTable from 'cyberway-middle-ui-components/packages/CTable/hooks/useTable';
  import type { ISearchState } from 'cyberway-middle-ui-components/packages/CTable/hooks/useTable';
  import type { CTableCellAction, CTableAction } from 'cyberway-middle-ui-components/lib/CTable';
  import OrderAmount from '@/components/order-amount.vue';
  import OrderStatusTag from '@/components/order-status-tag.vue';
  import { deleteOrder, queryOrders } from '@/service/order';
  import type { OrderListItem, OrderStatus } from '@/service/order/types';
  import { getServerErrorCode, isErrorPromptedByFramework } from '@/service/request-error';
  import OrderDetailModal from './order-detail-modal.vue';
  import OrderFormModal from './order-form-modal.vue';
  import { useConfig } from './use-config';

  const { successTip, errorTip, deleteConfirm } = useMessage();
  const { locale } = useI18n();
  const { columns, cellActions, searchFields } = useConfig();

  const searchData = reactive<Record<string, any>>({});
  const formModalRef = ref<{ open: (record?: OrderListItem) => void } | null>(null);
  const detailModalRef = ref<{ open: (id: string) => void } | null>(null);

  const tableActions = computed<CTableAction[]>(() => [
    {
      title: t('order-management.list.add'),
      key: 'add',
      type: 'primary',
      onClick: () => formModalRef.value?.open(),
    },
  ]);

  /**
   * 组装分页查询参数并请求后端，供表格 useTable 使用。
   * useTable 的 current 从 0 开始，后端从 1 开始。
   */
  async function fetchTableData({ current, pageSize }: ISearchState) {
    try {
      const { datas, total } = await queryOrders({
        current: current + 1,
        pageSize,
        orderNoLike: searchData.orderNoLike || undefined,
        customerId: searchData.customerId ? Number(searchData.customerId) : undefined,
        status: (searchData.status as OrderStatus) || undefined,
      });
      return { tableData: datas, total: Number(total) };
    } catch (error) {
      return { tableData: [], total: 0 };
    }
  }

  const { loading, pagination, dataState, didTableChange, getTableData } = useTable({
    request: fetchTableData,
    config: { showTotal: true, defaultPageSize: 20 },
  });

  /** 查询面板触发查询时，用最新的表单值替换上一次的查询条件。 */
  function handleQuery(formModel: Record<string, any>) {
    Object.keys(searchData).forEach((key) => delete searchData[key]);
    Object.assign(searchData, formModel);
    getTableData(true);
  }

  /** 查询面板重置后清空筛选条件并回到第一页。 */
  function handleReset() {
    Object.keys(searchData).forEach((key) => delete searchData[key]);
    getTableData(true);
  }

  function openDetail(id: string) {
    detailModalRef.value?.open(id);
  }

  /** 表格行内操作：详情、编辑（删除使用自定义插槽，见 deleteAction 插槽）。 */
  function handleCellAction({
    record,
    action,
  }: {
    record: OrderListItem;
    action: CTableCellAction;
  }) {
    if (action.key === 'details') {
      openDetail(record.id);
      return;
    }
    if (action.key === 'edit') {
      formModalRef.value?.open(record);
    }
  }

  async function removeOrder(record: OrderListItem) {
    const content = t('order-management.message.deleteConfirm', { orderNo: record.orderNo });
    // 第二个参数传 false：关闭删除确认框自带的“操作成功”提示，改为接口成功后再提示，
    // 避免删除失败（如订单不存在）时仍弹出“操作成功”。
    const confirmed = await deleteConfirm(content, false);
    if (!confirmed) {
      return;
    }
    try {
      await deleteOrder(record.id);
      successTip(t('order-management.message.deleteSuccess'));
      getTableData();
    } catch (error) {
      if (getServerErrorCode(error) === '90000102') {
        errorTip(t('order-management.message.orderNotFound'));
      } else if (!isErrorPromptedByFramework(error)) {
        errorTip(t('order-management.message.deleteFailed'));
      }
    }
  }

  /** 新增或编辑完成后刷新列表，新增订单时回到第一页。 */
  function handleSaved({ isCreate }: { isCreate: boolean }) {
    getTableData(isCreate);
  }

  onMounted(() => {
    getTableData(true);
  });
</script>

<style scoped lang="less">
  .order-management-list {
    width: 100%;
  }

  .order-management-list__delete {
    /* 覆盖组件库操作列链接的默认主题色（.c-table .action-area a） */
    color: #ff4d4f !important;
    text-decoration: none;

    &:hover {
      color: #ff7875 !important;
    }
  }
</style>
