package com.example.jay_pc.dcrypt;

/**
 * Created by Jay-pc on 3/10/2017.
 */

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.CursorLoader;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.lang.Thread.UncaughtExceptionHandler;
import java.util.ArrayList;

//import android.support.v4.content.CursorLoader;


public class DCryptActivity extends Activity {

    String dcryptPref = "com.example.jay_pc.dcrypt";
    private String fileShreddingPref = "SHREDDING";
    private Button startButton = null;
    Button stopButton = null;
    private Dialog helpDialog = null;
    private Dialog logDialog = null;
    //private GridView dbGridView = null;
    private TextView dbLogTextView = null;
    private String prevPicId, nextPicId;
    private String masterPassword = null;
    private AlertDialog.Builder passwordDialog = null;
    SharedPreferences dPref = null;
    private static boolean doShredding = false;
    private static com.example.jay_pc.dcrypt.DBLogger Log = null;
    //private Menu optionsMenu = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Log = new DBLogger(this);

        startButton = (Button) findViewById(R.id.startButton);
        stopButton = (Button) findViewById(R.id.stopButton);
        //dbGridView = (GridView) findViewById(R.id.dCryptGridView);
        dbLogTextView = (TextView) findViewById(R.id.dbMainTextView);

        startButton.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View view) {

                startService(new Intent(DCryptActivity.this, DCryptService.class));
            }
        });

        stopButton.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {

                stopService(new Intent(DCryptActivity.this, DCryptService.class));
                finish();
            }
        });

        dPref = getSharedPreferences(dcryptPref, MODE_PRIVATE);
        doShredding = dPref.getBoolean(fileShreddingPref, false);

        if (Intent.ACTION_MAIN.equals(getIntent().getAction())) {
            handleIntents();
        } else {
            acceptPasswordFromUser();
        }

        //setUpUncaughtHandler();
    }



    private void handleIntents() {

        Intent dbIntent = getIntent();
        String dbIntentAction = dbIntent.getAction();

        if (Intent.ACTION_SEND.equals(dbIntentAction)) {
            handleSendFile(dbIntent);
        } else if (Intent.ACTION_SEND_MULTIPLE.equals(dbIntentAction)) {
            handleSendMultipleFiles(dbIntent);
        } else if (Intent.ACTION_MAIN.equals(dbIntentAction)){
            initComponents();
            //toastAlert("main");
        }
    }

    private void handleSendMultipleFiles(Intent intent) {

        ArrayList<Uri> fileUris = intent.getParcelableArrayListExtra(Intent.EXTRA_STREAM);
        if (fileUris != null) {
            //toastAlert("Received " + fileUris.size() + " files!");
            for (Uri uri : fileUris) {
                File file = new File(getPath(uri));
                workOnOneFile(file);
            }
        }
    }


    private void handleSendFile(Intent intent) {

        //String fileType = intent.getType();
        Uri fileUri = intent.getParcelableExtra(Intent.EXTRA_STREAM);
        //toastAlert(fileUri + " received!");
        File file = null;
        file = new File(getPath(fileUri));
        workOnOneFile(file);
    }


    private void workOnOneFile(File file) {

        if (file.getName().endsWith(".dbx")) {
            if (decryptFile(file)) {
                toastAlert("Decrypted " + file.getAbsolutePath());
            } else {
                toastAlert("Decryption failed for " + file.getAbsolutePath());
            }
        } else {
            if (encryptFile(file)) {
                toastAlert("Encrypted " + file.getAbsolutePath());
            } else {
                toastAlert("Encryption failed for " + file.getAbsolutePath());
            }
        }
    }


    private boolean decryptFile(File file) {

        String prefix = "";
        DBEncryptFile dbEncryptFile = new DBEncryptFile(masterPassword);
        String srcFileName = file.getAbsolutePath();
        String dir = file.getParent();
        String destFileName = prefix + file.getName().replaceAll(".dbx", "");
        destFileName = dir + "/" + destFileName;
        if (new File(destFileName).exists()) {
            toastAlert("Decrypted file already FOUND!");
            Log.e("DCrypt", "Decrypted file " + destFileName + " already FOUND!");
            return false;
        }
        try {
            dbEncryptFile.decrypt(srcFileName, destFileName);
            if (doShredding) {
                if(dbEncryptFile.shredFile(srcFileName)) {
                    Log.e("DCrypt", "Shredded file " + srcFileName);
                } else {
                    if (new File(srcFileName).delete()) {
                        Log.e("DCrypt", "Shredded file " + srcFileName);
                    } else {
                        Log.e("DCrypt", "Shredding of file " + srcFileName + " failed!");
                    }
                }
            }
            return true;
        } catch(Exception ex) {
            new File(destFileName).delete();
            return false;
        }
    }


    private boolean encryptFile(File file) {

        DBEncryptFile dbEncryptFile = new DBEncryptFile(masterPassword);
        String srcFileName = file.getAbsolutePath();
        String destFileName = srcFileName + ".dbx";
        if (new File(destFileName).exists()) {
            toastAlert("Encrypted file already FOUND!");
            Log.e("DCrypt", "Encrypted file " + destFileName + " already FOUND!");
            return false;
        }
        try {
            dbEncryptFile.encrypt(srcFileName, destFileName);
            if (doShredding) {
                if(dbEncryptFile.shredFile(srcFileName)) {
                    Log.e("DCrypt", "Shredded file " + srcFileName);
                } else {
                    if (new File(srcFileName).delete()) {
                        Log.e("DCrypt", "Shredded file " + srcFileName);
                    } else {
                        Log.e("DCrypt", "Shredding of file " + srcFileName + " failed!");
                    }
                }
            }
            return true;
        } catch(Exception ex) {
            new File(destFileName).delete();
            return false;
        }

    }


    private void acceptPasswordFromUser() {

        passwordDialog = new AlertDialog.Builder(this);
        masterPassword = null;
        passwordDialog.setTitle("DCrypt");
        LayoutInflater layoutInflater = LayoutInflater.from(this);
        View view = layoutInflater.inflate(R.layout.password_dialog, null);
        passwordDialog.setView(view);
        final EditText input1 = (EditText) view.findViewById(R.id.passwordEditText1);
        final EditText input2 = (EditText) view.findViewById(R.id.passwordEditText2);

        passwordDialog.setCancelable(false);

        passwordDialog.setPositiveButton("OK", new DialogInterface.OnClickListener() {

            public void onClick(DialogInterface dialog, int whichButton) {

                masterPassword = input1.getText().toString();

                if (masterPassword == null || masterPassword.trim().equals("")) {
                    toastAlert("The password cannot be empty!");
                    acceptPasswordFromUser();
                    return;
                } else {
                    //toastAlert(masterPassword);
                    //System.out.println(masterPassword);
                    if (masterPassword.equals(input2.getText().toString())) {
                        handleIntents();
                    } else {
                        toastAlert("Entered passwords do not match!");
                        acceptPasswordFromUser();
                        return;
                    }

                }
            }
        });

        passwordDialog.setNegativeButton("CANCEL", new DialogInterface.OnClickListener() {

            public void onClick(DialogInterface dialog, int whichButton) {

                masterPassword = null;
            }
        });

        passwordDialog.show();
    }


    public String getPath(Uri uri) {

        Log.i("DCrypt", uri.toString());
        String fileName="unknown";//default fileName
        Uri filePathUri = uri;
        if (uri.getScheme().toString().compareTo("content")==0) {
            String[] projection = { MediaStore.Images.Media.DATA };
            CursorLoader loader = new CursorLoader(getBaseContext(), uri, projection, null, null, null);
            Cursor cursor = loader.loadInBackground();
            int column_index = cursor.getColumnIndexOrThrow(projection[0]);
            cursor.moveToFirst();
            fileName =  cursor.getString(column_index);
        } else if (uri.getScheme().compareTo("file")==0) {
            fileName = filePathUri.toString().replace("file://", "");
        } else {
            fileName = fileName + "_" + filePathUri.getLastPathSegment();
        }
        Log.i("DCrypt", fileName);
        return fileName;
    }

    protected void initComponents() {

		/*
		dbGridView.setAdapter(new DCryptImageAdapter(this));

		dbGridView.setOnItemClickListener(new OnItemClickListener() {

			public void onItemClick(AdapterView<?> parent, View v, int position, long id) {

	            prevPicId = DCryptImageAdapter.getPrevItemId(position).getAbsolutePath();
	            nextPicId = DCryptImageAdapter.getNextItemId(position).getAbsolutePath();
	            showImage(dbGridView.getAdapter().getItemId(position), position);
		    }
		});
		*/
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("DCrypt running..");
        stringBuilder.append("\n");
        if (doShredding) {
            stringBuilder.append("File Shredding is ENABLED");
        } else {
            stringBuilder.append("File Shredding is DISABLED");
        }
        stringBuilder.append("\n");
        dbLogTextView.setText(stringBuilder.toString());
    }


    protected void showImage(long itemId, int position) {

        Intent pictureViewer = new Intent(this, DCryptPictureViewer.class);
        pictureViewer.putExtra("currentPicId", DCryptImageAdapter.getCurrItemId(position));
        pictureViewer.putExtra("picPosition", position);
        pictureViewer.putExtra("prevPicId", prevPicId);
        pictureViewer.putExtra("nextPicId", nextPicId);

        startActivityForResult(pictureViewer, 0);
    }

    @Override
    protected void onDestroy() {

        super.onDestroy();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        //this.optionsMenu = menu;
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        Editor editor;
        switch (item.getItemId()) {
            case R.id.action_settings:
                Menu menu;
                if (item.hasSubMenu()) {
                    menu = item.getSubMenu();
                    if (doShredding) {
                        ((MenuItem) menu.getItem(0)).setChecked(true);
                    } else {
                        ((MenuItem) menu.getItem(1)).setChecked(true);
                    }
                }
                return true;
            case R.id.action_help:
                menuHelp();
                return true;
            case R.id.action_logs:
                dumpLogs();
                return true;
            case R.id.enableShredding:
                if (item.isChecked()) {
                    item.setChecked(false);
                } else {
                    item.setChecked(true);
                }
                doShredding = true;
                editor = dPref.edit();
                editor.putBoolean(fileShreddingPref, true);
                editor.commit();
                if (doShredding) {
                    toastAlert("Enabled Shredding");
                }
                return true;
            case R.id.disableShredding:
                if (item.isChecked()) {
                    item.setChecked(false);
                } else {
                    item.setChecked(true);
                }
                doShredding = false;
                editor = dPref.edit();
                editor.putBoolean(fileShreddingPref, false);
                editor.commit();
                if (!doShredding) {
                    toastAlert("Disabled Shredding");
                }
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }


    /**
     *
     */
    private void menuHelp() {

        helpDialog = new Dialog(this);
        helpDialog.setTitle(R.string.help_title);
        helpDialog.setOwnerActivity(DCryptActivity.this);
        helpDialog.setContentView(R.layout.help_dialog);
        helpDialog.show();
        //toastAlert("Help");
    }


    @Override
    public void onBackPressed() {
        if (helpDialog != null && helpDialog.isShowing()) {
            helpDialog.dismiss();
            toastAlert("dismissed!");
        }
        if (logDialog != null && logDialog.isShowing()) {
            logDialog.dismiss();
            toastAlert("dismissed!");
        }
        moveTaskToBack(true);
    }


    void toastAlert(String message) {
        Toast.makeText(getApplicationContext(), message, Toast.LENGTH_LONG).show();
        dbLogTextView.append(" *" + message + "\n");
    }



    public void copy(String srcPath, String destPath) throws IOException {
        File src = new File(srcPath);
        File dst = new File(destPath);
        InputStream in = new FileInputStream(src);
        OutputStream out = new FileOutputStream(dst);

        // Transfer bytes from in to out
        byte[] buf = new byte[1024];
        int len;
        while ((len = in.read(buf)) > 0) {
            out.write(buf, 0, len);
            out.flush();
        }
        in.close();
        out.close();
    }

    protected void dumpLogs() {
        logDialog = new Dialog(this);
        logDialog.setTitle(R.string.log_title);
        logDialog.setOwnerActivity(DCryptActivity.this);
        logDialog.setContentView(R.layout.log_dialog);
        try {
            Process process = Runtime.getRuntime().exec("logcat -d");
            BufferedReader bufferedReader = new BufferedReader(
                    new InputStreamReader(process.getInputStream()));

            StringBuilder log = new StringBuilder();
            String line;
            while ((line = bufferedReader.readLine()) != null) {
                if (line.contains("DCrypt") || line.contains("dcrypt")) {
                    log.append(line);
                    log.append("\n");
                }
            }
            TextView tv = (TextView) logDialog.findViewById(R.id.logTextView);
            tv.setText(log.toString());
        } catch (IOException exp) {
            Log.e("DCrypt", exp.getMessage());
        }
        logDialog.show();
    }


    public static void setUpUncaughtHandler() {

        Thread.setDefaultUncaughtExceptionHandler(new UncaughtExceptionHandler() {
            UncaughtExceptionHandler defaultHandler;
            {
                defaultHandler = Thread.getDefaultUncaughtExceptionHandler();
            }

            @Override
            public void uncaughtException(Thread thread, Throwable ex) {

                Log.e("DCrypt", ex.getMessage());
                defaultHandler.uncaughtException(thread, ex);
            }
        });
    }

    protected void appendToMainTextView(String message) {

        dbLogTextView.append(" *" + message + "\n");
    }
}
