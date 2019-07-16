<template>
  <input type="text"/>
</template>

<script>
import * as shareNewsServices from '../newsShareActivityServices.js';

function findUsers(query, callback) {
  if (!query || !query.length) {
    callback([]);
  } else {
    shareNewsServices.findUserSpaces(query).then(users => {
      if(users) {
        callback(users);
      }
    });
  }
}

export default {
  model: {
    prop: 'spaces',
    event: 'change'
  },
  props: {
    sourceProviders: {
      type: Array,
      default: () => []
    },
    spaces: {
      type: Array,
      default: () => []
    }
  },
  watch: {
    spaces() {
      this.$emit('change', this.spaces);
      this.bindspaces();
    }
  },
  mounted() {
    const thiss = this;
    const suggesterData = {
      type: 'tag',
      create: false,
      createOnBlur: false,
      highlight: false,
      openOnFocus: false,
      sourceProviders: this.sourceProviders,
      valueField: 'value',
      labelField: 'text',
      optionIconField: 'avatarUrl',
      searchField: ['text'],
      closeAfterSelect: false,
      dropdownParent: 'body',
      hideSelected: true,
      plugins: ['remove_button'],
      providers: {
        'exo:share-spaces': findUsers
      },
      onChange(items) {
        thiss.spaces = items.split(',');
      },
      renderMenuItem: function(item, escape) {
        let avatar = item.avatarUrl;
        if (avatar == null) {
          avatar = '/eXoSkin/skin/images/system/SpaceAvtDefault.png';
        }
        if(!item.text) {
          item.text = item.value;
        }
        return `<div class="optionItem" data-value="${item.text}"><div class="avatarSmall optionAvatar"><img src="${avatar}"></div><div class="optionName">${escape(item.text)}</div></div>`;
      },
    };
    //init suggester
    $(this.$el).suggester(suggesterData);
  },
  methods: {
    bindspaces() {
      const selectize = $(this.$el)[0].selectize;
      //
      this.spaces.forEach(par => {
        if (!selectize.items.includes(par)) {
          findUsers(par, users => {
            users.forEach(user => {
              if (user.text === par) {
                selectize.addOption(user);
                selectize.addItem(user.text);
              }
            });
          });
        }
      });
      const removeItems = [];
      selectize.items.forEach(item => {
        if (!this.spaces.includes(item)) {
          removeItems.push(item);
        }
      });
      removeItems.forEach(item => {
        selectize.removeItem(item, true);
      });
    }
  }
};
</script>