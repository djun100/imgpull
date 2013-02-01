package com.pioneer.k;

import java.io.Serializable;

import com.pioneer.util.StringUtil;

/*
 * Copyright (C) 2005-2010 PIONEER Inc.All Rights Reserved.
 * FileName：DataK.java
 * Description：
 * History：
 * 1.0 Denverhan 2013-1-17 Create
 */

public class DataK<T extends Number & Comparable<T> & Serializable> implements Serializable
{
	public T getOpen()
	{
		return mOpen;
	}

	public T getClose()
	{
		return mClose;
	}

	public T getHigh()
	{
		return mHigh;
	}

	public T getLow()
	{
		return mLow;
	}

	/**
	 * 更新周期内的高低点价格
	 * 
	 * @param mOpen
	 */
	private void updateRange(T currentPrice)
	{
		if (currentPrice.floatValue() < mLow.floatValue())
		{
			// 更新低点
			mLow = currentPrice;
		}

		if (currentPrice.floatValue() > mHigh.floatValue())
		{
			// 更新高点
			mHigh = currentPrice;
		}
	}

	/**
	 * 
	 * @param open 开盘价
	 * @param openTime 开盘时刻
	 * @return
	 */
	public DataK<T> setOpen(T open, long openTime)
	{
		return setOpen(open).setOpenTime(openTime);
	}

	public DataK<T> setOpen(T open)
	{
		this.mOpen = open;
		if (mClose == null)
		{
			mClose = open;
		}
		iniitalRange(open);
		updateRange(mOpen);
		return this;
	}


	/**
	 * 设置收盘价，如果已经超过了收盘周期，则返回一根新K线，否者返回当前K线。
	 * 
	 * @param close收盘价格
	 * @param closeTime 当前时刻 单位为秒
	 * @return
	 */
	public DataK<T> setClose(T close, long closeTime)
	{

		// 如果本K线没有初始化
		if (mOpenTime == 0L || mOpen == null)
		{
			updatePrice(close);
			setQuoteTime(closeTime);
			return this;
		}
		else
		{
			// TODO:1.检查有没有超期，如果超期则new一根新K线，并初始化该K线的状态，否则更新收盘价格和时刻，同时更新高低点。
			if (closeTime < mOpenTime)
			{
				// 无效数据，历史数据
				return this;
			}
			else if (closeTime - mOpenTime > mPeriod)
			{
				// 超期了
				final DataK<T> nextDataK = new DataK<T>(mPeriod);
				nextDataK.setClose(close, closeTime);
				return nextDataK;
			}
			else
			{
				// 没有超期
				updatePrice(close);
				setOpenTime(closeTime);
				return this;
			}
		}
	}

	@Override
	public String toString()
	{
		return "DataK [mOpen=" + mOpen + ", mClose=" + mClose + ", mHigh=" + mHigh + ", mLow=" + mLow + ", mQuoteTime="
				+ StringUtil.seconds2Time(mCloseTime) + ", mOpenTime=" + StringUtil.seconds2Time(mOpenTime) + ", mPeriod=" + mPeriod
				+  "]";
	}

	private void updatePrice(T close)
	{
		mClose = close;

		if (mOpen == null)
		{
			mOpen = close;
		}

		iniitalRange(close);
		updateRange(close);
	}


	/**
	 * 初始化高低点
	 * 
	 * @param close
	 */
	private void iniitalRange(T close)
	{
		if (mHigh == null)
		{
			mHigh = close;
		}

		if (mLow == null)
		{
			mLow = close;
		}
	}

	public DataK<T> setHigh(T mHigh)
	{
		this.mHigh = mHigh;
		return this;
	}

	public DataK<T> setLow(T mLow)
	{
		this.mLow = mLow;
		return this;
	}

	public DataK<T> setPeriodType(int type)
	{
		mPeriod = type;
		return this;
	}

	public int getPeriodType()
	{
		return mPeriod;
	}

	/**
	 * 
	 * @param PeriodType K线周期
	 */
	public DataK(int PeriodType)
	{
		mId = COUNT++;
		mPeriod = PeriodType;
	}

	/**
	 * 是否为阳线
	 * 
	 * @return
	 */
	public boolean isPositive()
	{
		return mClose.intValue() > mOpen.intValue();
	}

	/**
	 * 是否为阴线
	 * 
	 * @return
	 */
	public boolean isNegtive()
	{
		return mClose.intValue() < mOpen.intValue();
	}

	/**
	 * 是否为十字星
	 * 
	 * @return
	 */
	public boolean isCross()
	{
		return mClose.floatValue() == mOpen.floatValue() && mHigh.floatValue() > mClose.floatValue() && mLow.floatValue() < mClose.floatValue();
	}

	/**
	 * 获得周期内开收差
	 * 
	 * @return
	 */
	public float getAmplitude()
	{
		return mClose.floatValue() - mOpen.floatValue();
	}

	/**
	 * 获得价格在周期内的变动率
	 * 
	 * @return
	 */
	public float getRate()
	{
		return getAmplitude() / mPeriod;
	}

	/**
	 * 是否为大阳线
	 * 
	 * @return
	 */
	public boolean isFullPositive()
	{
		return mLow.floatValue() == mOpen.floatValue() && mHigh.floatValue() == mClose.floatValue() && isPositive();
	}

	/**
	 * 是否为大阴线
	 * 
	 * @return
	 */
	public boolean isFullNegtive()
	{
		return mLow.floatValue() == mClose.floatValue() && mHigh.floatValue() == mOpen.floatValue() && isNegtive();
	}

	/**
	 * 设置时刻的秒表示数据
	 * 
	 * @return
	 */
	private DataK<T> setQuoteTime(long time)
	{
		mCloseTime = time;

		if (mOpenTime == 0L)
		{
			// 如果没有设置开盘时刻，则初始化开盘时刻
			mOpenTime = time;
		}

		return this;
	}

	/**
	 * 获取
	 * 
	 * @return
	 */
	public long getCloseTime()
	{
		return mCloseTime;
	}

	public long getOpenTime()
	{
		return mOpenTime;
	}

	/**
	 * 设置本周期的开盘时刻,只允许设置一次，再次设置无效。
	 * 
	 * @param time
	 * @return
	 */
	private DataK<T> setOpenTime(long time)
	{
		if (mCloseTime < time)
		{
			mCloseTime = time;
		}

		if (mOpenTime == 0L)
		{
			mOpenTime = time;
		}

		return this;
	}

	private T			mOpen;
	private T			mClose;
	private T			mHigh;
	private T			mLow;

	// 收盘时刻
	private long		mCloseTime	= 0L;

	// 开盘时刻 单位秒
	private long		mOpenTime	= 0L;

	// 周期类型
	private int			mPeriod;

	private static int	COUNT		= 0;
	private int			mId			= 0;
}
