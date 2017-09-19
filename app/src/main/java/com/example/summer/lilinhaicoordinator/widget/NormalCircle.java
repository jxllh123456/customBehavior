package com.example.summer.lilinhaicoordinator.widget;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.view.View;
import android.view.WindowManager;

/**
 * Created by summer on 9/7/17.
 */
public class NormalCircle extends View{

      private static int DEFAULT_SIZE;
      private Paint mPaint;

      public NormalCircle(Context context, @Nullable AttributeSet attrs) {
            super(context, attrs);
            mPaint = new Paint();
            mPaint.setColor(context.getResources().getColor(android.R.color.holo_blue_light));
            mPaint.setAntiAlias(true);
            mPaint.setStyle(Paint.Style.FILL);
            WindowManager windowManager = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
            DEFAULT_SIZE = windowManager.getDefaultDisplay().getHeight()/10;
      }

      @Override
      protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
            int resultWidth = 0;
            int resultHeight = 0;
            int widthSize = MeasureSpec.getSize(widthMeasureSpec);
            int heightSize = MeasureSpec.getSize(heightMeasureSpec);
            int widthMode = MeasureSpec.getMode(widthMeasureSpec);
            int heightMode = MeasureSpec.getMode(heightMeasureSpec);
            switch (widthMode){
                  case MeasureSpec.AT_MOST:
                        resultWidth = DEFAULT_SIZE;
                        break;
                  case MeasureSpec.EXACTLY:
                        resultWidth = widthSize;
                        break;
                  case MeasureSpec.UNSPECIFIED:
                        break;
            }
            switch (heightMode){
                  case MeasureSpec.AT_MOST:
                        resultHeight = DEFAULT_SIZE;
                        break;
                  case MeasureSpec.EXACTLY:
                        resultHeight = heightSize;
                        break;
                  case MeasureSpec.UNSPECIFIED:
                        break;
            }
            setMeasuredDimension(resultWidth,resultHeight);
      }

      @Override
      protected void onDraw(Canvas canvas) {
            // 圆心、半径
            canvas.drawCircle(DEFAULT_SIZE/2,DEFAULT_SIZE/2,getMeasuredHeight()/2,mPaint);
      }
}
