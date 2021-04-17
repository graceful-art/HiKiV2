package com.hikivision.UI;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;

import com.hikivision.R;
/**
 * @author yueyang
 * @version V1.0
 * @describation
 * 机器人位置进度条
 * @modificationHistory
 */
public class RobotProgressBar extends View {
    private final String TAG = "RobotProgressBar";
    private int mProgress_outline_color = 0xFFFFFFFF;//外边框颜色
    private int mProgress_color = 0xFFFFFFFF; //进度条颜色
    private int mProgress_circle_color = 0xFF00FF00;  //圆圈颜色
    private int mProgress_text_color = 0xFFFFFFFF;  //进度字体颜色
    private int mProgress_text_bg_color = 0x50FFFFFF;  //进度背景颜色
    private float mProgress_circle_height = 56;// 56;  //圆圈高度
    private float mProgress_height = 40;// 25;  //progress高度
    private float mProgress_bar_height = 30;  //progress进度条高度
    private float mProgress_text_height = 35;  //进度文字高度
    private float mProgress_text_paddingH = 25;  //进度文字左右padding
    private float mProgress_text_paddingV = 0;  //进度文字上下pading
    private float mProgress_text_size = 32;  //进度文字字体大小
    private float mMaxProgress = 50;
    private volatile  float mProgress_progress_bar = 0;
    private int mProgressWidth;

    private Paint mOutLinePaint;
    private Paint mProgressPaint;
    private Paint mCirClePaint;
    private Paint mTextPain;
    private Paint mTextBgPain;

    public RobotProgressBar(Context context) {
        super(context);
        Log.d(TAG, "HProgressBar: 1");
    }

    public RobotProgressBar(Context context, AttributeSet attrs) {
        super(context, attrs);
        Log.d(TAG, "HProgressBar: 2");
        initDefStyleAttr(attrs);
        mOutLinePaint = new Paint();
        mOutLinePaint.setColor(mProgress_outline_color);
        mOutLinePaint.setAntiAlias(true);
        mOutLinePaint.setStrokeWidth(2);
        mOutLinePaint.setStyle(Paint.Style.STROKE); //设置空心

        mProgressPaint = new Paint();
        mProgressPaint.setColor(mProgress_color);
        mProgressPaint.setAntiAlias(true);

        mCirClePaint = new Paint();
        mCirClePaint.setColor(mProgress_circle_color);
        mCirClePaint.setAntiAlias(true);

        mTextBgPain = new Paint();
        mTextBgPain.setColor(mProgress_text_bg_color);
        mTextBgPain.setAntiAlias(true);

        mTextPain = new Paint();
        mTextPain.setColor(mProgress_text_color);
        mTextPain.setAntiAlias(true);
        mTextPain.setTextSize(mProgress_text_size);
        mTextPain.setTextAlign(Paint.Align.CENTER);
        mTextPain.setTextSize(mProgress_text_size);
    }

    public RobotProgressBar(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        Log.d(TAG, "HProgressBar: 3");
    }

    public RobotProgressBar(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        Log.d(TAG, "HProgressBar: 4");
    }

    private void initDefStyleAttr(AttributeSet attrs) {

        final TypedArray attributes = getContext().obtainStyledAttributes(attrs, R.styleable.HProgress);
        mProgress_outline_color = attributes.getColor(R.styleable.HProgress_hProgress_outline_color, mProgress_outline_color);
        mProgress_color = attributes.getColor(R.styleable.HProgress_hProgress_color, mProgress_color);
        mProgress_circle_color = attributes.getColor(R.styleable.HProgress_hProgress_circle_color, mProgress_circle_color);
        mProgress_text_color = attributes.getColor(R.styleable.HProgress_hProgress_text_color, mProgress_text_color);
        mProgress_text_bg_color = attributes.getColor(R.styleable.HProgress_hProgress_text_bg_color, mProgress_text_bg_color);
        mProgress_circle_height = (int) attributes.getDimension(R.styleable.HProgress_hProgress_circle_height, mProgress_circle_height);
        mProgress_height = (int) attributes.getDimension(R.styleable.HProgress_hProgress_height, mProgress_height);
        mProgress_bar_height = (int) attributes.getDimension(R.styleable.HProgress_hProgress_bar_height, mProgress_bar_height);
        mProgress_text_height = (int) attributes.getDimension(R.styleable.HProgress_hProgress_text_height, mProgress_text_height);
        mProgress_text_size = (int) attributes.getDimension(R.styleable.HProgress_hProgress_text_size, mProgress_text_size);
        mProgress_text_paddingH = (int) attributes.getDimension(R.styleable.HProgress_hProgress_text_paddingH, mProgress_text_paddingH);
        mProgress_text_paddingV = (int) attributes.getDimension(R.styleable.HProgress_hProgress_text_paddingV, mProgress_text_paddingV);
        mProgress_progress_bar = attributes.getInteger(R.styleable.HProgress_hProgress_progress_bar, (int)mProgress_progress_bar);
        mMaxProgress = attributes.getInteger(R.styleable.HProgress_hProgress_maxProgress, (int) mMaxProgress);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        mProgressWidth = MeasureSpec.getSize(widthMeasureSpec);
        setMeasuredDimension(mProgressWidth, (int )(mProgress_text_height + mProgress_circle_height));
    }

    @SuppressLint("ResourceAsColor")
    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        Rect rect = new Rect();
        mTextPain.getTextBounds(String.valueOf(getProgress()), 0, String.valueOf(getProgress()) .length(),rect);
        int w = rect.width();
        int h = rect.height();

        float spaceA = (mProgress_circle_height - mProgress_height) / 2;
        float spaceB = (mProgress_height - mProgress_bar_height) / 2;
        float progressMargin = mProgress_circle_height / 2;
        //长度变化范围等于控件长度减去两边的边距和空隙，由于是圆角进度条，还要减去内进度条的高度，即内进度条两边半圆的长度
        float changeRange = mProgressWidth - (progressMargin + spaceB) * 2  -  mProgress_bar_height;
        float startPoint = progressMargin + spaceB;
        float circleX = startPoint + mProgress_bar_height / 2  + changeRange *  getProgress() / mMaxProgress;

        float textStartX;
        float textEndX;
        if (circleX - w / 2 - mProgress_text_paddingH < 0) {
            textStartX = 0;
            textEndX = textStartX + w + mProgress_text_paddingH * 2;
        } else if (circleX + w / 2 + mProgress_text_paddingH >= mProgressWidth) {
            textStartX = mProgressWidth - mProgress_text_paddingH * 2 - w;
            textEndX = mProgressWidth;
        } else {
            textStartX = circleX - w / 2 - mProgress_text_paddingH;
            textEndX = circleX + w / 2 + mProgress_text_paddingH;
        }

        //画数字背景框
        RectF rectF2 = new RectF(textStartX,
                mProgress_circle_height,
                textEndX,
                mProgress_circle_height + mProgress_text_height);
        //canvas.drawRoundRect(rectF2, mProgress_text_height / 2, mProgress_text_height / 2, mTextBgPain);

        //画progressBar 外边
        RectF rectF = new RectF(progressMargin, spaceA,mProgressWidth - progressMargin, mProgress_height + spaceA);
        canvas.drawRoundRect(rectF, mProgress_height / 2, mProgress_height / 2, mOutLinePaint);

        //画progressBar 内进度条
        RectF rectF1 = new RectF(startPoint,
                spaceA + spaceB,
                startPoint + mProgress_bar_height + changeRange * getProgress()/mMaxProgress,
                mProgress_bar_height + spaceB + spaceA);
        canvas.drawRoundRect(rectF1, mProgress_bar_height / 2, mProgress_bar_height / 2, mProgressPaint);

        //画圆
        canvas.drawCircle(circleX,
                mProgress_circle_height / 2,
                mProgress_circle_height / 2, mCirClePaint);

        //画数字进度
        mTextPain.setColor(R.color.colorAccent);
        Paint.FontMetrics fontMetrics = mTextPain.getFontMetrics();
        canvas.drawText((int)getProgress() + "",
                (textStartX + textEndX) / 2,
                mProgress_circle_height + mProgress_text_height / 2 + (fontMetrics.descent - fontMetrics.ascent) / 2 - fontMetrics.descent-45,
                mTextPain);
    }

    public int getProgress() {
        return (int)mProgress_progress_bar;
    }

    public void setProgress(int progress) {
        if (progress > mMaxProgress) {
            throw new RuntimeException("progress mast less than  mMaxProgress");
        }
        mProgress_progress_bar = progress;
        postInvalidate();
    }

    public void setCenterColor(int color)
    {
        mProgress_circle_color=color;
        mCirClePaint = new Paint();
        mCirClePaint.setColor(mProgress_circle_color);
        mCirClePaint.setAntiAlias(true);
        postInvalidate();
    }

    public void setmMaxProgress(int maxProgress) {
        this.mMaxProgress = maxProgress;
    }
}