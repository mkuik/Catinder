package kuik.matthijs.catinder.ui;

import android.animation.ValueAnimator;
import android.content.Context;
import android.util.AttributeSet;
import android.view.animation.LinearInterpolator;
import android.widget.ImageView;
import kuik.matthijs.catinder.R;

public class Sun extends ImageView {

    ValueAnimator rotationAnimator;
    private static final String TAG = "Sun";
    private static final int SUN_RESOURCE = R.drawable.poly_sun_1;
    private long mem = 0;

    public Sun(Context context) {
        super(context);
        init();
    }

    public Sun(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public Sun(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {
        setImageResource(SUN_RESOURCE);
        rotationAnimator = ValueAnimator.ofFloat(0, 360);
        rotationAnimator.setDuration(20000);
        rotationAnimator.setRepeatCount(ValueAnimator.INFINITE);
        rotationAnimator.addUpdateListener(new Rotation());
        rotationAnimator.setInterpolator(new LinearInterpolator());
    }

    public void startAnimation() {
        rotationAnimator.start();
        rotationAnimator.setCurrentPlayTime(mem);
    }

    public void stopAnimation() {
        mem = rotationAnimator.getCurrentPlayTime();
        rotationAnimator.cancel();
    }

    protected class Rotation implements ValueAnimator.AnimatorUpdateListener {
        @Override
        public void onAnimationUpdate(ValueAnimator animation) {
            Float value = (Float) (animation.getAnimatedValue());
            setRotation(value);
        }
    }
}
