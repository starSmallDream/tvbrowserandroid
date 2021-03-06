/*
 * TV-Browser for Android
 * Copyright (C) 2013 René Mach (rene@tvbrowser.org)
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy of this software
 * and associated documentation files (the "Software"), to use, copy, modify or merge the Software,
 * furthermore to publish and distribute the Software free of charge without modifications and to
 * permit persons to whom the Software is furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES
 * OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NON-INFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE
 * LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR
 * IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */
package org.tvbrowser.view;

import android.content.Context;
import android.graphics.Canvas;
import android.util.AttributeSet;

import org.tvbrowser.tvbrowser.R;

import java.util.Calendar;
import java.util.List;

public class TimeBlockProgramTableLayout extends ProgramTableLayout {
  private final int[] mBlockHeights;
  private final int[] mBlockCumulatedHeights;
  private final int mBlockSize;
  private final Calendar mCurrentShownDay;
  
  private final boolean mGrowToBlock;

  /** View constructors for XML inflation (used by tools) */
  @SuppressWarnings("PointlessArithmeticExpression")
  public TimeBlockProgramTableLayout(Context context, AttributeSet attributeSet, int defStyleAttr) {
    super(context, attributeSet, defStyleAttr);
    mGrowToBlock = true;
    mBlockHeights = new int[(ProgramTableLayoutConstants.HOURS/200) + (ProgramTableLayoutConstants.HOURS % 200 > 0 ? 1 : 0)];
    mBlockCumulatedHeights = new int[mBlockHeights.length];
    mBlockSize = 200;
    mCurrentShownDay = Calendar.getInstance();
  }

  public TimeBlockProgramTableLayout(Context context, final List<Integer> channelIDsOrdered, int blockSize, final Calendar day, boolean growToBlock) {
    super(context, channelIDsOrdered);
    mGrowToBlock = growToBlock;

    mBlockHeights = new int[(ProgramTableLayoutConstants.HOURS/blockSize) + (ProgramTableLayoutConstants.HOURS % blockSize > 0 ? 1 : 0)];
    mBlockCumulatedHeights = new int[mBlockHeights.length];
    mBlockSize = blockSize;
    mCurrentShownDay = day;
  }

  @Override
  protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
    int[][] blockHeightCalc = new int[mBlockHeights.length][getColumnCount()];
    int[][] blockProgCount = new int[mBlockHeights.length][getColumnCount()];
    
    int widthSpec = MeasureSpec.makeMeasureSpec(ProgramTableLayoutConstants.COLUMN_WIDTH, MeasureSpec.EXACTLY);
    
    for(int i = 0; i < getChildCount(); i++) {
      ProgramPanel progPanel = (ProgramPanel)getChildAt(i);
      
      int sortIndex = getIndexForChannelID(progPanel.getChannelID());
      int block = progPanel.getStartHour(mCurrentShownDay) / mBlockSize;
      
      if(block >= 0 && sortIndex >= 0 && block < blockProgCount.length) {
        progPanel.measure(widthSpec, 0);
        blockHeightCalc[block][sortIndex] += progPanel.getMeasuredHeight();
        blockProgCount[block][sortIndex]++;
      }
    }
    
    int height = 0;
    
    for(int block = 0; block < blockHeightCalc.length; block++) {
      int maxBlockHeight = 0;
      
      for(int column : blockHeightCalc[block]) {
        maxBlockHeight = Math.max(column, maxBlockHeight);
      }
      
      height += maxBlockHeight;
      mBlockHeights[block] = maxBlockHeight;
      
      if(block < blockHeightCalc.length) {
        mBlockCumulatedHeights[block] = height;
      }
    }
    
    if(mGrowToBlock) {
      int[][] blockCurrentProgCount = new int[mBlockHeights.length][getColumnCount()];
      
      for(int i = 0; i < getChildCount(); i++) {
        ProgramPanel progPanel = (ProgramPanel)getChildAt(i);
        
        int sortIndex = getIndexForChannelID(progPanel.getChannelID());
        int block = progPanel.getStartHour(mCurrentShownDay) / mBlockSize;
        
        if(block >= 0 && sortIndex >= 0 && block < mBlockHeights.length) {
          int maxBlockHeight = mBlockHeights[block];
          int heightDiff = maxBlockHeight - blockHeightCalc[block][sortIndex];
          int blockProgCountValue = blockProgCount[block][sortIndex];
          
          blockCurrentProgCount[block][sortIndex]++;
          
          int addHeight = heightDiff/blockProgCountValue;
          
          int count = 1;
          
          int endBlock = progPanel.getEndHour(mCurrentShownDay) / mBlockSize;
          
          if(blockCurrentProgCount[block][sortIndex] == blockProgCountValue) {
            while((block + count) < (mBlockHeights.length) && blockProgCount[block + count][sortIndex] == 0 && endBlock > block + count) {
              addHeight += mBlockHeights[block + count++];
            }
            
            if(count == 1) {
              addHeight +=  heightDiff%blockProgCountValue;
            }
          }
          
          int newHeightSpec = MeasureSpec.makeMeasureSpec(progPanel.getMeasuredHeight() + addHeight, MeasureSpec.EXACTLY);
          
          progPanel.measure(widthSpec, newHeightSpec);
        }
      }
    }
    
    setMeasuredDimension(ProgramTableLayoutConstants.ROW_HEADER + ProgramTableLayoutConstants.GAP + (ProgramTableLayoutConstants.COLUMN_WIDTH+ProgramTableLayoutConstants.GAP) * getColumnCount(), height);
  }
  
  @Override
  protected void onLayout(boolean changed, int l, int t, int r, int b) {
    int[][] currentBlockHeight = new int[mBlockHeights.length][getColumnCount()];
    
    for(int i = 0; i < getChildCount(); i++) {
      ProgramPanel progPanel = (ProgramPanel)getChildAt(i);
      
      int sortIndex = getIndexForChannelID(progPanel.getChannelID());
      int block = progPanel.getStartHour(mCurrentShownDay) / mBlockSize;
      
      if(block >= 0 && sortIndex >= 0 && block < currentBlockHeight.length) {
        int x = l + ProgramTableLayoutConstants.ROW_HEADER + ProgramTableLayoutConstants.GAP + sortIndex * (ProgramTableLayoutConstants.COLUMN_WIDTH + ProgramTableLayoutConstants.GAP);
        int y = t + currentBlockHeight[block][sortIndex];
        
        if(block > 0) {
          y += mBlockCumulatedHeights[block-1];
        }
        
        currentBlockHeight[block][sortIndex] += progPanel.getMeasuredHeight();
        
        if(progPanel.getVisibility() != GONE) {
          progPanel.layout(x, y, x + ProgramTableLayoutConstants.COLUMN_WIDTH + ProgramTableLayoutConstants.GAP, y + progPanel.getMeasuredHeight());
        }
      }
    }
  }
  
  @Override
  protected void dispatchDraw(Canvas canvas) {
    for(int i = 0; i < mBlockHeights.length; i++) {
      if(i % 2 == 1) {
        canvas.drawRect(0, mBlockCumulatedHeights[i-1], canvas.getWidth(), mBlockCumulatedHeights[i-1] + mBlockHeights[i], ProgramTableLayoutConstants.BLOCK_PAINT);
      }
      
      int y = ProgramTableLayoutConstants.FONT_SIZE_ASCENT;
      
      if(i > 0) {
        y += mBlockCumulatedHeights[i-1];
      }
      
      int time = i * mBlockSize;
      
      if(time >= 24) {
        time -= 24;
      }

      final String value = getContext().getString(R.string.time_block_time_format, time);
      float length = ProgramTableLayoutConstants.TIME_BLOCK_TIME_PAINT.measureText(value);
      
      canvas.drawText(value, ProgramTableLayoutConstants.ROW_HEADER / 2 - length/2, y, ProgramTableLayoutConstants.TIME_BLOCK_TIME_PAINT);
    }
    
    for(int i = 0; i < getColumnCount(); i++) {
      int x = ProgramTableLayoutConstants.ROW_HEADER + i * (ProgramTableLayoutConstants.COLUMN_WIDTH + ProgramTableLayoutConstants.GAP);
      canvas.drawLine(x, 0, x, canvas.getHeight(), ProgramTableLayoutConstants.LINE_PAINT);
    }
    
    super.dispatchDraw(canvas);
  }
}