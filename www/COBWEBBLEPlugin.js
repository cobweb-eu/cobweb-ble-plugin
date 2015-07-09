var exec = require('cordova/exec');

exports.pollBLEData = function(success, error) {
    exec(success, error, "COBWEBBLEPlugin", "", []);
};

