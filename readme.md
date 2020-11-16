# Paytm All-in-One SDK ionic cordova

Paytm All-in-One SDK provides a swift, secure and seamless payment experience to your users by invoking the Paytm app (if installed on your user’s smartphone) to complete payment for your order.

Paytm All-in-One SDK enables payment acceptance via Paytm wallet, Paytm Payments Bank, saved Debit/Credit cards, Net Banking, BHIM UPI and EMI as available in your customer’s Paytm account. If Paytm app is not installed on a customer's device, the transaction will be processed via web view within the All-in-One SDK.

This Cordova plugin helps you to be able to use the All-in-One SDK with your ionic application. This plugin supports both Android and iOS.


## Installation:
Add the plugin in your ionic application with the following command.

`ionic cordova plugin add cordova-paytm-allinonesdk`

It is also possible to install the plugin via repo url directly

`ionic cordova plugin add https://github.com/paytm/paytm-allinonesdk-ionic-cordova.git`

## Pre-requisite
### ionic-native wrapper
1. Checkout ionic-native public repo from _https://github.com/ionic-team/ionic-native_

2. Add a wrapper for _AllInOneSDK_ in the ionic-native repo you created in the last step by adding the following command in ionic-native directory.
```
gulp plugin:create -n AllInOneSDK
```
Running the command above will create a new directory src/@ionic-native/plugins/all-in-one-sdk/ with a single file in there: _index.ts_. This file is where all the plugin definitions should be.

3. Remove all the statements from the _index.ts_ file and add the following code in it.
```
import { Injectable } from '@angular/core';
import { Plugin, Cordova, CordovaProperty, CordovaInstance, InstanceProperty, IonicNativePlugin } from '@ionic-native/core';
import { Observable } from 'rxjs';

/**
 * @name AllInOneSDK
 * @description
 * Paytm All-in-One SDK plugin for Cordova/Ionic Applications
 * Paytm All-in-One SDK provides a swift, secure and seamless payment experience to your users by invoking the Paytm app (if installed on your user’s smartphone) to complete payment for your order.
 * Paytm All-in-One SDK enables payment acceptance via Paytm wallet, Paytm Payments Bank, saved Debit/Credit cards, Net Banking, BHIM UPI and EMI as available in your customer’s Paytm account. If Paytm app is not installed on a customer's device, the transaction will be processed via web view within the All-in-One SDK.
 * For more information about Paytm All-in-One SDK, please visit https://developer.paytm.com/docs/all-in-one-sdk/
 *
 * @usage
 * ```typescript
 * import { AllInOneSDK } from '@ionic-native/all-in-one-sdk/ngx';
 *
 *
 * constructor(private allInOneSDK: AllInOneSDK) { }
 *
 * ...
 *
 * For below parameters see [documentation](https://developer.paytm.com/docs/all-in-one-sdk/)
 * let paymentIntent = { mid : merchantID, orderId: orderId, txnToken: transactionToken, amount: amount, isStaging: isStaging, callbackUrl:callBackURL }
 *
 * this.allInOneSDK.startTransaction(paymentIntent)
 *   .then((res: any) => console.log(res))
 *   .catch((error: any) => console.error(error));
 *
 * ```
 *
 * For iOS:
 * After adding the plugin, open the iOS project, you can find the same at <projectName>/platforms/ios.
 * In case merchant don’t have callback URL, Add an entry into Info.plist LSApplicationQueriesSchemes(Array) Item 0 (String)-> paytm
 * Add a URL Scheme “paytm”+”MID”
 *
 */
@Plugin({
  pluginName: 'AllInOneSDK',
  plugin: 'cordova-paytm-allinonesdk',
  pluginRef: 'AllInOneSDK',
  repo: 'https://github.com/paytm/paytm-allinonesdk-ionic-cordova.git',
  platforms: ['Android','iOS']
})
export class AllInOneSDK extends IonicNativePlugin {

  /**
   * This function checks if Paytm Application is available on the device.
   * If Paytm exists then it invokes Paytm Application with the parameters sent and creates an order.
   * If the Paytm Application is not available the transaction is continued on a webView within All-in-One SDK.
   * @param options {PaymentIntentModel} These parameters are required and will be used to create an order.
   * @return {Promise<PaytmResponse>} Returns a promise that resolves when a transaction completes(both failed and successful).
   */
  @Cordova()
  startTransaction(options : PaymentIntentModel): Promise<PaytmResponse> {
    return;
  }

}

/**
 * The response that will be recieved when any transaction is completed
 */
export interface PaytmResponse{
    message : string;
    response : string; // A stringified response of a hashmap returned from All-in-One SDK
}

/**
 * For below parameters see [documentation](https://developer.paytm.com/docs/all-in-one-sdk/)
 */
export interface PaymentIntentModel{
    mid : string; // Merchant ID
    orderId : string; // Order ID
    txnToken : string; // Transaction Token
    amount : string; // Amount
    isStaging: boolean; // Environment
    callbackUrl: string; // Callback URL
    restrictAppInvoke: boolean;
}
```
4. Run the command `npm run build` in your _ionic-native_ directory, this will create a _dist_ directory. The dist directory will contain a sub directory _@ionic-native_ with all the packages compiled in there. Copy the package(all-in-one-sdk) you created to your app's _node_modules_ under the _@ionic-native_ directory. 
For example: `cp -r ../ionic-native/dist/@ionic-native/plugins/all-in-one-sdk node_modules/@ionic-native`. Change the path of directories as per your project structure.

### For iOS:
Add iOS platform to you application.

`ionic cordova platform add ios`

This will create an iOS platform for your application at the following path: 
 _applicationName/platforms/ios/applicationName.xcworkspace_

Make the following changes in your iOS project.
1. In case merchant don’t have callback URL, Add an entry into Info.plist **LSApplicationQueriesSchemes(Array) Item 0 (String)-> paytm**

![info.plist](https://developer.paytm.com/assets/iosInvoke.png)

2. Add a URL Scheme **“paytm”+”MID”**
![urlScheme](https://developer.paytm.com/assets/app-invoke-ios-inti.png)

## Usage:
Add the plugin to your app's provider list

```
import { AllInOneSDK } from '@ionic-native/all-in-one-sdk/ngx'

@NgModule({
  declarations: [...],
  entryComponents: [...],
  imports: [...],
  providers: [..., AllInOneSDK],
  bootstrap: [...]
})
export class AppModule {}
```

In your page from where you want to invoke the All-in-One SDK, add the following code:

```
import { AllInOneSDK } from '@ionic-native/all-in-one-sdk/ngx'

constructor(private allInOneSDK : AllInOneSDK) {}
//Call Initiate Transaction API from your backend to generate Transaction Token.
let paymentIntent = { mid : "<Merchant ID>",
                      orderId: "<Order ID>",
                      txnToken: "<Transaction Token generated by Initiate Transaction API from your backend>", 
                      amount: "<Amount>", 
                      isStaging: "<Environment(true/false)>", 
                      callbackUrl: "<Callback URL>",
                      restrictAppInvoke: "<Restrict(true/false)>" };
this.allInOneSDK.startTransaction(paymentIntent).then(
resp => {
  // The response recieved after the transaction is completed will be an object containing `message` and `response`. You can parse both and use them as required in your application
  if(resp.response != '')alert(JSON.parse(resp.response));
  else alert(resp.message);
}).catch(error => {
  alert(error);
})
```

## All-In-One API & SDK reference

**https://developer.paytm.com/docs/all-in-one-sdk/**
