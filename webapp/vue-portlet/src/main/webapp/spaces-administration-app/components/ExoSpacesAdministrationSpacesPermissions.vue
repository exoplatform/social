<template>
  <div>
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
          <div v-show="spacesCreatorsEditMode">
            <div v-if="creators.length > 0">
              <div v-for="creator in creators" :key="creator">
                <h5>{{ creator }}</h5>
              </div>
            </div>
            <h5 v-if="creators.length === 0 && displayNoAssignmentCreators">{{ $t('social.spaces.administration.permissions.noAssignment') }}</h5>
          </div>
          <div v-show="!spacesCreatorsEditMode" class="inputUser">
            <input id="add-creators-suggester" type="text"/>
          </div>
        </td>
        <td v-if="spacesCreatorsEditMode" class="center actionContainer" >
          <a data-placement="bottom" rel="tooltip" class="actionIcon" data-original-title="Edit" @click="editCreateSpace()">
            <i class="uiIconEdit uiIconLightGray"></i>
          </a>
        </td>
        <td v-if="!spacesCreatorsEditMode" class="center actionContainer" >
          <a data-placement="bottom" rel="tooltip" class="actionIcon" data-original-title="Save" @click="savePermissionsCreateSpace()">
            <i class="uiIconSave uiIconLightGray"></i>
          </a>
          <a data-placement="bottom" rel="tooltip" class="actionIcon" data-original-title="Close" @click="editCreateSpace()">
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
          <div v-show="spacesAdministratorsEditMode">
            <div v-if="administrators.length > 0">
              <div v-for="administrator in administrators" :key="administrator">
                <h5>{{ administrator }}</h5>
              </div>
            </div>
            <h5 v-if="administrators.length === 0 && displayNoAssignmentAdministrators">{{ $t('social.spaces.administration.permissions.noAssignment') }}</h5>
          </div>
          <div v-show="!spacesAdministratorsEditMode" class="inputUser">
            <input id="add-administrators-suggester" type="text"/>
          </div>
        </td>
        <td v-if="spacesAdministratorsEditMode" class="center actionContainer" >
          <a data-placement="bottom" rel="tooltip" class="actionIcon" data-original-title="Edit" @click="editManageSpace()">
            <i class="uiIconEdit uiIconLightGray"></i>
          </a>
        </td>
        <td v-if="!spacesAdministratorsEditMode" class="center actionContainer" >
          <a data-placement="bottom" rel="tooltip" class="actionIcon" data-original-title="Save" @click="savePermissionsSpacesAdministrators()">
            <i class="uiIconSave uiIconLightGray"></i>
          </a>
          <a data-placement="bottom" rel="tooltip" class="actionIcon" data-original-title="Close" @click="editManageSpace()">
            <i class="uiIconClose uiIconLightGray"></i>
          </a>
        </td>
      </tr>
    </table>
  </div>
</template>
<script>
import * as spaceAdministrationServices from '../spaceAdministrationServices';
export default {
  data() {
    return {
      creators: [],
      administrators: [],
      spacesCreatorsEditMode: true,
      spacesAdministratorsEditMode: true,
      index: 0,
      settingValue: '',
      creatorsPermissions: null,
      administratorsPermissions: null,
      displayNoAssignmentCreators: false,
      displayNoAssignmentAdministrators: false
    };
  },
  created() {
    this.initSuggesterSpacesCreators();
    this.initSuggesterSpacesAdministrators();
    this.getSettingValueCreateSpace();
    this.getSettingValueSpaceAdministrators();
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
        if(this.creatorsPermissions && this.creatorsPermissions !== null) {
          for(const permission of this.creatorsPermissions) {
            const permissionExpression = `${permission.membershipType}:${permission.group}`;
            suggesterContainer[0].selectize.addOption({text: permissionExpression});
            suggesterContainer[0].selectize.addItem(permissionExpression);
          }
        }
      }
    },
    addSuggestedItemCreate(item) {
      if($('#add-creators-suggester') && $('#add-creators-suggester').length && $('#add-creators-suggester')[0].selectize) {
        const selectize = $('#add-creators-suggester')[0].selectize;
        item = selectize.options[item];
      }
      if(!this.creators.find(creator => creator.text === item.text)) {
        this.creators.push(item.text);
      }
    },
    removeSuggestedItemCreators(item) {
      if(this.creators.find(creator => creator === item)) {
        this.creators.splice(this.creators.indexOf(item), 1);
      }
    },
    savePermissionsCreateSpace() {
      if(this.creators){
        spaceAdministrationServices.updateSpacesAdministrationSetting('spacesCreators',
          this.creators.map(creator => {
            const splitCreator = creator.split(':');
            return { 'membershipType': splitCreator[0], 'group': splitCreator[1] };
          }));
      }
      this.spacesCreatorsEditMode = true;
    },
    getSettingValueCreateSpace() {
      spaceAdministrationServices.getSpacesAdministrationSetting('spacesCreators').then(data => {
        if(data) {
          this.creatorsPermissions = data.memberships;
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
        if(this.administratorsPermissions && this.administratorsPermissions !== null) {
          for(const permission of this.administratorsPermissions) {
            const permissionExpression = `${permission.membershipType}:${permission.group}`;
            suggesterContainer[0].selectize.addOption({text: permissionExpression});
            suggesterContainer[0].selectize.addItem(permissionExpression);
          }
        }      
      }
    },
    findGroups (query, callback) {
      if (!query.length) {
        return callback(); 
      }

      spaceAdministrationServices.getGroups(query).then(data => {
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
      if(!this.administrators.find(administrator => administrator.text === item.text)) {
        this.administrators.push(item.text);
      }
    },
    removeSuggestedItemAdinistrators(item) {
      if(this.administrators.find(administrator => administrator === item)) {
        this.administrators.splice(this.administrators.indexOf(item), 1);
      }
    },
    savePermissionsSpacesAdministrators() {
      if(this.administrators){
        spaceAdministrationServices.updateSpacesAdministrationSetting('spacesAdministrators',
          this.administrators.map(administrator => {
            const splitAdministrators = administrator.split(':');
            return { 'membershipType': splitAdministrators[0], 'group': splitAdministrators[1] };
          }));
      }
      this.spacesAdministratorsEditMode = true;
    },
    getSettingValueSpaceAdministrators(){
      spaceAdministrationServices.getSpacesAdministrationSetting('spacesAdministrators').then(data => {
        if(data) {
          this.administratorsPermissions = data.memberships;
        }
        this.displayNoAssignmentAdministrators = true;
        this.initSuggesterSpacesAdministrators();
      });
    },
    editCreateSpace(){
      if(this.spacesCreatorsEditMode) {
        this.spacesCreatorsEditMode = false;
      } else {
        this.getSettingValueCreateSpace();
        this.spacesCreatorsEditMode = true;
        this.spacesAdministratorsEditMode = true;
      }
    },
    editManageSpace(){
      if(this.spacesAdministratorsEditMode) {
        this.spacesAdministratorsEditMode = false;
      } else {
        this.getSettingValueSpaceAdministrators();
        this.spacesAdministratorsEditMode = true;
        this.spacesCreatorsEditMode = true;
      }
    }
  }
};
</script>



