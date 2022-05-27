import { NativeModules, Platform } from 'react-native';

interface IUploadConfig {
  endPoint: string;
  ak: string;
  sk: string;
  token: string;
  bucketName: string;
  objectName: string;
  filePath: string;
  socketTimeout?: number;
  connectionTimeout?: number;
  taskNum?: number;
  partSize?: number;
  checkpoint?: boolean;
}

const LINKING_ERROR =
  `The package 'react-native-hw-cloud' doesn't seem to be linked. Make sure: \n\n` +
  Platform.select({ ios: "- You have run 'pod install'\n", default: '' }) +
  '- You rebuilt the app after installing the package\n' +
  '- You are not using Expo managed workflow\n';

const HwCloud = NativeModules.HwCloud
  ? NativeModules.HwCloud
  : new Proxy(
      {},
      {
        get() {
          throw new Error(LINKING_ERROR);
        },
      }
    );

export function upload(config: IUploadConfig): Promise<any> {
  return HwCloud.upload(config);
}
