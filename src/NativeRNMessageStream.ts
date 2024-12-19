import type {TurboModule} from 'react-native';
import {TurboModuleRegistry} from 'react-native';

export interface Spec extends TurboModule {
  notifyInAppHandled(handled: boolean): void;
  useDefaultInAppNotification(useDefault: boolean): void;
  getMessages(): Promise<Array<Object>>;       
  getUnreadCount(): Promise<number>;
  markMessageAsRead(message: Object): Promise<null>;
  removeMessage(message: Object): Promise<null>;
  presentMessageDetail(message: Object): void;
  dismissMessageDetail(): void;
  registerMessageImpression(impressionType: number, message: Object): void; 
  clearMessages(): Promise<null>;
}

export default TurboModuleRegistry.getEnforcing<Spec>(
  'RNMessageStream',
);