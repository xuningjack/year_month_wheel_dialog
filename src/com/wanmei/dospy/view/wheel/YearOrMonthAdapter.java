package com.wanmei.dospy.view.wheel;

import java.util.List;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;

/**
 * 显示年月的双滚轮
 */
public class YearOrMonthAdapter extends ArrayWheelAdapter<String> {

	public YearOrMonthAdapter(Context context, List<String> items, int itemResource) {
		super(context, items, itemResource);
	}

	@Override
	public View getItem(int index, View convertView, ViewGroup parent) {
		return super.getItem(index, convertView, parent);
	}
}