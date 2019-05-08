<template>
  <div class="spaceTemplates">
    <table class="uiGrid table table-hover table-striped">
      <tr>
        <th>
          {{ $t('social.spaces.templates.template') }}
        </th>
        <th>
          {{ $t('social.spaces.templates.details') }}
        </th>
      </tr>
      <tr v-if="templates.length === 0">
        <td class="empty center" colspan="12"> {{ $t('social.spaces.templates.noTemplates') }} </td>
      </tr>
      <tr v-for="template in templates" :key="template.name">
        <td>{{ template.name }}</td>
        <td>
          <ul>
            <li><strong>{{ $t('social.spaces.templates.description') }}: </strong>{{ template.description }}</li>
            <li><strong>{{ $t('social.spaces.templates.hidden') }}: </strong>
              <p v-if="template.visibility === 'hidden'">{{ $t('social.spaces.templates.yes') }}</p>
              <p v-else>{{ $t('social.spaces.templates.no') }}</p>
            </li>
            <li><strong>{{ $t('social.spaces.templates.registration') }}: </strong>{{ template.registration }}</li>
            <li><strong>{{ $t('social.spaces.templates.applications') }}: </strong><p v-for="app in template.spaceApplicationList" :key="app.order">{{ app.portletName }}</p></li>
            <li><strong>{{ $t('social.spaces.templates.banner') }}: </strong><a>{{ template.bannerPath }}</a></li>
          </ul>
        </td>
      </tr>
    </table>
  </div>
</template>
<script>
import * as spaceTemplatesServices from '../spaceTemplatesServices';

export default {
  data() {
    return {
      templates: []
    };
  },
  created() {
    this.initTemplates();
  },
  methods: {
    initTemplates() {
      spaceTemplatesServices.getTemplates().then(data => {
        this.templates = data;
      });
    }
  }
};
</script>