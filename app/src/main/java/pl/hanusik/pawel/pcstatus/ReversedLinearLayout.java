package pl.hanusik.pawel.pcstatus;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;

import androidx.annotation.Nullable;

public class ReversedLinearLayout extends LinearLayout {
    public ReversedLinearLayout(Context context) {
        super(context);
    }

    public ReversedLinearLayout(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
    }

    public ReversedLinearLayout(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    public ReversedLinearLayout(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
    }

    public void addView(View child, int index) {
        super.addView(child, 0);
    }

    public void addView(View child, int index, ViewGroup.LayoutParams params) {
        super.addView(child, 0, params);
    }
}
