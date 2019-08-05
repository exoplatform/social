const path = require('path');
const merge = require('webpack-merge');
const webpackCommonConfig = require('./webpack.common.js');

// the display name of the war
const app = 'social-portlet';

// add the server path to your server location path
const exoServerPath = "/home/thomas/exoplatform/bundles/plfent-5.3.x-news-20190722.171714-147/platform-5.3.x-news-SNAPSHOT";

let config = merge(webpackCommonConfig, {
  output: {
    path: path.resolve(`${exoServerPath}/webapps/${app}/`),
    filename: 'js/[name].bundle.js'
  },
  devtool: 'inline-source-map'
});

module.exports = config;
