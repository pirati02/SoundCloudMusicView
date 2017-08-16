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

import io.reactivex.Observable;
import io.reactivex.annotations.NonNull;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Consumer;
import io.reactivex.schedulers.Schedulers;


public class VisualiserView extends View {

    private Paint wavePaint;
    private MediaPlayer mediaPlayer;
    private Visualizer visualizer;
    private List<Double> amplitudes = new ArrayList();
    private List<WaveItem> waves = new ArrayList<>();

    private float initialWaveHeight = 1500f;
    private float initialRawX = 350;
    private float rightScroll = 0f;
    private float wavesWidthSum = 0f;

    private CompositeDisposable disposables = new CompositeDisposable();

    public VisualiserView(Context context) {
        super(context);
        init();
    }

    public void init() {
        wavePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        wavePaint.setColor(Color.RED);
        wavePaint.setTextSize(TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP, 20, getResources().getDisplayMetrics()));

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

        Disposable mediaDisposable = Observable.just("object")
                .observeOn(Schedulers.io())
                .subscribeOn(Schedulers.io())
                .subscribe(new Consumer<Object>() {
                    @Override
                    public void accept(@NonNull Object o) throws Exception {
                        mediaPlayer = MediaPlayer.create(getContext(), Uri.parse("http://www.alazani.ge/base/AnchiskhatiP/Anchiskhati_-_Vasha_Kampania.mp3"));
                        //mediaPlayer.setDataSource("http://www.alazani.ge/base/AnchiskhatiP/Anchiskhati_-_Vasha_Kampania.mp3");
                        VisualiserView.this.mediaPlayer = mediaPlayer;

                        visualizer = new Visualizer(mediaPlayer.getAudioSessionId());
                        visualizer.setCaptureSize(Visualizer.getCaptureSizeRange()[1]);
                        //mediaPlayer.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
                        //  @Override
                        //public void onPrepared(MediaPlayer mediaPlayer) {
                        amplitudes = new ArrayList();
                        visualizer.setDataCaptureListener(listener, Visualizer.getMaxCaptureRate(), true, true);
                        visualizer.setEnabled(true);
                        mediaPlayer.start();
                        //    }
                        // });

                        mediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                            @Override
                            public void onCompletion(MediaPlayer mediaPlayer) {
                                visualizer.setEnabled(false);
                                if (disposables != null && !disposables.isDisposed())
                                    disposables.dispose();
                            }
                        });
                    }
                }, new Consumer<Throwable>() {
                    @Override
                    public void accept(@NonNull Throwable throwable) throws Exception {
                        throwable.printStackTrace();
                    }
                });
        disposables.add(mediaDisposable);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        initialWaveHeight = MeasureSpec.getSize(heightMeasureSpec);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        waves.clear();
        wavesWidthSum = 0f;
        float middle = initialRawX;
        for (int i = 0; i < amplitudes.size(); i += 3) {
            double wave = amplitudes.get(i) * 60;

            float left = middle;
            float right = middle + 10;
            float top = (float) (initialWaveHeight / 2 - (wave / 2));
            float bottom = (float) (initialWaveHeight / 2 + (wave / 2));

            waves.add(new WaveItem(top, bottom, left, right + 15));
            wavesWidthSum += (right + left);

            canvas.drawRect(left + 15, top, right, bottom, wavePaint);
            middle = right;
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

                WaveItem wave = null;
                for (int i = 0; i < waves.size(); i++) {
                    WaveItem waveItem = waves.get(i);
                    int left = (int) ((int) waveItem.getLeft() - rawX);
                    int right = (int) ((int) waveItem.getRight() - rawX);
                    if (left > 0 && right < 0 && (i + 1) == waves.size())
                        wave = waveItem;
                }

                if (initialRawX < 350 && wave != null) {
                    rightScroll += rawX;
                    wave = null;
                    invalidate();
                }
                return true;
        }
        return false;
    }
}
