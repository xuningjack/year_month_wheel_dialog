package com.wanmei.dospy.view;

import java.util.ArrayList;
import java.util.List;

import android.app.Activity;
import android.content.Context;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.WindowManager;

import com.example.android_year_month_wheel.R;
import com.wanmei.dospy.view.wheel.ArrayWheelAdapter;
import com.wanmei.dospy.view.wheel.IWheelClickedListener;
import com.wanmei.dospy.view.wheel.IWheelScrollListener;
import com.wanmei.dospy.view.wheel.WheelView;

public class PageHelper {
	
	private static final String TAG = "PageHelper";

	View mPopupWindow;
	private Activity mContext;
	private List<String> pageArray = new ArrayList<String>();
	private int mBottomSize;
	private int mItemHeight;

	private static final int MSG_SHOW = 2;
	private static final int MSG_DISMISS = 4;
	/** 搜索的最起始日期年份 */
	public static final int FROM_YEAR = 1990;
	/** 搜索的最起始日期月份 */
	public static final int MIN_MONTH = 1, MAX_MONTH = 12;

	/**
	 * 选择滚轮item后的回调
	 */
	public interface IPageSelectedListener {
		void onPageSelected(int page);

		void onYearMonthSelected(int year, int month);
	}

	private List<String> mYearArray = new ArrayList<String>(), mMonthArray = new ArrayList<String>();
	private IPageSelectedListener mListener;
	private CustomDialog mPopMenu;
	private WheelView mYearWheelView, mMonthWheelView;
	private int mYearIndex, mMonthIndex;
	/**外部传进来操作滚轮的监听器*/
	private View.OnClickListener mOnClickListener;

	public PageHelper(Activity context, View.OnClickListener listener) {
		mContext = context;
		mOnClickListener = listener;
		initPopMenu();
	}

	/**
	 * 初始化年月的双滚轮
	 */
	private void initPopMenu() {
		mPopMenu = new CustomDialog(mContext).createPopupWindow();
		mPopMenu.setCanceledOnTouchOutside(true);
		final View doubleWheel = LayoutInflater.from(mContext).inflate(R.layout.layout_page_popup_view, null);
		WindowManager.LayoutParams layoutParams = mPopMenu.getWindow().getAttributes();
		layoutParams.gravity = Gravity.BOTTOM;
		layoutParams.width = ViewGroup.LayoutParams.MATCH_PARENT;
		layoutParams.height = ViewGroup.LayoutParams.WRAP_CONTENT;
		layoutParams.windowAnimations = R.style.popmenu_anim_style;
		doubleWheel.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				if(mOnClickListener != null){
					mOnClickListener.onClick(doubleWheel);
				}
			}
		});
		mPopMenu.setContentView(doubleWheel);


		mYearWheelView = (WheelView) doubleWheel.findViewById(R.id.year_page);
		mYearWheelView.setViewAdapter(new YearOrMonthAdapter(mContext, mYearArray, R.layout.item_wheel_view_align_right));
		mYearWheelView.setVisibleItems(5);
		mYearWheelView.setCurrentItem(0);
		mYearWheelView.setShadowColor(0xFF000000, 0x88000000, 0x00000000);
		//可设置是否开启近大远小效果，不写下行代码则为正常展示效果
		mYearWheelView.setIsItemSizeChange(20, 4);//参数1：标准TextSize 参数2：TextSize每远一个Item的缩小倍数
		mYearWheelView.addClickingListener(new IWheelClickedListener() {
			@Override
			public void onItemClicked(WheelView wheel, int itemIndex) {
				mYearIndex = itemIndex;
				selectYearAndMonth(mYearIndex, mMonthIndex);
			}
		});
		mYearWheelView.addScrollingListener(new IWheelScrollListener() {
			@Override
			public void onScrollingStarted(WheelView wheel) {
			}

			@Override
			public void onScrollingFinished(WheelView wheel) {
				mYearIndex = wheel.getCurrentItem();
			}
		});

		mMonthWheelView = (WheelView) doubleWheel.findViewById(R.id.month_page);
		mMonthWheelView.setViewAdapter(new YearOrMonthAdapter(mContext, mMonthArray, R.layout.item_wheel_view_align_left));
		mMonthWheelView.setVisibleItems(5);
		mMonthWheelView.setCurrentItem(0);
		mMonthWheelView.setShadowColor(0xFF000000, 0x88000000, 0x00000000);
		//可设置是否开启近大远小效果，不写下行代码则为正常展示效果
		mMonthWheelView.setIsItemSizeChange(20, 4);//参数1：标准TextSize 参数2：TextSize每远一个Item的缩小倍数
		mMonthWheelView.addClickingListener(new IWheelClickedListener() {
			@Override
			public void onItemClicked(WheelView wheel, int itemIndex) {
				mMonthIndex = itemIndex;
				selectYearAndMonth(mYearIndex, mMonthIndex);
			}
		});
		mMonthWheelView.addScrollingListener(new IWheelScrollListener() {
			@Override
			public void onScrollingStarted(WheelView wheel) {
			}

			@Override
			public void onScrollingFinished(WheelView wheel) {
				mMonthIndex = wheel.getCurrentItem();
			}
		});
	}

	public void selectYearAndMonth(int year, int month) {
		if (mListener != null) {
			mListener.onYearMonthSelected(year, month);
			mPopMenu.dismiss();
		}
	}

	public void setListener(IPageSelectedListener listener) {
		mListener = listener;
	}


	/**
	 * 设置年份选择的范围
	 * @param from
	 * @param to
	 */
	public void setYearArrange(int from, int to) {
		mYearArray.clear();
		for (int i = from; i <= to; i++) {
			String prompt = mContext.getString(R.string.year_prompt, i);
			mYearArray.add(prompt);
		}
	}

	/**
	 * 设置月份选择的范围
	 * @param from
	 * @param to
	 */
	public void setMonthArrange(int from, int to) {
		mMonthArray.clear();
		for (int i = from; i <= to; i++) {
			String prompt = mContext.getString(R.string.month_prompt, i);
			mMonthArray.add(prompt);
		}
	}


	/**
	 * 显示弹出滚轮
	 * @param currentPageNum
	 */
	public void showPopup(int currentPageNum) {
	}

	/**
	 * 显示弹出滚轮
	 *
	 * @param year  年份
	 * @param month 月份
	 */
	public void showPopup(int year, int month) {
		if (mYearArray.size() <= 0 || mMonthArray.size() <= 0) {
			return;
		}
		//显示年份滚轮
		mYearWheelView.setViewAdapter(new YearOrMonthAdapter(mContext, mYearArray, R.layout.item_wheel_view_align_right));
		mYearWheelView.setWheelBackground(R.drawable.wheel_bg_day);
		mYearWheelView.setWheelForeground(R.drawable.wheel_fg_day);   //设置选择条目的前景
		mYearWheelView.setDrawShadows(false);
		mYearWheelView.setCurrentItem(year - FROM_YEAR);

		//显示月份滚轮
		mMonthWheelView.setViewAdapter(new YearOrMonthAdapter(mContext, mMonthArray, R.layout.item_wheel_view_align_left));
		mMonthWheelView.setWheelBackground(R.drawable.wheel_bg_day);
		mMonthWheelView.setWheelForeground(R.drawable.wheel_fg_day);     //设置选择条目的前景
		mMonthWheelView.setDrawShadows(false);
		mMonthWheelView.setCurrentItem(month);

		mPopMenu.show();
	}

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


	/**
	 * 获得年份的序号
	 * @return
	 */
	public int getYearIndex() {
		return mYearIndex;
	}

	/**
	 * 获得月份的序号
	 * @return
	 */
	public int getMonthIndex() {
		return mMonthIndex;
	}
}