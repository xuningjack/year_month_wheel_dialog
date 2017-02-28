package com.wanmei.dospy.view.wheel;

import java.util.List;
import android.content.Context;
import com.example.android_year_month_wheel.R;

/**
 * The simple Array wheel adapter
 * @param <T> the element type
 */
public class ArrayWheelAdapter<T> extends AbstractWheelTextAdapter {

	// items
	private List<T> items;
	
	/**
	 * 构造方法
	 * @param context
	 * @param items 布局文件，如R.layout.item_wheel_view
	 * @param itemResource
	 */
	public ArrayWheelAdapter(Context context, List<T> items, int itemResource) {
		super(context, itemResource, R.id.text);
		this.items = items;
	}

	/**
	 * Constructor
	 * @param context the current context
	 * @param items   the items
	 */
	public ArrayWheelAdapter(Context context, List<T> items) {
		super(context, R.layout.item_wheel_view, R.id.name);
		this.items = items;
	}


	@Override
	public CharSequence getItemText(int index) {
		if (index >= 0 && index < items.size()) {
			T item = items.get(index);
			if (item instanceof CharSequence) {
				return (CharSequence) item;
			}
			return item.toString();
		}
		return null;
	}

	@Override
	public int getItemsCount() {
		return items.size();
	}
}