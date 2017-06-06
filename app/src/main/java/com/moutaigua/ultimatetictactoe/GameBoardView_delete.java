package com.moutaigua.ultimatetictactoe;

import android.content.Context;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.view.View;

/**
 * Created by mou on 3/18/17.
 */

public class GameBoardView_delete extends View {
    public GameBoardView_delete(Context context) {
        super(context);
        inflate(getContext(), R.layout.board_global, null);
    }

    public GameBoardView_delete(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        inflate(getContext(), R.layout.board_global, null);
    }

    public GameBoardView_delete(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        inflate(getContext(), R.layout.board_global, null);
    }

    @Override
    public void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int widthMode = View.MeasureSpec.getMode( widthMeasureSpec );
        int widthPixels = View.MeasureSpec.getSize( widthMeasureSpec );
        int heightMode = View.MeasureSpec.getMode( heightMeasureSpec );
        int heightPixels = View.MeasureSpec.getSize( heightMeasureSpec );
        if( widthPixels > heightMode ){
            int newWidthPixels = heightPixels;
            int newWidthMeasureSpec = View.MeasureSpec.makeMeasureSpec(newWidthPixels, widthMode);
            super.onMeasure(widthMeasureSpec, newWidthMeasureSpec);
        } else if ( widthPixels < heightPixels ) {
            int newHeightPixels = widthPixels;
            int newHeightMeasureSpec = View.MeasureSpec.makeMeasureSpec(newHeightPixels, heightMode);
            super.onMeasure(widthMeasureSpec, newHeightMeasureSpec);
        } else {
            super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        }
    }


}
