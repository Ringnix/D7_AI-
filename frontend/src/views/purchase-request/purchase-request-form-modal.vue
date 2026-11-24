<template>
  <a-modal
    v-model:visible="visible"
    :title="modalTitle"
    :width="1040"
    :confirm-loading="saving"
    :mask-closable="false"
    @ok="handleOk"
    @cancel="handleCancel"
  >
    <a-form
      ref="formRef"
      :model="formData"
      :rules="rules"
      :label-col="{ span: 5 }"
      :wrapper-col="{ span: 19 }"
    >
      <a-row :gutter="16">
        <a-col :span="12">
          <a-form-item :label="t('purchase-request.form.title')" name="title">
            <a-input
              v-model:value="formData.title"
              :maxlength="100"
              :placeholder="t('purchase-request.form.titlePlaceholder')"
            />
          </a-form-item>
        </a-col>
        <a-col :span="12">
          <a-form-item :label="t('purchase-request.form.applicantName')" name="applicantName">
            <a-input
              v-model:value="formData.applicantName"
              :maxlength="100"
              :placeholder="t('purchase-request.form.applicantNamePlaceholder')"
            />
          </a-form-item>
        </a-col>
        <a-col :span="12">
          <a-form-item :label="t('purchase-request.form.department')" name="department">
            <a-input
              v-model:value="formData.department"
              :maxlength="100"
              :placeholder="t('purchase-request.form.departmentPlaceholder')"
            />
          </a-form-item>
        </a-col>
        <a-col :span="12">
          <a-form-item :label="t('purchase-request.form.remark')" name="remark">
            <a-input
              v-model:value="formData.remark"
              :maxlength="500"
              :placeholder="t('purchase-request.form.remarkPlaceholder')"
            />
          </a-form-item>
        </a-col>
      </a-row>
    </a-form>

    <div class="items-header">
      <span class="items-title">{{ t('purchase-request.form.itemsTitle') }}</span>
      <a-button type="dashed" size="small" @click="addItem">
        {{ t('purchase-request.form.addItem') }}
      </a-button>
    </div>
    <a-table
      :columns="itemColumns"
      :data-source="items"
      :pagination="false"
      size="small"
      bordered
      row-key="_key"
    >
      <template #bodyCell="{ column, record, index }">
        <template v-if="column.key === 'itemName'">
          <a-input
            v-model:value="record.itemName"
            :maxlength="100"
            :placeholder="t('purchase-request.form.itemNamePlaceholder')"
          />
        </template>
        <template v-else-if="column.key === 'spec'">
          <a-input
            v-model:value="record.spec"
            :maxlength="100"
            :placeholder="t('purchase-request.form.specPlaceholder')"
          />
        </template>
        <template v-else-if="column.key === 'unit'">
          <a-input
            v-model:value="record.unit"
            :maxlength="20"
            :placeholder="t('purchase-request.form.unitPlaceholder')"
          />
        </template>
        <template v-else-if="column.key === 'quantity'">
          <a-input-number
            v-model:value="record.quantity"
            :min="0.01"
            :precision="2"
            style="width: 100%"
            :placeholder="t('purchase-request.form.quantityPlaceholder')"
          />
        </template>
        <template v-else-if="column.key === 'unitPrice'">
          <a-input-number
            v-model:value="record.unitPrice"
            :min="0.01"
            :precision="2"
            style="width: 100%"
            :placeholder="t('purchase-request.form.unitPricePlaceholder')"
          />
        </template>
        <template v-else-if="column.key === 'amount'"> ¥ {{ itemAmount(record) }} </template>
        <template v-else-if="column.key === 'action'">
          <a-button type="link" size="small" danger @click="removeItem(index)">
            {{ t('purchase-request.form.removeItem') }}
          </a-button>
        </template>
      </template>
    </a-table>
    <div class="items-total">
      {{ t('purchase-request.form.totalAmount') }}：¥ {{ totalAmount }}
    </div>
  </a-modal>
</template>

<script lang="ts">
  export default {
    name: 'PurchaseRequestFormModal',
  };
</script>

<script setup lang="ts">
  import { computed, reactive, ref } from 'vue';
  import { t } from 'mp-cli/i18n';
  import useMessage from 'mp-cli/hooks/use-message';
  import {
    createPurchaseRequest,
    getPurchaseRequest,
    modifyPurchaseRequest,
  } from '@/service/purchase-request';
  import type {
    PurchaseRequestFormData,
    PurchaseRequestListItem,
  } from '@/service/purchase-request/types';
  import { getServerErrorCode, isErrorPromptedByFramework } from '@/service/request-error';

  interface ItemRow {
    _key: number;
    itemName: string;
    spec?: string;
    unit?: string;
    quantity?: number;
    unitPrice?: number;
  }

  interface FormInstance {
    validate: () => Promise<void>;
    resetFields: () => void;
  }

  const emit = defineEmits<{
    (e: 'saved', payload: { isCreate: boolean }): void;
  }>();

  const { successTip, errorTip } = useMessage();

  const visible = ref(false);
  const saving = ref(false);
  const formRef = ref<FormInstance | null>(null);
  const editingId = ref('');
  const editingVersion = ref(0);
  let keySeed = 0;

  const formData = reactive<PurchaseRequestFormData>({
    title: '',
    applicantName: '',
    department: '',
    remark: '',
    items: [],
  });

  const items = ref<ItemRow[]>([]);

  const rules = {
    title: [
      {
        required: true,
        whitespace: true,
        message: t('purchase-request.form.titleRequired'),
      },
    ],
    applicantName: [
      {
        required: true,
        whitespace: true,
        message: t('purchase-request.form.applicantNameRequired'),
      },
    ],
    department: [
      {
        required: true,
        whitespace: true,
        message: t('purchase-request.form.departmentRequired'),
      },
    ],
  };

  const itemColumns = computed(() => [
    {
      title: t('purchase-request.form.itemName'),
      dataIndex: 'itemName',
      key: 'itemName',
      width: 200,
    },
    { title: t('purchase-request.form.spec'), dataIndex: 'spec', key: 'spec', width: 160 },
    { title: t('purchase-request.form.unit'), dataIndex: 'unit', key: 'unit', width: 90 },
    {
      title: t('purchase-request.form.quantity'),
      dataIndex: 'quantity',
      key: 'quantity',
      width: 120,
    },
    {
      title: t('purchase-request.form.unitPrice'),
      dataIndex: 'unitPrice',
      key: 'unitPrice',
      width: 130,
    },
    { title: t('purchase-request.form.amount'), dataIndex: 'amount', key: 'amount', width: 120 },
    { title: t('global.operation'), dataIndex: 'action', key: 'action', width: 90 },
  ]);

  const modalTitle = computed(() =>
    editingId.value ? t('purchase-request.form.editTitle') : t('purchase-request.form.addTitle'),
  );

  const totalAmount = computed(() =>
    items.value
      .reduce((sum, item) => {
        const quantity = Number(item.quantity ?? 0);
        const unitPrice = Number(item.unitPrice ?? 0);
        if (Number.isNaN(quantity) || Number.isNaN(unitPrice)) {
          return sum;
        }
        return sum + quantity * unitPrice;
      }, 0)
      .toFixed(2),
  );

  function itemAmount(record: ItemRow): string {
    const quantity = Number(record.quantity ?? 0);
    const unitPrice = Number(record.unitPrice ?? 0);
    if (Number.isNaN(quantity) || Number.isNaN(unitPrice)) {
      return '0.00';
    }
    return (quantity * unitPrice).toFixed(2);
  }

  function addItem() {
    keySeed += 1;
    items.value.push({
      _key: keySeed,
      itemName: '',
      spec: '',
      unit: '',
      quantity: undefined,
      unitPrice: undefined,
    });
  }

  function removeItem(index: number) {
    items.value.splice(index, 1);
  }

  function resetForm() {
    formData.title = '';
    formData.applicantName = '';
    formData.department = '';
    formData.remark = '';
    items.value = [];
    formRef.value?.resetFields();
    addItem();
  }

  /**
   * 打开弹框。传入列表记录时为编辑，会重新查询详情取得明细和版本号。
   * @param record 当前行申请单
   */
  async function open(record?: PurchaseRequestListItem) {
    editingId.value = record?.id ?? '';
    editingVersion.value = record?.version ?? 0;
    resetForm();
    visible.value = true;
    if (record) {
      try {
        const detail = await getPurchaseRequest(record.id);
        formData.title = detail.title;
        formData.applicantName = detail.applicantName;
        formData.department = detail.department;
        formData.remark = detail.remark ?? '';
        editingVersion.value = detail.version;
        items.value = detail.items.map((item) => {
          keySeed += 1;
          return {
            _key: keySeed,
            itemName: item.itemName,
            spec: item.spec ?? '',
            unit: item.unit ?? '',
            quantity: Number(item.quantity),
            unitPrice: Number(item.unitPrice),
          };
        });
      } catch {
        visible.value = false;
        errorTip(t('purchase-request.message.detailFailed'));
      }
    }
  }

  /** 校验明细：至少一行，且物品名称、数量、单价均填写有效。 */
  function validateItems(): string {
    if (items.value.length === 0) {
      return t('purchase-request.message.itemsRequired');
    }
    for (let index = 0; index < items.value.length; index += 1) {
      const item = items.value[index];
      if (!item.itemName || !item.itemName.trim()) {
        return t('purchase-request.message.itemNameRequired', { index: index + 1 });
      }
      if (!item.quantity || Number(item.quantity) <= 0) {
        return t('purchase-request.message.quantityRequired', { index: index + 1 });
      }
      if (!item.unitPrice || Number(item.unitPrice) <= 0) {
        return t('purchase-request.message.unitPriceRequired', { index: index + 1 });
      }
    }
    return '';
  }

  async function handleOk() {
    try {
      await formRef.value?.validate();
    } catch {
      return;
    }
    const itemError = validateItems();
    if (itemError) {
      errorTip(itemError);
      return;
    }
    const payload: PurchaseRequestFormData = {
      title: formData.title.trim(),
      applicantName: formData.applicantName.trim(),
      department: formData.department.trim(),
      remark: formData.remark?.trim() || undefined,
      items: items.value.map((item) => ({
        itemName: item.itemName.trim(),
        spec: item.spec?.trim() || undefined,
        unit: item.unit?.trim() || undefined,
        quantity: Number(item.quantity),
        unitPrice: Number(item.unitPrice),
      })),
    };
    saving.value = true;
    try {
      if (editingId.value) {
        await modifyPurchaseRequest(editingId.value, payload, editingVersion.value);
        successTip(t('purchase-request.message.editSuccess'));
      } else {
        await createPurchaseRequest(payload);
        successTip(t('purchase-request.message.createSuccess'));
      }
      visible.value = false;
      emit('saved', { isCreate: !editingId.value });
    } catch (error) {
      const errorCode = getServerErrorCode(error);
      if (errorCode === '90000206') {
        errorTip(t('purchase-request.message.versionConflict'));
      } else if (errorCode === '90000203') {
        errorTip(t('purchase-request.message.statusInvalid'));
      } else if (!isErrorPromptedByFramework(error)) {
        errorTip(t('purchase-request.message.saveFailed'));
      }
    } finally {
      saving.value = false;
    }
  }

  function handleCancel() {
    visible.value = false;
  }

  defineExpose({ open });
</script>

<style scoped lang="less">
  .items-header {
    display: flex;
    align-items: center;
    justify-content: space-between;
    margin: 8px 0 12px;
  }

  .items-title {
    font-weight: 600;
  }

  .items-total {
    margin-top: 12px;
    font-weight: 600;
    text-align: right;
  }
</style>
