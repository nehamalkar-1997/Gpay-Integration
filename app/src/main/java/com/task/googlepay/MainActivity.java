package com.task.googlepay;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {

    EditText amountEt,messageEt,nameEt,upiIdEt;
    Button pay;

    final  int UPI_PAYMENT=0;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        initializeViews();
        pay.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //Getting the values from the edit texts.
                String amount =amountEt.getText().toString() ;
                String name = nameEt.getText().toString();
                String upiId = upiIdEt.getText().toString();
                String message = messageEt.getText().toString();
                payUsingUpi(amount,upiId,name,message);
            }
        });
    }
    void initializeViews(){
        pay = findViewById(R.id.btn_googlePay);
        amountEt= findViewById(R.id.amount_et);
        upiIdEt= findViewById(R.id.upi_id);
        nameEt= findViewById(R.id.name);
        messageEt= findViewById(R.id.message);
    }
    void payUsingUpi(String amount,String upiId,String name, String message){
        Uri uri = Uri.parse("upi://pay").buildUpon()
                .appendQueryParameter("pa",upiId)
                .appendQueryParameter("pn",name)
                .appendQueryParameter("tn",message)
                .appendQueryParameter("am",amount)
                .appendQueryParameter("cu","INR")
                .build();

        Intent upiPayIntent = new Intent(Intent.ACTION_VIEW);
        upiPayIntent.setData(uri);

        //always show the dialog to user to choose an app
        Intent chooser = Intent.createChooser(upiPayIntent,"Pay with");

        //check if intent resolve
        if (null!= chooser.resolveActivity(getPackageManager())){
            startActivityForResult(chooser,UPI_PAYMENT);
        }else{
            Toast.makeText(this, "No UPI app found,Please Install one to continue", Toast.LENGTH_SHORT).show();
        }
    }
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        switch (requestCode){
            case UPI_PAYMENT:
                if ((RESULT_OK == resultCode) || (resultCode == 11)){
                   if (data != null){
                       String text = data.getStringExtra("response");
                       Log.d("UPI","onActivityResult:"+ text);
                       ArrayList<String> dataList = new ArrayList<>();
                       dataList.add(text);
                       upiPaymentOperation(dataList);
                   }else{
                       Log.d("UPI","onActivityResult:"+"Return data is null");
                       ArrayList<String> dataList = new ArrayList<>();
                       dataList.add("nothing");
                       upiPaymentOperation(dataList);
                   }
            }else{
                    Log.d("UPI","onActivityResult:"+"Return data is null");//when user simply back without payment
                    ArrayList<String> dataList = new ArrayList<>();
                    dataList.add("nothing");
                    upiPaymentOperation(dataList);
                }
                break;
        }
    }
    private void upiPaymentOperation(ArrayList<String> data){
        if (isConnectionAvailable(MainActivity.this)){
            String str =data.get(0);
            Log.d("UPI PAY", "upiPaymentDataOperation:" +str);
            String paymentCancel ="";
            if (str == null) str= "discard";
            String status= "";
            String approvalRefNo = "";
            String response[] = str.split("&");
            for (int i = 0; i< response.length; i ++){
                String equalStr[] = response[i].split("=");
                if (equalStr.length >=2){
                    if (equalStr[0].toLowerCase().equals("Status".toLowerCase())){
                        status = equalStr[1].toLowerCase();
                    }
                    else if (equalStr[0].toLowerCase().equals("ApprovalRefNo".toLowerCase())|| equalStr[0].toLowerCase().equals("txnRef".toLowerCase())){
                        approvalRefNo = equalStr[1];
                    }
                }
                else {
                    paymentCancel = "Payment Cancel by User.";
                }
            }
            if (status.equals("success")){
                //code to handle successful transaction here.
                Toast.makeText(MainActivity.this, "Transaction Successful", Toast.LENGTH_SHORT).show();
                Log.d("UPI","responseStr:" +approvalRefNo);
            }
            else if ("Payment Cancel by User.".equals(paymentCancel)){
                Toast.makeText(MainActivity.this, "Payment Cancel by User", Toast.LENGTH_SHORT).show();
            }
            else {
                Toast.makeText(MainActivity.this, "Transaction failed. \n Please try again", Toast.LENGTH_SHORT).show();
            }
        }else{
            Toast.makeText(MainActivity.this, "Internet Connection is not available. \n Please check and try again", Toast.LENGTH_SHORT).show();
        }
    }
    public static boolean isConnectionAvailable(Context context){
        ConnectivityManager connectivityManager =(ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        if(connectivityManager != null){
            @SuppressLint("MissingPermission") NetworkInfo networkInfo = connectivityManager.getActiveNetworkInfo();
            if (networkInfo != null && networkInfo.isConnected()
                    && networkInfo.isConnectedOrConnecting()
                    && networkInfo.isAvailable()){
                return true;
            }
        }
        return false;
    }
}