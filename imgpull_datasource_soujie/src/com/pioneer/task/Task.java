package com.pioneer.task;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/*
 * Copyright (C) 2005-2010 PIONEER Inc.All Rights Reserved.
 * FileName：Task.java
 * Description：
 * History：
 * 1.0 Denverhan 2012-11-8 Create
 */

public abstract class Task implements Runnable
{
	public static final int		ERROR_CODE_INVALIDATE	= 1000;
	public static final int		ERROR_CODE_IO			= 1001;
	public static final int		ERROR_CODE_NETWORK		= 1002;
	public static final int		ERROR_CODE_PROTOCOL		= 1003;

	// 任务正在进行
	public static final int		STATUS_DOING			= 100;

	// 任务完成
	public static final int		STATUS_COMPLEMENT		= 101;

	// 任务失败
	public static final int		STATUS_FAILED			= 102;

	// 任务创建
	public static final int		STATUS_CREATE			= 103;

	// 任务取消
	public static final int		STATUS_CANCEL			= 104;

	// 错误码。
	protected int				mErrorCode				= ERROR_CODE_INVALIDATE;

	// 出错信息
	protected String			mErrorMsg;

	// 任务输出数据
	protected Object			mData;

	// 任务状态
	protected int				mStatus;

	// 任务进度值
	protected float				mProgress;
	private List<TaskObserver>	mObservers;

	protected boolean			mAutoClearObservers		= true;
	protected boolean			mHasCanceled			= false;

	private int					mUsedTime;

	private long				mTaskStartTime;

	public Task()
	{
		mStatus = STATUS_CREATE;
		mObservers = Collections.synchronizedList(new ArrayList<TaskObserver>());

		mTaskStartTime = System.currentTimeMillis();
	}

	/**
	 * 添加观察者observer。
	 * 
	 * @param observer
	 */
	public void addObserver(TaskObserver observer)
	{
		synchronized (mObservers)
		{
			if (!mObservers.contains(observer))
			{
				mObservers.add(observer);
			}
		}
	}

	/**
	 * 删除观察者observer。
	 * 
	 * @param observer
	 */
	public void removeObserver(TaskObserver observer)
	{
		synchronized (mObservers)
		{
			if (mObservers.contains(observer))
			{
				mObservers.remove(observer);
			}
		}
	}


	/**
	 * 清除所有观察者
	 */
	public void clearObservers()
	{
		synchronized (mObservers)
		{
			mObservers.clear();
		}

	}

	/**
	 * 通知观察者。
	 */
	protected void notifyObserver()
	{
		updateUsedTime();
		switch (mStatus)
		{
			case STATUS_COMPLEMENT:
			{
				synchronized (mObservers)
				{
					for (TaskObserver observer : mObservers)
					{
						if (observer != null)
						{
							observer.onTaskComplement(this);
						}
					}
				}

				break;
			}
			case STATUS_FAILED:
			{
				synchronized (mObservers)
				{
					for (TaskObserver observer : mObservers)
					{
						if (observer != null)
						{
							observer.onTaskFailed(this);
						}
					}
				}

				break;
			}
			case STATUS_DOING:
			{
				synchronized (mObservers)
				{
					for (TaskObserver observer : mObservers)
					{
						if (observer != null)
						{
							observer.onTaskProgress(this);
						}
					}
				}

				break;
			}
			case STATUS_CREATE:
			{
				synchronized (mObservers)
				{
					for (TaskObserver observer : mObservers)
					{
						if (observer != null)
						{
							observer.onTaskCreate(this);
						}
					}
				}

				break;
			}
			case STATUS_CANCEL:
			{
				synchronized (mObservers)
				{

					for (TaskObserver observer : mObservers)
					{
						if (observer != null)
						{
							observer.onTaskCancel(this);
						}
					}
				}

				break;
			}
		}
	}

	/**
	 * 更新任务耗时。
	 */
	private void updateUsedTime()
	{
		mUsedTime = (int)(System.currentTimeMillis() - mTaskStartTime);
	}

	/**
	 * 返回任务进度值
	 * 
	 * @return
	 */
	public float getProgress()
	{
		return mProgress;
	}

	/**
	 * 获得任务数据
	 * 
	 * @return
	 */
	public Object getData()
	{
		return mData;
	}

	/**
	 * 获得错误码
	 * 
	 * @return
	 */
	public int getErrorCode()
	{
		return mErrorCode;
	}

	/**
	 * 获得出错信息
	 * 
	 */
	public String getErrorMsg()
	{
		return mErrorMsg;
	}

	/**
	 * 设置任务完成后是否自动释放观察者。
	 * 
	 * @param b
	 */
	public void setAutoClearObservers(boolean b)
	{
		mAutoClearObservers = b;
	}

	public int getStatus()
	{
		return mStatus;
	}

	/**
	 * 取消任务
	 */
	public void cancel()
	{
		mHasCanceled = true;
	}

	protected void checkFreeObservers()
	{
		if (mAutoClearObservers)
		{
			clearObservers();
		}
	}
	
	/**
	 * 获得任务耗时 单位毫秒
	 * @return
	 */
	public int getUsedTime()
	{
		return mUsedTime;
	}
}
