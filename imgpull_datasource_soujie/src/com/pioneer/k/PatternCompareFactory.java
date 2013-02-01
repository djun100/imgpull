package com.pioneer.k;

import java.io.Serializable;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;

import com.pioneer.engine.AppEngine;
import com.pioneer.k.DataKCompareFactory.CompareType;
import com.pioneer.silver.R;
import com.pioneer.util.StringUtil;

/*
 * Copyright (C) 2005-2010 PIONEER Inc.All Rights Reserved.
 * FileName：PatternCompareFactory.java
 * Description：K线组合形态比较工厂。
 * History：
 * 1.0 Denverhan 2013-1-18 Create
 */

public class PatternCompareFactory<T extends Number & Comparable<T> & Serializable>
{

	/**
	 * 形态种类
	 * 
	 * @author Denverhan
	 * 
	 */
	public static enum CompareType
	{
		THREE_POSITIVE,
		THREE_NEGTIVE,
		FIVE_POSITIVE,
		FIVE_NEGTIVE,
		STEP_INCREASE,
		STEP_DECREASE
	}

	private Map<String, Boolean>						mAnalyseNames	= new Hashtable<String, Boolean>();
	private Map<String, PatternComparable<T>>	mHolder			= new Hashtable<String, PatternComparable<T>>();
	private static PatternCompareFactory<Float>	mInstance		= new PatternCompareFactory<Float>();

	public PatternCompareFactory()
	{
		final Field[] fields = CompareType.class.getFields();
		if (fields != null)
		{
			for (Field field : fields)
			{
				mAnalyseNames.put(field.getName(), AppEngine.getInstance().getAppSetting().getPatternAnalyseSwitch(field.getName()));
			}
		}
	}

	public static PatternCompareFactory<Float> getInstance()
	{
		return mInstance;
	}

	/**
	 * 获得K线形态比较器。
	 * 
	 * @param type需要获得的比较器的类型
	 * @return K线比较器
	 */
	public PatternComparable<T> getPatternComparable(final String type)
	{

		PatternComparable<T> comparable = mHolder.get(type);
		if (comparable == null)
		{
			if (type.equalsIgnoreCase(CompareType.THREE_NEGTIVE.toString()))
			{
				comparable = new PatternComparable<T>()
				{

					@Override
					public PatternAnalyseResult analyseDataKSequence(List<DataK<T>> rowDatas)
					{
						final int countExpected = 3;
						if (rowDatas == null || rowDatas.size() < countExpected)
						{
							return null;
						}

						boolean isNegtive = true;
						for (int i = 0; i < countExpected; i++)
						{
							isNegtive &= rowDatas.get(i).isNegtive();
						}

						if (isNegtive)
						{

							return new PatternAnalyseResult(type, StringUtil.getString(R.string.pattern_analyse_less_green_three_soldier) + type,
									countExpected, rowDatas.get(0).getPeriodType());
						}
						else
						{
							return null;
						}
					}
				};
			}
			else if (type.equalsIgnoreCase(CompareType.THREE_POSITIVE.toString()))
			{
				comparable = new PatternComparable<T>()
				{

					@Override
					public PatternAnalyseResult analyseDataKSequence(List<DataK<T>> rowDatas)
					{
						final int countExpected = 3;
						if (rowDatas == null || rowDatas.size() < countExpected)
						{
							return null;
						}
						boolean isNegtive = true;
						for (int i = 0; i < countExpected; i++)
						{
							isNegtive &= rowDatas.get(i).isPositive();
						}

						if (isNegtive)
						{

							return new PatternAnalyseResult(type, StringUtil.getString(R.string.pattern_analyse_more_red_three_soldier)
									+ type.toString(), countExpected, rowDatas.get(0).getPeriodType());
						}
						else
						{
							return null;
						}
					}
				};
			}
			else if (type.equalsIgnoreCase(CompareType.FIVE_POSITIVE.toString()))
			{
				comparable = new PatternComparable<T>()
				{

					@Override
					public PatternAnalyseResult analyseDataKSequence(List<DataK<T>> rowDatas)
					{
						final int countExpected = 5;
						if (rowDatas == null || rowDatas.size() < countExpected)
						{
							return null;
						}

						boolean isNegtive = true;
						for (int i = 0; i < countExpected; i++)
						{
							isNegtive &= rowDatas.get(i).isPositive();
						}

						if (isNegtive)
						{

							return new PatternAnalyseResult(type, StringUtil.getString(R.string.pattern_analyse_more_red_five_soldier)
									+ type.toString(), countExpected, rowDatas.get(0).getPeriodType());
						}
						else
						{
							return null;
						}
					}
				};
			}
			else if (type.equalsIgnoreCase(CompareType.FIVE_NEGTIVE.toString()))
			{
				comparable = new PatternComparable<T>()
				{

					@Override
					public PatternAnalyseResult analyseDataKSequence(List<DataK<T>> rowDatas)
					{
						final int countExpected = 5;
						if (rowDatas == null || rowDatas.size() < countExpected)
						{
							return null;
						}

						boolean isNegtive = true;
						for (int i = 0; i < countExpected; i++)
						{
							isNegtive &= rowDatas.get(i).isNegtive();
						}

						if (isNegtive)
						{
							return new PatternAnalyseResult(type, StringUtil.getString(R.string.pattern_analyse_less_green_five_soldier)
									+ type.toString(), countExpected, rowDatas.get(0).getPeriodType());
						}
						else
						{
							return null;
						}
					}
				};
			}
			else if (type.equalsIgnoreCase(CompareType.STEP_INCREASE.toString()))
			{
				comparable = new PatternComparable<T>()
				{

					@Override
					public PatternAnalyseResult analyseDataKSequence(List<DataK<T>> rowDatas)
					{
						final int countExpected = 3;
						if (rowDatas == null || rowDatas.size() < countExpected)
						{
							return null;
						}

						boolean flag = true;
						float amplitude = rowDatas.get(0).getAmplitude() + 1.0f;
						for (int i = 0; i < countExpected; i++)
						{
							final DataK<T> dataK = rowDatas.get(i);
							if (dataK.isPositive())
							{
								if (amplitude > dataK.getAmplitude())
								{
									amplitude = dataK.getAmplitude();
								}
								else
								{
									flag = false;
									break;
								}
							}
							else
							{
								flag = false;
								break;
							}
						}

						if (flag)
						{
							return new PatternAnalyseResult(type, StringUtil.getString(R.string.pattern_analyse_increase_red_three_soldier)
									+ type.toString(), countExpected, rowDatas.get(0).getPeriodType());
						}
						else
						{
							return null;
						}
					}
				};

			}
			else if (type.equalsIgnoreCase(CompareType.STEP_DECREASE.toString()))
			{
				comparable = new PatternComparable<T>()
				{

					@Override
					public PatternAnalyseResult analyseDataKSequence(List<DataK<T>> rowDatas)
					{
						final int countExpected = 3;
						if (rowDatas == null || rowDatas.size() < countExpected)
						{
							return null;
						}

						boolean flag = true;
						float amplitude = rowDatas.get(0).getAmplitude() - 1.0f;
						for (int i = 0; i < countExpected; i++)
						{
							final DataK<T> dataK = rowDatas.get(i);
							if (dataK.isNegtive())
							{
								if (amplitude < dataK.getAmplitude())
								{
									amplitude = dataK.getAmplitude();
								}
								else
								{
									flag = false;
									break;
								}
							}
							else
							{
								flag = false;
								break;
							}
						}

						if (flag)
						{
							return new PatternAnalyseResult(type, StringUtil.getString(R.string.pattern_analyse_decrease_green_three_soldier)
									+ type.toString(), countExpected, rowDatas.get(0).getPeriodType());
						}
						else
						{
							return null;
						}
					}
				};

			}
			mHolder.put(type, comparable);
		}

		return comparable;
	}

	/**
	 * 返回分析方法名称列表视图
	 * 
	 * @return
	 */
	public Map<String, Boolean> getAnalyseNames()
	{
		return Collections.unmodifiableMap(mAnalyseNames);
	}
}
