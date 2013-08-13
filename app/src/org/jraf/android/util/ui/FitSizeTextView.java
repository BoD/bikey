package org.jraf.android.util.ui;

import android.content.Context;
import android.graphics.Rect;
import android.text.StaticLayout;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.View;
import android.widget.TextView;

import org.jraf.android.util.Log;

public class FitSizeTextView extends TextView {
    public FitSizeTextView(Context context) {
        super(context);
        init();
    }

    public FitSizeTextView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public FitSizeTextView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init();
    }

    private void init() {
        setLayerType(View.LAYER_TYPE_SOFTWARE, null);
    }

    private void resetTextSize(int width, int height) {
        if (width == 0 || height == 0) return;
        int w = width - getPaddingLeft() - getPaddingRight();
        int h = height - getPaddingTop() - getPaddingBottom();

        int textSize = h;
        Rect bounds = new Rect();
        int textWidth;
        int textHeight;

        do {
            Log.d("textSize=" + textSize);
            textSize *= .9;
            setTextSize(TypedValue.COMPLEX_UNIT_PX, textSize);
            measureText(getText(), bounds);
            textWidth = bounds.width();
            textHeight = bounds.height();
            Log.d("- textWidth=" + textWidth + " textHeight=" + textHeight);
        } while (textWidth > w || textHeight > h);
        Log.d("----");
    }

    private void measureText(CharSequence text, Rect bounds) {
        StaticLayout tempLayout = new StaticLayout(text, getPaint(), Integer.MAX_VALUE, android.text.Layout.Alignment.ALIGN_NORMAL, 1f, 0f, false);
        bounds.left = (int) tempLayout.getLineLeft(0);
        bounds.right = (int) tempLayout.getLineRight(0);
        bounds.top = tempLayout.getLineTop(0);
        bounds.bottom = tempLayout.getLineBottom(0);
    }

    //    @Override
    //    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
    //        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
    //        int width = MeasureSpec.getSize(widthMeasureSpec);
    //        int height = MeasureSpec.getSize(heightMeasureSpec);
    //        resetTextSize(width, height);
    //        setMeasuredDimension(width, height);
    //    }

    @Override
    protected void onTextChanged(final CharSequence text, final int start, final int before, final int after) {
        resetTextSize(getWidth(), getHeight());
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        if (w != oldw || h != oldh) {
            resetTextSize(w, h);
        }
    }
}
