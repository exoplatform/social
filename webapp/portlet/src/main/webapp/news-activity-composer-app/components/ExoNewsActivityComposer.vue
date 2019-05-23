<template>
  <div id="newsActivityComposer" class="uiBox">
    <form id="newsForm" action="#" method="post" @submit="postNews">

      <div class="newsFormRow newsFormTitle">
        <label class="newsFormLabel" for="newsTitle">{{ $t("activity.composer.news.title") }} : </label>
        <input id="newsTitle" v-model="title" :maxlength="TITLE_MAX_LENGTH" class="newsFormInput" type="text">
      </div>

      <div class="newsFormRow newsFormContent">
        <label class="newsFormLabel" for="newsContent">{{ $t("activity.composer.news.content") }} : </label>
        <textarea id="newsContent" v-model="content" class="newsFormInput" type="text" name="newsContent"></textarea>
      </div>

      <div class="newsFormRow newsFormButtons">
        <span class="uiCheckbox">
          <input id="pinArticle" v-model="pinArticle" type="checkbox" class="checkbox ">
          <span>{{ $t("activity.composer.news.pinArticle") }}</span>
        </span>
        <button id="newsPost" :disabled="postDisabled" class="btn btn-primary">{{ $t("activity.composer.news.post") }}</button>
        <a id="newsPlus" :data-original-title="$t('activity.composer.news.moreOptions')" class="btn btn-primary" rel="tooltip" data-placement="bottom">
          <i class="uiIconSimplePlus"></i>
        </a>
      </div>

    </form>
  </div>
</template>

<script>
export default {
  data() {
    return {
      SMARTPHONE_LANDSCAPE_WIDTH: 768,
      TITLE_MAX_LENGTH: 150,
      title: '',
      content: '',
      pinArticle: false
    };
  },
  computed: {
    postDisabled: function() {
      return !this.title || !this.content;
    }
  },
  created() {
    const textarea = document.querySelector('#activityComposerTextarea');
    const shareButton = document.querySelector('#ShareButton');
    if(textarea && shareButton) {
      textarea.style.display = 'none';
      shareButton.style.display = 'none';
    }
  },
  mounted(){
    $('[rel="tooltip"]').tooltip();
    this.initCKEditor();
  },
  beforeDestroy() {
    const textarea = document.querySelector('#activityComposerTextarea');
    const shareButton = document.querySelector('#ShareButton');
    if(textarea && shareButton) {
      textarea.style.display = 'block';
      shareButton.style.display = 'block';
    }
  },
  methods: {
    initCKEditor: function () {
      let extraPlugins = 'simpleLink,selectImage,suggester,hideBottomToolbar';
      const windowWidth = $(window).width();
      const windowHeight = $(window).height();
      if (windowWidth > windowHeight && windowWidth < this.SMARTPHONE_LANDSCAPE_WIDTH) {
        // Disable suggester on smart-phone landscape
        extraPlugins = 'simpleLink,selectImage';
      }

      // this line is mandatory when a custom skin is defined
      CKEDITOR.basePath = '/commons-extension/ckeditor/';

      const self = this;
      $('textarea#newsContent').ckeditor({
        customConfig: '/commons-extension/ckeditorCustom/config.js',
        extraPlugins: extraPlugins,
        removePlugins: 'image',
        extraAllowedContent: 'img[style,class,src,referrerpolicy,alt,width,height]',
        on: {
          change: function(evt) {
            const newData = evt.editor.getData();
            const pureText = newData ? newData.replace(/<[^>]*>/g, '').replace(/&nbsp;/g,'').trim() : '';
            self.content = pureText;
          }
        }
      });
    },
    postNews: function(e) {
      e.preventDefault();
    }
  }
};
</script>