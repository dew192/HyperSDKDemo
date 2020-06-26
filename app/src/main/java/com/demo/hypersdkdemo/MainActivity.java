package com.demo.hypersdkdemo;

import android.os.Bundle;
import android.os.Handler;
import android.text.TextUtils;
import android.util.Log;
import android.view.ViewGroup;
import android.webkit.WebView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import org.json.JSONException;
import org.json.JSONObject;

import in.juspay.hypersdk.core.PaymentConstants;
import in.juspay.hypersdk.data.JuspayResponseHandler;
import in.juspay.hypersdk.ui.HyperPaymentsCallbackAdapter;
import in.juspay.services.HyperServices;

public class MainActivity extends AppCompatActivity {

    private String value;
    private HyperServices hyperServices;
    private TextView textView;
    private String merchantId = "TUL_PPRD";
    private String clientId = "TUL_PPRD_android";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        textView = findViewById(R.id.textView);
        WebView.setWebContentsDebuggingEnabled(true);

        prefetchJuspay();
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                hyperSDKInitialize();
            }
        }, 5000);
    }

    private void getHyperServicesInstance() {
        if (null == hyperServices) {
            synchronized (this) {
                if (null == hyperServices)
                    hyperServices = new HyperServices(this, (ViewGroup) findViewById(R.id.container));
            }
        }
    }

    private void prefetchJuspay() {
        // checking prefetch conditions
        boolean useBetaAssets = false; // To Discuss with Amol If you want to use beta assets
        JSONObject payload = new JSONObject();
        JSONObject innerPayload = new JSONObject();
        try {
            innerPayload.put("clientId", clientId);
            payload.put("betaAssets", useBetaAssets);
            payload.put("payload", innerPayload);
            payload.put("service", "in.juspay.ec"); //juspay service
        } catch (JSONException e) {
            e.printStackTrace();
        }
        HyperServices.preFetch(this, payload);
    }

    private void hyperSDKInitialize() {
        getHyperServicesInstance();
        JSONObject payload = new JSONObject();
        try {
            payload.put("requestId", "jusPayInitiate1");
            payload.put("service", "in.juspay.ec");
            payload.put("betaAssets", true);
            JSONObject innerPayload = new JSONObject();
            innerPayload.put("action", "initiate");
            innerPayload.put("merchantId", merchantId);
            innerPayload.put("clientId", clientId);
            innerPayload.put("customerId", "123456789");
            innerPayload.put("environment", PaymentConstants.ENVIRONMENT.PRODUCTION);
            payload.put("payload", innerPayload);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        Log.i("HyperSDKInitiate", "Request: " + payload);
        updateStringInTextView("HyperSDKInitiate: Request -->\n" + payload);

        hyperServices.initiate(payload, new HyperPaymentsCallbackAdapter() {
            @Override
            public void onEvent(JSONObject data, JuspayResponseHandler juspayResponseHandler) {
                try {
                    String event = data.getString("event");
                    switch (event) {
                        case "show_loader":
                            // Show loader
                            Log.d("EVENT", "show_loader");
                            break;
                        case "hide_loader":
                            // Hide loader
                            Log.d("EVENT", "hide_loader");
                            break;
                        case "initiate_result": {
                            Log.i("HyperSDKInitiate", "initiate_result" + data.optJSONObject("payload"));
                            updateStringInTextView("\n HyperSDKInitiate: initiate_result -->" + data);
                            break;
                        }
                        case "process_result": {
                            Log.i("HyperSDKInitiate", "process_result" + data.optJSONObject("payload"));
                            break;
                        }
                    }

                    if (TextUtils.isEmpty(data.optString("error"))) {
                        hyperSDKIsDeviceReady(false);
                        hyperSDKGetUPIApps();
                    }
                    // Merchant Code
                } catch (Exception e) {
                    // Merchant Code
                }
            }
        });
    }

    private void hyperSDKIsDeviceReady(final boolean isLast) {
        getHyperServicesInstance();
        JSONObject payload = new JSONObject();
        try {
            payload.put("requestId", isLast ? "jusPayIsDeviceReady1" : "jusPayIsDeviceReady2");
            payload.put("service", "in.juspay.ec");
            payload.put("betaAssets", false);
            JSONObject innerPayload = new JSONObject();
            innerPayload.put("action", "isDeviceReady");
            innerPayload.put("sdkPresent", isLast ? "ANDROID_PHONEPE" : "ANDROID_GOOGLEPAY");
            payload.put("payload", innerPayload);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        Log.i("HyperSDKIsDeviceReady", "Request: " + payload);
        updateStringInTextView("\n HyperSDKIsDeviceReady: Request -->\n" + payload);
        hyperServices.initiate(payload, new HyperPaymentsCallbackAdapter() {
            @Override
            public void onEvent(JSONObject data, JuspayResponseHandler juspayResponseHandler) {
                try {
                    String event = data.getString("event");
                    if (event.equals("show_loader")) {
                        // Show loader
                    } else if (event.equals("hide_loader")) {
                        // Hide loader
                    } else if (event.equals("initiate_result")) {
                        // Get the response
                        JSONObject response = data.optJSONObject("payload");
                        Log.i("HyperSDKIsDeviceReady", "initiate_result" + response);
                        updateStringInTextView("\n HyperSDKIsDeviceReady: initiate_result -->" + data);
                    } else if (event.equals("process_result")) {
                        // Get the response
                        JSONObject response = data.optJSONObject("payload");
                        Log.i("HyperSDKIsDeviceReady", "payload" + response);
                        updateStringInTextView("\n HyperSDKIsDeviceReady: process_result -->" + data);
                        if (!isLast)
                            hyperSDKIsDeviceReady(true);
                    }
                    // Merchant Code
                } catch (Exception e) {
                    // Merchant Code
                }
            }
        });
    }

    private void hyperSDKGetUPIApps() {
        getHyperServicesInstance();

        JSONObject payload = new JSONObject();
        try {
            payload.put("requestId", "jusPayGetUPIApps1");
            payload.put("service", "in.juspay.ec");
            payload.put("betaAssets", false);
            JSONObject innerPayload = new JSONObject();
            innerPayload.put("action", "upiTxn");
            innerPayload.put("orderId", "Default-1");
            innerPayload.put("getAvailableApps", true);
            innerPayload.put("showLoader", false);
            payload.put("payload", innerPayload);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        Log.i("HyperSDKGetUPIApps", "Request: " + payload);
        updateStringInTextView("\n HyperSDKGetUPIApps: Request -->\n" + payload);
        hyperServices.initiate(payload, new HyperPaymentsCallbackAdapter() {
            @Override
            public void onEvent(JSONObject data, JuspayResponseHandler juspayResponseHandler) {
                try {
                    String event = data.getString("event");
                    Log.i("HyperSDKGetUPIApps", "Data" + data);
                    if (event.equals("show_loader")) {
                        // Show loader
                    } else if (event.equals("hide_loader")) {
                        // Hide loader
                    } else if (event.equals("initiate_result")) {
                        // Get the response
                        JSONObject response = data.optJSONObject("payload");
                        Log.i("HyperSDKGetUPIApps", "initiate_result" + response);
                        updateStringInTextView("\n HyperSDKGetUPIApps: initiate_result -->" + data);
                    } else if (event.equals("process_result")) {
                        // Get the response
                        JSONObject response = data.optJSONObject("payload");
                        filterUPIApps(response);
                        Log.i("HyperSDKGetUPIApps", "payload" + response);
                        updateStringInTextView("\n HyperSDKGetUPIApps: process_result -->" + data);
                    }
                    // Merchant Code
                } catch (Exception e) {
                    // Merchant Code
                }
            }
        });
    }

    private void filterUPIApps(JSONObject response) {

    }

    private void updateStringInTextView(String str) {
//        textView.setText(String.format("%s%s", textView.getText(), str));
        textView.append(str + "\n");
    }

}