<template>
  <div class="spacesPermissions">
    <table class="uiGrid table table-hover table-striped">
      <tr>          
        <th>
          {{ $t('social.spaces.administration.permissions') }}
        </th>
        <th>
          {{ $t('social.spaces.administration.permissions.groups') }}
        </th>
        <th class="actions">
          {{ $t('social.spaces.administration.permissions.actions') }}
        </th>
      </tr>
      <tr>
        <td>
          <h5 class="title">{{ $t('social.spaces.administration.permissions.createSpace') }}</h5>
          <h5>{{ $t('social.spaces.administration.permissions.descriptionCreateSpace') }}</h5>
        </td>
        <td>
          <div v-show="!spacesCreatorsEditMode">
            <div v-if="creators.length > 0">
              <div v-for="creator in creators" :key="creator">
                <h5>{{ creator }}</h5>
              </div>
            </div>
            <h5 v-if="creators.length === 0 && displayNoAssignmentCreators">{{ $t('social.spaces.administration.permissions.noAssignment') }}</h5>
          </div>
          <div v-show="spacesCreatorsEditMode" class="inputUser">
            <input id="add-creators-suggester" type="text"/>
          </div>
        </td>
        <td v-if="!spacesCreatorsEditMode" class="center actionContainer" >
          <a v-exo-tooltip.bottom.body="$t('social.spaces.administration.permissions.actions.edit')" class="actionIcon" @click="editCreateSpace()">
            <i class="uiIconEdit uiIconLightGray"></i>
          </a>
        </td>
        <td v-if="spacesCreatorsEditMode" class="center actionContainer" >
          <a v-exo-tooltip.bottom.body="$t('social.spaces.administration.permissions.actions.save')" class="actionIcon" @click="savePermissionsCreateSpace()">
            <i class="uiIconSave uiIconLightGray"></i>
          </a>
          <a v-exo-tooltip.bottom.body="$t('social.spaces.administration.permissions.actions.cancel')" class="actionIcon" @click="editCreateSpace()">
            <i class="uiIconClose uiIconLightGray"></i>
          </a>
        </td>       
      </tr>
      <tr>
        <td>
          <h5 class="title">{{ $t('social.spaces.administration.permissions.manageSpaces') }}</h5>
          <h5>{{ $t('social.spaces.administration.permissions.descriptionManageSpaces') }}</h5>
        </td>
        <td>
          <div v-show="!spacesAdministratorsEditMode">
            <div v-if="administrators.length > 0">
              <div v-for="administrator in administrators" :key="administrator">
                <h5>{{ administrator }}</h5>
              </div>
            </div>
            <h5 v-if="administrators.length === 0 && displayNoAssignmentAdministrators">{{ $t('social.spaces.administration.permissions.noAssignment') }}</h5>
          </div>
          <div v-show="spacesAdministratorsEditMode" class="inputUser">
            <input id="add-administrators-suggester" type="text"/>
          </div>
        </td>
        <td v-if="!spacesAdministratorsEditMode" class="center actionContainer" >
          <a v-exo-tooltip.bottom.body="$t('social.spaces.administration.permissions.actions.edit')" class="actionIcon" @click="editManageSpace()">
            <i class="uiIconEdit uiIconLightGray"></i>
          </a>
        </td>
        <td v-if="spacesAdministratorsEditMode" class="center actionContainer" >
          <a v-exo-tooltip.bottom.body="$t('social.spaces.administration.permissions.actions.save')" class="actionIcon" @click="savePermissionsSpacesAdministrators()">
            <i class="uiIconSave uiIconLightGray"></i>
          </a>
          <a v-exo-tooltip.bottom.body="$t('social.spaces.administration.permissions.actions.cancel')" class="actionIcon" @click="editManageSpace()">
            <i class="uiIconClose uiIconLightGray"></i>
          </a>
        </td>
      </tr>
    </table>
  </div>
</template>
<script>
import * as spacesAdministrationServices from '../spacesAdministrationServices';
export default {
  data() {
    return {
      creators: [],
      administrators: [],
      spacesCreatorsEditMode: false,
      spacesAdministratorsEditMode: false,
      displayNoAssignmentCreators: false,
      displayNoAssignmentAdministrators: false
    };
  },
  created() {
    this.getSettingValueCreateSpace();
    this.getSettingValueSpacesAdministrators();
  },
  methods: {
    initSuggesterSpacesCreators() {
      const suggesterContainer = $('#add-creators-suggester');
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
        $('#add-creators-suggester')[0].selectize.clear();
        if(this.creators && this.creators !== null) {
          for(const permission of this.creators) {
            suggesterContainer[0].selectize.addOption({text: permission});
            suggesterContainer[0].selectize.addItem(permission);
          }
        }
      }
    },
    addSuggestedItemCreate(item) {
      if($('#add-creators-suggester') && $('#add-creators-suggester').length && $('#add-creators-suggester')[0].selectize) {
        const selectize = $('#add-creators-suggester')[0].selectize;
        item = selectize.options[item];
      }
      if(!this.creators.find(creator => creator === item.text)) {
        this.creators.push(item.text);
      }
    },
    removeSuggestedItemCreators(item) {
      const suggesterContainer = $('#add-creators-suggester');
      for(let i=this.creators.length-1; i>=0; i--) {
        if(this.creators[i] === item) {
          this.creators.splice(i, 1);
          suggesterContainer[0].selectize.removeOption(item);
          suggesterContainer[0].selectize.removeItem(item);
        }
      }
    },
    savePermissionsCreateSpace() {
      $('.tooltip.fade.bottom.in').remove();
      if(this.creators){
        spacesAdministrationServices.updateSpacesAdministrationSetting('spacesCreators',
          this.creators.map(creator => {
            const splitCreator = creator.split(':');
            return { 'membershipType': splitCreator[0], 'group': splitCreator[1] };
          }));
      }
      this.spacesCreatorsEditMode = false;
    },
    getSettingValueCreateSpace() {
      spacesAdministrationServices.getSpacesAdministrationSetting('spacesCreators').then(data => {
        if(data) {
          this.creators = [];
          for(const permission of data.memberships) {
            const permissionExpression = `${permission.membershipType}:${permission.group}`;
            this.creators.push(permissionExpression);
          }
        }
        this.displayNoAssignmentCreators = true;
        this.initSuggesterSpacesCreators();
      });
    },
    initSuggesterSpacesAdministrators() {
      const suggesterContainer = $('#add-administrators-suggester');
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
          renderMenuItem (item, escape) {
            return component.renderMenuItem(item, escape);
          },
          renderItem(item) {
            return `<div class="item">${item.text}</div>`;
          },
          onItemAdd(item) {
            component.addSuggestedItem(item);
          },
          onItemRemove(item) {
            component.removeSuggestedItemAdinistrators(item);
          },
          sortField: [{field: 'order'}, {field: '$score'}],
          providers: {
            'exo:spacesAdministration': component.findGroups
          }
        };
        suggesterContainer.suggester(suggesterData);
        $('#add-administrators-suggester')[0].selectize.clear();
        if(this.administrators && this.administrators !== null) {
          for(const permission of this.administrators) {
            suggesterContainer[0].selectize.addOption({text: permission});
            suggesterContainer[0].selectize.addItem(permission);
          }
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
    addSuggestedItem(item) {
      if($('#add-administrators-suggester') && $('#add-administrators-suggester').length && $('#add-administrators-suggester')[0].selectize) {
        const selectize = $('#add-administrators-suggester')[0].selectize;
        item = selectize.options[item];
      }
      if(!this.administrators.find(administrator => administrator === item.text)) {
        this.administrators.push(item.text);
      }
    },
    removeSuggestedItemAdinistrators(item) {
      const suggesterContainer = $('#add-administrators-suggester');
      for(let i=this.administrators.length-1; i>=0; i--) {
        if(this.administrators[i] === item) {
          this.administrators.splice(i, 1);
          suggesterContainer[0].selectize.removeOption(item);
          suggesterContainer[0].selectize.removeItem(item);
        }
      }
    },
    savePermissionsSpacesAdministrators() {
      $('.tooltip.fade.bottom.in').remove();
      if(this.administrators){
        spacesAdministrationServices.updateSpacesAdministrationSetting('spacesAdministrators',
          this.administrators.map(administrator => {
            const splitAdministrators = administrator.split(':');
            return { 'membershipType': splitAdministrators[0], 'group': splitAdministrators[1] };
          }));
      }
      this.spacesAdministratorsEditMode = false;
    },
    getSettingValueSpacesAdministrators(){
      spacesAdministrationServices.getSpacesAdministrationSetting('spacesAdministrators').then(data => {
        if(data) {
          this.administrators = [];
          for(const permission of data.memberships) {
            const permissionExpression = `${permission.membershipType}:${permission.group}`;
            this.administrators.push(permissionExpression);
          }
        }
        this.displayNoAssignmentAdministrators = true;
        this.initSuggesterSpacesAdministrators();
      });
    },
    editCreateSpace(){
      $('.tooltip.fade.bottom.in').remove();
      if(this.spacesCreatorsEditMode) {
        this.spacesCreatorsEditMode = false;
      } else {
        this.spacesCreatorsEditMode = true;
      }
      this.getSettingValueCreateSpace();
    },
    editManageSpace(){
      $('.tooltip.fade.bottom.in').remove();
      if(this.spacesAdministratorsEditMode) {
        this.spacesAdministratorsEditMode = false;
      } else {
        this.spacesAdministratorsEditMode = true;
      }
      this.getSettingValueSpacesAdministrators();
    }
  }
};
</script>



