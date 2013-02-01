package com.pioneer.k;

import java.io.Serializable;
import java.util.Arrays;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;

import com.pioneer.pricecenter.LatestPriceCenter;

/*
 * Copyright (C) 2005-2010 PIONEER Inc.All Rights Reserved.
 * FileName：DataKSequenceCenter.java
 * Description：
 * History：
 * 1.0 Administrator 2013-1-19 Create
 */

public class DataKSequenceManager implements Serializable
{
	private static final long					serialVersionUID	= 7079620655582080307L;

	private int[]								mPeriods			= new int[] { PeriodType.ONE_MINUTES, PeriodType.THREE_MINUTES,
			PeriodType.FIVE_MINUTES, PeriodType.TEN_MINUTES, PeriodType.FIFTEEN_MINUTES, PeriodType.THIRTY_MINUTES, PeriodType.ONE_HOUR };

	private Map<String, DataKSequence<Float>>	mSequenceMap;


	/**
	 * 初始化所有K线头。
	 */
	public DataKSequenceManager()
	{
		initialAllDataKSequence();
	}

	/**
	 * 初始化各种K线序列。
	 */
	public void initialAllDataKSequence()
	{
		LatestPriceCenter.getInstance().clearObserver();
		
		if (mSequenceMap == null)
		{
			// 构造产生的对象要这么初始化
			mSequenceMap = new Hashtable<String, DataKSequence<Float>>();

			for (int period : mPeriods)
			{
				DataKSequence<Float> dataKSequence = new DataKSequence<Float>(period);
				LatestPriceCenter.getInstance().addObserver(dataKSequence);
				mSequenceMap.put(PeriodType.getPeriodName(period), dataKSequence);
			}
		}
		else
		{
			// 反序列化产生的对象要这么初始化
			for (String key : mSequenceMap.keySet())
			{
				LatestPriceCenter.getInstance().addObserver(mSequenceMap.get(key));
			}
		}

	}

	@Override
	public String toString()
	{
		return "DataKSequenceManager [ mSequenceMap=" + mSequenceMap + "]";
	}


	/**
	 * 通过周期获取对应的K线序列。
	 * 
	 * @param period
	 * @return
	 */
	public List<DataK<Float>> getDataKList(int period)
	{
		return mSequenceMap.get(PeriodType.getPeriodName(period)).getDataKList();
	}

}
