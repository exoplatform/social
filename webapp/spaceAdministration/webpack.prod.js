const path = require('path');
const merge = require('webpack-merge');
const webpackCommonConfig = require('./webpack.common.js');

// the display name of the war
const app = 'spacesAdministration';

const config = merge(webpackCommonConfig, {
  output: {
    path: path.resolve(__dirname, `./target/${app}/`),
    filename: 'js/[name].bundle.js'
  }
});

module.exports = config;
