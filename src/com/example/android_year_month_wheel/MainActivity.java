package com.example.android_year_month_wheel;

import java.util.Calendar;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import com.wanmei.dospy.view.PageHelper;

public class MainActivity extends Activity {

	private PageHelper mPageHelper;
	private TextView mFromDate, mToDate;
	private int mMaxYear, mSelectYear, mSelectMonth;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		Calendar calendar = Calendar.getInstance();
		mMaxYear = calendar.get(Calendar.YEAR);
		mFromDate = (TextView) findViewById(R.id.from_date);
		mToDate = (TextView)findViewById(R.id.to_date);
		mPageHelper = new PageHelper(MainActivity.this, new View.OnClickListener() {
			
			@Override
			public void onClick(View arg0) {
				// TODO Auto-generated method stub
				
			}
		});

		final int FROM_YEAR = 1990;
		mPageHelper.setYearArrange(FROM_YEAR, mMaxYear);
		// mPageHelper.setMaxPageNum(mMaxYear); //设置显示的最大页数
		mPageHelper.selectYearAndMonth(mSelectYear, mSelectMonth);
		mFromDate.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				mPageHelper.showPopup(mSelectYear, mSelectMonth); // 设置当前显示页
			}
		});

		final int FROM_MONTH = 1;
		mPageHelper.setMonthArrange(1, 12);

		mPageHelper.setListener(new PageHelper.IPageSelectedListener() {
			@Override
			public void onPageSelected(int page) {

			}

			@Override
			public void onYearMonthSelected(int year, int month) {
				mSelectYear = year;
				mSelectMonth = month;
				String yearStr = getString(R.string.year_prompt, year + FROM_YEAR);
				String monthStr = getString(R.string.month_prompt, month + FROM_MONTH);
				mFromDate.setText(yearStr + "\t" + monthStr);
			}
		});

	}
}
