package com.pioneer.pricecenter;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Vector;

import com.pioneer.engine.AppEngine;
import com.pioneer.k.DataK;

import android.util.Log;

/*
 * Copyright (C) 2005-2010 PIONEER Inc.All Rights Reserved.
 * FileName：LatestPriceCenter.java
 * Description：
 * History：
 * 1.0 Administrator 2013-1-19 Create
 */

public class LatestPriceCenter<T extends Number & Comparable<T> & Serializable>
{
	private final String		TAG	= "LatestPriceCenter";

	// 最新价格
	private float				mLatestPrice;

	// 最新价格的引用时间
	private long				mQuoteTime;

	// 观察者容器
	private List<PriceObserver>	mObservers;

	private LatestPriceCenter()
	{
		mObservers = new ArrayList<PriceObserver>();
	}

	private static LatestPriceCenter<Float>	mIntance	= new LatestPriceCenter<Float>();

	public static LatestPriceCenter<Float> getInstance()
	{
		return mIntance;
	}

	/**
	 * 更新价格
	 * 
	 * @param price 新的价格
	 * @param quoteTime 新价格的引用时间,单位为秒
	 * @return 返回这次数据是否比上次的新
	 */
	public synchronized boolean updatePrice(float price, long quoteTime)
	{
		if (quoteTime == mQuoteTime)
		{
			return false;
		}
		else
		{
			mQuoteTime = quoteTime;
			mLatestPrice = price;
			Log.d(TAG, "DataKManager=" + AppEngine.getInstance().getDataKManager().toString());
			notifyObserver();
			return true;
		}
	}

	/**
	 * 通知观察者
	 */
	private void notifyObserver()
	{
		Log.d(TAG, "notifyObserver() mObservers.size()=" + mObservers.size());
		for (PriceObserver observer : mObservers)
		{
			observer.onPriceChanged(mLatestPrice, mQuoteTime);
		}
	}

	/**
	 * 添加价格观察者
	 * 
	 * @param o
	 * @return 是否添加成功
	 */
	public synchronized boolean addObserver(PriceObserver o)
	{
		Log.d(TAG, "addObserver() o =" + o.toString());
		if (o == null || mObservers.contains(o))
		{
			return false;
		}

		mObservers.add(o);

		return true;
	}

	/**
	 * 删除观察者
	 * 
	 * @param o
	 * @return
	 */
	public synchronized boolean removeObserver(PriceObserver o)
	{
		mObservers.remove(o);

		return true;
	}
	
	/**
	 * 删除所有的观察者
	 * 
	 * @return
	 */
	public synchronized boolean clearObserver()
	{
		mObservers.clear();
		
		return true;
	}

}
