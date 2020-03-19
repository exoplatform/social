<template>
  <div id="bindingReports">
    <div v-show="loading">
      <v-skeleton-loader
        class="mx-auto"
        type="table-heading,table-tbody">
      </v-skeleton-loader>
    </div>
    <div v-show="!loading">
      <v-card-title>
        <v-layout justify-space-between>
          <v-flex xs7></v-flex>
          <v-flex xs2>
            <v-text-field
              v-model="search"
              append-icon="search"
              single-line
              solo
              flat
              hide-details>
            </v-text-field>
          </v-flex>
          <v-flex xs2>
            <v-select
              :items="items"
              label="All bindings"
              solo
              flat
            ></v-select>
          </v-flex>          
        </v-layout>
      </v-card-title>
      <v-data-table
        :headers="headers"
        :items="desserts"
        :search="search"
        disable-sort
      ></v-data-table>
    </div>
  </div>
</template>

<script>
export default {
  data() {
    return {
      loading: false,
      search: '',
      items: ['All bindings', 'New binding', 'Removing binding', 'Synchronization'],
      desserts: [],
      headers: [
        { text: 'Space',value: 'space' },
        { text: 'Group', value: 'group' },
        { text: 'Start date', value: 'startDate' },
        { text: 'End date', value: 'endDate' },
        { text: 'Operation Type', value: 'operationType' },
        { text: 'Added users', value: 'addedUsers' },
        { text: 'Removed users', value: 'removedUsers' },
        { text: 'File', value: 'file' },
      ],
    };
  }
};
</script>