import { NativeModules } from 'react-native';
let Carnival = NativeModules.RNCarnival;
let SailthruMobile = NativeModules.RNSailthruMobile;

/**
 * A map for submitting collections of attributes to the SDK.
 * @deprecated Use SailthruMobile.AttributeMap
 */
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
 * @param {int} quantity The quantity of the item.
 * @param {string} title The name/title of the item.
 * @param {int} price The price of one unit of the item in cents (e.g. $10.99 is 1099).
 * @param {string} ID The ID you have set for the item.
 * @param {string} url The url for the item.
 * @deprecated Use SailthruMobile.PurchaseItem
 */
Carnival.PurchaseItem = function(quantity, title, price, id, url) {
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
    this.images = images;
  }
}

/**
 * Creates a purchase item from a content item.
 * @param {Object} contentItem The content item.
 * @deprecated Use SailthruMobile.PurchaseItem.fromContentItem
 */
Carnival.PurchaseItem.fromContentItem = function(contentItem) {
  var purchaseItem = new this(contentItem.purchase_qty, contentItem.title, contentItem.price, contentItem.sku, contentItem.url);
  purchaseItem.tags = contentItem.tags;
  purchaseItem.vars = contentItem.vars;
  purchaseItem.images = contentItem.images;
  return purchaseItem;
}

/**
 * Creates a Purchase object with the required field.
 * @param {Array} purchaseItems an array of {Carnival.PurchaseItem} objects.
 * @deprecated Use SailthruMobile.Purchase
 */
Carnival.Purchase = function(purchaseItems) {
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
    this.vars = vars;
  }

  /**
   * Set the message ID associated with the purchase. This is required to have revenue data matched
   * to email sends in Sailthru. Pass the identifying message_id of the email the user is coming from;
   * this will be the value stored in the sailthru_bid cookie for your domain. The message attribution
   * will be displayed in your Campaign Summary, Transactional Report, Purchase Log, and in Lifecycle
   * Optimizer Metrics.
   * @param {string} messageId the message ID
   */
  this.setMessageId = function(messageId) {
    this.message_id = messageId;
  }
}

/**
 * Creates a Purchase object from an array of ContentItem objects.
 * @param {Array} contentItems an array of ContentItem objects.
 * @deprecated Use SailthruMobile.Purchase.fromContentItems
 */
Carnival.Purchase.fromContentItems = function(contentItems) {
  var purchaseItems = [];
  contentItems.forEach(function (item, index) {
    purchaseItems.push(new Carnival.PurchaseItem(item));
  });
  return new this(purchaseItems);
}

/**
 * Sailthru Mobile
 */

/**
 * A map for submitting collections of attributes to the SDK.
 */
SailthruMobile.AttributeMap = function() {
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

SailthruMobile.MessageImpressionType = {StreamView: 1, DetailView: 2, InAppView: 0};
SailthruMobile.DeviceValues = {Attributes: 1, MessageStream: 2, Events: 4, ClearAll: 7};

/**
 * Creates a purchase item with the required fields.
 * @param {int} quantity The quantity of the item.
 * @param {string} title The name/title of the item.
 * @param {int} price The price of one unit of the item in cents (e.g. $10.99 is 1099).
 * @param {string} ID The ID you have set for the item.
 * @param {string} url The url for the item.
 */
SailthruMobile.PurchaseItem = function(quantity, title, price, id, url) {
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
    this.images = images;
  }
}

/**
 * Creates a purchase item from a content item.
 * @param {Object} contentItem The content item.
 */
SailthruMobile.PurchaseItem.fromContentItem = function(contentItem) {
  var purchaseItem = new this(contentItem.purchase_qty, contentItem.title, contentItem.price, contentItem.sku, contentItem.url);
  purchaseItem.tags = contentItem.tags;
  purchaseItem.vars = contentItem.vars;
  purchaseItem.images = contentItem.images;
  return purchaseItem;
}

/**
 * Creates a Purchase object with the required field.
 * @param {Array} purchaseItems an array of {Carnival.PurchaseItem} objects.
 */
SailthruMobile.Purchase = function(purchaseItems) {
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
    this.vars = vars;
  }

  /**
   * Set the message ID associated with the purchase. This is required to have revenue data matched
   * to email sends in Sailthru. Pass the identifying message_id of the email the user is coming from;
   * this will be the value stored in the sailthru_bid cookie for your domain. The message attribution
   * will be displayed in your Campaign Summary, Transactional Report, Purchase Log, and in Lifecycle
   * Optimizer Metrics.
   * @param {string} messageId the message ID
   */
  this.setMessageId = function(messageId) {
    this.message_id = messageId;
  }
}

/**
 * Creates a Purchase object from an array of ContentItem objects.
 * @param {Array} contentItems an array of ContentItem objects.
 */
SailthruMobile.Purchase.fromContentItems = function(contentItems) {
  var purchaseItems = [];
  contentItems.forEach(function (item, index) {
    purchaseItems.push(new Carnival.PurchaseItem(item));
  });
  return new this(purchaseItems);
}

module.exports = { Carnival : Carnival, SailthruMobile : SailthruMobile };
