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

        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M &&
                checkSelfPermission(Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(new String[]{Manifest.permission.RECORD_AUDIO}, 202);
        } else {
            visualiserView = new VisualiserView(this);
            root.addView(visualiserView);
        }
        if (visualiserView != null)
            //test link
            visualiserView.setAudioUrl("https://online.freemusicdownloads.world/get-file?vid=W3q8Od5qJio&fn=Rammstein+-+Du+Hast+%28Official+Video%29&pl=False&dt=MP3");
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == 202) {
            visualiserView = new VisualiserView(this);
            visualiserView.setAudioUrl("https://online.freemusicdownloads.world/get-file?vid=W3q8Od5qJio&fn=Rammstein+-+Du+Hast+%28Official+Video%29&pl=False&dt=MP3");
        }
    }
}
