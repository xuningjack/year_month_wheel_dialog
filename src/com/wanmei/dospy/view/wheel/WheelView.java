package com.wanmei.dospy.view.wheel;

import java.util.LinkedList;
import java.util.List;

import android.content.Context;
import android.database.DataSetObserver;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable;
import android.graphics.drawable.GradientDrawable.Orientation;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup.LayoutParams;
import android.view.animation.Interpolator;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.example.android_year_month_wheel.R;

public final class WheelView extends View {
	private static final int DEF_VISIBLE_ITEMS = 5;
	private int mVisibleItems = DEF_VISIBLE_ITEMS;
	private int[] SHADOWS_COLORS = new int[] { 0xFF111111,
	  0x00AAAAAA, 0x00AAAAAA };
	private int mWheelBackground = R.drawable.wheel_bg_day;
	private int mWheelForeground = R.drawable.wheel_val;
	private Drawable mCenterDrawable;

	private IWheelViewAdapter mViewAdapter;
	private WheelRecycle mRecycle = new WheelRecycle(this);
	private LinearLayout mItemsLayout;
	private int mCurrentItem = 0;
	boolean mIsCyclic = false;

	private List<IWheelScrollListener> mScrollingListeners = new LinkedList<IWheelScrollListener>();
	private List<IWheelChangedListener> mChangingListeners = new LinkedList<IWheelChangedListener>();
	private List<IWheelClickedListener> mClickingListeners = new LinkedList<IWheelClickedListener>();

	private WheelScroller mWheelScroller;
	private boolean mIsScrollingPerformed;
	private boolean mIsItemSizeChange;
	private int mScrollingOffset;
	private int mItemNormalTextSize;
	private int mReduceSizeMultiplePreItem;

	private WheelScroller.IScrollingListener mIScrollingListener = new WheelScroller.IScrollingListener() {
		@Override
		public void onStarted() {
			mIsScrollingPerformed = true;
			notifyScrollingListenersAboutStart();
		}

		@Override
		public void onScroll(int distance) {
			doScroll(distance);
			int height = getHeight();
			if (mScrollingOffset > height) {
				mScrollingOffset = height;
				mWheelScroller.stopScrolling();
			} else if (mScrollingOffset < -height) {
				mScrollingOffset = -height;
				mWheelScroller.stopScrolling();
			}
		}

		@Override
		public void onFinished() {
			if (mIsScrollingPerformed) {
				notifyScrollingListenersAboutEnd();
				mIsScrollingPerformed = false;
			}
			mScrollingOffset = 0;
			invalidate();
		}

		@Override
		public void onJustify() {
			if (Math.abs(mScrollingOffset) > WheelScroller.MIN_DELTA_FOR_SCROLLING) {
				mWheelScroller.scroll(mScrollingOffset, 0);
			}
		}
	};

	public WheelView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		initData(context);
	}

	public WheelView(Context context, AttributeSet attrs) {
		super(context, attrs);
		initData(context);
	}

	public WheelView(Context context) {
		super(context);
		initData(context);
	}

	private void initData(Context context) {
		mWheelScroller = new WheelScroller(context, mIScrollingListener);
	}

	public void setWheelBackground(int resource) {
		mWheelBackground = resource;
		setBackgroundResource(mWheelBackground);
	}

	public void setWheelForeground(int resource) {
		mWheelForeground = resource;
		mCenterDrawable = getContext().getResources().getDrawable(mWheelForeground);
	}

	public void setShadowColor(int start, int middle, int end) {
		SHADOWS_COLORS = new int[] { start, middle, end };
	}

	/**
	 * Sets view adapter. Usually new adapters contain different views, so
	 * it needs to rebuild view by calling measure().
	 *
	 * @param viewAdapter the view adapter
	 */
	public void setViewAdapter(IWheelViewAdapter viewAdapter) {
		if (mViewAdapter != null) {
			mViewAdapter.unregisterDataSetObserver(dataObserver);
		}
		mViewAdapter = viewAdapter;
		if (mViewAdapter != null) {
			mViewAdapter.registerDataSetObserver(dataObserver);
		}
		invalidateWheel(true);
	}

	/**
	 * Invalidates wheel
	 *
	 * @param clearCaches if true then cached views will be clear
	 */
	public void invalidateWheel(boolean clearCaches) {
		if (clearCaches) {
			mRecycle.clearAll();
			if (mItemsLayout != null) {
				mItemsLayout.removeAllViews();
			}
			mScrollingOffset = 0;
		} else if (mItemsLayout != null) {
			// cache all items
			mRecycle.recycleItems(mItemsLayout, firstItem, new ItemsRange());
		}

		invalidate();
	}


	/**
	 * Sets the current item w/o animation. Does nothing when index is wrong.
	 *
	 * @param index the item index
	 */
	public void setCurrentItem(int index) {
		setCurrentItem(index, false);
	}

	/**
	 * Sets the current item. Does nothing when index is wrong.
	 *
	 * @param index    the item index
	 * @param animated the animation flag
	 */
	public void setCurrentItem(int index, boolean animated) {
		if (mViewAdapter == null || mViewAdapter.getItemsCount() == 0) {
			return; // throw?
		}
		int itemCount = mViewAdapter.getItemsCount();
		if (index < 0 || index >= itemCount) {
			if (mIsCyclic) {
				while (index < 0) {
					index += itemCount;
				}
				index %= itemCount;
			} else {
				return; // throw?
			}
		}
		if (index != mCurrentItem) {
			if (animated) {
				int itemsToScroll = index - mCurrentItem;
				if (mIsCyclic) {
					int scroll = itemCount + Math.min(index, mCurrentItem) - Math.max(index, mCurrentItem);
					if (scroll < Math.abs(itemsToScroll)) {
						itemsToScroll = itemsToScroll < 0 ? scroll : -scroll;
					}
				}
				scroll(itemsToScroll, 0);
			} else {
				mScrollingOffset = 0;
				int old = mCurrentItem;
				mCurrentItem = index;
				notifyChangingListeners(old, mCurrentItem);
				invalidate();
			}
		}
	}

	/**
	 * Sets the desired count of visible items.
	 * Actual amount of visible items depends on wheel layout parameters.
	 * To apply changes and rebuild view call measure().
	 *
	 * @param count the desired count for visible items
	 */
	public void setVisibleItems(int count) {
		mVisibleItems = count;
	}


	/**
	 * Notifies listeners about starting scrolling
	 */
	protected void notifyScrollingListenersAboutStart() {
		for (IWheelScrollListener listener : mScrollingListeners) {
			listener.onScrollingStarted(this);
		}
	}

	/**
	 * Top and bottom items offset (to hide that)
	 */
	private static final int ITEM_OFFSET_PERCENT = 0;

	/**
	 * Left and right padding value
	 */
	private static final int PADDING = 10;

	// Item height
	private int itemHeight = 0;


	// Shadows drawables
	private GradientDrawable topShadow;
	private GradientDrawable bottomShadow;

	// Draw Shadows
	private boolean drawShadows = true;

	// The number of first item in layout
	private int firstItem;


	/**
	 * Set the the specified scrolling interpolator
	 * @param interpolator the interpolator
	 */
	public void setInterpolator(Interpolator interpolator) {
		mWheelScroller.setInterpolator(interpolator);
	}

	/**
	 * Gets view adapter
	 * @return the view adapter
	 */
	public IWheelViewAdapter getViewAdapter() {
		return mViewAdapter;
	}

	// Adapter listener
	private DataSetObserver dataObserver = new DataSetObserver() {
		@Override
		public void onChanged() {
			invalidateWheel(false);
		}

		@Override
		public void onInvalidated() {
			invalidateWheel(true);
		}
	};


	/**
	 * Adds wheel changing listener
	 * @param listener the listener
	 */
	public void addChangingListener(IWheelChangedListener listener) {
		mChangingListeners.add(listener);
	}

	/**
	 * Removes wheel changing listener
	 * @param listener the listener
	 */
	public void removeChangingListener(IWheelChangedListener listener) {
		mChangingListeners.remove(listener);
	}

	/**
	 * Notifies changing listeners
	 * @param oldValue the old wheel value
	 * @param newValue the new wheel value
	 */
	protected void notifyChangingListeners(int oldValue, int newValue) {
		for (IWheelChangedListener listener : mChangingListeners) {
			listener.onChanged(this, oldValue, newValue);
		}
	}

	/**
	 * Adds wheel scrolling listener
	 * @param listener the listener
	 */
	public void addScrollingListener(IWheelScrollListener listener) {
		mScrollingListeners.add(listener);
	}

	/**
	 * Removes wheel scrolling listener
	 * @param listener the listener
	 */
	public void removeScrollingListener(IWheelScrollListener listener) {
		mScrollingListeners.remove(listener);
	}

	/**
	 * Notifies listeners about ending scrolling
	 */
	protected void notifyScrollingListenersAboutEnd() {
		for (IWheelScrollListener listener : mScrollingListeners) {
			listener.onScrollingFinished(this);
		}
	}

	/**
	 * Adds wheel clicking listener
	 * @param listener the listener
	 */
	public void addClickingListener(IWheelClickedListener listener) {
		mClickingListeners.add(listener);
	}

	/**
	 * Removes wheel clicking listener
	 * @param listener the listener
	 */
	public void removeClickingListener(IWheelClickedListener listener) {
		mClickingListeners.remove(listener);
	}

	/**
	 * Notifies listeners about clicking
	 */
	protected void notifyClickListenersAboutClick(int item) {
		for (IWheelClickedListener listener : mClickingListeners) {
			listener.onItemClicked(this, item);
		}
	}


	/**
	 * Tests if wheel is cyclic. That means before the 1st item there is shown the last one
	 * @return true if wheel is cyclic
	 */
	public boolean isCyclic() {
		return mIsCyclic;
	}

	/**
	 * Set wheel cyclic flag
	 * @param isCyclic the flag to set
	 */
	public void setCyclic(boolean isCyclic) {
		this.mIsCyclic = isCyclic;
		invalidateWheel(false);
	}


	/**
	 * Initializes resources
	 */
	private void initResourcesIfNecessary() {
		if (mCenterDrawable == null) {
			mCenterDrawable = getContext().getResources().getDrawable(mWheelForeground);
		}

		if (topShadow == null) {
			topShadow = new GradientDrawable(Orientation.TOP_BOTTOM, SHADOWS_COLORS);
		}

		if (bottomShadow == null) {
			bottomShadow = new GradientDrawable(Orientation.BOTTOM_TOP, SHADOWS_COLORS);
		}

		setBackgroundResource(mWheelBackground);
	}

	/**
	 * Calculates desired height for layout
	 * @param layout the source layout
	 * @return the desired layout height
	 */
	private int getDesiredHeight(LinearLayout layout) {
		if (layout != null && layout.getChildAt(0) != null) {
			itemHeight = layout.getChildAt(0).getMeasuredHeight();
		}

		int desired = itemHeight * mVisibleItems - itemHeight * ITEM_OFFSET_PERCENT / 50;

		return Math.max(desired, getSuggestedMinimumHeight());
	}

	/**
	 * Returns height of wheel item
	 * @return the item height
	 */
	private int getItemHeight() {
		if (itemHeight != 0) {
			return itemHeight;
		}

		if (mItemsLayout != null && mItemsLayout.getChildAt(0) != null) {
			itemHeight = mItemsLayout.getChildAt(0).getHeight();
			return itemHeight;
		}

		return getHeight() / mVisibleItems;
	}

	/**
	 * Calculates control width and creates text layouts
	 * @param widthSize the input layout width
	 * @param mode      the layout mode
	 * @return the calculated control width
	 */
	private int calculateLayoutWidth(int widthSize, int mode) {
		initResourcesIfNecessary();

		// TODO: make it static
		mItemsLayout.setLayoutParams(new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT));
		mItemsLayout.measure(MeasureSpec.makeMeasureSpec(widthSize, MeasureSpec.UNSPECIFIED),
		  MeasureSpec.makeMeasureSpec(0, MeasureSpec.UNSPECIFIED));
		int width = mItemsLayout.getMeasuredWidth();

		if (mode == MeasureSpec.EXACTLY) {
			width = widthSize;
		} else {
			width += 2 * PADDING;

			// Check against our minimum width
			width = Math.max(width, getSuggestedMinimumWidth());

			if (mode == MeasureSpec.AT_MOST && widthSize < width) {
				width = widthSize;
			}
		}

		mItemsLayout.measure(MeasureSpec.makeMeasureSpec(width - 2 * PADDING, MeasureSpec.EXACTLY),
		  MeasureSpec.makeMeasureSpec(0, MeasureSpec.UNSPECIFIED));

		return width;
	}

	@Override
	protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
		int widthMode = MeasureSpec.getMode(widthMeasureSpec);
		int heightMode = MeasureSpec.getMode(heightMeasureSpec);
		int widthSize = MeasureSpec.getSize(widthMeasureSpec);
		int heightSize = MeasureSpec.getSize(heightMeasureSpec);

		buildViewForMeasuring();

		int width = calculateLayoutWidth(widthSize, widthMode);

		int height;
		if (heightMode == MeasureSpec.EXACTLY) {
			height = heightSize;
		} else {
			height = getDesiredHeight(mItemsLayout);

			if (heightMode == MeasureSpec.AT_MOST) {
				height = Math.min(height, heightSize);
			}
		}

		setMeasuredDimension(width, height);
	}

	@Override
	protected void onLayout(boolean changed, int l, int t, int r, int b) {
		layout(r - l, b - t);
	}

	/**
	 * Sets layouts width and height
	 *
	 * @param width  the layout width
	 * @param height the layout height
	 */
	private void layout(int width, int height) {
		int itemsWidth = width - 2 * PADDING;

		mItemsLayout.layout(0, 0, itemsWidth, height);
	}

	@Override
	protected void onDraw(Canvas canvas) {
		super.onDraw(canvas);
		if (mViewAdapter != null && mViewAdapter.getItemsCount() > 0) {
			updateView();
			drawCenterRect(canvas);
			drawItems(canvas);
		}
		if (drawShadows) {
			drawShadows(canvas);
		}
	}

	/**
	 * Draws shadows on top and bottom of control
	 *
	 * @param canvas the canvas for drawing
	 */
	private void drawShadows(Canvas canvas) {
		int height = (int) (2.0 * getItemHeight());
		topShadow.setBounds(0, 0, getWidth(), height);
		topShadow.draw(canvas);

		bottomShadow.setBounds(0, getHeight() - height, getWidth(), getHeight());
		bottomShadow.draw(canvas);
	}

	/**
	 * Draws items
	 *
	 * @param canvas the canvas for drawing
	 */
	private void drawItems(Canvas canvas) {
		canvas.save();

		int top = (mCurrentItem - firstItem) * getItemHeight() + (getItemHeight() - getHeight()) / 2;
		canvas.translate(PADDING, -top + mScrollingOffset);
		mItemsLayout.draw(canvas);
		canvas.restore();
	}

	/**
	 * Draws rect for current value
	 *
	 * @param canvas the canvas for drawing
	 */
	private void drawCenterRect(Canvas canvas) {
		int center = getHeight() / 2;
		int offset = (int) (getItemHeight() / 2 * 1.2);
		mCenterDrawable.setBounds(0, center - offset, getWidth(), center + offset);
		mCenterDrawable.draw(canvas);
	}

	@Override
	public boolean onTouchEvent(MotionEvent event) {
		if (!isEnabled() || getViewAdapter() == null) {
			return true;
		}

		switch (event.getAction()) {
		case MotionEvent.ACTION_MOVE:
			if (getParent() != null) {
				getParent().requestDisallowInterceptTouchEvent(true);
			}
			break;

		case MotionEvent.ACTION_UP:
			//滑动判定
			if (!mIsScrollingPerformed) {
				int distance = (int) event.getY() - getHeight() / 2;
				if (distance > 0) {
					distance += getItemHeight() / 2;
				} else {
					distance -= getItemHeight() / 2;
				}
				int items = distance / getItemHeight();
				if (isValidItemIndex(mCurrentItem + items)) {
					notifyClickListenersAboutClick(mCurrentItem + items);
				}
			}
			break;
		}

		return mWheelScroller.onTouchEvent(event);
	}

	/**
	 * Scrolls the wheel
	 *
	 * @param delta the scrolling value
	 */
	private void doScroll(int delta) {
		mScrollingOffset += delta;

		int itemHeight = getItemHeight();
		int count = mScrollingOffset / itemHeight;

		int pos = mCurrentItem - count;
		int itemCount = mViewAdapter.getItemsCount();

		int fixPos = mScrollingOffset % itemHeight;
		if (Math.abs(fixPos) <= itemHeight / 2) {
			fixPos = 0;
		}
		if (mIsCyclic && itemCount > 0) {
			if (fixPos > 0) {
				pos--;
				count++;
			} else if (fixPos < 0) {
				pos++;
				count--;
			}
			// fix position by rotating
			while (pos < 0) {
				pos += itemCount;
			}
			pos %= itemCount;
		} else {
			//
			if (pos < 0) {
				count = mCurrentItem;
				pos = 0;
			} else if (pos >= itemCount) {
				count = mCurrentItem - itemCount + 1;
				pos = itemCount - 1;
			} else if (pos > 0 && fixPos > 0) {
				pos--;
				count++;
			} else if (pos < itemCount - 1 && fixPos < 0) {
				pos++;
				count--;
			}
		}

		int offset = mScrollingOffset;
		if (pos != mCurrentItem) {
			setCurrentItem(pos, false);
		} else {
			invalidate();
		}

		// update offset
		mScrollingOffset = offset - count * itemHeight;
		if (mScrollingOffset > getHeight()) {
			if (getHeight() <= 0) {
				mScrollingOffset = 0;
			} else {
				mScrollingOffset = mScrollingOffset % getHeight() + getHeight();
			}
		}
	}

	public void scroll(int itemsToScroll, int time) {
		int distance = itemsToScroll * getItemHeight() - mScrollingOffset;
		mWheelScroller.scroll(distance, time);
	}

	/**
	 * Calculates range for wheel items
	 *
	 * @return the items range
	 */
	private ItemsRange getItemsRange() {
		if (getItemHeight() == 0) {
			return null;
		}

		int first = mCurrentItem;
		int count = 1;

		while (count * getItemHeight() < getHeight()) {
			first--;
			count += 2; // top + bottom items
		}

		if (mScrollingOffset != 0) {
			if (mScrollingOffset > 0) {
				first--;
			}
			count++;

			// process empty items above the first or below the second
			int emptyItems = mScrollingOffset / getItemHeight();
			first -= emptyItems;
			count += Math.asin(emptyItems);
		}
		return new ItemsRange(first, count);
	}

	/**
	 * Rebuilds wheel items if necessary. Caches all unused items.
	 *
	 * @return true if items are rebuilt
	 */
	private boolean rebuildItems() {
		boolean updated = false;
		ItemsRange range = getItemsRange();
		if (mItemsLayout != null) {
			int first = mRecycle.recycleItems(mItemsLayout, firstItem, range);
			updated = firstItem != first;
			firstItem = first;
		} else {
			createItemsLayout();
			updated = true;
		}

		if (!updated) {
			updated = firstItem != range.getFirst() || mItemsLayout.getChildCount() != range.getCount();
		}

		if (firstItem > range.getFirst() && firstItem <= range.getLast()) {
			for (int i = firstItem - 1; i >= range.getFirst(); i--) {
				if (!addViewItem(i, true)) {
					break;
				}
				firstItem = i;
			}
		} else {
			firstItem = range.getFirst();
		}

		int first = firstItem;
		for (int i = mItemsLayout.getChildCount(); i < range.getCount(); i++) {
			if (!addViewItem(firstItem + i, false) && mItemsLayout.getChildCount() == 0) {
				first++;
			}
		}
		firstItem = first;
		if(mIsItemSizeChange) {
			for(int i = 0; i < mItemsLayout.getChildCount(); i++) {
				View view = mItemsLayout.getChildAt(i);
				int size = Math.abs((mCurrentItem - firstItem) - i) * mReduceSizeMultiplePreItem;
				TextView textView = mViewAdapter.getTextView(view, mViewAdapter.getTextViewItemResourceId());
				textView.setTextSize(mItemNormalTextSize - size);
				int alpha = 255 - Math.abs((mCurrentItem - firstItem) - i) * 80;
//				if(AppConfiguration.getInstance(getContext()).isNightMode()){
//					textView.setTextColor(Color.argb(alpha, 255, 255, 255));   //设置文本的透明度
//				}else{
					textView.setTextColor(Color.argb(alpha, 0, 0, 0));
//				}
			}
		}

		return updated;
	}

	/**
	 * Updates view. Rebuilds items and label if necessary, recalculate items sizes.
	 */
	private void updateView() {
		if (rebuildItems()) {
			calculateLayoutWidth(getWidth(), MeasureSpec.EXACTLY);
			layout(getWidth(), getHeight());
		}
	}

	/**
	 * Creates item layouts if necessary
	 */
	private void createItemsLayout() {
		if (mItemsLayout == null) {
			mItemsLayout = new LinearLayout(getContext());
			mItemsLayout.setOrientation(LinearLayout.VERTICAL);
		}
	}

	/**
	 * Builds view for measuring
	 */
	private void buildViewForMeasuring() {
		// clear all items
		if (mItemsLayout != null) {
			mRecycle.recycleItems(mItemsLayout, firstItem, new ItemsRange());
		} else {
			createItemsLayout();
		}

		// add views
		// all items must be included to measure width correctly
		// PS: only add VisibleItems is more efficient —— 2015.5.20
		for (int i = mVisibleItems - 1; i >= 0; i--) {
			if (addViewItem(i, true)) {
				firstItem = i;
			}
		}
	}

	/**
	 * Adds view for item to items layout
	 *
	 * @param index the item index
	 * @param first the flag indicates if view should be first
	 * @return true if corresponding item exists and is added
	 */
	private boolean addViewItem(int index, boolean first) {
		View view = getItemView(index);
		if (view != null) {
			if (first) {
				mItemsLayout.addView(view, 0);
			} else {
				mItemsLayout.addView(view);
			}

			return true;
		}

		return false;
	}

	/**
	 * Checks whether intem index is valid
	 *
	 * @param index the item index
	 * @return true if item index is not out of bounds or the wheel is cyclic
	 */
	private boolean isValidItemIndex(int index) {
		return mViewAdapter != null && mViewAdapter.getItemsCount() > 0 &&
		  (mIsCyclic || index >= 0 && index < mViewAdapter.getItemsCount());
	}

	/**
	 * Returns view for specified item
	 *
	 * @param index the item index
	 * @return item view or empty view if index is out of bounds
	 */
	private View getItemView(int index) {
		if (mViewAdapter == null || mViewAdapter.getItemsCount() == 0) {
			return null;
		}
		int count = mViewAdapter.getItemsCount();
		if (!isValidItemIndex(index)) {
			return mViewAdapter.getEmptyItem(mRecycle.getEmptyItem(), mItemsLayout);
		} else {
			while (index < 0) {
				index = count + index;
			}
		}

		index %= count;

		View itemView = mViewAdapter.getItem(index, mRecycle.getItem(), mItemsLayout);
		if(mIsItemSizeChange) {
			TextView textView = mViewAdapter.getTextView(itemView, mViewAdapter.getTextViewItemResourceId());
			textView.setTextSize(mItemNormalTextSize);
		}

		return itemView;
	}

	/**
	 * Stops scrolling
	 */
	public void stopScrolling() {
		mWheelScroller.stopScrolling();
	}

	public void setIsItemSizeChange(int normalSize, int reduceSize) {
		this.mIsItemSizeChange = true;
		this.mItemNormalTextSize = normalSize;
		this.mReduceSizeMultiplePreItem = reduceSize;
	}

	public void setDrawShadows(boolean drawShadows) {
		this.drawShadows = drawShadows;
	}
	
	/**
	 * 获取当前的item
	 * @return
	 */
	public int getCurrentItem() {
		return mCurrentItem;
	}
}