package com.nowfloats.Store;

import android.content.Context;
import android.content.res.TypedArray;

import androidx.recyclerview.widget.RecyclerView;

import android.util.AttributeSet;

import com.thinksity.R;

/**
 * Created by vinay on 29-06-2018.
 */

public class MaxHeightRecyclerView extends RecyclerView {
    private int mMaxHeight;

    public MaxHeightRecyclerView(Context context) {
        super(context);
    }

    public MaxHeightRecyclerView(Context context, AttributeSet attrs) {
        super(context, attrs);
        initialize(context, attrs);
    }

    public MaxHeightRecyclerView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initialize(context, attrs);
    }

    private void initialize(Context context, AttributeSet attrs) {
        TypedArray arr = context.obtainStyledAttributes(attrs, R.styleable.MaxHeightScrollView);
        mMaxHeight = arr.getLayoutDimension(R.styleable.MaxHeightScrollView_maxHeight, mMaxHeight);
        arr.recycle();
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        if (mMaxHeight > 0) {
            heightMeasureSpec = MeasureSpec.makeMeasureSpec(mMaxHeight, MeasureSpec.AT_MOST);
        }
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
    }
}