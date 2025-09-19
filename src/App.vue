<template>
  <div id="app">
    <router-view />
  </div>
</template>

<script setup>
// App.vue 保持简洁，只包含路由视图
</script>

<style>
#app {
  min-height: 100vh;
}

* {
  margin: 0;
  padding: 0;
  box-sizing: border-box;
}

body {
  font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, 'Helvetica Neue', Arial, sans-serif;
  background-color: #f5f5f5;
}

.page-container {
  padding: 20px;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.search-form {
  margin-bottom: 20px;
}

.pagination-container {
  text-align: center;
  margin-top: 20px;
}
</style>