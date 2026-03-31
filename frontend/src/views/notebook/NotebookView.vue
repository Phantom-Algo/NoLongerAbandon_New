<template>
  <el-card shadow="never">
    <template #header>
      <span>单词本模块骨架</span>
    </template>
    <el-table :data="rows" style="width: 100%">
      <el-table-column prop="name" label="单词本名称" />
      <el-table-column prop="count" label="单词数" width="120" />
      <el-table-column prop="sortMode" label="默认排序" width="160" />
    </el-table>
  </el-card>
</template>

<script setup lang="ts">
const rows = [
  { name: '考研高频', count: 0, sortMode: '加入时间' },
  { name: '易错词复盘', count: 0, sortMode: '搜索次数' }
]
</script>