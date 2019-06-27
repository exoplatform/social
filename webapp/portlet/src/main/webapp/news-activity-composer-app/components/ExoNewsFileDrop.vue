<template>
  <div class="fileDrop">
    <div class="dropZone">
      <label v-if="files.length == 0" class="dropMsg" for="uploadedFile">
        <i class="uiIcon uiIconUpload"></i> {{ $t('activity.composer.news.label.attachFile') }}
      </label>
      <input id="uploadedFile" :value="selectedFile" type="file" class="attachFile"/>

      <div v-show="error" class="uploadError alert alert-error v-content">
        {{ error }}
      </div>

      <div v-if="files.length > 0" class="abortFile pull-right">
        <a :title="$t('activity.composer.news.btn.cancel')" class="removeButton" href="#" rel="tooltip"
           data-placement="top" @click="abortUpload(files[0].name)">
          <i class="uiIconRemove"></i>
        </a>
      </div>
      <img v-if="files.length > 0" :src="files[0].src">
    </div>
  </div>
</template>

<script>
import {spacesConstants} from '../../js/spacesConstants.js';

const ERROR_SHOW_TIME = 5000;
export default {
  model: {
    prop: 'files',
    event: 'change'
  },
  props: {
    files: {
      type: Array,
      default: () => []
    }
  },
  data() {
    return {
      selectedFile: null,
      error: ''
    };
  },
  mounted() {
    const MAX_RANDOM_NUMBER = 5000;
    const $dropzoneContainer = $(this.$el).find('.dropZone');
    const thiss = this;

    let uploadId = '';
    $dropzoneContainer.filedrop({
      fallback_id: 'uploadedFile',  // an identifier of a standard file input element
      url: function () {
        const random = Math.round(Math.random() * MAX_RANDOM_NUMBER);
        const now = Date.now();
        uploadId = `${random}-${now}`;

        return `${spacesConstants.UPLOAD_API}?uploadId=${uploadId}&action=upload`;
      },  // upload handler, handles each file separately, can also be a function taking the file and returning a url
      paramname: 'userfile',          // POST parameter name used on serverside to reference file
      error: function (err) {
        switch (err) {
        case 'ErrorBrowserNotSupported':
        case 'BrowserNotSupported':
          thiss.setErrorCode('activity.composer.news.error.BrowserNotSupported');
          break;
        case 'ErrorTooManyFiles':
        case 'TooManyFiles':
          thiss.setErrorCode('activity.composer.news.error.TooManyFiles', [spacesConstants.MAX_UPLOAD_FILES]);
          break;
        case 'ErrorFileTooLarge':
        case 'FileTooLarge':
          thiss.setErrorCode('activity.composer.news.error.FileTooLarge', [spacesConstants.MAX_UPLOAD_SIZE]);
          break;
        case 'ErrorFileTypeNotAllowed':
        case 'FileTypeNotAllowed':
          thiss.setErrorCode('activity.composer.news.error.FileTypeNotAllowed');
          break;
        }
      },
      allowedfiletypes: ['image/jpeg','image/png','image/gif'],
      allowedfileextensions: ['.jpg','.jpeg','.png','.gif'],
      maxfiles: spacesConstants.MAX_UPLOAD_FILES,
      maxfilesize: spacesConstants.MAX_UPLOAD_SIZE,    // max file size in MBs
      rename: thiss.getFileName,
      uploadStarted: function (i, file) {
        // Reinit error when upload action started
        if (i === 0) {
          thiss.error = null;
        }
        const fileDetails = {
          id: null,
          uploadId: uploadId,
          name: file.name,
          size: file.size,
          src: null,
          progress: 0,
          file: file,
          finished: false,
        };
          // Add avatar
        const reader = new FileReader();
        reader.onload = (e) => {
          const data = e.target.result;
          thiss.$set(fileDetails, 'src', data);
          thiss.$set(fileDetails, 'data', data);
          thiss.$forceUpdate();
        };
        reader.readAsDataURL(file);
        // Add file to list
        thiss.files.push(fileDetails);
      },
      uploadFinished: function () {
        thiss.$emit('change', thiss.files);
      },
      beforeEach: function () {
        if (thiss.files.length >= spacesConstants.MAX_UPLOAD_FILES) {
          thiss.setErrorCode('activity.composer.news.error.TooManyFiles', [spacesConstants.MAX_UPLOAD_FILES]);
          return false;
        }
      },
      afterAll: function () {
        thiss.selectedFile = null;
      }
    });
  },
  methods: {
    abortUpload(name) {
      this.deleteFile(name);
    },
    deleteFile(name) {
      const idx = this.files.findIndex(f => f.name === name);
      const file = this.files[idx];
      this.files.splice(idx, 1);
      this.$emit('change', this.files);

      if (file.uploadId) {
        fetch(`${spacesConstants.UPLOAD_API}?uploadId=${file.uploadId}&action=delete`, {
          method: 'post',
          credentials: 'include'
        });
      }
    },
    setErrorCode(code, param) {
      this.error = this.$t(code, param);
      setTimeout(() => {
        this.error = '';
      }, ERROR_SHOW_TIME);
    },
  }
};
</script>
