import type {TurboModule} from 'react-native';
import type {EventEmitter} from 'react-native/Libraries/Types/CodegenTypes';
import {TurboModuleRegistry} from 'react-native';

export interface RNMessage {
  title: string,
  text: string,
  id: string,
  type: string,
  html_text?: string,
  custom?: Object,
  url?: string,
  card_image_url?: string,
  card_media_url?: string,
  is_read: boolean,
  created_at?: string,
}

export interface Spec extends TurboModule {
  notifyInAppHandled(handled: boolean): void;
  useDefaultInAppNotification(useDefault: boolean): void;
  getMessages(): Promise<Array<RNMessage>>;       
  getUnreadCount(): Promise<number>;
  markMessageAsRead(message: RNMessage): Promise<null>;
  removeMessage(message: RNMessage): Promise<null>;
  presentMessageDetail(message: RNMessage): void;
  dismissMessageDetail(): void;
  registerMessageImpression(impressionType: number, message: RNMessage): void; 
  clearMessages(): Promise<null>;

  readonly onInAppNotification: EventEmitter<RNMessage>;
}

export default TurboModuleRegistry.getEnforcing<Spec>(
  'RNMessageStream',
);