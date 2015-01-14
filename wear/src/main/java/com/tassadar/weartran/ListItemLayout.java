package com.tassadar.weartran;

import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.content.Context;
import android.support.wearable.view.WearableListView;
import android.util.AttributeSet;
import android.view.View;
import android.widget.RelativeLayout;

public class ListItemLayout extends RelativeLayout implements WearableListView.OnCenterProximityListener {
    public ListItemLayout(Context context) {
        super(context);
        onNonCenterPosition(false);
    }

    public ListItemLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
        onNonCenterPosition(false);
    }

    public ListItemLayout(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        onNonCenterPosition(false);
    }

    @Override
    public void onCenterPosition(boolean animate) {

        if(animate) {
            if(m_curAnim != null && m_curAnim.isRunning()) {
                if (!m_animToBig) {
                    m_curAnim.cancel();
                } else {
                    return;
                }
            }
            m_curAnim = new AnimatorSet();
            m_curAnim.play(ObjectAnimator.ofFloat(this, View.SCALE_X, getScaleX(), 1.f))
                    .with(ObjectAnimator.ofFloat(this, View.SCALE_Y, getScaleY(), 1.f))
                    .with(ObjectAnimator.ofFloat(this, View.ALPHA, getAlpha(), 1.f));
            m_curAnim.setDuration(75);
            m_curAnim.start();
            m_animToBig = true;
        } else {
            setAlpha(1.f);
            setScaleX(1.f);
            setScaleY(1.f);
        }
    }

    @Override
    public void onNonCenterPosition(boolean animate) {
        final float alpha = 0.3f;
        final float scale = 0.8f;
        if(animate) {
            if(m_curAnim != null && m_curAnim.isRunning()) {
                if(m_animToBig) {
                    m_curAnim.cancel();
                } else {
                    return;
                }
            }
            m_curAnim = new AnimatorSet();
            m_curAnim.play(ObjectAnimator.ofFloat(this, View.SCALE_X, getScaleX(), scale))
                .with(ObjectAnimator.ofFloat(this, View.SCALE_Y, getScaleY(), scale))
                .with(ObjectAnimator.ofFloat(this, View.ALPHA, getAlpha(), alpha));
            m_curAnim.setDuration(75);
            m_curAnim.start();
            m_animToBig = false;
        } else {
            setScaleX(scale);
            setScaleY(scale);
            setAlpha(alpha);
        }
    }

    private AnimatorSet m_curAnim;
    boolean m_animToBig;
}
