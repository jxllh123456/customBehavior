package com.example.summer.lilinhaicoordinator.behavior;

import android.content.Context;
import android.support.design.widget.CoordinatorLayout;
import android.util.AttributeSet;
import android.view.View;

import com.example.summer.lilinhaicoordinator.R;
import com.example.summer.lilinhaicoordinator.widget.NormalCircle;

/**
 * Created by summer on 9/19/17.
 * 观察者
 */

public class SubscriberCircleBehavior extends NormalCircleBehavior{

      public SubscriberCircleBehavior() {
      }

      public SubscriberCircleBehavior(Context context, AttributeSet attrs) {
            super(context, attrs);
      }

      @Override
      public boolean onLayoutChild(CoordinatorLayout parent, NormalCircle child, int layoutDirection) {
            super.onLayoutChild(parent, child, layoutDirection);
            if (R.id.nc_test1==child.getId())
            setLeftAndRightOffset(200);
            // ...
            // ...
            return true;
      }

      @Override
      public boolean onDependentViewChanged(CoordinatorLayout parent, NormalCircle child, View dependency) {
            // child.setX(dependency.getX());
            //setLeftAndRightOffset(dependency.getRight()-child.getLeft());
            child.setY(dependency.getY());
            //setTopAndBottomOffset();
            return true;
      }

      @Override
      public boolean layoutDependsOn(CoordinatorLayout parent, NormalCircle child, View dependency) {
           return dependency.getId()==R.id.nc_test;
      }



}
