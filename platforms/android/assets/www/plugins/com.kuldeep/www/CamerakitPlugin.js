cordova.define("com.kuldeep.CamerakitPlugin", function(require, exports, module) {


var exec = require('cordova/exec');

var testing = function(){};

testing.prototype.camera = function (arg0, success, error) {
    exec(success, error, 'CamerakitPlugin', 'CameraActivity', [arg0]);
};

testing.install = function () {
    if (!window.plugins) {
      window.plugins = {};
  
    }
  
    window.plugins.start = new testing();
    return window.plugins.start;
  
  };
  
cordova.addConstructor(testing.install);

});
