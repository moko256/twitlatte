package com.github.moko256.twicalico;

import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import android.widget.GridLayout;
import android.widget.ImageView;

/**
 * Created by moko256 on 2016/06/11.
 *
 * @author moko256
 */
public class TweetImageTableView extends GridLayout {

    private ImageView imageViews[];
    private int imageNum=1;

    /* {row,column,rowSpan,colSpan} */
    private final int params[][][]={
            {{0,0,2,2},{},{},{}},
            {{0,0,2,1},{0,1,2,1},{},{}},
            {{0,0,2,1},{0,1,1,1},{1,1,1,1},{}},
            {{0,0,1,1},{0,1,1,1},{1,0,1,1},{1,1,1,1}}
    };

    public TweetImageTableView(Context context) {
        this(context, null);
    }

    public TweetImageTableView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public TweetImageTableView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);

        final int ids[]={
                R.id.layout_tweet_image_1,
                R.id.layout_tweet_image_2,
                R.id.layout_tweet_image_3,
                R.id.layout_tweet_image_4
        };

        LayoutInflater.from(getContext()).inflate(R.layout.layout_tweet_image_table,this);

        imageViews=new ImageView[4];

        for(int i=0;i<ids.length;i++){
            imageViews[i]=(ImageView) findViewById(ids[i]);
        }
    }

    @Override
    protected void onMeasure(int widthSpec, int heightSpec) {
        int widthSize=MeasureSpec.getSize(widthSpec);
        int heightSize=MeasureSpec.getSize(heightSpec);

        int widthMode=MeasureSpec.getMode(widthSpec);
        int heightMode=MeasureSpec.getMode(heightSpec);

        if (widthMode!=MeasureSpec.EXACTLY){
            widthMode=MeasureSpec.EXACTLY;
            widthSize=heightSize/9*16;
        }

        if (heightMode!=MeasureSpec.EXACTLY){
            heightMode=MeasureSpec.EXACTLY;
            heightSize=widthSize/16*9;
        }

        int measuredWidthSpec=MeasureSpec.makeMeasureSpec(widthSize,widthMode);
        int measuredHeightSpec=MeasureSpec.makeMeasureSpec(heightSize,heightMode);

        setMeasuredDimension(measuredWidthSpec,measuredHeightSpec);

        super.onMeasure(measuredWidthSpec, measuredHeightSpec);
    }

    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {

        for(int i=0;i<4;i++){

            ImageView imageView=imageViews[i];

            if(i<imageNum){

                int param[]=params[imageNum-1][i];

                imageView.setVisibility(VISIBLE);
                imageView.setLayoutParams(makeGridLayoutParams(param[0],param[1],param[2],param[3]));
            }else{
                imageView.setLayoutParams(makeGridLayoutParams(0,0,0,0));
                imageView.setVisibility(GONE);
            }

        }

        super.onLayout(changed, left, top, right, bottom);
    }

    public void setImageNumber(int num){
        imageNum=num;
        forceLayout();
    }

    public int getImageNumber(){
        return imageNum;
    }

    public ImageView getImageView(int i){
        return imageViews[i];
    }

    private LayoutParams makeGridLayoutParams(int row, int column, int rowSpan, int colSpan){
        LayoutParams params=new LayoutParams(new ViewGroup.LayoutParams(getMeasuredWidth()/2*colSpan,getMeasuredHeight()/2*rowSpan));
        params.rowSpec=spec(row,rowSpan);
        params.columnSpec=spec(column,colSpan);
        return params;
    }
}
