package com.dengyy.weatherapp.ui;

import android.animation.ValueAnimator;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.LinearGradient;
import android.graphics.Paint;
import android.graphics.Shader;
import android.util.AttributeSet;
import android.view.View;
import android.view.animation.LinearInterpolator;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.graphics.ColorUtils;

import com.dengyy.weatherapp.R;

public class WeatherBackgroundView extends View {

    public static final int WEATHER_SUNNY = 0;
    public static final int WEATHER_CLOUDY = 1;
    public static final int WEATHER_RAIN = 2;
    public static final int WEATHER_SNOW = 3;

    private final Paint backgroundPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint blobPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint accentPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint particlePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint hazePaint = new Paint(Paint.ANTI_ALIAS_FLAG);

    private final ValueAnimator animator = ValueAnimator.ofFloat(0f, 1f);

    private int weatherType = WEATHER_SUNNY;
    private int topColor;
    private int bottomColor;
    private int glowColor;
    private int accentColor;
    private float progress;

    public WeatherBackgroundView(Context context) {
        this(context, null);
    }

    public WeatherBackgroundView(Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public WeatherBackgroundView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        animator.setDuration(18000L);
        animator.setRepeatCount(ValueAnimator.INFINITE);
        animator.setInterpolator(new LinearInterpolator());
        animator.addUpdateListener(animation -> {
            progress = (float) animation.getAnimatedValue();
            invalidate();
        });
        setWeatherType(WEATHER_SUNNY);
    }

    public void setWeatherType(int weatherType) {
        if (this.weatherType == weatherType && topColor != 0) {
            return;
        }
        this.weatherType = weatherType;
        switch (weatherType) {
            case WEATHER_RAIN:
                topColor = getColorCompat(R.color.main_bg_rain_top);
                bottomColor = getColorCompat(R.color.main_bg_rain_bottom);
                glowColor = ColorUtils.setAlphaComponent(0xFF9DD8FF, 78);
                accentColor = ColorUtils.setAlphaComponent(0xFF244F73, 58);
                break;
            case WEATHER_SNOW:
                topColor = getColorCompat(R.color.main_bg_snow_top);
                bottomColor = getColorCompat(R.color.main_bg_snow_bottom);
                glowColor = ColorUtils.setAlphaComponent(0xFFFFFFFF, 125);
                accentColor = ColorUtils.setAlphaComponent(0xFFC7E5F8, 72);
                break;
            case WEATHER_CLOUDY:
                topColor = getColorCompat(R.color.main_bg_cloudy_top);
                bottomColor = getColorCompat(R.color.main_bg_cloudy_bottom);
                glowColor = ColorUtils.setAlphaComponent(0xFFF4FBFF, 86);
                accentColor = ColorUtils.setAlphaComponent(0xFF9AB7CC, 56);
                break;
            case WEATHER_SUNNY:
            default:
                topColor = getColorCompat(R.color.main_bg_sunny_top);
                bottomColor = getColorCompat(R.color.main_bg_sunny_bottom);
                glowColor = ColorUtils.setAlphaComponent(0xFFFFE18A, 136);
                accentColor = ColorUtils.setAlphaComponent(0xFFB6E8FF, 72);
                break;
        }
        invalidate();
    }

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        if (!animator.isStarted()) {
            animator.start();
        } else if (animator.isPaused()) {
            animator.resume();
        }
    }

    @Override
    protected void onDetachedFromWindow() {
        animator.cancel();
        super.onDetachedFromWindow();
    }

    @Override
    protected void onDraw(@NonNull Canvas canvas) {
        super.onDraw(canvas);
        float width = getWidth();
        float height = getHeight();
        if (width <= 0f || height <= 0f) {
            return;
        }

        drawGradientBackground(canvas, width, height);
        drawHaze(canvas, width, height);
        drawAmbientBlobs(canvas, width, height);

        switch (weatherType) {
            case WEATHER_RAIN:
                drawRain(canvas, width, height);
                break;
            case WEATHER_SNOW:
                drawSnow(canvas, width, height);
                break;
            case WEATHER_CLOUDY:
                drawCloudBands(canvas, width, height);
                break;
            case WEATHER_SUNNY:
            default:
                drawSunGlow(canvas, width, height);
                break;
        }
    }

    private void drawGradientBackground(Canvas canvas, float width, float height) {
        backgroundPaint.setShader(new LinearGradient(
                0f,
                0f,
                0f,
                height,
                topColor,
                bottomColor,
                Shader.TileMode.CLAMP
        ));
        canvas.drawRect(0f, 0f, width, height, backgroundPaint);
        backgroundPaint.setShader(null);
    }

    private void drawHaze(Canvas canvas, float width, float height) {
        float sweep = (float) Math.sin(progress * Math.PI * 2d * 0.5d);

        hazePaint.setColor(ColorUtils.setAlphaComponent(ColorUtils.blendARGB(topColor, 0xFFFFFFFF, 0.35f), 72));
        canvas.drawCircle(
                width * (0.48f + sweep * 0.05f),
                height * 0.36f,
                width * 0.44f,
                hazePaint
        );

        hazePaint.setColor(ColorUtils.setAlphaComponent(ColorUtils.blendARGB(bottomColor, 0xFFFFFFFF, 0.22f), 62));
        canvas.drawCircle(
                width * (0.72f - sweep * 0.04f),
                height * 0.78f,
                width * 0.36f,
                hazePaint
        );
    }

    private void drawAmbientBlobs(Canvas canvas, float width, float height) {
        float wave = (float) Math.sin(progress * Math.PI * 2d);
        float wave2 = (float) Math.cos(progress * Math.PI * 2d * 0.8d);

        blobPaint.setColor(glowColor);
        canvas.drawCircle(
                width * (0.78f + wave * 0.04f),
                height * (0.18f + wave2 * 0.03f),
                width * 0.30f,
                blobPaint
        );

        blobPaint.setColor(accentColor);
        canvas.drawCircle(
                width * (0.20f - wave2 * 0.05f),
                height * (0.32f + wave * 0.04f),
                width * 0.24f,
                blobPaint
        );

        blobPaint.setColor(ColorUtils.setAlphaComponent(glowColor, 70));
        canvas.drawCircle(
                width * (0.85f - wave * 0.06f),
                height * (0.86f - wave2 * 0.04f),
                width * 0.22f,
                blobPaint
        );
    }

    private void drawSunGlow(Canvas canvas, float width, float height) {
        float sunWaveX = (float) Math.sin(progress * Math.PI * 2d);
        float sunWaveY = (float) Math.cos(progress * Math.PI * 2d);
        float sunX = width * (0.78f + sunWaveX * 0.03f);
        float sunY = height * (0.17f + sunWaveY * 0.02f);

        accentPaint.setColor(ColorUtils.setAlphaComponent(0xFFFFF5C4, 118));
        canvas.drawCircle(sunX, sunY, width * 0.15f, accentPaint);

        accentPaint.setColor(ColorUtils.setAlphaComponent(0xFFFFCC67, 76));
        canvas.drawCircle(sunX, sunY, width * 0.08f, accentPaint);
    }

    private void drawCloudBands(Canvas canvas, float width, float height) {
        accentPaint.setColor(ColorUtils.setAlphaComponent(0xFFFFFFFF, 44));
        float baseY = height * 0.25f;
        float shift = width * 0.06f * progress;
        canvas.drawRoundRect(-width * 0.10f + shift, baseY, width * 0.62f + shift, baseY + 84f, 42f, 42f, accentPaint);
        canvas.drawRoundRect(width * 0.34f - shift, baseY + 120f, width * 1.08f - shift, baseY + 210f, 46f, 46f, accentPaint);
    }

    private void drawRain(Canvas canvas, float width, float height) {
        particlePaint.setStrokeWidth(2.4f);
        particlePaint.setColor(ColorUtils.setAlphaComponent(0xFFD7EEFF, 54));
        particlePaint.setStyle(Paint.Style.STROKE);

        float offset = height * progress;
        for (int index = 0; index < 10; index++) {
            float startX = width * (0.08f + index * 0.06f);
            float startY = ((index * 74f) + offset) % (height + 120f) - 120f;
            canvas.drawLine(startX, startY, startX - width * 0.045f, startY + 52f, particlePaint);
        }
    }

    private void drawSnow(Canvas canvas, float width, float height) {
        particlePaint.setStyle(Paint.Style.FILL);
        particlePaint.setColor(ColorUtils.setAlphaComponent(0xFFFFFFFF, 108));

        for (int index = 0; index < 12; index++) {
            float drift = (float) Math.sin((progress * 6d) + index) * width * 0.015f;
            float cx = width * (0.08f + (index % 6) * 0.16f) + drift;
            float cy = ((height * progress * 1.1f) + index * 96f) % (height + 120f) - 60f;
            float radius = 4f + (index % 3) * 2f;
            canvas.drawCircle(cx, cy, radius, particlePaint);
        }
    }

    private int getColorCompat(int colorRes) {
        return getContext().getColor(colorRes);
    }
}
