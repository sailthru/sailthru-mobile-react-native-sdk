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

/**
 * Creates a purchase item with the required fields.
 * @param {Number} quantity The quantity of the item.
 * @param {String} title The name/title of the item.
 * @param {Number} price The price of one unit of the item.
 * @param {Number} ID The ID you have set for the item.
 * @param {String} url The url for the item.
 */
Carnival.PurchaseItem = function(quantity, title, price, id, url) {
  if (!quantity instanceof Number) {
    throw new TypeError(quantity + ' is not a number');
    return;
  }
  if (!title instanceof String) {
    throw new TypeError(title + ' is not a string');
    return;
  }
  if (!price instanceof Number) {
    throw new TypeError(price + ' is not a number');
    return;
  }
  if (!id instanceof Number) {
    throw new TypeError(id + ' is not a number');
    return;
  }
  if (!url instanceof String) {
    throw new TypeError(url + ' is not a string');
    return;
  }

  this.qty = quantity;
  this.title = title;
  this.price = price;
  this.id = id;
  this.url = url;

  /**
   * Sets the tags for the product.
   * @param {Array} tags Array of strings containing tags for the product.
   */
  this.setTags = function(tags) {
    if (!Array.isArray(tags)) {
      throw new TypeError(tags + ' is not an array');
      return;
    }
    this.tags = tags;
  }

  /**
   * Sets vars that can be any number of custom fields and values to attach to each item for later retrieval
   * in templates or use in Audience Builder or Lifecycle Optimizer. For example, you may want to
   * specify item attributes such as color, size, material, or an item-specific coupon code that
   * was used.
   * @param {Object} vars the vars to set.
   */
  this.setVars = function(vars) {
    if (!vars instanceof Object) {
      throw new TypeError(vars + ' is not an object');
      return;
    }
    this.vars = vars;
  }

  /**
   * Sets a map of images, full and/or thumb, to objects specifying the URL for each image.
   * Use the name “full” to denote the full-sized image, and “thumb” to denote the thumbnail-sized
   * image. For example, the value of images might be:
   *      {
   *         “full” : {
   *             “url” : “http://example.com/f.jpg”
   *         },
   *         “thumb” : {
   *             “url” : “http://example.com/t.jpg”
   *         }
   *     }
   * This allows you to easily include product images when messaging users with order
   * confirmations and abandoned-cart reminders.
   * @param {Object} images images to set.
   */
  this.setImages = function(images) {
    if (!images instanceof Object) {
      throw new TypeError(images + ' is not an object');
      return;
    }
    this.images = images;
  }
}

/**
 * Creates a Purchase object with the required field.
 * @param {Array} purchaseItems an array of {Carnival.PurchaseItem} objects.
 */
Carnival.Purchase = function(purchaseItems) {
  if (!Array.isArray(purchaseItems)) {
    throw new TypeError(purchaseItems + ' is not an array');
    return;
  }

  this.items = purchaseItems;

  /**
   * Sets any number of custom variables to attach to the order. These are commonly used with the
   * Audience Builder “Purchase Order Var Is” query. For example, you could specify the shipping
   * address, estimated delivery date given, credit card type used, whether a deal was used, the
   * promo code used, etc.Note that a vars object may also exist at the item level. See the example
   * code, and the items definition in this table.You may use any custom order variable name(s).
   * Note that the following variable name is reserved at the order level, for a particular purpose:
   *     st_cost – The client’s cost for the items in the purchase, in cents. The value should be
   *     an integer, and is recommended if you are using Retention Analytics in order to report
   *     net revenue.
   * @param {Object} vars map containing the custom fields for the purchase.
   */
  this.setVars = function(vars) {
    if (!vars instanceof Object) {
      throw new TypeError(vars + ' is not an object');
      return;
    }
    this.vars = vars;
  }
}

module.exports = Carnival;
