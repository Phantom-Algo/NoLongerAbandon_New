<template>
  <span class="svg-icon" :style="sizeStyle" v-html="content"></span>
</template>

<script setup lang="ts">
import { computed } from 'vue'

const props = withDefaults(defineProps<{
  content: string
  size?: number | string
}>(), {
  size: 18
})

const sizeStyle = computed(() => {
  const s = typeof props.size === 'number' ? `${props.size}px` : props.size
  return { width: s, height: s }
})
</script>

<style scoped>
.svg-icon {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
}

.svg-icon :deep(svg) {
  width: 100%;
  height: 100%;
  stroke: currentColor;
}
</style>