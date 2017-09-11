package com.example.summer.lilinhaicoordinator.widget;

import android.content.Context;
import android.util.AttributeSet;

/**
 * Created by summer on 9/7/17.
 * 通过反射（注解）来拿这个behavior，所以必须要有默认构造
 */
public class NormalCircleBehavior extends ClassicalTouchAndScrollerBehavior<NormalCircle> {

      public NormalCircleBehavior() {
      }

      public NormalCircleBehavior(Context context, AttributeSet attrs) {
            super(context, attrs);
      }
}
