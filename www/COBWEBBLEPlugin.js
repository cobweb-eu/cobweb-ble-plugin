var exec = require('cordova/exec');

exports.photoWithVA = function(success, error) {
    exec(success, error, "COBWEBBLEPlugin", "", []);
};

