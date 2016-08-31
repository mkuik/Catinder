package kuik.matthijs.catinder.ui;

import android.animation.Animator;
import android.content.Context;
import android.content.res.TypedArray;
import android.util.AttributeSet;
import android.view.animation.Animation;
import android.view.animation.TranslateAnimation;
import android.widget.FrameLayout;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import kuik.matthijs.catinder.R;

/**
 * TODO: document your custom view class.
 */
public class HeartsOverlay extends FrameLayout {

    private static final String TAG = "HeartsOverlay";
    private int numberOfHearts;
    private List<List<Heart>> hearts = new ArrayList<>();

    public HeartsOverlay(Context context) {
        super(context);
        init(null, 0);
    }

    public HeartsOverlay(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(attrs, 0);
    }

    public HeartsOverlay(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init(attrs, defStyle);
    }

    private void init(AttributeSet attrs, int defStyle) {
        // Load attributes
        final TypedArray a = getContext().obtainStyledAttributes(
                attrs, R.styleable.HeartsOverlay, defStyle, 0);
        numberOfHearts = a.getInteger(R.styleable.HeartsOverlay_numberOfHearts, 1);
        a.recycle();
    }

    public void newGroup() {
        newGroup(getWidth(), getHeight());
    }

    public void newGroup(final int width, final int height) {
        if (width == 0 || height == 0) return;
        final int paddingLeft = getPaddingLeft();
        final int paddingTop = getPaddingTop();
        final int paddingRight = getPaddingRight();
        final int paddingBottom = getPaddingBottom();
        final int contentWidth = width - paddingLeft - paddingRight;
        final int contentHeight = height - paddingTop - paddingBottom;

        List<Heart> group = new ArrayList<>();
        for (int i = 0; i != numberOfHearts; ++i) {
            Heart heart = new Heart(getContext());
            group.add(heart);
            addView(heart);
            new PlaceHeart(heart, contentWidth, contentHeight, i).run();
        }
        hearts.add(group);
    }

    public int getCount() {
        return hearts.size();
    }

    class PlaceHeart implements Runnable {

        Heart heart;
        int width;
        int height;
        int offset;

        public PlaceHeart(Heart heart, int width, int height, int offset) {
            this.heart = heart;
            this.height = height;
            this.offset = offset;
            this.width = width;
        }

        @Override
        public void run() {
            Integer size = new Random().nextInt(50) + 50;
            Integer radius = size / 5;
            LayoutParams params = (LayoutParams) heart.getLayoutParams();
            params.height = size;
            params.width = size + (size / 2);
            params.leftMargin = new Random().nextInt(width - params.height - radius) + radius;
            params.topMargin = new Random().nextInt(height - params.width - radius) + radius;
            heart.setRadius(radius);
            heart.setOffset(offset);
            heart.setLayoutParams(params);
        }
    }

    public void breakHearts() {
        if (hearts.isEmpty()) return;
        List<Heart> group = hearts.get(0);
        hearts.remove(0);
        for (final Heart heart : group) {
            heart.getBreakAnimator().addListener(new Animator.AnimatorListener() {
                @Override
                public void onAnimationStart(Animator animation) {

                }

                @Override
                public void onAnimationEnd(Animator animation) {
                    removeView(heart);
                }

                @Override
                public void onAnimationCancel(Animator animation) {
                    removeView(heart);
                }

                @Override
                public void onAnimationRepeat(Animator animation) {

                }
            });
            heart.breakApart();
        }
    }

    public void moveHeartsToRight() {
        if (hearts.isEmpty()) return;
        List<Heart> group = hearts.get(0);
        hearts.remove(0);
        for (final Heart heart : group) {
            TranslateAnimation animation = new TranslateAnimation(0, getWidth(), 0, 0);
            animation.setDuration(1000);
            animation.setAnimationListener(new Animation.AnimationListener() {
                @Override
                public void onAnimationStart(Animation animation) {

                }

                @Override
                public void onAnimationEnd(Animation animation) {
                    removeView(heart);
                }

                @Override
                public void onAnimationRepeat(Animation animation) {

                }
            });
            heart.startAnimation(animation);
        }
    }

    public void clear() {
        removeAllViews();
        hearts.clear();
    }
}
