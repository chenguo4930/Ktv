package com.example.lrcview;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.media.MediaPlayer;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;

import com.example.lrcview.bean.LrcBean;
import com.example.lrcview.util.LrcUtil;

import java.util.List;

/**
 * @author 程果
 */
public class LrcView extends View {

    public static final int MODE_NORMAL = 0;
    public static final int MODE_KTV = 1;
    /**
     * 每行歌词的高度
     */
    public static final int LRC_ITEM_HEIGHT = 80;

    private List<LrcBean> list;
    private Paint gPaint;
    private Paint hPaint;
    private int width = 0, height = 0;
    private int currentPosition = 0;
    private MediaPlayer player;
    private int lastPosition = 0;
    private int highLineColor;
    private int lrcColor;
    private int mode = MODE_NORMAL;
    /**
     * 歌词的高度
     */
    private int mLrcHeight;
    /**
     * 触摸初始位置Y
     */
    private int mTouchY;
    private int mCurrentY;
    /**
     * 是否是游戏者
     * 游戏者：可以不可以手动滑动，动态高亮显示歌词
     * 观众：可以手动滑动，不动态高亮显示歌词
     */
    private boolean mIsPlayer;

    public void setHighLineColor(int highLineColor) {
        this.highLineColor = highLineColor;
    }

    public void setLrcColor(int lrcColor) {
        this.lrcColor = lrcColor;
    }

    public void setMode(int mode) {
        this.mode = mode;
    }

    public void setPlayer(MediaPlayer player) {
        this.player = player;
    }

    /**
     * 标准歌词字符串
     *
     * @param lrc 歌词字符串
     */
    public void setLrc(String lrc) {
        list = LrcUtil.parseStr2List(lrc);
        mLrcHeight = (list.size() + 1) * LRC_ITEM_HEIGHT;
    }

    /**
     * @param isPlayer 是不是主播端
     */
    public void setIsPlayer(boolean isPlayer) {
        mIsPlayer = isPlayer;
    }

    public LrcView(Context context) {
        this(context, null);
    }

    public LrcView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public LrcView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);

        TypedArray ta = context.obtainStyledAttributes(attrs, R.styleable.LrcView);
        highLineColor = ta.getColor(R.styleable.LrcView_hignLineColor, getResources().getColor(R.color.green));
        lrcColor = ta.getColor(R.styleable.LrcView_lrcColor, getResources().getColor(android.R.color.darker_gray));
        mode = ta.getInt(R.styleable.LrcView_lrcMode, mode);
        ta.recycle();

        gPaint = new Paint();
        gPaint.setAntiAlias(true);
        gPaint.setColor(lrcColor);
        gPaint.setTextSize(36);
        //设置文字的基准线为居中，X Y 是文字中间基准线的位置
        gPaint.setTextAlign(Paint.Align.CENTER);

        hPaint = new Paint();
        hPaint.setAntiAlias(true);
        hPaint.setColor(highLineColor);
        hPaint.setTextSize(36);
        hPaint.setTextAlign(Paint.Align.CENTER);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        if (width == 0 || height == 0) {
            width = getMeasuredWidth();
            height = getMeasuredHeight();
        }
        if (list == null || list.size() == 0) {
            canvas.drawText("暂无歌词", width / 2, height / 2, gPaint);
            return;
        }

        getCurrentPosition();

        int currentMillis = player.getCurrentPosition();
        drawLrc2(canvas, currentMillis);
        if (mIsPlayer) {
            long start = list.get(currentPosition).getStart();

            float v = (currentMillis - start) > 500 ? currentPosition * LRC_ITEM_HEIGHT : lastPosition * LRC_ITEM_HEIGHT + (currentPosition - lastPosition) * LRC_ITEM_HEIGHT * ((currentMillis - start) / 500f);
            setScrollY((int) v);
            if (getScrollY() == currentPosition * LRC_ITEM_HEIGHT) {
                lastPosition = currentPosition;
            }
            postInvalidateDelayed(100);
        }
    }

    private void drawLrc2(Canvas canvas, int currentMillis) {
        if (mode == 0) {
            for (int i = 0; i < list.size(); i++) {
                if (mIsPlayer) {
                    if (i == currentPosition) {
                        canvas.drawText(list.get(i).getLrc(), width / 2, height / 2 + LRC_ITEM_HEIGHT * i, hPaint);
                    } else {
                        canvas.drawText(list.get(i).getLrc(), width / 2, height / 2 + LRC_ITEM_HEIGHT * i, gPaint);
                    }
                } else {
                    canvas.drawText(list.get(i).getLrc(), width / 2, 100 + LRC_ITEM_HEIGHT * i, gPaint);
                }
            }
        } else {
            for (int i = 0; i < list.size(); i++) {
                canvas.drawText(list.get(i).getLrc(), width / 2, height / 2 + LRC_ITEM_HEIGHT * i, gPaint);
            }
            String highLineLrc = list.get(currentPosition).getLrc();
            int highLineWidth = (int) gPaint.measureText(highLineLrc);
            int leftOffset = (width - highLineWidth) / 2;
            LrcBean lrcBean = list.get(currentPosition);
            long start = lrcBean.getStart();
            long end = lrcBean.getEnd();
            int i = (int) ((currentMillis - start) * 1.0f / (end - start) * highLineWidth);
            if (i > 0) {
                Bitmap textBitmap = Bitmap.createBitmap(i, LRC_ITEM_HEIGHT, Bitmap.Config.ARGB_8888);
                Canvas textCanvas = new Canvas(textBitmap);
                textCanvas.drawText(highLineLrc, highLineWidth / 2, LRC_ITEM_HEIGHT, hPaint);
                canvas.drawBitmap(textBitmap, leftOffset, height / 2 + LRC_ITEM_HEIGHT * (currentPosition - 1), null);
            }
        }
    }

    public void init() {
        currentPosition = 0;
        lastPosition = 0;
        setScrollY(0);
        invalidate();
    }

    private void getCurrentPosition() {
        try {
            int currentMillis = player.getCurrentPosition();
            if (currentMillis < list.get(0).getStart()) {
                currentPosition = 0;
                return;
            }
            if (currentMillis > list.get(list.size() - 1).getStart()) {
                currentPosition = list.size() - 1;
                return;
            }
            for (int i = 0; i < list.size(); i++) {
                if (currentMillis >= list.get(i).getStart() && currentMillis < list.get(i).getEnd()) {
                    currentPosition = i;
                    return;
                }
            }
        } catch (Exception e) {
            postInvalidateDelayed(100);
        }
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (mIsPlayer || getMeasuredHeight() > mLrcHeight) {
            return super.onTouchEvent(event);
        }
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                mTouchY = (int) event.getY();
                mCurrentY = getScrollY();
                break;
            case MotionEvent.ACTION_MOVE:
                int moveY = (int) (event.getY() - mTouchY);
                int y = mCurrentY - moveY;
                if (y <= mLrcHeight - getMeasuredHeight() && y >= 0) {
                    setScrollY(y);
                    Log.e("---", "-----y=" + y);
                }
                Log.e("---", "------ getScrollY()=" + getScrollY());
                Log.e("---", "------ getMeasuredHeight()=" + getMeasuredHeight());
                return true;
            case MotionEvent.ACTION_UP:
                break;
            default:
        }
        return true;
    }
}
