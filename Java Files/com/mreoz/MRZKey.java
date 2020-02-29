package com.mreoz;


import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.ActivityManager;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Process;
import android.preference.PreferenceManager;
import android.provider.Settings;
import android.util.Base64;
import android.widget.EditText;
import android.widget.Toast;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.UUID;

public class MRZKey {

    public static void LoadMe(final Context ctx) {
        MRZKey.LoadUpdate(ctx);
    }

    private static void LoadUpdate(final Context ctx) {
        if (isPermissionGranted(ctx)) {
            Update(ctx);
        }
    }
    private static String getUniqueId(Context ctx) {
        String key = (getDeviceName() + Settings.Secure.getString(ctx.getContentResolver(), Settings.Secure.ANDROID_ID) + Build.HARDWARE).replace(" ", "");
        UUID uniqueKey = UUID.nameUUIDFromBytes(key.getBytes());
        return uniqueKey.toString().replace("-", "");
    }

    private static String getDeviceName() {
        String manufacturer = Build.MANUFACTURER;
        String model = Build.MODEL;
        if (model.startsWith(manufacturer)) {
            return model;
        } else {
            return manufacturer + " " + model;
        }
    }
    public static void Putme(final Context ctx, String mrz) {
        if (isPermissionGranted(ctx)) {
            final Toast tos = Toast.makeText(ctx, "Success :)", Toast.LENGTH_LONG);
            final Toast toa = Toast.makeText(ctx, "Your license is incorrect :(", Toast.LENGTH_LONG);
            AlertDialog.Builder builder = new AlertDialog.Builder(ctx);
            builder.setTitle("Key System source by MRZ");
            final EditText et = new EditText(ctx);
            if (!getKey(ctx).isEmpty())
                et.setText(getKey(ctx));
            builder.setCancelable(false);
            builder.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface param1DialogInterface, int param1Int) {
                    if (mrz.toString().trim().equals(et.getText().toString().trim())) {
                        if (!getKey(ctx).equals(mrz))
                            PreferenceManager.getDefaultSharedPreferences(ctx).edit().putString("key", mrz).apply();
                        param1DialogInterface.cancel();
                        tos.show();
                        startFloater(ctx);
                        return;
                    }
                    else{
                        toa.show();
                        Putme(ctx, mrz);
                    }

                }
            });
            builder.setNegativeButton("Copy ID", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    ClipboardManager clipboard = (ClipboardManager) ctx.getSystemService(Context.CLIPBOARD_SERVICE);
                    ClipData clip = ClipData.newPlainText("text",  getUniqueId(ctx));
                    clipboard.setPrimaryClip(clip);
                    Putme(ctx, mrz);
                }
            });
            builder.setView(et);
            builder.create();
            builder.show();
        }
    }

    static void startPatcher(Context ctx) {
        if (Build.VERSION.SDK_INT >= 23 && !Settings.canDrawOverlays(ctx)) {
            Intent intent = new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION, Uri.parse("package:" + ((Activity)ctx).getPackageName()));
            ((Activity)ctx).startActivityForResult(intent, 123);
        } else {
            startFloater(ctx);
        }
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
    private static void startFloater(Context ctx) {
        if (!isServiceRunning(ctx)) {
            ((Activity)ctx).startService(new Intent(((Activity)ctx), FloatingModMenuService.class));
        } else {
            Toast.makeText(((Activity)ctx), "Service Already Running!", Toast.LENGTH_SHORT).show();
        }
    }
    private static boolean isServiceRunning(Context ctx) {
        ActivityManager manager = (ActivityManager) ((Activity)ctx).getSystemService(Context.ACTIVITY_SERVICE);
        if (manager != null) {
            for (ActivityManager.RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) {
                if (FloatingModMenuService.class.getName().equals(service.service.getClassName())) {
                    return true;
                }
            }
        }
        return false;
    }

    @SuppressLint("StaticFieldLeak")
    public static void Update(Context ctx) { (new AsyncTask<Void, Void, String>() {
        private ProgressDialog progDlg;
        String keypassword = "";
        String online = "";
        protected String doInBackground(Void... param1VarArgs) {
            String sb = "";
            try {
                if (!MRZKey.isInternetAvailable(ctx))
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
            final StringBuilder sb = new StringBuilder();
            sb.append(encode(keypassword, getUniqueId(ctx)));
            if (online.contains("online=true")) {
                Putme(ctx, String.valueOf(sb));
                return;
            }
            AlertDialog.Builder builder = new AlertDialog.Builder(ctx);
            builder.setTitle("Error!").setMessage("This Mod Disable right now. Try again later");
            builder.setNeutralButton("Ok", new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface param2DialogInterface, int param2Int) { Process.killProcess(Process.myPid()); }
            }).setCancelable(false);
            builder.create();
            builder.show();
        }

        protected void onPreExecute() {
            ProgressDialog progressDialog = new ProgressDialog(ctx);
            this.progDlg = progressDialog;
            progressDialog.setMessage("Info Updating...");
            this.progDlg.setCancelable(false);
            this.progDlg.show();
        }
    }).execute(new Void[0]); }

    private static boolean isInternetAvailable(Context ctx) {
        ConnectivityManager connectivityManager = (ConnectivityManager) ctx.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        return activeNetworkInfo != null && activeNetworkInfo.isConnected();
    }

    public static String getKey(Context paramContext) { return PreferenceManager.getDefaultSharedPreferences(paramContext).getString("key", ""); }

    public static boolean isPermissionGranted(final Context ctx) {
        if (Build.VERSION.SDK_INT < 23 || Settings.canDrawOverlays(ctx))
            return true;
        AlertDialog.Builder builder = new AlertDialog.Builder(ctx);
        builder.setMessage("This app need Syatem_overlay permission to draw the mod menu!");
        builder.setCancelable(false);
        builder.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface param1DialogInterface, int param1Int) {
                Intent intent = new Intent("android.settings.action.MANAGE_OVERLAY_PERMISSION", Uri.parse("package:" + ctx.getPackageName()));
                ((Activity)ctx).startActivityForResult(intent, 3);
                ((Activity)ctx).finish();
            }
        });
        builder.setNegativeButton("Exit", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface param1DialogInterface, int param1Int) { ((Activity)ctx).finish(); }
        });
        builder.create().show();
        return false;
    }
}
