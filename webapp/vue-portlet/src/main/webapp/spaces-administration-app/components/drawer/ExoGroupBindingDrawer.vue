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
          <v-flex class="xs1 mr-2">
            <v-btn
              icon
              class="rightIcon"
              @click="$emit('close')">
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
          class="pt-7 pl-3 mb-4"
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
        <v-layout column>
          <v-flex>
            <span class="subtitle-1">
              {{ $t('social.spaces.administration.manageSpaces.spaceBindingForm.boundGroups') }}
            </span>
          </v-flex>
          <v-flex class="boundGroups">
            <v-list flat subheader dense>
              <v-list-item-group>
                <div v-for="i in items" :key="i">
                  <v-list-item>
                    <v-list-item-content>
                      *:/platform/users {{ i }}
                    </v-list-item-content>
                    <v-list-item-action class="delete">
                      <v-btn
                        small
                        icon
                        class="rightIcon">
                        <i class="uiIconDeleteUser uiIconLightGray"></i>
                      </v-btn>
                    </v-list-item-action>
                  </v-list-item>
                  <v-list-item>
                    <v-list-item-content>
                      *:/organization/employees {{ i }}
                    </v-list-item-content>
                    <v-list-item-action class="delete">
                      <v-btn
                        small
                        icon
                        class="rightIcon">
                        <i class="uiIconDeleteUser uiIconLightGray"></i>
                      </v-btn>
                    </v-list-item-action>
                  </v-list-item>
                  <v-list-item>
                    <v-list-item-content>
                      *:/platform {{ i }}
                    </v-list-item-content>
                    <v-list-item-action absolute class="delete">
                      <v-btn
                        small
                        icon
                        class="rightIcon">
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
          <button type="button" class="btn ml-2" @click="$emit('close')">{{ $t('social.spaces.administration.manageSpaces.spaceBindingForm.cancel') }}</button>
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
            @click="$emit('close')">
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

export default {
  props: {
    spaceId: {
      type: String,
      default: null,
    }
  },
  data() {
    return {
      textAreaValue : '',
      groups: [],
      showSelectGroupsTree : false,
      items: [1],
    };
  },
  computed : {
    isAllowToSave() {
      return this.groups && this.groups.length > 0;
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
          closeAfterSelect: true,
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
          for(const permission of this.groups) {
            suggesterContainer[0].selectize.addOption({text: permission});
            suggesterContainer[0].selectize.addItem(permission);
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
          suggesterContainer[0].selectize.removeOption(item);
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
        for(const group of data) {
          groups.push({
            avatarUrl: null,
            text: `*:${group.id}`,
            value: `*:${group.id}`,
            type: 'group'
          });
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
    }
  }
};
</script>