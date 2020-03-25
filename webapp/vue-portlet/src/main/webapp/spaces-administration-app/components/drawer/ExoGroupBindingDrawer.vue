<template>
  <div v-if="!showSelectGroupsTree">
    <v-flex id="GroupBindingForm">
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
            <span> {{ spaceToBind.displayName }} </span>
          </v-flex>
          <v-flex pt-1 class="spaceName">
            <span> {{ spaceDisplayName }} </span>
          </v-flex>
        </v-layout>
        <v-layout
          class="pt-5 pl-3 mb-4"
          wrap>
          <v-flex xs9>
            <input id="add-groups" type="text"/>
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
        <v-layout v-if="groupSpaceBindings.length > 0" column>
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
          <v-flex class="xs7"></v-flex>
          <button type="button" class="btn ml-2" @click="cancelBinding">{{ $t('social.spaces.administration.manageSpaces.spaceBindingForm.cancel') }}</button>
          <button :disabled="!isAllowToSave" type="button" class="btn btn-primary ml-6" @click="$emit('openBindingModal', groups)">
            {{ $t('social.spaces.administration.manageSpaces.spaceBindingForm.apply') }}
          </button>
        </v-layout>
      </v-card-actions>      
    </v-flex>
  </div>
  <div v-else>
    <v-card-title 
      class="title">
      <v-layout 
        class="pa-2" 
        justify-center
        align-baseline 
        row>
        <v-flex
          align-self-end
          class="xs1">
          <v-btn
            icon
            class="leftIcon"
            @click="back">
            <v-icon small>arrow_back</v-icon>
          </v-btn>
        </v-flex>
        <v-flex>
          <span class="subtitle-1">
            {{ $t('social.spaces.administration.manageSpaces.spaceBindingForm.selectGroups') }}
          </span>
        </v-flex>
        <v-flex class="xs1 mr-2">
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
    }
  },
  data() {
    return {
      textAreaValue: '',
      groups: [],
      showSelectGroupsTree: false,
      avatar: spacesConstants.DEFAULT_SPACE_AVATAR,
    };
  },
  computed : {
    isAllowToSave() {
      return this.groups && this.groups.length > 0;
    },
    spaceDisplayName() {
      return  this.spaceToBind ? this.spaceToBind.displayName : '';
    },
    boundGroups() {
      return this.groupSpaceBindings? this.groupSpaceBindings.map(binding => binding.group) : [];
    }
  },
  mounted() {
    this.initSuggesterGroupsToBind();
  },
  updated() {
    this.initSuggesterGroupsToBind();
  },
  methods : {
    initSuggesterGroupsToBind() {
      const suggesterContainer = $('#add-groups');
      if(suggesterContainer && suggesterContainer.length && suggesterContainer.suggester) {
        const component = this;
        const suggesterData = {
          type: 'tag',
          plugins: ['remove_button', 'restore_on_backspace'],
          create: false,
          createOnBlur: false,
          highlight: false,
          openOnFocus: false,
          sourceProviders: ['exo:spacesAdministration'],
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
          renderItem(item) {
            return `<div class="item">${item.text}</div>`;
          },
          onItemAdd(item) {
            component.addSuggestedItemCreate(item);
          },
          onItemRemove(item) {
            component.removeSuggestedItemCreators(item);
          },
          sortField: [{field: 'order'}, {field: '$score'}],
          providers: {
            'exo:spacesAdministration': component.findGroups
          }
        };
        suggesterContainer.suggester(suggesterData);
        $('#add-groups')[0].selectize.clear();
        if(this.groups && this.groups !== null) {
          for(const group of this.groups) {
            suggesterContainer[0].selectize.addOption({text: group});
            suggesterContainer[0].selectize.addItem(group);
          }
        }
      }
    },
    addSuggestedItemCreate(item) {
      if($('#add-groups') && $('#add-groups').length && $('#add-groups')[0].selectize) {
        const selectize = $('#add-groups')[0].selectize;
        item = selectize.options[item];
      }
      if(!this.groups.find(creator => creator === item.text)) {
        this.groups.push(item.text);
      }
    },
    removeSuggestedItemCreators(item) {
      const suggesterContainer = $('#add-groups');
      for(let i=this.groups.length-1; i>=0; i--) {
        if(this.groups[i] === item) {
          this.groups.splice(i, 1);
          suggesterContainer[0].selectize.removeItem(item);
        }
      }
    },
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
      let items;
      const selectizeInput = document.getElementById('GroupBindingForm').getElementsByClassName('has-items')[0];
      if (selectizeInput && selectizeInput !== null) {
        items = selectizeInput.getElementsByClassName('item');
        if (items) {
          Array.prototype.forEach.call(items, function (item) {
            item.remove();
          });
        }
      }
      this.initSuggesterGroupsToBind();
      this.$emit('close');
    },
    renderGroupName(groupName) {
      let groupPrettyName = groupName.slice(groupName.lastIndexOf('/') + 1, groupName.length);
      groupPrettyName = groupPrettyName.charAt(0).toUpperCase() + groupPrettyName.slice(1);
      return `${groupPrettyName} (${groupName})`;
    }
  }
};
</script>
