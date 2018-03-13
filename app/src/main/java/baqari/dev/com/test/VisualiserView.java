package baqari.dev.com.test;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.media.MediaPlayer;
import android.media.audiofx.Visualizer;
import android.net.Uri;
import android.text.TextUtils;
import android.util.AttributeSet;
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

    private float initialWaveHeight = 1500f;
    private float initialRawX = 350;
    private float _xDelta = 0;
    private String mAudioUrl = null;

    private CompositeDisposable disposables = new CompositeDisposable();

    public VisualiserView(Context context) {
        super(context);
    }

    public VisualiserView(Context context, AttributeSet attrs) {
        super(context, attrs);

        TypedArray viewAttrs = context.obtainStyledAttributes(attrs, R.styleable.VisualiserView);
        try {
            mAudioUrl = viewAttrs.getString(R.styleable.VisualiserView_audioUrl);
        } finally {
            viewAttrs.recycle();
        }
        init();
    }

    public void setAudioUrl(String audioUrl) {
        mAudioUrl = audioUrl;
        init();
    }

    public void init() {
        wavePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        wavePaint.setColor(Color.RED);
        wavePaint.setTextSize(TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP, 20, getResources().getDisplayMetrics()));
        initPlayer();
    }

    public void initPlayer() {
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
                postInvalidate();
            }

            @Override
            public void onFftDataCapture(Visualizer visualizer, byte[] waveform, int j) {

            }
        };

        Disposable mediaDisposable = Observable.just(mAudioUrl)
                .observeOn(Schedulers.io())
                .subscribeOn(Schedulers.io())
                .subscribe(new Consumer<String>() {
                    @Override
                    public void accept(@NonNull String url) throws Exception {
                        if (url == null || TextUtils.isEmpty(url))
                            throw new NotSupportedException("Must provide valid url");

                        mediaPlayer = MediaPlayer.create(getContext(), Uri.parse(url));
                        VisualiserView.this.mediaPlayer = mediaPlayer;

                        visualizer = new Visualizer(mediaPlayer.getAudioSessionId());
                        visualizer.setCaptureSize(Visualizer.getCaptureSizeRange()[1]);
                        amplitudes = new ArrayList();
                        visualizer.setDataCaptureListener(listener, Visualizer.getMaxCaptureRate(), true, true);
                        visualizer.setEnabled(true);
                        mediaPlayer.start();

                        mediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                            @Override
                            public void onCompletion(MediaPlayer mediaPlayer) {
                                visualizer.setEnabled(false);
                                if (!disposables.isDisposed())
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
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        disposables.dispose();
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

            float left = middle;
            float right = middle + 10;
            float top = (float) (initialWaveHeight / 2 - (wave / 2));
            float bottom = (float) (initialWaveHeight / 2 + (wave / 2));

            canvas.drawRect(left + 15, top, right, bottom, wavePaint);
            middle = right;
        }
    }


    @Override
    public boolean onTouchEvent(MotionEvent event) {

        float rawX = event.getRawX();
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                _xDelta = rawX - initialRawX;
                return true;
            case MotionEvent.ACTION_MOVE:
                initialRawX = rawX - _xDelta;
                invalidate();
                return true;
        }
        return false;
    }
}
