package com.wanmei.dospy.view.wheel;

import android.content.Context;
import android.graphics.Typeface;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

public abstract class AbstractWheelTextAdapter extends AbstractWheelAdapter {
	/**
	 * Text view resource. Used as a default view for adapter.
	 */
	public static final int TEXT_VIEW_ITEM_RESOURCE = -1;
	/**
	 * No resource constant.
	 */
	protected static final int NO_RESOURCE = 0;
	/**
	 * Default text color
	 */
	public static final int DEFAULT_TEXT_COLOR = 0xFF101010;
	/**
	 * Default text size
	 */
	public static final int DEFAULT_TEXT_SIZE = 24;

	protected Context mContext;
	protected int mItemResourceId;
	protected int mItemTextResourceId;
	protected LayoutInflater mInflater;
	protected int mEmptyItemResourceId;

	protected AbstractWheelTextAdapter(Context context) {
		this(context, TEXT_VIEW_ITEM_RESOURCE);
	}

	protected AbstractWheelTextAdapter(Context context, int itemResource) {
		this(context, itemResource, NO_RESOURCE);
	}

	protected AbstractWheelTextAdapter(Context context, int itemResource, int itemTextResource) {
		mContext = context;
		mItemResourceId = itemResource;
		mItemTextResourceId = itemTextResource;

		mInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
	}

	@Override
	public View getItem(int index, View convertView, ViewGroup parent) {
		if (index >= 0 && index < getItemsCount()) {
			if (convertView == null) {
				convertView = getView(mItemResourceId, parent);
			}
			TextView textView = getTextView(convertView, mItemTextResourceId);
			if (textView != null) {
				CharSequence text = getItemText(index);
				textView.setText(text == null ? "" : text);

				if (mItemResourceId == TEXT_VIEW_ITEM_RESOURCE) {
					configureTextView(textView);
				}
			}
			return convertView;
		}
		return null;
	}

	@Override
	public View getEmptyItem(View convertView, ViewGroup parent) {
		if (convertView == null) {
			convertView = getView(mEmptyItemResourceId, parent);
		}
		if (mEmptyItemResourceId == TEXT_VIEW_ITEM_RESOURCE && convertView instanceof TextView) {
			configureTextView((TextView) convertView);
		}

		return convertView;
	}

	/**
	 * Loads view from resources
	 *
	 * @param resource the resource Id
	 * @return the loaded view or null if resource is not set
	 */
	private View getView(int resource, ViewGroup parent) {
		switch (resource) {
			case NO_RESOURCE:
				return null;
			case TEXT_VIEW_ITEM_RESOURCE:
				return new TextView(mContext);
			default:
				return mInflater.inflate(resource, parent, false);
		}
	}

	/**
	 * Loads a text view from view
	 *
	 * @param view         the text view or layout containing it
	 * @param textResource the text resource Id in layout
	 * @return the loaded text view
	 */
	public TextView getTextView(View view, int textResource) {
		TextView text = null;
		try {
			if (textResource == NO_RESOURCE && view instanceof TextView) {
				text = (TextView) view;
			} else if (textResource != NO_RESOURCE) {
				text = (TextView) view.findViewById(textResource);
			}
		} catch (ClassCastException e) {
			throw new IllegalStateException(
					"AbstractWheelAdapter requires the resource ID to be a TextView", e);
		}
		return text;
	}

	/**
	 * Returns text for specified item
	 *
	 * @param index the item index
	 * @return the text of specified items
	 */
	protected abstract CharSequence getItemText(int index);

	/**
	 * Configures text view. Is called for the TEXT_VIEW_ITEM_RESOURCE views.
	 *
	 * @param view the text view to be configured
	 */
	protected void configureTextView(TextView view) {
		//view.setTextColor(DEFAULT_TEXT_COLOR);
		view.setGravity(Gravity.CENTER);
		view.setTextSize(DEFAULT_TEXT_SIZE);
		view.setLines(1);
//		if(AppConfiguration.getInstance(mContext).isNightMode()){
//			view.setTextColor(mContext.getResources().getColor(R.color.white));
//		}else{
		view.setTextColor(mContext.getResources().getColor(android.R.color.black));
//		}
		view.setTypeface(Typeface.SANS_SERIF, Typeface.BOLD);
	}

	@Override
	public int getTextViewItemResourceId() {
		return mItemTextResourceId;
	}
}
