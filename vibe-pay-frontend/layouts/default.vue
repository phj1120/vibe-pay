<template>
  <v-app>
    <v-navigation-drawer app permanent>
      <v-list-item title="VibePay" subtitle="Payment Demo"></v-list-item>
      <v-divider></v-divider>
      <v-list density="compact" nav>
        <v-list-item prepend-icon="mdi-view-dashboard" title="Home" value="home" to="/"></v-list-item>
        <v-list-item prepend-icon="mdi-account-multiple" title="Members" value="members" to="/members"></v-list-item>
        <v-list-item prepend-icon="mdi-package-variant-closed" title="Products" value="products" to="/products"></v-list-item>
        <v-list-item prepend-icon="mdi-cart" title="New Order" value="order" to="/order"></v-list-item>
      </v-list>
    </v-navigation-drawer>
    <v-main>
      <v-container fluid>
        <slot />
      </v-container>
    </v-main>
  </v-app>
</template>

<script setup>
</script>
