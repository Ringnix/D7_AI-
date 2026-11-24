/** 采购申请单状态标签，列表与详情共用。 */
<template>
  <a-tag :color="color">{{ t(`purchase-request.status.${status}`) }}</a-tag>
</template>

<script lang="ts">
  import { computed } from 'vue';
  import type { PurchaseRequestStatus } from '@/service/purchase-request/types';
  import { t } from 'mp-cli/i18n';

  export default {
    name: 'PurchaseRequestStatusTag',
    props: {
      status: {
        type: String,
        default: '',
      },
    },
    setup(props: { status: PurchaseRequestStatus }) {
      const color = computed(() => {
        const colorMap: Record<string, string> = {
          DRAFT: 'default',
          SUBMITTED: 'processing',
          APPROVED: 'success',
          REJECTED: 'error',
          CANCELLED: 'warning',
        };
        return colorMap[props.status] ?? 'default';
      });
      return { color, t };
    },
  };
</script>
