package com.mrz.keygen;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.DialogInterface;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Process;
import android.support.v7.app.AlertDialog;
import android.util.Base64;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.URL;
public class MainActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_main);
        Button mStart = findViewById(R.id.startbtn);
        mStart.setOnClickListener(v -> Update(this));
        Button mStop = findViewById(R.id.stopbtn);
        mStop.setOnClickListener(v -> Process.killProcess(Process.myPid()));
    }
    public static String encode(String paramString1, String paramString2) { return new String(Base64.encode(xorWithKey(paramString1.getBytes(), paramString2.getBytes()), 0)); }
    private static byte[] xorWithKey(byte[] paramArrayOfByte1, byte[] paramArrayOfByte2) {
        byte[] arrayOfByte = new byte[paramArrayOfByte1.length];
        for (int i = 0;; i++) {
            if (i >= paramArrayOfByte1.length)
                return arrayOfByte;
            arrayOfByte[i] = (byte)(paramArrayOfByte1[i] ^ paramArrayOfByte2[i % paramArrayOfByte2.length]);
        }
    }
    private static boolean isInternetAvailable(Context ctx) {
        ConnectivityManager connectivityManager = (ConnectivityManager) ctx.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        return activeNetworkInfo != null && activeNetworkInfo.isConnected();
    }
    @SuppressLint("StaticFieldLeak")
    public void Update(Context ctx) { (new AsyncTask<Void, Void, String>() {
        private ProgressDialog progDlg;
        String keypassword = "";
        String online = "";
        protected String doInBackground(Void... param1VarArgs) {
            try {
                if (!isInternetAvailable(ctx))
                    return "?";
                BufferedReader bufferedReader = new BufferedReader(new InputStreamReader((new URL("https://mreozzmod.github.io/KeyTest/Sever/status.html")).openConnection().getInputStream()));
                while (true) {
                    String str = bufferedReader.readLine();
                    if (str != null) {
                        StringBuilder stringBuilder = new StringBuilder();
                        (online) = stringBuilder.append(online).append(str).toString();
                        continue;
                    }
                    break;
                }
                bufferedReader.close();
                BufferedReader bufferedReader2 = new BufferedReader(new InputStreamReader((new URL("https://mreozzmod.github.io/KeyTest/Sever/keypass.html")).openConnection().getInputStream()));
                while (true) {
                    String str = bufferedReader2.readLine();
                    if (str != null) {
                        StringBuilder stringBuilder = new StringBuilder();
                        (keypassword) = stringBuilder.append(keypassword).append(str).toString();
                        continue;
                    }
                    break;
                }
                bufferedReader2.close();
                return "1";
            } catch (Exception param1VarArg) {
                return "?";
            }
        }

        @SuppressLint("NewApi")
        protected void onPostExecute(String param1String) {
            this.progDlg.dismiss();
            if (param1String.equals("?")) {
                AlertDialog.Builder builder1 = new AlertDialog.Builder(ctx);
                builder1.setTitle("Attention!").setMessage("You Are Not Connect With Internet!, Please connect to internet to continue.");
                builder1.setNeutralButton("Ok", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface param2DialogInterface, int param2Int) { ((Activity)ctx).finish(); }
                }).setCancelable(false);
                builder1.create();
                builder1.show();
                return;
            }
            KeyGenerators(ctx, keypassword);
        }
        protected void onPreExecute() {
            ProgressDialog progressDialog = new ProgressDialog(ctx);
            this.progDlg = progressDialog;
            progressDialog.setMessage("Info Updating...");
            this.progDlg.setCancelable(false);
            this.progDlg.show();
        }
    }).execute(new Void[0]); }

    private void KeyGenerators(Context ctx, String keypassword) {
        AlertDialog.Builder builder = new AlertDialog.Builder(ctx);
        builder.setTitle("MRZ | Keys");
        final EditText mail = new EditText(ctx);
        mail.setHint("Input id");
        builder.setView(mail);
        final StringBuilder ssb = new StringBuilder();
        builder.setNegativeButton("Generate", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                ssb.append(encode(keypassword, mail.getText().toString()));
                ClipboardManager clipboard = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
                ClipData clip = ClipData.newPlainText("text",  ssb);
                clipboard.setPrimaryClip(clip);
                Toast.makeText(getApplicationContext(), "Copied Key "+ssb,Toast.LENGTH_LONG).show();
            }
        }).setCancelable(false);
        builder.create();
        builder.show();
    }
}
