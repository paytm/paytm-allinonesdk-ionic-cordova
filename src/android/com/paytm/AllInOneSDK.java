/**
 */
package com.example;

import org.apache.cordova.CallbackContext;
import org.apache.cordova.CordovaInterface;
import org.apache.cordova.CordovaPlugin;
import org.apache.cordova.CordovaWebView;
import org.apache.cordova.PluginResult;
import org.apache.cordova.PluginResult.Status;
import org.json.JSONObject;
import org.json.JSONArray;
import org.json.JSONException;

import android.util.Log;
import android.os.Bundle;
import android.content.Context;
import android.content.Intent;

import java.util.Date;

import com.paytm.pgsdk.PaytmOrder;
import com.paytm.pgsdk.TransactionManager;
import com.paytm.pgsdk.PaytmPaymentTransactionCallback;

public class AllInOneSDK extends CordovaPlugin {
  private static final String TAG = "AllInOneSDK";
  private CallbackContext cbContext;
  public void initialize(CordovaInterface cordova, CordovaWebView webView) {
    super.initialize(cordova, webView);
    Log.d(TAG, "Initializing AllInOneSDK");
    this.cordova.setActivityResultCallback((CordovaPlugin)this);
  }

  public boolean execute(String action, JSONArray args, final CallbackContext callbackContext) throws JSONException {
    if(action.equals("startTransaction")) {
      this.cbContext = callbackContext;
      Log.d(TAG, "starting transactions");
      Log.d(TAG, args.get(0).toString());
      cordova.setActivityResultCallback(this);
      cordova.getThreadPool().execute(
        new Runnable(){
          @Override
          public void run(){
            try{
              JSONObject paymentIntent = (JSONObject)args.get(0);
              String orderId = paymentIntent.getString("orderId");
              String mid = paymentIntent.getString("mid");
              String txnToken = paymentIntent.getString("txnToken");
              String amount = paymentIntent.getString("amount");
              Boolean isStaging = paymentIntent.getBoolean("isStaging");
              String callbackuUrl = paymentIntent.getString("callbackUrl");

              PaytmOrder paytmOrder = new PaytmOrder(orderId, mid, txnToken, amount, callbackuUrl);

              TransactionManager transactionManager = new TransactionManager(paytmOrder, new PaytmPaymentTransactionCallback(){
                @Override
                public void someUIErrorOccurred(String inErrorMessage) {
                  Log.d(TAG, "someUIErrorOccurred");
                  callbackContext.sendPluginResult(createPluginResult(inErrorMessage, null)); 
                }
                @Override
                public void onTransactionResponse(Bundle inResponse) {
                  Log.d(TAG, "onTransactionResponse");
                  Log.d(TAG, inResponse.toString());
                  callbackContext.sendPluginResult(createPluginResult("", inResponse.toString()));
                }
        
                @Override
                public void networkNotAvailable() { // If network is not
                  Log.d(TAG, "networkNotAvailable");
                  callbackContext.sendPluginResult(createPluginResult("networkNotAvailable", null)); 
                }
        
                @Override
                public void onErrorProceed(String error) {
                  Log.d(TAG, "onErrorProceed");
                  Log.d(TAG, error);
                  callbackContext.sendPluginResult(createPluginResult(error, null)); 
                }
        
                @Override
                public void clientAuthenticationFailed(String inErrorMessage) {
                  // This method gets called if client authentication
                  // failed. // Failure may be due to following reasons //
                  // 1. Server error or downtime. // 2. Server unable to
                  // generate checksum or checksum response is not in
                  // proper format. // 3. Server failed to authenticate
                  // that client. That is value of payt_STATUS is 2. //
                  // Error Message describes the reason for failure.
                  Log.d(TAG, "clientAuthenticationFailed");
                  callbackContext.sendPluginResult(createPluginResult(inErrorMessage, null)); 
                }
        
                @Override
                public void onErrorLoadingWebPage(int iniErrorCode, String inErrorMessage, String inFailingUrl) {
                  Log.d(TAG, "onErrorLoadingWebPage");
                  callbackContext.sendPluginResult(createPluginResult(inErrorMessage, null)); 
                }
        
                // had to be added: NOTE
                @Override
                public void onBackPressedCancelTransaction() {
                  Log.d(TAG, "onBackPressedCancelTransaction");
                  callbackContext.sendPluginResult(createPluginResult("onBackPressedCancelTransaction", null)); 
                }
        
                @Override
                public void onTransactionCancel(String inErrorMessage, Bundle inResponse) {
                  Log.d(TAG, "onTransactionCancel " + inErrorMessage);
                  Log.d(TAG, inResponse.toString());
                  callbackContext.sendPluginResult(createPluginResult(inErrorMessage, inResponse.toString())); 
        
                }
                
              });
              transactionManager.startTransaction(cordova.getActivity(), 100);

            }catch(Exception e){
              Log.e(TAG, e.getMessage());
            }
          }
        }
      );            
    } 
    return true;
  }

  PluginResult createPluginResult(String message, String response){
    JSONObject data = new JSONObject();
    try{
      data.put("message", message);
      data.put("response", response);
    }catch(Exception e){
      Log.d(TAG, e.getMessage());
    }
    
    PluginResult result = new PluginResult(PluginResult.Status.OK, data);
    result.setKeepCallback(true);
    return result;
  }

  @Override
  public void onActivityResult(int requestCode, int resultCode, Intent data) {
    Log.d(TAG, "onActivityResult, req. code: "+requestCode);
    if(requestCode == 100){
      this.cbContext.sendPluginResult(createPluginResult(data.getStringExtra("nativeSdkForMerchantMessage"), data.getStringExtra("response")));
    }
    super.onActivityResult(requestCode, resultCode, data);
  }
}
