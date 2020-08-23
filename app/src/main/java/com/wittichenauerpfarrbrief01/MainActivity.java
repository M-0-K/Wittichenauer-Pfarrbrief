package com.wittichenauerpfarrbrief01;
import android.app.DownloadManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.ColorMatrix;
import android.graphics.ColorMatrixColorFilter;
import android.graphics.Paint;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.Switch;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.github.barteksc.pdfviewer.PDFView;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;
import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;




public class MainActivity extends AppCompatActivity {

    private long downloadID;
    public String filename;
    public String downloadlink = new String("");
    public SharedPreferences pref;
    public SharedPreferences.Editor editor;
    public Button btInfo;



    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        pref = getSharedPreferences("Wittichenauer Pfarrbrief", 0);
        editor = pref.edit();
        if(pref.getBoolean("Darkmode", false)){
            setTheme(R.style.DarkTheme);
        }else{
            setTheme(R.style.LightTheme);
        }
        setContentView(R.layout.activity_main);
        btInfo = (Button) findViewById(R.id.btInfo);
        btInfo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent explicitIntent = new Intent(MainActivity.this, Info.class);
                startActivity(explicitIntent);
            }
        });

        registerReceiver(onDownloadComplete,new IntentFilter(DownloadManager.ACTION_DOWNLOAD_COMPLETE));

        File file = new File(getExternalFilesDir(null),pref.getString("filename", ""));
        int alt = 8;

        if(pref.getString("filename", "").equals("") == false){
            alt = alter(pref.getString("filename", "20.07.2001"));
        }

        if (alt < 7){

            showpdf(file);
        }else if(Connection() == false) {
            Toast.makeText(MainActivity.this, "Verbindend Sie ihr Smartphone mit dem Internet, um den aktuellen Pfarrbrief zu downloaden!", Toast.LENGTH_LONG).show();
        }else {
            Toast.makeText(MainActivity.this, "Der Aktuelle Pfarrbrief wird heruntergeladen!", Toast.LENGTH_LONG).show();
        }
        if(Connection() == true) {
            link();
        }

    }

    private BroadcastReceiver onDownloadComplete = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            //Fetching the download id received with the broadcast
            long id = intent.getLongExtra(DownloadManager.EXTRA_DOWNLOAD_ID, -1);
            //Checking if the received broadcast is for our enqueued download by matching download id

            if (downloadID == id) {
                Date datum = new Date();
                SimpleDateFormat datfor = new SimpleDateFormat("dd.MM.yyyy HH:mm");
                editor.putString("ddatum", datfor.format(datum));
                editor.commit();
                Toast.makeText(MainActivity.this, "Download ist erfolgt!", Toast.LENGTH_SHORT).show();
                File file = new File(getExternalFilesDir(null),filename);
                showpdf(file);
            }
        }
    };

    public void link() {
        Runnable r = new Runnable() {
            @Override
            public void run() {
                try {

                    Document doc = Jsoup.connect("https://st-mariae-himmelfahrt-wittichenau.de").get();
                    Elements links = doc.select("a#link_pfarrbrief");
                    downloadlink = links.attr("abs:href") + "";


                } catch (IOException e) {
                    e.printStackTrace();
                } finally {
                    filename = name(downloadlink);
                    int i = alter(filename);
                    File file = new File(getExternalFilesDir(null),filename);
                    if (pref.getString("filename", "").equals(filename) == false){
                        editor.putString("filename", filename);
                        editor.commit();
                        beginDownload(file);
                    }
                }
            }
        };
        Thread t = new Thread(r);
        t.start();
    }

    public void beginDownload(File file){

        //Toast.makeText(MainActivity.this, "Der Aktuelle Pfarrbrief wird heruntergeladen!", Toast.LENGTH_LONG).show();
        DownloadManager.Request request=new DownloadManager.Request(Uri.parse(downloadlink))
                .setTitle("Pfarrbrief")// Title of the Download Notification
                .setDescription("Downloading")// Description of the Download Notification
                .setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE)// Visibility of the download Notification
                .setDestinationUri(Uri.fromFile(file))// Uri of the destination file
                .setRequiresCharging(false)// Set if charging is required to begin the download
                .setAllowedOverMetered(true)// Set if download is allowed on Mobile network
                .setAllowedOverRoaming(true);// Set if download is allowed on roaming network
        DownloadManager downloadManager= (DownloadManager) getSystemService(DOWNLOAD_SERVICE);
        downloadID = downloadManager.enqueue(request);// enqueue puts the download request in the queue.
    }
    @Override
    public void onDestroy() {
        super.onDestroy();
        unregisterReceiver(onDownloadComplete);

    }

    public void showpdf(File file){

        PDFView pdfView = findViewById(R.id.pdfView);

        if (file.exists()) {

            pdfView.fromUri(Uri.fromFile(file))
                    .enableSwipe(true)
                    .swipeHorizontal(false)
                    .enableDoubletap(true)
                    .defaultPage(0)
                    .enableAnnotationRendering(true)
                    .password(null)
                    .scrollHandle(null)
                    .nightMode(pref.getBoolean("Darkmode", false))
                    .load();

        }else if(Connection()){
            link();
        }else {
            Toast.makeText(MainActivity.this, "Verbindend Sie ihr Smartphone mit dem Internet, um den aktuellen Pfarrbrief zu downloaden!", Toast.LENGTH_LONG).show();
        }
    }

    public String name(String dl){
        String name1 = dl;
        name1 = name1.substring(72 ,name1.length()-4);
        return name1;
        // " 20.07.2001 Pfarrbrief "
    }

    private boolean Connection() {
        boolean Wifi = false;
        boolean Mobile = false;

        ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo[] netInfo = cm.getAllNetworkInfo();
        for (NetworkInfo NI : netInfo) {
            if (NI.getTypeName().equalsIgnoreCase("WIFI")) {
                if (NI.isConnected()) {
                    Wifi = true;
                }
            }
            if (NI.getTypeName().equalsIgnoreCase("MOBILE"))
                if (NI.isConnected()) {
                    Mobile = true;
                }
        }
        return Wifi || Mobile;
    }

    public int alter(String name) {
        Date downDate = new Date();
        Date today = new Date();
        name = leerzeichen(name);
        for (int i = 0; i < name.length(); i++) {
            if (name.charAt(i) == '2' || name.charAt(i) == '3' || name.charAt(i) == '1' ||name.charAt(i) == '0') {
                name = name.substring(i,i+10);
                break;
            } // end of if
        }
        editor.putString("pfdatum", name);
        editor.commit();
        name = name.replace(".","/");
        try {
            downDate = new SimpleDateFormat("dd/MM/yyyy").parse(name);
        } catch(Exception e) {
            return 9;
        }
        long diff = today.getTime() - downDate.getTime();
        return (int)diff / (1000 * 60 * 60 * 24);
        //return 8;
    }

    public String leerzeichen(String str) {
        String sp = null;
        for (int i = 0; i < str.length(); i++) {
            if (str.charAt(i) != ' ') {
                sp = sp + str.charAt(i);
            }
        }
        return sp;
    }
    private Bitmap toNightMode(Bitmap bmpOriginal, boolean bestQuality)
    {
        int width, height;
        height = bmpOriginal.getHeight();
        width = bmpOriginal.getWidth();

        Bitmap nightModeBitmap = Bitmap.createBitmap(width, height, bestQuality ? Bitmap.Config.ARGB_8888 : Bitmap.Config.RGB_565);
        Canvas c = new Canvas(nightModeBitmap);
        Paint paint = new Paint();

        ColorMatrix grayScaleMatrix = new ColorMatrix();
        grayScaleMatrix.setSaturation(0);
        ColorMatrix invertMatrix =
                new ColorMatrix(new float[] {
                        -1,  0,  0,  0, 255,
                        0, -1,  0,  0, 255,
                        0,  0, -1,  0, 255,
                        0,  0,  0,  1,   0});

        ColorMatrix nightModeMatrix = new ColorMatrix();
        nightModeMatrix.postConcat(grayScaleMatrix);
        nightModeMatrix.postConcat(invertMatrix);

        paint.setColorFilter(new ColorMatrixColorFilter(nightModeMatrix));
        c.drawBitmap(bmpOriginal, 0, 0, paint);

        return nightModeBitmap;
    }
   
}