package com.example.summer.lilinhaicoordinator.widget;

import android.content.Context;
import android.support.design.widget.CoordinatorLayout;
import android.support.v4.view.MotionEventCompat;
import android.support.v4.view.VelocityTrackerCompat;
import android.support.v4.view.ViewCompat;
import android.support.v4.widget.ScrollerCompat;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.VelocityTracker;
import android.view.View;
import android.view.ViewConfiguration;

/**
 * Created by lilinhai on 2017/7/11.
 */

public class ClassicalTouchAndScrollerBehavior<V extends View> extends ATViewOffsetBehavior<V> {

    private static final int INVALID_POINTER = -1;
    private static final String TAG = "HEADER_BEHAVIOR";

    private Runnable mFlingRunnable;
    protected ScrollerCompat mScroller;

    private boolean mIsBeingDragged;
    private int mActivePointerId = INVALID_POINTER;
    private int mLastMotionY;
    private int mLastMotionX;
    private int mTouchSlop = -1;
    private VelocityTracker mVelocityTracker;
    private int mOverScrollDelta;

    public ClassicalTouchAndScrollerBehavior() {
    }

    public ClassicalTouchAndScrollerBehavior(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    public boolean onInterceptTouchEvent(CoordinatorLayout parent, V child, MotionEvent ev) {
        if (mTouchSlop < 0) {
            mTouchSlop = ViewConfiguration.get(parent.getContext()).getScaledTouchSlop();
        }

        final int action = ev.getAction();

        // Shortcut since we're being dragged
        if (action == MotionEvent.ACTION_MOVE && mIsBeingDragged) {
            return true;
        }

        switch (MotionEventCompat.getActionMasked(ev)) {
            case MotionEvent.ACTION_DOWN: {
                mIsBeingDragged = false;
                final int x = (int) ev.getX();
                final int y = (int) ev.getY();
                if (canDragView(child) && parent.isPointInChildBounds(child, x, y)) {
                    mLastMotionY = y;
                    mActivePointerId = ev.getPointerId(0);
                    ensureVelocityTracker();
                }
                break;
            }

            case MotionEvent.ACTION_MOVE: {
                final int activePointerId = mActivePointerId;
                if (activePointerId == INVALID_POINTER) {
                    // If we don't have a valid id, the touch down wasn't on content.
                    break;
                }
                final int pointerIndex = ev.findPointerIndex(activePointerId);
                if (pointerIndex == -1) {
                    break;
                }
                final int x = (int) ev.getX(pointerIndex);
                final int y = (int) ev.getY(pointerIndex);
                final int xDiff = Math.abs(x - mLastMotionX);
                final int yDiff = Math.abs(y - mLastMotionY);
                if (yDiff > mTouchSlop||xDiff>mTouchSlop) {
                    startDragging();
                    mLastMotionY = y;
                    mLastMotionX = x;
                }
                break;
            }

            case MotionEvent.ACTION_CANCEL:
            case MotionEvent.ACTION_UP: {
                stopDragging();
                mActivePointerId = INVALID_POINTER;
                if (mVelocityTracker != null) {
                    mVelocityTracker.recycle();
                    mVelocityTracker = null;
                }
                break;
            }
        }

        if (mVelocityTracker != null) {
            mVelocityTracker.addMovement(ev);
        }

        return mIsBeingDragged;
    }

    @Override
    public boolean onTouchEvent(CoordinatorLayout parent, V child, MotionEvent ev) {
        if (mTouchSlop < 0) {
            mTouchSlop = ViewConfiguration.get(parent.getContext()).getScaledTouchSlop();
        }

        switch (MotionEventCompat.getActionMasked(ev)) {
            case MotionEvent.ACTION_DOWN: {
                final int x = (int) ev.getX();
                final int y = (int) ev.getY();

                if (parent.isPointInChildBounds(child, x, y) && canDragView(child)) {
                    mOverScrollDelta = 0;
                    mLastMotionY = y;
                    mLastMotionX = x;
                    mActivePointerId = ev.getPointerId(0);
                    ensureVelocityTracker();
                } else {
                    return false;
                }
                break;
            }

            case MotionEvent.ACTION_MOVE: {
                final int activePointerIndex = ev.findPointerIndex(mActivePointerId);
                if (activePointerIndex == -1) {
                    return false;
                }

                final int y = (int) ev.getY(activePointerIndex);
                final int x = (int) ev.getX(activePointerIndex);
                // // TODO: 9/7/17 再加一个dx就好了
                int dy = mLastMotionY - y;
                int dx = mLastMotionX - x;

                if (!mIsBeingDragged && Math.abs(dy) > mTouchSlop) {
                    startDragging();
                    if (dy > 0) {
                        // 手指向上滑
                        dy -= mTouchSlop;
                    } else {
                        dy += mTouchSlop;
                    }
                    if (dx>0){
                        dx -=mTouchSlop;
                    }else{
                        dx +=mTouchSlop;
                    }
                }

                if (mIsBeingDragged) {
                    mLastMotionY = y;
                    mLastMotionX = x;
                    // 处理OverScroll，超出可滚动范围时缓存超出部分，反向滑动时先消费缓存的部分。
                    if (mOverScrollDelta != 0) {
                        if (dy > 0 && mOverScrollDelta > 0) {
                            mOverScrollDelta += dy;
                            break;
                        } else if (dy < 0 && mOverScrollDelta < 0) {
                            mOverScrollDelta += dy;
                            break;
                        } else {
                            if (Math.abs(mOverScrollDelta) >= Math.abs(dy)) {
                                // 不能完全消费
                                mOverScrollDelta += dy;
                                break;
                            } else {
                                // 有多余dy
                                dy += mOverScrollDelta;
                                mOverScrollDelta = 0;
                            }
                        }
                    }

                    // We're being dragged so scroll the ABL
                    int scrolled = scroll(dy,dx);
                    if (scrolled != dy) {
                        mOverScrollDelta += dy - scrolled;
                    }
                }
                break;
            }

            case MotionEvent.ACTION_UP:
                if (mVelocityTracker != null) {
                    mVelocityTracker.addMovement(ev);
                    mVelocityTracker.computeCurrentVelocity(1000);
                    // 这是一个矢量速度
                    float yvel = VelocityTrackerCompat.getYVelocity(mVelocityTracker,
                            mActivePointerId);
                    float xvel = VelocityTrackerCompat.getXVelocity(mVelocityTracker,mActivePointerId);
                    fling(parent, child,yvel,xvel);
                }
                // $FALLTHROUGH
            case MotionEvent.ACTION_CANCEL: {
                stopDragging();
                mActivePointerId = INVALID_POINTER;
                if (mVelocityTracker != null) {
                    mVelocityTracker.recycle();
                    mVelocityTracker = null;
                }
                break;
            }
        }

        if (mVelocityTracker != null) {
            mVelocityTracker.addMovement(ev);
        }

        return true;
    }

    private void startDragging() {
        mIsBeingDragged = true;
        onStartDragging();
    }

    private void stopDragging() {
        mIsBeingDragged = false;
        onStopDragging();
    }

    protected void onStartDragging() {

    }

    protected void onStopDragging() {

    }


    /**
     * @param newOffset mScroller.getCurrY();
     * @return
     */
    public int setHeaderTopBottomOffset( int newOffset,
                                       int newOffsetX) {
        final int curOffset = getTopAndBottomOffset();
        final int curOffsetX = getLeftAndRightOffset();
        int consumed = 0;
        // && curOffset <= maxOffset
            // If we have some scrolling range, and we're currently within the min and max
            // offsets, calculate a new offset
            //newOffset = ATMathUtils.constrain(newOffset, minOffset, maxOffset);
            if (curOffsetX!=newOffsetX){
                setLeftAndRightOffset(newOffsetX);
            }

            if (curOffset != newOffset) {
                setTopAndBottomOffset(newOffset);
                // Update how much dy we have consumed
                consumed = curOffset - newOffset;
            }

        return consumed;
    }

    public int getTopBottomOffsetForScrollingSibling() {
        return getTopAndBottomOffset();
    }

    private int getLeftRightOffsetForScrollingSibling(){
        return getLeftAndRightOffset();
    }

    public final int scroll(int dy,int dx) {
        return setHeaderTopBottomOffset(getTopBottomOffsetForScrollingSibling() - dy,getLeftRightOffsetForScrollingSibling()-dx);
    }

    protected boolean fling(CoordinatorLayout coordinatorLayout, V layout,
                            float velocityY,float velocityX) {
        int maxY = coordinatorLayout.getMeasuredHeight()-layout.getMeasuredHeight();
        int maxX = coordinatorLayout.getMeasuredWidth()-layout.getMeasuredWidth();
        Log.e("MAXY",maxY+"");
        if (mFlingRunnable != null) {
            layout.removeCallbacks(mFlingRunnable);
            mFlingRunnable = null;
        }

        if (mScroller == null) {
            mScroller = ScrollerCompat.create(layout.getContext());
        }
        // just like the original way :mScroller.startScroll();
        // 通过log观察 Scroller.getCurrY 和 velocityY的值可以发现 当velocityY>=0时(手指向下滑动)，Scroller.getCurrY 就是0,即不可以弹性滑动。当velocityY<0时，就可以弹性滑动。that's why?跟fling的最后一个参数有关!
        Log.e("velocityY",Math.round(velocityY)+"");
        mScroller.fling(
                getLeftAndRightOffset(),getTopAndBottomOffset(), // curr
                Math.round(velocityX),Math.round(velocityY), // velocity.
                0,maxX, // x
                0,maxY); // y

        if (mScroller.computeScrollOffset()) {
            mFlingRunnable = new FlingRunnable(coordinatorLayout, layout);
            ViewCompat.postOnAnimation(layout, mFlingRunnable);
            return true;
        } else {
            onFlingFinished(coordinatorLayout, layout);
            return false;
        }
    }

    protected void stopFling() {
        if (mScroller != null) {
            mScroller.abortAnimation();
        }
    }

    /**
     * Called when a fling has finished, or the fling was initiated but there wasn't enough
     * velocity to start it.
     */
    protected void onFlingFinished(CoordinatorLayout parent, V layout) {
        // no-op
    }

    /**
     * Return true if the view can be dragged.
     */
    protected boolean canDragView(V view) {
        return true;
    }

    /**
     * Returns the maximum px offset when {@code view} is being dragged.
     */
    protected int getMaxDragOffset(V view) {
        return -view.getHeight();
    }

    protected int getScrollRangeForDragFling(V view) {
        return view.getHeight();
    }

    private void ensureVelocityTracker() {
        if (mVelocityTracker == null) {
            mVelocityTracker = VelocityTracker.obtain();
        }
    }

    private class FlingRunnable implements Runnable {
        private final CoordinatorLayout mParent;
        private final V mLayout;

        FlingRunnable(CoordinatorLayout parent, V layout) {
            mParent = parent;
            mLayout = layout;
        }

        @Override
        public void run() {
            if (mLayout != null && mScroller != null) {
                if (mScroller.computeScrollOffset()) {
                    // mScroller.getCurrY() 是今次移动的这一点点加上以前的getTop() 的和,这句话的意思就跟传统滑动写法的view.scrollTo() 一个意思
                    int currY = mScroller.getCurrY();
                    int currX = mScroller.getCurrX();
                    Log.e(TAG,currY+"");
                    setHeaderTopBottomOffset(mScroller.getCurrY(),mScroller.getCurrX());
                    // Post ourselves so that we run on the next animation. 就是一个handler.post 跟animation毛关系都没有,just like the original way:view.postInvalidate()
                    ViewCompat.postOnAnimation(mLayout, this);
                } else {
                    onFlingFinished(mParent, mLayout);
                }
            }
        }
    }
}
