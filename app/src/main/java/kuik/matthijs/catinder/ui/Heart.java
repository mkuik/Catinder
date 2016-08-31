package kuik.matthijs.catinder.ui;

import android.animation.ValueAnimator;
import android.content.Context;
import android.util.AttributeSet;
import android.view.animation.DecelerateInterpolator;
import android.view.animation.LinearInterpolator;
import android.widget.ImageView;
import android.widget.LinearLayout;
import kuik.matthijs.catinder.R;

/**
 * Created by Matthijs on 01/08/16.
 */
public class Heart extends LinearLayout {

    ValueAnimator floatAnimator;
    ValueAnimator breakAnimator;
    private static final String TAG = "Heart";
    private float offset = 0;
    private Integer radius = 0;
    private static final int[] HEART_LEFT_RESOURCES = {
            R.mipmap.heart1_half0,
            R.mipmap.heart2_half0,
            R.mipmap.heart3_half0,
            R.mipmap.heart4_half0
    };
    private static final int[] HEART_RIGHT_RESOURCES = {
            R.mipmap.heart1_half1,
            R.mipmap.heart2_half1,
            R.mipmap.heart3_half1,
            R.mipmap.heart4_half1
    };
    private ImageView left;
    private ImageView right;
    private static int counter = 0;

    public Heart(Context context) {
        super(context);
        init();
    }

    public Heart(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public Heart(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {
        inflate(getContext(), R.layout.heart, this);

        int resourceIndex = counter % HEART_LEFT_RESOURCES.length;
        counter++;

        left = (ImageView) findViewById(R.id.left_heart);
        right = (ImageView) findViewById(R.id.right_heart);
        left.setImageResource(HEART_LEFT_RESOURCES[resourceIndex]);
        right.setImageResource(HEART_RIGHT_RESOURCES[resourceIndex]);

        floatAnimator = ValueAnimator.ofFloat(0, 1);
        floatAnimator.setDuration(20000);
        floatAnimator.setRepeatCount(ValueAnimator.INFINITE);
        floatAnimator.addUpdateListener(new FloatAnimation());
        floatAnimator.setInterpolator(new LinearInterpolator());

        breakAnimator = ValueAnimator.ofFloat(0, 1);
        breakAnimator.setDuration(300);
        breakAnimator.setInterpolator(new DecelerateInterpolator());
        breakAnimator.addUpdateListener(new BreakAnimation());
    }

    public void startAnimation() {
        floatAnimator.start();
    }

    public void stopAnimation() {
        floatAnimator.end();
    }

    public void breakApart() {
        breakAnimator.start();
    }

    protected class BreakAnimation implements ValueAnimator.AnimatorUpdateListener {
        @Override
        public void onAnimationUpdate(ValueAnimator animation) {
            float value = (float)animation.getAnimatedValue();

            int distance = (int) (getWidth() * 0.15 * value);
            left.setTranslationX(-distance);
            right.setTranslationX(distance);

            float rotation = value * 15;
            left.setRotation(-rotation);
            right.setRotation(rotation);
        }
    }

    protected class FloatAnimation implements ValueAnimator.AnimatorUpdateListener {
        @Override
        public void onAnimationUpdate(ValueAnimator animation) {
            Float value = (Float) (animation.getAnimatedValue());

            double piValue = Math.PI * 2 * value;
            double x = Math.sin(piValue * 4 + offset);
            double y = Math.cos(piValue * 3 + offset);
            setTranslationX((float) x * radius);
            setTranslationY((float) y * radius);

            setRotation((float) (x * y * 10));
        }
    }

    public void heal() {
        left.setTranslationX(0);
        right.setTranslationX(0);
        left.setRotation(0);
        right.setRotation(0);
    }

    public float getOffset() {
        return offset;
    }

    public void setOffset(float offset) {
        this.offset = offset;
    }

    public Integer getRadius() {
        return radius;
    }

    public void setRadius(Integer radius) {
        this.radius = radius;
    }

    public ValueAnimator getBreakAnimator() {
        return breakAnimator;
    }

    public ValueAnimator getFloatAnimator() {
        return floatAnimator;
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        stopAnimation();
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        if (w != 0 && h != 0 && !floatAnimator.isRunning()) {
            startAnimation();
        }
    }
}
