import type {TurboModule} from 'react-native';
import {TurboModuleRegistry} from 'react-native';

export interface Spec extends TurboModule {
  setAttributes(attributeMap: Object): Promise<null>;
  removeAttribute(key: string): Promise<null>;
  clearAttributes(): Promise<null>;
  logEvent(name: string, vars: Object | null): void;
  setUserId(userId: string | null): Promise<null>;
  setUserEmail(userEmail: string | null): Promise<null>;
  trackClick(sectionId: string, url: string | null): Promise<null>;
  trackPageview(url: string, tags: Array<string> | null): Promise<null>;
  trackImpression(sectionId: string, urls: Array<string> | null): Promise<null>;
  setProfileVars(vars: Object): Promise<null>;
  getProfileVars(): Promise<Object>;
  logPurchase(purchase: Object): Promise<null>;
  logAbandonedCart(purchase: Object): Promise<null>;
  clearEvents(): Promise<null>;
}

export default TurboModuleRegistry.getEnforcing<Spec>(
  'RNEngageBySailthru',
);