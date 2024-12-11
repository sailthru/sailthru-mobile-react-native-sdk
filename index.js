import { NativeModules } from 'react-native';
const { RNMarigold, RNEngageBySailthru, RNMessageStream } = NativeModules;

/**
 * A map for submitting collections of attributes to the SDK.
 */
RNEngageBySailthru.AttributeMap = function() {
  this.MergeRules = {Update: 0, Replace: 1}

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
    }

    var array = [];
    for (var i in value) {
      if (typeof value[i] === 'string') {
        array.push(value[i]);
      } else {
        throw new TypeError(key + ': value at index ' + i + ' is not a string');
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
    }

    var array = [];
    for (var i in value) {
      if (typeof value[i] === 'number' && isFinite(value[i]) && Math.floor(value[i]) === value[i]) {
        array.push(value[i]);
      } else {
        throw new TypeError(key + ': value at index ' + i + ' is not an integer');
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
    }

    var array = [];
    for (var i in value) {
      if (typeof value[i] === 'number') {
        array.push(value[i]);
      } else {
        throw new TypeError(key + ': value at index ' + i + ' is not a number');
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
    }

    var array = [];
    for (var i in value) {
      if (value[i] instanceof Date && value[i].toString() !== 'Invalid Date') {
        array.push(value[i].getTime());
      } else {
        throw new TypeError(key + ': value at index ' + i + ' is not a Date');
      }
    }

    this.attributes[key] = {type: 'dateArray', value: array};
  }
}

RNMessageStream.MessageImpressionType = {StreamView: 1, DetailView: 2, InAppView: 0};

/**
 * Creates a purchase item with the required fields.
 * @param {int} quantity The quantity of the item.
 * @param {string} title The name/title of the item.
 * @param {int} price The price of one unit of the item in cents (e.g. $10.99 is 1099).
 * @param {string} ID The ID you have set for the item.
 * @param {string} url The url for the item.
 */
RNEngageBySailthru.PurchaseItem = function(quantity, title, price, id, url) {
  if (typeof quantity === 'number' && isFinite(quantity) && Math.floor(quantity) === quantity) {
    this.qty = quantity;
  } else {
    throw new TypeError(quantity + ' is not an integer');
  }
  if (typeof title === 'string') {
    this.title = title;
  } else {
    throw new TypeError(title + ' is not a string');
  }
  if (typeof price === 'number' && isFinite(price) && Math.floor(price) === price) {
    this.price = price;
  } else {
    throw new TypeError(price + ' is not an integer');
  }
  if (typeof id === 'string') {
    this.id = id;
  } else {
    throw new TypeError(id + ' is not a string');
  }
  if (typeof url === 'string') {
    this.url = url;
  } else {
    throw new TypeError(url + ' is not a string');
  }

  /**
   * Sets the tags for the product.
   * @param {Array} tags Array of strings containing tags for the product.
   */
  this.setTags = function(tags) {
    if (!Array.isArray(tags)) {
      throw new TypeError(tags + ' is not an array');
      return;
    }

    var array = [];
    for (var i in tags) {
      if (typeof tags[i] === 'string') {
        array.push(tags[i]);
      } else {
        throw new TypeError('value at index ' + i + ' is not a string');
        return;
      }
    }

    this.tags = array;
  }

  /**
   * Sets vars that can be any number of custom fields and values to attach to each item for later retrieval
   * in templates or use in Audience Builder or Lifecycle Optimizer. For example, you may want to
   * specify item attributes such as color, size, material, or an item-specific coupon code that
   * was used.
   * @param {Object} vars the vars to set.
   */
  this.setVars = function(vars) {
    if (typeof vars === 'object') {
      this.vars = vars;
    } else {
      throw new TypeError(vars + ' is not a valid object');
    }
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
    if (typeof images !== 'object') {
      throw new TypeError(images + ' is not a valid object');
    }
    var imageMap = {};
    if (typeof images.full === 'object' && typeof images.full.url === 'string') {
      imageMap.full = { "url": images.full.url };
    }
    if (typeof images.thumb === 'object' && typeof images.thumb.url === 'string') {
      imageMap.thumb = { "url": images.thumb.url };
    }
    this.images = imageMap;
  }
}

/**
 * Creates a purchase adjustment with the required fields.
 * @param {string} title The name/title of the adjustment.
 * @param {int} price The price of the adjustment in cents (e.g. $10.99 is 1099, -$23.45 is -2345).
 */
RNEngageBySailthru.PurchaseAdjustment = function(title, price) {
  if (typeof title === 'string') {
    this.title = title;
  } else {
    throw new TypeError(title + ' is not a string');
  }

  if (typeof price === 'number' && isFinite(price) && Math.floor(price) === price) {
    this.price = price;
  } else {
    throw new TypeError(price + ' is not an integer');
  }
}

/**
 * Creates a Purchase object with the required field.
 * @param {Array} purchaseItems an array of {RNEngageBySailthru.PurchaseItem} objects.
 */
RNEngageBySailthru.Purchase = function(purchaseItems) {
  if (!Array.isArray(purchaseItems)) {
    throw new TypeError(purchaseItems + ' is not an array');
    return;
  }

  var array = [];
  for (var i in purchaseItems) {
    if (typeof purchaseItems[i] === 'object') {
      array.push(purchaseItems[i]);
    } else {
      throw new TypeError('value at index ' + i + ' is not a purchase item');
      return;
    }
  }
  this.items = array;

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
    if (typeof vars === 'object') {
      this.vars = vars;
    } else {
      throw new TypeError(vars + ' is not a valid object');
    }
  }

  /**
   * Set the message ID associated with the purchase. This is required to have revenue data matched
   * to email sends in Marigold. Pass the identifying message_id of the email the user is coming from;
   * this will be the value stored in the sailthru_bid cookie for your domain. The message attribution
   * will be displayed in your Campaign Summary, Transactional Report, Purchase Log, and in Lifecycle
   * Optimizer Metrics.
   * @param {string} messageId the message ID
   */
  this.setMessageId = function(messageId) {
    if (typeof messageId === 'string') {
      this.message_id = messageId;
    } else {
      throw new TypeError(messageId + ' is not a string');
    }
  }

  /**
   * An array of the adjustments (positive or negative) that should be applied to the total order value.
   * Title and price (in cents) are required. The amount should be negative to factor in a deduction to
   * the final price, such as a discount; the amount should be positive to factor in an additional cost,
   * such as shipping. For example, -1000 on an item originally priced at $25 would reduce the price and
   * pass $15 to the user’s profile under the price field for that item. Recommended keys:
   * tax – Taxes applied to order
   * shipping – Any shipping and/or handling fees applied to order
   * discount – Discount off order from promotion code, coupon, etc.
   * gift_card – Amount of order covered by gift card payment
   * gift_wrap – Additional fee for gift wrapping.
   * credits – Amount of order covered by account credit
   * tip – Any gratuity added to purchase
   * If you are using Retention Analytics, these keys or similar custom keys are highly recommended.
   * @param {Array} purchaseAdjustments an array of {Marigold.PurchaseAdjustment} objects.
   */
  this.setPurchaseAdjustments = function(purchaseAdjustments) {
    if (!Array.isArray(purchaseAdjustments)) {
      throw new TypeError(purchaseAdjustments + ' is not an array');
      return;
    }

    var array = [];
    for (var i in purchaseAdjustments) {
      if (typeof purchaseAdjustments[i] === 'object') {
        array.push(purchaseAdjustments[i]);
      } else {
        throw new TypeError('value at index ' + i + ' is not a purchase adjustment');
        return;
      }
    }

    this.adjustments = array;
  }
}

module.exports = {
  Marigold: RNMarigold,
  EngageBySailthru: RNEngageBySailthru,
  MessageStream: RNMessageStream
};