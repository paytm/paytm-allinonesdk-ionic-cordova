
var exec = require('cordova/exec');

var PLUGIN_NAME = 'AllInOneSDK';

var AllInOneSDK = {
  startTransaction: function(options, success, error) {
    exec(success, error, PLUGIN_NAME, 'startTransaction', [options]);
  }
};

module.exports = AllInOneSDK;
