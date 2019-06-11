<template>
  <div id="newsActivityComposer" class="uiBox">
    <form id="newsForm" @submit.prevent="postNews">

      <div class="newsFormLabelsInputs">
        <div class="newsFormColumn newsFormLabels">
          <label class="newsFormLabel newsFormTitleLabel" for="newsTitle">{{ $t("activity.composer.news.title") }} : </label>
          <label class="newsFormLabel newsFormContentLabel" for="newsContent">{{ $t("activity.composer.news.content") }} : </label>
        </div>

        <div class="newsFormColumn newsFormInputs">
          <input id="newsTitle" v-model="newsActivity.title" :maxlength="max" class="newsFormInput" type="text">
          <textarea id="newsContent" v-model="newsActivity.content" class="newsFormInput" type="text" name="newsContent"></textarea>
          <div class="newsFormButtons">
            <span class="uiCheckbox">
              <input id="pinArticle" v-model="pinArticle" type="checkbox" class="checkbox ">
              <span>{{ $t("activity.composer.news.pinArticle") }}</span>
            </span>
            <button id="newsPost" :disabled="postDisabled" class="btn btn-primary">{{ $t("activity.composer.news.post") }}</button>
            <a id="newsPlus" :data-original-title="$t('activity.composer.news.moreOptions')" class="btn btn-primary"
               rel="tooltip" data-placement="bottom">
              <i class="uiIconSimplePlus"></i>
            </a>
          </div>
        </div>
      </div>

    </form>
  </div>
</template>

<script>
import * as  newsActivityComposerServices  from '../newsActivityComposerServices';
export default {
  data() {
    return {
      newsActivity: {
        title: '',
        content: '',
      },
      pinArticle: false,
      SMARTPHONE_LANDSCAPE_WIDTH: 768,
      TITLE_MAX_LENGTH: 150,
    };
  },
  computed: {
    postDisabled: function() {
      return !this.newsActivity.title || !this.newsActivity.title.trim() || !this.newsActivity.content || !this.newsActivity.content.replace(/<[^>]*>/g, '').replace(/&nbsp;/g,'').trim();
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
            self.newsActivity.content = evt.editor.getData();
          }
        }
      });
    },
    postNews: function() {
      newsActivityComposerServices.saveNewsActivity({
        title: this.newsActivity.title,
        body:  this.newsActivity.content,
        type: 'news'
      }).then(() => {
        // reset form
        this.newsActivity.title = '';
        this.newsActivity.content = '';
        CKEDITOR.instances['newsContent'].setData('');
        this.pinArticle = false;

        // refresh activity stream
        const refreshButton = document.querySelector('.uiActivitiesDisplay #RefreshButton');
        if(refreshButton) {
          refreshButton.click();
        }
      });
    },
  }
};
</script>
