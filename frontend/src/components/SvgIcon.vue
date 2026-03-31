<template>
  <span class="svg-icon" v-html="content"></span>
</template>

<script setup lang="ts">
defineProps<{
  content: string
}>()
</script>

<style scoped>
.svg-icon {
  display: inline-flex;
  width: 18px;
  height: 18px;
}

.svg-icon :deep(svg) {
  width: 100%;
  height: 100%;
  stroke: currentColor;
}
</style>