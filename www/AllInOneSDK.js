
var exec = require('cordova/exec');

var PLUGIN_NAME = 'AllInOneSDK';

var AllInOneSDK = {
  startTransaction: function(options, cb) {
    exec(cb, null, PLUGIN_NAME, 'startTransaction', [options]);
  }
};

module.exports = AllInOneSDK;
