package com.example.jeongho.mediaplayer;

import android.content.Context;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.widget.ImageView;

/**
 * Created by jeongho on 4/23/16.
 */
public class SquareImageView extends ImageView {
    private enum AnimationStatus {
        NORMAL,
        DARKENING,
        DARKENED
    }

    final float finalOpacity = 0.45f;

    private AnimationStatus animationStatus = AnimationStatus.NORMAL;

    public SquareImageView(final Context context) {
        super(context);
    }

    public SquareImageView(final Context context, final AttributeSet attrs) {
        super(context, attrs);
    }

    public SquareImageView(final Context context, final AttributeSet attrs, final int defStyle) {
        super(context, attrs, defStyle);
    }

    @Override
    protected void onMeasure(final int widthMeasureSpec, final int heightMeasureSpec) {
        final int gridWidth = getDefaultSize(getSuggestedMinimumWidth(), widthMeasureSpec);
        setMeasuredDimension(gridWidth, gridWidth);
    }

    @Override
    protected void onSizeChanged(final int width, final int height, final int oldWidth, final int oldHeight) {
        super.onSizeChanged(width, width, oldWidth, oldHeight);
    }

    @Override
    protected void onAnimationEnd() {
        super.onAnimationEnd();

        if (animationStatus == AnimationStatus.DARKENING) {
            animationStatus = AnimationStatus.DARKENED;
            clearAnimation();
            setAlpha(finalOpacity);
        }
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        switch (event.getAction()) {
            case android.view.MotionEvent.ACTION_DOWN: {
                if (animationStatus == AnimationStatus.NORMAL) {
                    animationStatus = AnimationStatus.DARKENING;

                    Animation animation = new AlphaAnimation(1.0f, finalOpacity);
                    animation.setDuration(250);

                    startAnimation(animation);
                }
                break;
            }
            case android.view.MotionEvent.ACTION_UP:
            case android.view.MotionEvent.ACTION_CANCEL: {
                clearAnimation();
                setAlpha(1.0f);
                animationStatus = AnimationStatus.NORMAL;

                break;
            }
        }

        super.onTouchEvent(event);

        return true;
    }
}
