var exec = require('cordova/exec');

exports.pollBLEData = function(success, error) {
    exec(success, error, "COBWEBBLEPlugin", "btRead", []);
};

exports.testBLEData = function(success, error) {
    exec(success, error, "COBWEBBLEPlugin", "test", []);
};

exports.testArrayBLEData = function(success, error) {
    exec(success, error, "COBWEBBLEPlugin", "testArr", []);
};

