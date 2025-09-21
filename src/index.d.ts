// import type {EventEmitter} from 'react-native/Libraries/Types/CodegenTypes';

declare module 'react-native-marigold' {
  export interface MarigoldStatic {
    /**
     * Forward a location to the Marigold Mobile platform. This method can be used when you're already tracking location in your app
     * and you just want to forward your existing calls to the Marigold mobile SDK.
     * @param lat Latitude to send
     * @param lon Longitude to send
     */
    updateLocation(lat: number, lon: number): void;
    /**
     * Returns the current device's ID as a string.
     */
    getDeviceID(): Promise<string>;
    /**
     * Enabled location tracking based on IP Address. Tracking location tracking is enabled by default.
     * Use this method for users who may not want to have their location tracked at all.
     * @param enabled A boolean value indicating whether or not to disable location based on IP Address.
     */
    setGeoIPTrackingEnabled(enabled: boolean): Promise<null>;
    /**
     * Enable or disable crash tracking for recording sessions which end in a crash.
     * Warning: This is for advanced uses where in some cases, crash handlers from Test Flight or Fabric (Crashlytics) interrupt our crash detection.
     * If you are not experiencing these issues, do not use this method.
     * @param enabled A boolean value indicating whether or not to install the crash handlers.
     */
    setCrashHandlersEnabled(enabled: boolean): void;
    /**
     * Log a registration event with Marigold. This is used to log users signing in and out of the app.
     * Pass the ID you wish to use for the sign-in or null for a sign-out.
     * @param userId The ID of the user signing in, or null for sign-out.
     */
    logRegistrationEvent(userId: string | null): void;
    /**
     * Request permission from the user to display notification content.
     */
    registerForPushNotifications(): void;
    /**
     * Sync the current statee of the notification display settings with the Marigold mobile platform.
     */
    syncNotificationSettings(): void;
    /**
     * Enables or disables the showing of in-app notifications.
     * @param enabled A boolean value indicating whether in-app notfications are enabled.
     */
    setInAppNotificationsEnabled(enabled: boolean): void;
  }

  export interface Message {
    /** The title of the message. */
    title: string;
    /** The body text of the message. */
    text: string;
    /** The unique ID of the message. */
    id: string;
    /** The type of the message. */
    type: string;
    /** The body text of the message rendered into HTML. */
    html_text?: string;
    /** A map of arbitary attributes set on the message. */
    custom?: Object;
    /** The URL attached to the message. */
    url?: string;
    /** The URL of the image attached to the message. */
    card_image_url?: string;
    /** The URL of the video attached to the message. */
    card_media_url?: string;
    /** Whether or not the message has been marked as read. */
    is_read: boolean;
    /** The time at which the message was created. */
    created_at?: string;
  }

  export interface MessageStreamStatic {
    /**
     * Callback to let the SDK know whether the in-app notification has been successfully handled at
     * the React Native level.
     * @param handled A boolean value indicating whether or not the in-app notification has been handled.
     * Return false to fallback on the default handling.
     */
    notifyInAppHandled(handled: boolean): void;
    /**
     * Set whether the SDK should pass the in-app notification to the React Native layer for handling, or
     * just pass straight to the default.
     * @param useDefault A boolean to indicate whether use the default in-app notification handling.
     */
    useDefaultInAppNotification(useDefault: boolean): void;
    /**
     * Asynchronously returns an array of messages for the device.
     */
    getMessages(): Promise<Array<Message>>;
    /**
     * Asynchronously returns the total number of unread messages in the message stream.
     */
    getUnreadCount(): Promise<number>;
    /**
     * Asynchronously marks a given message as read.
     * @param message The message to mark as read.
     */
    markMessageAsRead(message: Message): Promise<null>;
    /**
     * Asynchronously remove the message from the Message Stream.
     * @param message 
     */
    removeMessage(message: Message): Promise<null>;
    /**
     * Shows the message detail screen for a given message
     * @param message The message to display.
     */
    presentMessageDetail(message: Message): void;
    /**
     * Dismisses the currently displayed message detail screen.
     * @note No-op on Android.
     */
    dismissMessageDetail(): void;
    /**
     * Creates an impression for a message for a given interaction type.
     * @param impressionType the MessageImpressionType type of the impression.
     * @param message The message to apply the impression to.
     */
    registerMessageImpression(impressionType: number, message: Message): void;
    /**
     * Asynchronously clear the Message Stream for the device.
     */
    clearMessages(): Promise<null>;

    // TODO - remove since it's only in new arch? 
    readonly onInAppNotification?: any;

    MessageImpressionType: MessageStreamStatic.MessageImpressionType
  }

  export namespace MessageStreamStatic {
    export type MessageImpressionType = { StreamView: 1, DetailView: 2, InAppView: 0 };
  }

  export interface EngageBySailthruStatic {
    /**
     * Asyncronously set a collection of attributes on the device.
     * @param attributeMap The attributes to set.
     */
    setAttributes(attributeMap: EngageBySailthruStatic.AttributeMap): Promise<null>;
    /**
     * Asyncronously remove an attribute value for a given key.
     * @param key The string value of the key.
     */
    removeAttribute(key: string): Promise<null>;
    /**
     * Asyncronously clear the Attribute data from the device.
     */
    clearAttributes(): Promise<null>;
    /**
     * Log a custom event with the given name.
     * @param name The name of the custom event to be logged.
     * @param vars The associated variables for the event (optional).
     */
    logEvent(name: string, vars: Object | null): void;
    /**
     * Asyncronously clear the custom events from the device data.
     */
    clearEvents(): Promise<null>;
    /**
     * Set a user ID for the device.
     * @param userId The ID of the user to be set.
     */
    setUserId(userId: string | null): Promise<null>;
    /**
     * Sets a user email for the device.
     * @param userEmail The email of the user to be set.
     */
    setUserEmail(userEmail: string | null): Promise<null>;
    /**
     * Track that a section has been tapped on, transitioning the user to a detail view.
     * @param sectionId the Section ID corresponding to the section being tapped on.
     * @param url the URL of the detail being transitioned to.
     */
    trackClick(sectionId: string, url: string | null): Promise<null>;
    /**
     * Register a pageview has occurred.
     * @param url The URL of the content we're tracking a view of. Must be a valid URL with protocol http:// or https:// 
     * this generally should correspond to the web link of the content being tracked, and the stored URL in the Sailthru content collection.
     * @param tags Tags for this content (optional).
     */
    trackPageview(url: string, tags: Array<string> | null): Promise<null>;
    /**
     * Registers an impression - a reasonable expectation that a user has seen a piece of content.
     * @param sectionId the Section ID corresponding to the section being viewed
     * @param urls a List of the URLs of the items contained within this section. Useful if multiple items
     * of content are contained within a section, otherwise just pass a single-item array.
     */
    trackImpression(sectionId: string, urls: Array<string> | null): Promise<null>;
    /**
     * Set the profile level vars.
     * @param vars JSON of vars to set on the server.
     */
    setProfileVars(vars: Object): Promise<null>;
    /**
     * Retrieve the profile vars.
     */
    getProfileVars(): Promise<Object>;
    /**
     * Asyncronously log a purchase with Sailthru platform. This can be used for mobile purchase attribution.
     * @param purchase The purchase to log with the platform.
     */
    logPurchase(purchase: EngageBySailthruStatic.Purchase): Promise<null>;
    /**
     * Asyncronously log a cart abandonment with the Sailthru platform. Use this to initiate cart abandoned flows.
     * @param purchase The abandoned purchase to log with the platform.
     */
    logAbandonedCart(purchase: EngageBySailthruStatic.Purchase): Promise<null>;

    AttributeMap: EngageBySailthruStatic.AttributeMap
    PurchaseAdjustment: EngageBySailthruStatic.PurchaseAdjustment
    PurchaseItem: EngageBySailthruStatic.PurchaseItem
    Purchase: EngageBySailthruStatic.Purchase
  }

  export namespace EngageBySailthruStatic {
    export interface AttributeMap {
        new(): AttributeMap;
        /**
         * Return the full attributes map.
         */
        getAttributes(): AttributeMap.Attributes;
        /**
         * Return the attribute associated with the provided key.
         * @param key The key for the attribute to return.
         */
        get(key: string): any | null
        /**
         * Remove the attribute for the provided key.
         * @param key The key for the attribute to remove.
         */
        remove(key: string): void;
        /**
         * Set the merge rule for the attribute map.
         * @param rule Whether to update existing values or completely replace them.
         */
        setMergeRule(rule: number): void;
        /**
         * Set a string value for the provided key.
         * @param key The key for the attribute.
         * @param value The value for the attribute.
         */
        setString(key: string, value: string): void;
        /**
         * Set an array of string values for the provided key.
         * @param key The key for the attribute.
         * @param value The values for the attribute.
         */
        setStringArray(key: string, value: Array<string>): void;
        /**
         * Set an integer value for the provided key.
         * @param key The key for the attribute.
         * @param value The value for the attribute.
         */
        setInteger(key: string, value: number): void;
        /**
         * Set an array of integer values for the provided key.
         * @param key The key for the attribute.
         * @param value The values for the attribute.
         */
        setIntegerArray(key: string, value: Array<number>): void;
        /**
         * Set a boolean value for the provided key.
         * @param key The key for the attribute.
         * @param value The value for the attribute.
         */
        setBoolean(key: string, value: boolean): void;
        /**
         * Set a float value for the provided key.
         * @param key The key for the attribute.
         * @param value The value for the attribute.
         */
        setFloat(key: string, value: number): void;
        /**
         * Set an array of float values for the provided key.
         * @param key The key for the attribute.
         * @param value The values for the attribute.
         */
        setFloatArray(key: string, value: Array<number>): void;
        /**
         * Set a date value for the provided key.
         * @param key The key for the attribute.
         * @param value The value for the attribute.
         */
        setDate(key: string, value: Date): void;
        /**
         * Set an array of date values for the provided key.
         * @param key The key for the attribute.
         * @param value The values for the attribute.
         */
        setDateArray(key: string, value: Array<Date>): void;

        MergeRules: AttributeMap.MergeRules
    }

    namespace AttributeMap {
        export type MergeRules = { Update: 0, Replace: 1 }
        export interface Attributes {
          attributes: any,
          mergeRule: AttributeMap.MergeRules
        }
    }

    export interface PurchaseAdjustment {
      /**
       * Create a purchase adjustment instance with the required fields.
       * @param title The title of the purchase adjustment.
       * @param price The cost of a single purchase adjustment in cents.
       */
      new(title: string, price: number): PurchaseAdjustment,
      /** Short user-readable name/title of the purchase adjustment. */
      title: string,
      /** Price of one purchase adjustment, in cents (e.g. $10.99 is 1099). */
      price: number,
    }

    export interface PurchaseItem {
      /**
       * Create a purchase item instance with the required fields.
       *
       * @param quantity The quantity of the item.
       * @param title    The title of the item.
       * @param price    The cost of a single item in cents.
       * @param ID       Your identifier for the item.
       * @param URL      The URL for the item.
       */
      new(quantity: number, title: string, price: number, id: string, url: string): PurchaseItem,
      /** Quantity of the item purchased. */
      quantity: number;
      /** Short user-readable name/title of the item purchased. */
      title: string;
      /** Price of one item, in cents (e.g. $10.99 is 1099). */
      price: number;
      /** Your unique identifier (for example, SKU) for the item. */
      id: string;
      /** The URL of the item. */
      url: string;
      /** A list of tags applicable to the product. */
      tags?: Array<string>;
      /** Any number of custom fields and values to attach to each item. */
      vars?: Object;
      /** A map of image types full and/or thumb to objects specifying the URL for each image. Use the
       * name “full” to denote the full-sized image, and “thumb” to denote the thumbnail-sized image. */
      images?: Object;
      /**
       * Set the tags associated with the purchase item.
       * @param tags Array of tag strings for the purchase item.
       */
      setTags(tags: Array<string>): void;
      /**
       * Set the associated vars for the purchase item.
       * @param vars The vars to set on the purchase item.
       */
      setVars(vars: Object): void;
      /**
       * Set the images associated with the purchase item.
       * @param images Images to set on the purchase item.
       */
      setImages(images: PurchaseItem.Images): void;
    }

    namespace PurchaseItem {
        export interface Images {
          full?: {
            url: string;
          }
          thumb?: {
            url: string;
          }
        }
    }

    export interface Purchase {
      /**
       * Create a purchase instance with the required fields.
       * @param purchaseItems The purchase items associated with the purchase.
       */
      new(purchaseItems: Array<PurchaseItem>): Purchase;
      /** An array containing all items in the user’s cart. */
      items: Array<PurchaseItem>;
      /** Any number of custom variables to attach to the order. */
      vars?: Object;
      /** ID of the corresponding message for the purchase. */
      message_id?: string;
      /** An array of the adjustments (positive or negative) that should be applied to the total order value. */
      adjustments?: Array<PurchaseAdjustment>
      /**
       * Set the associated vars for the purchase.
       * @param vars The vars to set on the purchase.
       */
      setVars(vars: Object): void;
      /**
       * Set the associated message ID for the purchase. Required to have revenue data matched to email sends in Sailthru. Pass the identifying
       * message_id of the email the user is coming from; this will be the value stored in the sailthru_bid cookie for your domain.
       * @param messageId The ID of the associated message.
       */
      setMessageId(messageId: string): void;
      /**
       * Attach an array of purchase adjustments to the purchase.
       * @param purchaseAdjustments The adjustments to attach.
       */
      setPurchaseAdjustments(purchaseAdjustments: Array<PurchaseAdjustment>): void;
    }
  }
  
  export const Marigold: MarigoldStatic
  export type Marigold = MarigoldStatic

  export const MessageStream: MessageStreamStatic
  export type MessageStream = MessageStreamStatic

  export const EngageBySailthru: EngageBySailthruStatic
  export type EngageBySailthru = EngageBySailthruStatic
}
