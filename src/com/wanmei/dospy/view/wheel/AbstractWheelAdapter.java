package com.wanmei.dospy.view.wheel;

import android.database.DataSetObserver;
import android.view.View;
import android.view.ViewGroup;

import java.util.LinkedList;
import java.util.List;

public abstract class AbstractWheelAdapter implements IWheelViewAdapter {
	private List<DataSetObserver> mDatasetObservers;

	@Override
	public View getEmptyItem(View convertView, ViewGroup parent) {
		return null;
	}

	@Override
	public void registerDataSetObserver(DataSetObserver observer) {
		if (mDatasetObservers == null) {
			mDatasetObservers = new LinkedList<DataSetObserver>();
		}
		mDatasetObservers.add(observer);
	}

	@Override
	public void unregisterDataSetObserver(DataSetObserver observer) {
		if (mDatasetObservers != null) {
			mDatasetObservers.remove(observer);
		}
	}

	/**
	 * Notifies observers about data changing
	 */
	protected void notifyDataChangedEvent() {
		if (mDatasetObservers != null) {
			for (DataSetObserver observer : mDatasetObservers) {
				observer.onChanged();
			}
		}
	}

	/**
	 * Notifies observers about invalidating data
	 */
	protected void notifyDataInvalidatedEvent() {
		if (mDatasetObservers != null) {
			for (DataSetObserver observer : mDatasetObservers) {
				observer.onInvalidated();
			}
		}
	}
}
