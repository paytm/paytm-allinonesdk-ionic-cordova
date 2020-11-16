/**
 */
package com.paytm;

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
import java.util.Iterator;

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
              Boolean restrictAppInvoke = paymentIntent.getBoolean("restrictAppInvoke");
              String callbackuUrl = paymentIntent.getString("callbackUrl");

              PaytmOrder paytmOrder = new PaytmOrder(orderId, mid, txnToken, amount, callbackuUrl);

              TransactionManager transactionManager = new TransactionManager(paytmOrder, new PaytmPaymentTransactionCallback(){
                @Override
                public void someUIErrorOccurred(String inErrorMessage) {
                  Log.d(TAG, "someUIErrorOccurred");
                  callbackContext.sendPluginResult(createPluginResult(false,inErrorMessage, null)); 
                }
                @Override
                public void onTransactionResponse(Bundle inResponse) {
                  Log.d(TAG, "onTransactionResponse");
                  Log.d(TAG, inResponse.toString());
                  callbackContext.sendPluginResult(createPluginResult(true, "", getJsonObjString(inResponse)));
                }
        
                @Override
                public void networkNotAvailable() { // If network is not
                  Log.d(TAG, "networkNotAvailable");
                  callbackContext.sendPluginResult(createPluginResult(false, "networkNotAvailable", null)); 
                }
        
                @Override
                public void onErrorProceed(String error) {
                  Log.d(TAG, "onErrorProceed");
                  Log.d(TAG, error);
                  callbackContext.sendPluginResult(createPluginResult(false,error, null)); 
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
                  callbackContext.sendPluginResult(createPluginResult(false,inErrorMessage, null)); 
                }
        
                @Override
                public void onErrorLoadingWebPage(int iniErrorCode, String inErrorMessage, String inFailingUrl) {
                  Log.d(TAG, "onErrorLoadingWebPage");
                  callbackContext.sendPluginResult(createPluginResult(false ,inErrorMessage, null)); 
                }
        
                // had to be added: NOTE
                @Override
                public void onBackPressedCancelTransaction() {
                  Log.d(TAG, "onBackPressedCancelTransaction");
                  callbackContext.sendPluginResult(createPluginResult(false ,"onBackPressedCancelTransaction", null)); 
                }
        
                @Override
                public void onTransactionCancel(String inErrorMessage, Bundle inResponse) {
                  Log.d(TAG, "onTransactionCancel " + inErrorMessage);
                  Log.d(TAG, inResponse.toString());
                  callbackContext.sendPluginResult(createPluginResult(false ,inErrorMessage, getJsonObjString(inResponse))); 
        
                }
                
              });
              Log.d(TAG, "isStaging " + isStaging);
              if(isStaging){
                Log.d(TAG, "setting staging showPaymentUrl");
                transactionManager.setShowPaymentUrl("https://securegw-stage.paytm.in/theia/api/v1/showPaymentPage");              
              }
              Log.d(TAG, "restrictAppInvoke " + restrictAppInvoke);
              if(restrictAppInvoke){
                Log.d(TAG, "disabling app invoke");
                transactionManager.setAppInvokeEnabled(false);
              }
              transactionManager.setCallingBridge("cordova");
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

  PluginResult createPluginResult(Boolean isSuccess, String message, String response){
    JSONObject data = new JSONObject();
    try{
      data.put("message", message);
      if(isSuccess)data.put("response", response);
    }catch(Exception e){
      Log.d(TAG, e.getMessage());
    }
    
    PluginResult result;
    if(isSuccess){
      result = new PluginResult(PluginResult.Status.OK, data);
    }else{
      result = new PluginResult(PluginResult.Status.ERROR, message);
    }
    result.setKeepCallback(true);
    return result;
  }

  String getJsonObjString(Bundle bundle){
    Iterator<String> it = bundle.keySet().iterator();
    JSONObject obj = new JSONObject();
    try{
      while(it.hasNext()){
        String key = it.next();
        obj.put(key, bundle.get(key));
      }
    }catch(Exception e){
      Log.d(TAG, e.getMessage());
    }
    return obj.toString();
  }

  @Override
  public void onActivityResult(int requestCode, int resultCode, Intent data) {
    Log.d(TAG, "onActivityResult, req. code: "+requestCode);
    if(requestCode == 100 && data != null){
      this.cbContext.sendPluginResult(createPluginResult(true, data.getStringExtra("nativeSdkForMerchantMessage"), data.getStringExtra("response")));
    }
    super.onActivityResult(requestCode, resultCode, data);
  }
}