package baqari.dev.com.test;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.media.MediaPlayer;
import android.media.audiofx.Visualizer;
import android.net.Uri;
import android.util.TypedValue;
import android.view.MotionEvent;
import android.view.View;

import java.util.ArrayList;
import java.util.List;


public class VisualiserView extends View {

    private Paint wavePaint;
    private MediaPlayer mediaPlayer;
    private Visualizer visualizer;
    private List<Double> amplitudes;

    private float initialWaveHeight = 1500f;
    private float initialRawX = 350;

    public VisualiserView(Context context) {
        super(context);
        init();
    }

    public void init() {
        wavePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        wavePaint.setColor(Color.RED);
        wavePaint.setTextSize(TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP, 20, getResources().getDisplayMetrics()));

        mediaPlayer = MediaPlayer.create(getContext(), Uri.parse("http://www.alazani.ge/base/AnchiskhatiP/Anchiskhati_-_Vasha_Kampania.mp3"));
        visualizer = new Visualizer(mediaPlayer.getAudioSessionId());
        visualizer.setCaptureSize(Visualizer.getCaptureSizeRange()[1]);

        final Visualizer.OnDataCaptureListener listener = new Visualizer.OnDataCaptureListener() {
            @Override
            public void onWaveFormDataCapture(Visualizer visualizer, byte[] waveform, int j) {
                double amplitude = 0;
                for (int i = 0; i < waveform.length / 2; i++) {
                    double y = (waveform[i * 2] | waveform[i * 2 + 1] << 8) / 32768.0;
                    amplitude += Math.abs(y);
                }
                amplitude = amplitude / waveform.length / 2;
                amplitudes.add(amplitude * 60);
                invalidate();
            }

            @Override
            public void onFftDataCapture(Visualizer visualizer, byte[] waveform, int i) {

            }
        };

        mediaPlayer.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
            @Override
            public void onPrepared(MediaPlayer mediaPlayer) {
                amplitudes = new ArrayList();
                visualizer.setDataCaptureListener(listener, Visualizer.getMaxCaptureRate(), true, true);
                visualizer.setEnabled(true);
                mediaPlayer.start();
            }
        });

        mediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
            @Override
            public void onCompletion(MediaPlayer mediaPlayer) {
                visualizer.setEnabled(false);
            }
        });
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        initialWaveHeight = MeasureSpec.getSize(heightMeasureSpec);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        float middle = initialRawX;
        for (int i = 0; i < amplitudes.size(); i += 3) {
            double wave = amplitudes.get(i) * 60;

            float startX = middle;
            float stopX = startX + 7;
            float tBottomY = initialWaveHeight / 2;
            float tTopY = (float) (tBottomY - (wave / 2));
            float bBottomY = (float) (tBottomY + (wave / 2));

            canvas.drawRect(startX + 10, tTopY, stopX, bBottomY, wavePaint);
            middle = stopX;
        }
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        float rawX = event.getX();
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                initialRawX = rawX - initialRawX;
                return true;
            case MotionEvent.ACTION_MOVE:
                rawX = event.getRawX();
                initialRawX = rawX - initialRawX;
                invalidate();
                return true;
        }
        return false;
    }
}
