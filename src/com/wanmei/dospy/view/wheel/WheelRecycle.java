package com.wanmei.dospy.view.wheel;

import android.view.View;
import android.widget.LinearLayout;

import java.util.LinkedList;
import java.util.List;

/**
 * Recycle stores mWheelView mItems to reuse.
 */
public final class WheelRecycle {
	private WheelView mWheelView;
	// Cached mItems
	private List<View> mItems;
	// Cached empty mItems
	private List<View> mEmptyItems;

	public WheelRecycle(WheelView wheel) {
		mWheelView = wheel;
	}

	public void clearAll() {
		if (mItems != null) {
			mItems.clear();
		}
		if (mEmptyItems != null) {
			mEmptyItems.clear();
		}
	}


	/**
	 * Recycles mItems from specified layout.
	 * There are saved only mItems not included to specified range.
	 * All the cached mItems are removed from original layout.
	 *
	 * @param layout    the layout containing mItems to be cached
	 * @param firstItem the number of first item in layout
	 * @param range     the range of current mWheelView mItems
	 * @return the new value of first item number
	 */
	public int recycleItems(LinearLayout layout, int firstItem, ItemsRange range) {
		int index = firstItem;
		for (int i = 0; i < layout.getChildCount(); ) {
			if (!range.contains(index)) {
				recycleView(layout.getChildAt(i), index);
				layout.removeViewAt(i);
				if (i == 0) { // first item
					firstItem++;
				}
			} else {
				i++; // go to next item
			}
			index++;
		}
		return firstItem;
	}

	/**
	 * Gets item view
	 *
	 * @return the cached view
	 */
	public View getItem() {
		return getCachedView(mItems);
	}

	/**
	 * Gets empty item view
	 *
	 * @return the cached empty view
	 */
	public View getEmptyItem() {
		return getCachedView(mEmptyItems);
	}


	/**
	 * Adds view to specified cache. Creates a cache list if it is null.
	 *
	 * @param view  the view to be cached
	 * @param cache the cache list
	 * @return the cache list
	 */
	private List<View> addView(View view, List<View> cache) {
		if (cache == null) {
			cache = new LinkedList<View>();
		}

		cache.add(view);
		return cache;
	}

	/**
	 * Adds view to cache. Determines view type (item view or empty one) by index.
	 *
	 * @param view  the view to be cached
	 * @param index the index of view
	 */
	private void recycleView(View view, int index) {
		int count = mWheelView.getViewAdapter().getItemsCount();

		if ((index < 0 || index >= count) && !mWheelView.isCyclic()) {
			// empty view
			mEmptyItems = addView(view, mEmptyItems);
		} else {
			while (index < 0) {
				index = count + index;
			}
			index %= count;
			mItems = addView(view, mItems);
		}
	}

	/**
	 * Gets view from specified cache.
	 *
	 * @param cache the cache
	 * @return the first view from cache.
	 */
	private View getCachedView(List<View> cache) {
		if (cache != null && cache.size() > 0) {
			View view = cache.get(0);
			cache.remove(0);
			return view;
		}
		return null;
	}

}
