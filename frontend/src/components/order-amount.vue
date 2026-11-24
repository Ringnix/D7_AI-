<template>
  <span class="order-amount">{{ displayAmount }}</span>
</template>

<script lang="ts">
  export default {
    name: 'OrderAmount',
  };
</script>

<script setup lang="ts">
  import { computed } from 'vue';

  interface OrderAmountProps {
    /** 后端返回的订单金额，字符串或数字均可 */
    value?: string | number | null;
  }

  const props = withDefaults(defineProps<OrderAmountProps>(), {
    value: null,
  });

  const displayAmount = computed(() => {
    if (props.value === null || props.value === undefined || props.value === '') {
      return '--';
    }
    const amount = Number(props.value);
    if (Number.isNaN(amount)) {
      return '--';
    }
    const text = amount.toLocaleString('zh-CN', {
      minimumFractionDigits: 2,
      maximumFractionDigits: 2,
    });
    return `¥ ${text}`;
  });
</script>
