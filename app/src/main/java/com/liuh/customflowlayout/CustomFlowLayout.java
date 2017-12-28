package com.liuh.customflowlayout;

import android.content.Context;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;

import java.util.ArrayList;
import java.util.List;

/**
 * Date: 2017/12/28 09:35
 * Description:自定义一个流式布局
 * 写这个代码写的我生无可恋 T_T
 */

public class CustomFlowLayout extends ViewGroup {

    private static final String TAG = CustomFlowLayout.class.getSimpleName();

    public CustomFlowLayout(Context context) {
        this(context, null);
    }

    public CustomFlowLayout(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public CustomFlowLayout(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    /**
     * 我们只需要支持margin,所以直接使用系统的MarginLayoutParams
     *
     * @param attrs
     * @return
     */
    @Override
    public LayoutParams generateLayoutParams(AttributeSet attrs) {
        return new MarginLayoutParams(getContext(), attrs);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
//        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        //获得它的父容器为它设置的宽高和测量模式
        int widthSize = MeasureSpec.getSize(widthMeasureSpec);
        int widthMode = MeasureSpec.getMode(widthMeasureSpec);
        int heightSize = MeasureSpec.getSize(heightMeasureSpec);
        int heightMode = MeasureSpec.getMode(heightMeasureSpec);

        Log.e(TAG, "------widthSize:" + widthSize + "-------heightSize:" + heightSize);

        int width = 0;
        int height = 0;
        //记录每一行的宽度
        int lineWidth = 0;
        //记录每一行的高度
        int lineHeight = 0;

        int cCount = getChildCount();

        for (int i = 0; i < cCount; i++) {
            View child = getChildAt(i);
            measureChild(child, widthMeasureSpec, heightMeasureSpec);

            MarginLayoutParams lp = (MarginLayoutParams) child.getLayoutParams();

            int childWidth = child.getMeasuredWidth() + lp.leftMargin + lp.rightMargin;

            int childHeight = child.getMeasuredHeight() + lp.topMargin + lp.bottomMargin;

            if (lineWidth + childWidth > widthSize) {
                //取最大
                width = Math.max(lineWidth, childWidth);
                //开始新一行,并记录宽度
                lineWidth = childWidth;
                //高度叠加
                height += lineHeight;
                //开启新一行,并记录高度
                lineHeight = childHeight;
            } else {
                //否则累加lineWidth,lineHeight
                lineWidth += childWidth;
                lineHeight = Math.max(lineHeight, childHeight);
            }

            //如果是最后一个,则将当前记录的最大宽度和当前lineWidth做比较
            if (i == cCount - 1) {
                width = Math.max(width, lineWidth);
                height += lineHeight;
            }
        }

        setMeasuredDimension(widthMode == MeasureSpec.EXACTLY ? widthSize : width,
                heightMode == MeasureSpec.EXACTLY ? heightSize : height);

    }

    //存储所有的View,按行记录
    private List<List<View>> mAllViews = new ArrayList<List<View>>();
    //记录每一行的最大高度
    private List<Integer> mLineHeight = new ArrayList<Integer>();

    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {
        mAllViews.clear();
        mLineHeight.clear();

        int width = getWidth();

        int lineWidth = 0;
        int lineHeight = 0;
        //存储每一行所有的childView
        List<View> lineViews = new ArrayList<View>();
        int cCount = getChildCount();
        //遍历所有的孩子
        for (int i = 0; i < cCount; i++) {
            View child = getChildAt(i);
            MarginLayoutParams lp = (MarginLayoutParams) child.getLayoutParams();

            int childWidth = child.getMeasuredWidth();
            int childHeight = child.getMeasuredHeight();

            if (childWidth + lp.leftMargin + lp.rightMargin + lineWidth > width) {
                //如果需要换行
                //记录这一行所有的View以及最大高度
                mLineHeight.add(lineHeight);
                //将当前行的childView保存,然后开启新的ArrayList保存下一行的childView
                mAllViews.add(lineViews);
                //重置行宽
                lineWidth = 0;
                lineViews = new ArrayList<View>();
            }
            //如果不需要换行,则累加
            lineWidth += childWidth + lp.leftMargin + lp.rightMargin;
            lineHeight = Math.max(lineHeight, childHeight + lp.topMargin + lp.bottomMargin);
            lineViews.add(child);
        }

        //记录最后一行
        mLineHeight.add(lineHeight);
        mAllViews.add(lineViews);

        int left = 0;
        int top = 0;

        //得到总行数
        int lineNums = mAllViews.size();
        for (int i = 0; i < lineNums; i++) {
            //每一行的所有的views
            lineViews = mAllViews.get(i);
            lineHeight = mLineHeight.get(i);

            Log.e(TAG, "第" + i + "行:" + lineViews.size() + "," + lineViews);

            Log.e(TAG, "第" + i + "行:" + lineHeight);

            for (int j = 0; j < lineViews.size(); j++) {
                View child = lineViews.get(j);
                if (child.getVisibility() == View.GONE) {
                    continue;
                }
                MarginLayoutParams lp = (MarginLayoutParams) child.getLayoutParams();
                //计算childView的left,top,right,bottom
                int lc = left + lp.leftMargin;
                int tc = top + lp.topMargin;
                int rc = lc + child.getMeasuredWidth();
                int bc = tc + child.getMeasuredHeight();

                Log.e(TAG, child + " , l = " + lc + " , t = " + t + " , r ="
                        + rc + " , b = " + bc);
                child.layout(lc, tc, rc, bc);

                left += child.getMeasuredWidth() + lp.rightMargin + lp.leftMargin;
            }
            left = 0;
            top += lineHeight;
        }
    }
}
