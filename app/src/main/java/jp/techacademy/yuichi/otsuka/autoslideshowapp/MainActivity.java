package jp.techacademy.yuichi.otsuka.autoslideshowapp;

import android.Manifest;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.os.Build;
import android.content.ContentUris;
import android.content.ContentResolver;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;

import java.util.Timer;
import java.util.TimerTask;

import android.os.Handler;


public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    private static final int PERMISSIONS_REQUEST_CODE = 100;
    private static int NUMBEROFWHOLEIMAGECONTENTS;
    private static int NUMBEROFCURRENTIMAGECONTENTS = 0;
    Handler handler1 = new Handler();
    Timer timer1 = new Timer();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            // Android 6用
            if (checkSelfPermission(Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
                Button buttonSusumu = (Button) findViewById(R.id.bTsusumu);
                Button buttonStopRun = (Button) findViewById(R.id.bTstoprun);
                Button buttonModoru = (Button) findViewById(R.id.bTmodoru);
                buttonSusumu.setOnClickListener(this);
                buttonStopRun.setOnClickListener(this);
                buttonModoru.setOnClickListener(this);

                getContentsInfo();
            } else {
                requestPermissions(new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, PERMISSIONS_REQUEST_CODE);
            }
        } else {
            // Android 5用
            Button buttonSusumu = (Button) findViewById(R.id.bTsusumu);
            Button buttonStopRun = (Button) findViewById(R.id.bTstoprun);
            Button buttonModoru = (Button) findViewById(R.id.bTmodoru);
            buttonSusumu.setOnClickListener(this);
            buttonStopRun.setOnClickListener(this);
            buttonModoru.setOnClickListener(this);

            getContentsInfo();
        }

        timer1 = null;
    }

    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.bTsusumu) {
            goNext();
        }
        if (v.getId() == R.id.bTstoprun) {
            runSto();
        }
        if (v.getId() == R.id.bTmodoru) {
            goPrev();
        }
    }

    private void goNext() {
        NUMBEROFCURRENTIMAGECONTENTS++;
        if (NUMBEROFCURRENTIMAGECONTENTS >= NUMBEROFWHOLEIMAGECONTENTS) {
            NUMBEROFCURRENTIMAGECONTENTS = 0;
        }
        dispImage();
    }

    private void runSto() {
        if (this.timer1 == null) {
            handler1.post(new Runnable() {
                @Override
                public void run() {
                    Button buttonSusumu = (Button) findViewById(R.id.bTsusumu);
                    buttonSusumu.setVisibility(View.INVISIBLE);

                    Button buttonStopRun = (Button) findViewById(R.id.bTstoprun);
                    buttonStopRun.setText("停止");

                    Button buttonModoru = (Button) findViewById(R.id.bTmodoru);
                    buttonModoru.setVisibility(View.INVISIBLE);
                }
            });
            this.timer1 = new Timer();
            TimerTask timerTask1 = new TimerTask() {
                @Override
                public void run() {
                    Log.d("showAppp", "timer is running");
                    //goNext();
                    handler1.post(new Runnable() {
                        @Override
                        public void run() {
                            goNext();
                            Log.d("showAppp", "run msg for UIthre");
                        }
                    });
                }
            };
            this.timer1.schedule(timerTask1, 2000, 2000);
            return;


        } else {
            Log.d("showAppp", "timer stopped");
            handler1.post(new Runnable() {
                @Override
                public void run() {
                    Button buttonSusumu = (Button) findViewById(R.id.bTsusumu);
                    buttonSusumu.setVisibility(View.VISIBLE);

                    Button buttonStopRun = (Button) findViewById(R.id.bTstoprun);
                    buttonStopRun.setText("再生");

                    Button buttonModoru = (Button) findViewById(R.id.bTmodoru);
                    buttonModoru.setVisibility(View.VISIBLE);
                }
            });
            this.timer1.cancel();
            this.timer1 = null;
            return;
        }
    }

    private void goPrev() {
        NUMBEROFCURRENTIMAGECONTENTS--;
        if (NUMBEROFCURRENTIMAGECONTENTS < 0) {
            NUMBEROFCURRENTIMAGECONTENTS = NUMBEROFWHOLEIMAGECONTENTS - 1;
        }
        dispImage();
    }

    private void dispImage() {
        ContentResolver resolver = getContentResolver();
        Cursor cursor = resolver.query(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, null, null, null, null);
        Log.d("showAppp", "NUMBEROFCURRENTIMAGECONTENTS = " + String.valueOf(NUMBEROFCURRENTIMAGECONTENTS) + " / " + String.valueOf(NUMBEROFWHOLEIMAGECONTENTS));

        cursor.moveToPosition(NUMBEROFCURRENTIMAGECONTENTS);
        int fieldIndex = cursor.getColumnIndex(MediaStore.Images.Media._ID);
        Long id = cursor.getLong(fieldIndex);
        Uri imageUri = ContentUris.withAppendedId(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, id);
        ImageView imageVIew = (ImageView) findViewById(R.id.imageView);
        imageVIew.setImageURI(imageUri);
        cursor.close();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        switch (requestCode) {
            case PERMISSIONS_REQUEST_CODE:
                if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    Log.d("showAppp", "ファイルへのアクセスが許可された");
                    Button buttonSusumu = (Button) findViewById(R.id.bTsusumu);
                    Button buttonStopRun = (Button) findViewById(R.id.bTstoprun);
                    Button buttonModoru = (Button) findViewById(R.id.bTmodoru);

                    buttonSusumu.setOnClickListener(this);
                    buttonStopRun.setOnClickListener(this);
                    buttonModoru.setOnClickListener(this);

                    getContentsInfo();
                } else {
                    Log.d("showAppp", "ファイルへのアクセスが許可されない");
                }
                break;
            default:
                break;
        }
    }

    private void getContentsInfo() {
        NUMBEROFWHOLEIMAGECONTENTS = 0;
        ContentResolver resolver = getContentResolver();
        Cursor cursor = resolver.query(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, null, null, null, null);

        if (cursor.moveToFirst()) {
            int fieldIndex = cursor.getColumnIndex(MediaStore.Images.Media._ID);
            Log.d("showAppp", "fieldindex = " + String.valueOf(fieldIndex));
            Long id = cursor.getLong(fieldIndex);
            Uri imageUri = ContentUris.withAppendedId(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, id);
            ImageView imageVIew = (ImageView) findViewById(R.id.imageView);
            imageVIew.setImageURI(imageUri);

            do {
                NUMBEROFWHOLEIMAGECONTENTS++;
            } while (cursor.moveToNext());

        }
        cursor.close();
        Log.d("showAppp", "NUMBEROFWHOLEIMAGECONTENTS s = " + String.valueOf(NUMBEROFWHOLEIMAGECONTENTS));
    }
}