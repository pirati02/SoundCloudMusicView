package baqari.dev.com.test;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.widget.RelativeLayout;

public class MainActivity extends AppCompatActivity {

    VisualiserView visualiserView;
    RelativeLayout root;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        root = (RelativeLayout) findViewById(R.id.root);

        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) {
            int hasAudioPermission = checkSelfPermission(Manifest.permission.RECORD_AUDIO);

            if (hasAudioPermission != PackageManager.PERMISSION_GRANTED) {
                requestPermissions(new String[]{Manifest.permission.RECORD_AUDIO}, 202);
            } else {
                visualiserView = new VisualiserView(this);
                root.addView(visualiserView);
            }
        } else {
            visualiserView = new VisualiserView(this);
            root.addView(visualiserView);
        }
        if (visualiserView != null)
            visualiserView.setAudioUrl("http://www.alazani.ge/base/AnchiskhatiK/Anchiskhati_-_Dideba_Magaliani.mp3");
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == 202) {
            visualiserView = new VisualiserView(this);
            root.addView(visualiserView);
        }
    }
}
