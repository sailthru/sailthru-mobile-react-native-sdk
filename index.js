import { NativeModules } from 'react-native';
let Carnival = NativeModules.RNCarnival;

Carnival.AttributeMap = function() {
  this.MergeRules = {Update: 1, Replace: 2}

  this.mergeRule = this.MergeRules.Update;
  this.attributes = {};
  this.getAttributes = function() {return {attributes: this.attributes, mergeRule: this.mergeRule}}
  this.get = function(key) {return this.attributes[key] || null}
  this.remove = function(key) {delete this.attributes[key]}
  this.setMergeRule = function(rule) {
    switch (rule) {
      case this.MergeRules.Update:
      case this.MergeRules.Replace:
        this.mergeRule = rule;
        return;
    }

    throw new TypeError('Invalid merge rule');
  }

  this.setString = function(key, value) {
    if (typeof value === 'string') {
      this.attributes[key] = {type: 'string', value: value};
    } else {
      throw new TypeError(key + ' is not a string');
    }
  }

  this.setStringArray = function(key, value) {
    if (!Array.isArray(value)) {
      throw new TypeError(key + ' is not an array');
      return;
    }

    var array = [];
    for (var i in value) {
      if (typeof value[i] === 'string') {
        array.push(value[i]);
      } else {
        throw new TypeError(key + ': value at index ' + i + ' is not a string');
        return;
      }
    }

    this.attributes[key] = {type: 'stringArray', value: array};
  }

  this.setInteger = function(key, value) {
    if (typeof value === 'number' && isFinite(value) && Math.floor(value) === value) {
      this.attributes[key] = {type: 'integer', value: value};
    } else {
      throw new TypeError(key + ' is not an integer');
    }
  }

  this.setIntegerArray = function(key, value) {
    if (!Array.isArray(value)) {
      throw new TypeError(key + ' is not an array');
      return;
    }

    var array = [];
    for (var i in value) {
      if (typeof value[i] === 'number' && isFinite(value[i]) && Math.floor(value[i]) === value[i]) {
        array.push(value[i]);
      } else {
        throw new TypeError(key + ': value at index ' + i + ' is not an integer');
        return;
      }
    }

    this.attributes[key] = {type: 'integerArray', value: array};
  }

  this.setBoolean = function(key, value) {
    if (typeof value === 'boolean') {
      this.attributes[key] = {type: 'boolean', value: value};
    } else {
      throw new TypeError(key + ' is not a boolean');
    }
  }

  this.setFloat = function(key, value) {
    if (typeof value === 'number') {
      this.attributes[key] = {type: 'float', value: value};
    } else {
      throw new TypeError(key + ' is not a number');
    }
  }

  this.setFloatArray = function(key, value) {
    if (!Array.isArray(value)) {
      throw new TypeError(key + ' is not an array');
      return;
    }

    var array = [];
    for (var i in value) {
      if (typeof value[i] === 'number') {
        array.push(value[i]);
      } else {
        throw new TypeError(key + ': value at index ' + i + ' is not a number');
        return;
      }
    }

    this.attributes[key] = {type: 'floatArray', value: array};
  }

  this.setDate = function(key, value) {
    if (value instanceof Date && value.toString() !== 'Invalid Date') {
      this.attributes[key] = {type: 'date', value: value.getTime()};
    } else {
      throw new TypeError(key + ' is not a valid Date object');
    }
  }

  this.setDateArray = function(key, value) {
    if (!Array.isArray(value)) {
      throw new TypeError(key + ' is not an array');
      return;
    }

    var array = [];
    for (var i in value) {
      if (value[i] instanceof Date && value[i].toString() !== 'Invalid Date') {
        array.push(value[i].getTime());
      } else {
        throw new TypeError(key + ': value at index ' + i + ' is not a Date');
        return;
      }
    }

    this.attributes[key] = {type: 'dateArray', value: array};
  }
}

Carnival.MessageImpressionType = {StreamView: 1, DetailView: 2, InAppView: 0};
Carnival.DeviceValues = {Attributes: 1, MessageStream: 2, Events: 4, ClearAll: 7};

module.exports = Carnival;
