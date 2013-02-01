package com.pioneer.k;

import java.io.Serializable;
import java.util.Hashtable;
import java.util.Map;

/*
 * Copyright (C) 2005-2010 PIONEER Inc.All Rights Reserved.
 * FileName：ComparetorFactory.java
 * Description：两根K线比较方法工厂。
 * History：
 * 1.0 Denverhan 2013-1-17 Create
 */

public class DataKCompareFactory<T extends Number & Comparable<T> & Serializable>
{
	/**
	 * 比较类型
	 * 
	 * @author Denverhan
	 * 
	 */
	public static enum CompareType
	{
		BODY,
		OPEN,
		CLODE,
		HIGH,
		AVERAGE,
		LOW,
		TIME
	}


	private Map<CompareType, DataKComparable<T>>	mHolder		= new Hashtable<CompareType, DataKComparable<T>>();

	private static DataKCompareFactory<Float>		mInstance	= new DataKCompareFactory<Float>();

	public static DataKCompareFactory<Float> getInstance()
	{
		return mInstance;
	}

	/**
	 * 获得K线比较器。
	 * 
	 * @param type需要获得的比较器的类型
	 * @return K线比较器
	 */
	public DataKComparable<T> getDataKComparable(CompareType type)
	{

		final DataKComparable<T> dataKComparable = mHolder.get(type);
		if (dataKComparable == null)
		{
			DataKComparable<T> compare = null;
			if (type == CompareType.CLODE)
			{
				compare = new DataKComparable<T>()
				{

					@Override
					public int compare(DataK<T> left, DataK<T> right)
					{
						if (left == null || right == null)
						{
							throw new NullPointerException();
						}

						return left.getClose().intValue() - right.getClose().intValue();
					}
				};
			}
			else if (type == CompareType.OPEN)
			{
				compare = new DataKComparable<T>()
				{

					@Override
					public int compare(DataK<T> left, DataK<T> right)
					{
						if (left == null || right == null)
						{
							throw new NullPointerException();
						}

						return left.getOpen().intValue() - right.getOpen().intValue();
					}
				};
			}
			else if (type == CompareType.AVERAGE)
			{
				compare = new DataKComparable<T>()
				{

					@Override
					public int compare(DataK<T> left, DataK<T> right)
					{
						if (left == null || right == null)
						{
							throw new NullPointerException();
						}

						int leftAvg = (left.getOpen().intValue() + left.getClose().intValue()) / 2;
						int rightAvg = (right.getOpen().intValue() + right.getClose().intValue()) / 2;


						return leftAvg - rightAvg;
					}
				};
			}
			else if (type == CompareType.HIGH)
			{
				compare = new DataKComparable<T>()
				{

					@Override
					public int compare(DataK<T> left, DataK<T> right)
					{
						if (left == null || right == null)
						{
							throw new NullPointerException();
						}

						return left.getHigh().intValue() - right.getHigh().intValue();
					}
				};
			}
			else if (type == CompareType.LOW)
			{
				compare = new DataKComparable<T>()
				{

					@Override
					public int compare(DataK<T> left, DataK<T> right)
					{
						if (left == null || right == null)
						{
							throw new NullPointerException();
						}

						return left.getLow().intValue() - right.getLow().intValue();
					}
				};
			}
			else if (type == CompareType.TIME)
			{
				compare = new DataKComparable<T>()
				{

					@Override
					public int compare(DataK<T> left, DataK<T> right)
					{
						if (left == null || right == null)
						{
							throw new NullPointerException();
						}

						return (int) (right.getCloseTime() - left.getCloseTime());
					}
				};
			}

			mHolder.put(type, compare);
			return compare;
		}

		return dataKComparable;
	}
}
