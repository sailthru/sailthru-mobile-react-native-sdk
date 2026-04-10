import type {TurboModule} from 'react-native';
import {TurboModuleRegistry} from 'react-native';

export interface Spec extends TurboModule {
  logRegistrationEvent(userId: string | null): Promise<null>;
}

export default TurboModuleRegistry.getEnforcing<Spec>(
  'RNCheetah',
);