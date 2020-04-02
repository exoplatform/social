<template>
  <div class="bindingReports">
    <div v-if="loading">
      <v-skeleton-loader
        class="mx-auto"
        type="table-heading,table-tbody">
      </v-skeleton-loader>
    </div>
    <div v-else>
      <div class="reportsFilterSearch">
        <v-layout justify-end row>
          <v-flex xs2>
            <v-text-field
              v-model="search"
              :placeholder="$t('social.spaces.administration.binding.reports.search')"
              prepend-inner-icon="search"
              single-line
              flat
              hide-details>
            </v-text-field>
          </v-flex>
          <v-flex class="filter" ml-2 mr-4 xs2>
            <div class="btn-group">
              <button class="btn dropdown-toggle" data-toggle="dropdown">
                {{ action }}
                <i class="uiIconMiniArrowDown uiIconLightGray"></i><span/>
              </button>
              <ul class="dropdown-menu">
                <li><a href="#" @click="action = operationTypes[0]"> {{ operationTypes[0] }} </a></li>
                <li><a href="#" @click="action = operationTypes[1]"> {{ operationTypes[1] }} </a></li>
                <li><a href="#" @click="action = operationTypes[2]"> {{ operationTypes[2] }} </a></li>
                <li><a href="#" @click="action = operationTypes[3]"> {{ operationTypes[3] }} </a></li>
              </ul>
            </div>
          </v-flex>
        </v-layout>
      </div>
      <v-data-table
        :headers="headers"
        :items="operations"
        :search="search"
        :footer-props="{
          itemsPerPageText: `${$t('social.spaces.administration.binding.reports.table.footer.rows.per.page')}:`,        
        }"
        disable-sort>
        <template slot="item" slot-scope="props">
          <tr>
            <td class="text-md-start">
              <img v-if="props.item.space.avatarUrl != null" :src="props.item.space.avatarUrl" class="avatar" />
              <img v-else :src="avatar" class="avatar" />
              {{ props.item.space.displayName }}
            </td>
            <td class="text-md-center">{{ props.item.group.name }}</td>
            <td class="text-md-center">{{ props.item.startDate }}</td>
            <td class="text-md-center">
              <div v-if="props.item.endDate !== 'null'"> {{ props.item.endDate }} </div>
              <div v-else class="inProgress">
                <v-progress-circular
                  indeterminate
                  color="primary">
                </v-progress-circular> <span>In progress</span>
              </div>
            </td>
            <td class="text-md-center">{{ getOperationType(props.item.operationType) }}</td>
            <td class="text-md-center">{{ props.item.addedUsers }}</td>
            <td class="text-md-center">{{ props.item.removedUsers }}</td>
            <td class="text-md-center"> 
              <v-btn
                icon
                class="rightIcon"
                @click="uploadCSVFile(props.item.space.id, props.item.operationType, props.item.group.id, props.item.bindingId)">
                <v-icon
                  medium
                  class="uploadFile">
                  mdi-download
                </v-icon>
              </v-btn>
            </td>
          </tr>
        </template>
      </v-data-table>
    </div>
  </div>
</template>

<script>
import * as spacesAdministrationServices from '../spacesAdministrationServices';
import {spacesConstants} from '../../js/spacesConstants';  

export default {
  data() {
    return {
      loading: true,
      avatar: spacesConstants.DEFAULT_SPACE_AVATAR,
      search: '',
      action: `${this.$t('social.spaces.administration.binding.reports.filter.all.bindings')}`,
      operationTypes: [
        `${this.$t('social.spaces.administration.binding.reports.filter.all.bindings')}`, 
        `${this.$t('social.spaces.administration.binding.reports.filter.add')}`,
        `${this.$t('social.spaces.administration.binding.reports.filter.remove')}`,
        `${this.$t('social.spaces.administration.binding.reports.filter.synchronize')}`
      ],
      operations: [],
    };
  },
  computed: {
    headers() {
      return [
        { text: `${this.$t('social.spaces.administration.manageSpaces.space')}`, align: 'center', value: 'space.displayName' },
        { text: `${this.$t('social.spaces.administration.binding.reports.table.title.group')}`, align: 'center', filterable: false },
        { text: `${this.$t('social.spaces.administration.binding.reports.table.title.start.date')}`, align: 'center', filterable: false },
        { text: `${this.$t('social.spaces.administration.binding.reports.table.title.end.date')}`, align: 'center', filterable: false },
        { text: `${this.$t('social.spaces.administration.binding.reports.table.title.operation.type')}`,
          align: 'center',
          filterable: true,
          value: 'operationType',
          filter: value => {
            if (this.action === `${this.$t('social.spaces.administration.binding.reports.filter.all.bindings')}`) {
              return true;
            }
            return this.getOperationType(value).toLowerCase() === this.action.toLowerCase();
          },
        },
        { text: `${this.$t('social.spaces.administration.binding.reports.table.title.added.users')}`, align: 'center', filterable: false },
        { text: `${this.$t('social.spaces.administration.binding.reports.table.title.removed.users')}`, align: 'center', filterable: false },
        { text: `${this.$t('social.spaces.administration.binding.reports.table.title.File')}`, align: 'center', filterable: false },
      ];
    }
  },
  created() {
    spacesAdministrationServices.getBindingReportOperations().then(data => {
      this.operations = data.groupSpaceBindingReportOperations;
    }).finally(() => this.loading = false);
  }, methods: {
    uploadCSVFile(spaceId, action, groupId, groupBindingId) {
      spacesAdministrationServices.getReport(spaceId, action, groupId, groupBindingId);
    },
    getOperationType(type) {
      const action = type.toLowerCase();
      return `${this.$t(`social.spaces.administration.binding.reports.filter.${action}`)}`;
    }
  },
};
</script>
