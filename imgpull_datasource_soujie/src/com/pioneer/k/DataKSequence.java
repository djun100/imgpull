package com.pioneer.k;

import java.io.Serializable;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import android.util.Log;

import com.pioneer.constant.AppInt;
import com.pioneer.engine.AppEngine;
import com.pioneer.k.DataKCompareFactory.CompareType;
import com.pioneer.pricecenter.PriceObserver;
import com.pioneer.silver.R;
import com.pioneer.util.StringUtil;

/*
 * Copyright (C) 2005-2010 PIONEER Inc.All Rights Reserved.
 * FileName：PriceCenter.java
 * Description：K线容器。
 * History：
 * 1.0 Denverhan 2013-1-18 Create
 */

public class DataKSequence<T extends Number & Comparable<T> & Serializable> implements PriceObserver, Serializable
{
	// 用于存放天然价格数据
	private List<DataK<Float>>	mDataKContainer;
	private DataK<T>			mLatestDataK;

	// 本序列保存的K线的周期
	private int					mPeriodType;

	/**
	 * K线序列构造函数
	 * 
	 * @param periodType所存K线的周期
	 */
	public DataKSequence(int periodType)
	{
		mDataKContainer = new LinkedList<DataK<Float>>();
		mPeriodType = periodType;
	}

	/**
	 * 返回K线列表视图
	 * 
	 * @return
	 */
	public List<DataK<Float>> getDataKList()
	{
		return Collections.unmodifiableList(mDataKContainer);
	}

	@Override
	public String toString()
	{
		float[] amp = new float[5];
		for (int i = 0; i < 5 && mDataKContainer.size() > 5; i++)
		{
			amp[i] = mDataKContainer.get(i).getAmplitude();
		}

		return "DataKSequence [mDataKContainer.size()=" + mDataKContainer.size() + ", period=" + PeriodType.getPeriodName(mPeriodType) + ", amp="
				+ Arrays.toString(amp) + "]";
	}


	/**
	 * 获得最近的一根K线。
	 * 
	 * @return
	 */
	public DataK<T> getLatestDataK()
	{

		return mLatestDataK;
	}

	/**
	 * 更新价格中心
	 * 
	 * @param price
	 * @param quoteTime 单位为秒
	 */
	@SuppressWarnings("unchecked")
	public void updatePrice(float price, long quoteTime)
	{
		// 如果数据中心没有初始化
		if (mLatestDataK == null)
		{
			mLatestDataK = (DataK<T>) new DataK<Float>(mPeriodType).setClose(Float.valueOf(price), quoteTime);
		}
		else
		{
			DataK<T> newDataK = mLatestDataK.setClose((T) Float.valueOf(price), quoteTime);

			// 检查是否超期了，如果超期了，则更新最新价格，并且将原来的最新价格入库。
			if (newDataK != mLatestDataK)
			{
				mDataKContainer.add((DataK<Float>) mLatestDataK);
				checkDataKPattern();
				mLatestDataK = newDataK;

				// 检查是否释放部分K线。
				if (mDataKContainer.size() >= AppInt.DATAK_SIZE)
				{
					for (int i = mDataKContainer.size() - 1; i > AppInt.DATAK_SIZE - AppInt.DATAK_CLEAR_COUNT; i--)
					{
						mDataKContainer.remove(i);
					}
				}

				Log.d("LatestPriceCenter", "add datak=" + mLatestDataK.toString());
			}
			else
			{
				Log.d("LatestPriceCenter", "old datak");
			}
		}

		// 对K线按照时间重新排序
		Collections.sort(mDataKContainer, new Comparator<DataK<Float>>()
		{

			@Override
			public int compare(DataK<Float> lhs, DataK<Float> rhs)
			{
				return DataKCompareFactory.getInstance().getDataKComparable(CompareType.TIME).compare((DataK<Float>) lhs, (DataK<Float>) rhs);
			}
		});
	}

	/**
	 * 检查K线形态
	 */
	private void checkDataKPattern()
	{
		final Map<String, Boolean> analyseNames = PatternCompareFactory.getInstance().getAnalyseNames();
		if (analyseNames != null)
		{
			final Set<String> keySet = analyseNames.keySet();
			if (keySet != null)
			{
				for (String methodName : keySet)
				{
					//检查该分析方法是否启用，启用了才分析，没有启用直接处理下一个方法。
					if (analyseNames.get(methodName))
					{
						final PatternAnalyseResult analyseDataKSequence = PatternCompareFactory.getInstance().getPatternComparable(methodName)
								.analyseDataKSequence(getDataKList());
						if (analyseDataKSequence != null)
						{
							final String contentText = analyseDataKSequence.getAnalyseComment();
							AppEngine.getInstance().showNotification(
									StringUtil.getString(R.string.pattern_period) + PeriodType.getPeriodName(analyseDataKSequence.getDataKPeriod()),
									contentText + PeriodType.getPeriodName(analyseDataKSequence.getDataKPeriod()), contentText);
						}
					}
				}
			}
		}
	}

	@Override
	public void onPriceChanged(float latestPrice, long quotTime)
	{
		updatePrice(latestPrice, quotTime);
	}
}
