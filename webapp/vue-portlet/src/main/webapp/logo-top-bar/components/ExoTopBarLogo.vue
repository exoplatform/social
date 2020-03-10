<template>
  <v-app id="brandingToBar" class="border-box-sizing" flat>
    <v-container pa-3>
      <v-row class="mx-0">
        <a :href="homeLink" class=" pr-3 logoContainer">
          <v-img src="/portal/rest/v1/platform/branding/logo" max-width="75" max-height="50"/>
        </a>
        <a v-show="brandingCompanyName" :href="homeLink" class=" pl-2 align-self-center brandingContainer">
          <span class="subtitle-2 font-weight-bold">{{ brandingCompanyName }}</span>
        </a>
      </v-row>
    </v-container>
  </v-app>
</template>
<script>
export default {
  data() {
    return {
      homeLink: `${eXo.env.portal.context}/${eXo.env.portal.portalName}`,
      branding: null,
    };
  },
  computed: {
    brandingCompanyName() {
      return this.branding && this.branding.companyName;
    }
  },
  created() {
    return fetch(`${eXo.env.portal.context}/${eXo.env.portal.rest}/v1/platform/branding`, {
      method: 'GET',
      credentials: 'include',
    })
      .then(resp => resp.json())
      .then(data => this.branding = data);
  }
};
</script>