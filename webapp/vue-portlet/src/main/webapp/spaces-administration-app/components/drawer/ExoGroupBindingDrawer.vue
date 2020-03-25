<template>
  <div>
    <v-flex v-show="!showSelectGroupsTree" id="GroupBindingForm">
      <v-card-title 
        class="title">
        <v-layout class="pa-2" row>
          <v-flex>
            <span>
              {{ $t('social.spaces.administration.manageSpaces.spaceBindingForm.title') }}
            </span>
          </v-flex>
          <v-flex xs1>
            <v-btn
              icon
              class="rightIcon"
              @click="closeDrawer">
              <v-icon 
                large
                class="closeIcon">
                close
              </v-icon>
            </v-btn>          
          </v-flex>
        </v-layout>
      </v-card-title>
      <div class="content">
        <v-layout
          class="mt-4 pl-2"
          wrap>
          <v-flex align-center xs1>
            <img v-if="spaceToBind && spaceToBind.avatarUrl != null" :src="spaceToBind.avatarUrl" class="avatar" />
            <img v-else :src="avatar" class="avatar" />
          </v-flex>
          <v-flex pt-1 class="spaceName">
            <span> {{ spaceDisplayName }} </span>
          </v-flex>
        </v-layout>
        <v-layout
          class="pt-5 pl-3 mb-4"
          wrap>
          <v-flex xs9>
            <exo-suggester 
              v-model="groups"
              :options="suggesterOptions"
              :source-providers="[findGroups]"
              :bound-groups="groupSpaceBindings"
              :placeholder="$t('social.spaces.administration.manageSpaces.spaceBindingForm.textField.placeHolder')"/>
          </v-flex>
          <v-flex xs1/>
          <v-flex xs1>
            <v-btn
              v-exo-tooltip.bottom.body="$t('social.spaces.administration.manageSpaces.spaceBindingForm.selectList')"
              icon
              class="rightIcon"
              @click="goToSelectGroups">
              <i class="uiIconSpaceBinding uiIconGroup"></i>
            </v-btn>
          </v-flex>
        </v-layout>
        <v-layout v-show="boundGroupsLoading" column>
          <v-flex pl-4>
            <v-skeleton-loader
              class="mx-auto"
              type="heading">
            </v-skeleton-loader>
          </v-flex>
          <v-spacer></v-spacer>
          <v-flex pt-4 pl-4 pr-8>
            <v-skeleton-loader
              class="mx-auto"
              type="paragraph@3">
            </v-skeleton-loader>
          </v-flex>
        </v-layout>
        <v-layout v-if="groupSpaceBindings.length > 0 && !boundGroupsLoading" column>
          <v-flex>
            <span class="subtitle-1">
              {{ $t('social.spaces.administration.manageSpaces.spaceBindingForm.boundGroups') }}
            </span>
          </v-flex>
          <v-flex class="boundGroups">
            <v-list flat subheader dense>
              <v-list-item-group>
                <div v-for="(binding, index) in groupSpaceBindings" :key="index">
                  <v-list-item>
                    <v-list-item-content>
                      {{ renderGroupName(binding.group) }}
                    </v-list-item-content>
                    <v-list-item-action class="delete">
                      <v-btn
                        small
                        icon
                        class="rightIcon"
                        @click="$emit('openRemoveBindingModal', binding)">
                        <i class="uiIconDeleteUser uiIconLightGray"></i>
                      </v-btn>
                    </v-list-item-action>
                  </v-list-item>
                </div>              
              </v-list-item-group>
            </v-list>
          </v-flex>
        </v-layout>
      </div>
      <v-card-actions absolute class="drawerActions">
        <v-layout>
          <v-flex class="xs6"></v-flex>
          <button type="button" class="btn ml-2" @click="cancelBinding">{{ $t('social.spaces.administration.manageSpaces.spaceBindingForm.cancel') }}</button>
          <button :disabled="!isAllowToSave" type="button" class="btn btn-primary ml-6" @click="$emit('openBindingModal', groups)">
            {{ $t('social.spaces.administration.manageSpaces.spaceBindingForm.apply') }}
          </button>
        </v-layout>
      </v-card-actions>      
    </v-flex>
    <div v-show="showSelectGroupsTree">
      <exo-group-binding-second-level-drawer :already-selected="groups" :group-space-bindings="groupSpaceBindings" @selectionSaved="selectionSaved" @back="back" @close="closeDrawer"></exo-group-binding-second-level-drawer>
    </div>
  </div>
</template>

<script>
import * as spacesAdministrationServices from '../../spacesAdministrationServices';
import { spacesConstants } from '../../../js/spacesConstants';

export default {
  props: {
    spaceToBind: {
      type: String,
      default: null,
    },
    groupSpaceBindings: {
      type: Array,
      default: null,
    },
    boundGroupsLoading: {
      type: Boolean,
      default: true,
    },
  },
  data() {
    const component = this;
    return {
      textAreaValue: '',
      groups: [],
      showSelectGroupsTree: false,
      avatar: spacesConstants.DEFAULT_SPACE_AVATAR,
      suggesterOptions: {
        type: 'tag',
        plugins: ['remove_button', 'restore_on_backspace'],
        create: false,
        createOnBlur: false,
        highlight: false,
        openOnFocus: false,
        valueField: 'text',
        labelField: 'text',
        searchField: ['text'],
        closeAfterSelect: false,
        dropdownParent: 'body',
        hideSelected: true,
        placeholder:`@ ${this.$t('social.spaces.administration.manageSpaces.spaceBindingForm.textField.placeHolder')}`,
        renderMenuItem (item, escape) {
          return component.renderMenuItem(item, escape);
        },
        sortField: [{field: 'order'}, {field: '$score'}],
      }
    };
  },
  computed : {
    isAllowToSave() {
      return this.groups && this.groups.length > 0;
    },
    spaceDisplayName() {
      return  this.spaceToBind ? this.spaceToBind.displayName : '';
    }
  },
  methods : {
    findGroups (query, callback) {
      if (!query.length) {
        return callback();
      }
      spacesAdministrationServices.getGroups(query).then(data => {
        const groups = [];
        const boundGroups = this.groupSpaceBindings.map(binding => binding.group);
        for(const group of data) {
          if (!group.id.startsWith('/spaces') && !boundGroups.includes(group.id)) {
            groups.push({
              avatarUrl: null,
              text: group.id,
              value: group.id,
              type: 'group'
            });
          }
        }
        callback(groups);
      });
    },
    renderMenuItem (item, escape) {
      return `
        <div class="item">${escape(item.value)}</div>
      `;
    },
    goToSelectGroups() {
      this.showSelectGroupsTree = true;
    },
    back() {
      this.showSelectGroupsTree = !this.showSelectGroupsTree;
    },
    closeDrawer() {
      this.showSelectGroupsTree = false;
      this.$emit('close');
    },
    cancelBinding() {
      this.groups = [];
      this.$emit('close');
      this.showSelectGroupsTree = false;
    },
    renderGroupName(groupName) {
      let groupPrettyName = groupName.slice(groupName.lastIndexOf('/') + 1, groupName.length);
      groupPrettyName = groupPrettyName.charAt(0).toUpperCase() + groupPrettyName.slice(1);
      return `${groupPrettyName} (${groupName})`;
    },
    selectionSaved(groupsIds) {
      this.showSelectGroupsTree = false;
      this.groups = [];
      this.groups.push(...groupsIds);
    },
  }
};
</script>
