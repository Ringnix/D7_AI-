<template>
  <a-tag class="order-status-tag" :color="statusColor">{{ statusLabel }}</a-tag>
</template>

<script lang="ts">
  export default {
    name: 'OrderStatusTag',
  };
</script>

<script setup lang="ts">
  import { computed } from 'vue';
  import { useI18n } from 'vue-i18n';
  import type { OrderStatus } from '@/service/order/types';

  interface OrderStatusTagProps {
    /** 订单状态编码 */
    status?: OrderStatus | string;
  }

  const props = withDefaults(defineProps<OrderStatusTagProps>(), {
    status: '',
  });

  const STATUS_COLOR_MAP: Record<string, string> = {
    CREATED: 'blue',
    PAID: 'green',
    CANCELLED: 'red',
  };

  const { t } = useI18n();

  const statusColor = computed(() => STATUS_COLOR_MAP[props.status] ?? 'default');

  const statusLabel = computed(() =>
    props.status ? t(`order-management.status.${props.status}`) : '--',
  );
</script>

<style scoped lang="less">
  .order-status-tag {
    margin-inline-end: 0;
  }
</style>
