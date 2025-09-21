import type {TurboModule} from 'react-native';
import {TurboModuleRegistry} from 'react-native';

export interface Spec extends TurboModule {
  updateLocation(lat: number, lon: number): void;
  getDeviceID(): Promise<string>;  
  setGeoIPTrackingEnabled(enabled: boolean): Promise<null>;
  setCrashHandlersEnabled(enabled: boolean): void;
  logRegistrationEvent(userId: string | null): void; 
  registerForPushNotifications(): void;
  syncNotificationSettings(): void;
  setInAppNotificationsEnabled(enabled: boolean): void;
}

export default TurboModuleRegistry.getEnforcing<Spec>(
  'RNMarigold',
);