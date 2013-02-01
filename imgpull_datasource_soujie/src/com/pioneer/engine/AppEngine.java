package com.pioneer.engine;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintStream;

import android.app.ActivityManager;
import android.app.ActivityManager.RunningServiceInfo;
import android.content.Context;
import android.content.Intent;
import android.os.Handler;

import com.pioneer.StatusBarNotification;
import com.pioneer.constant.AppString;
import com.pioneer.k.DataKSequenceManager;
import com.pioneer.setting.AppSetting;
import com.pioneer.silver.R;
import com.pioneer.util.FileUtil;
import com.pioneer.util.StringUtil;

/*
 * Copyright (C) 2005-2010 PIONEER Inc.All Rights Reserved.
 * FileName:ppEngine.java
 * Description:
 * History:
 * 1.0 Denverhan 2012-9-13 Create
 */

public class AppEngine
{

	private static AppEngine				mInstance	= new AppEngine();
	private Thread.UncaughtExceptionHandler	mDefaultHandler;
	private Context							mContext;
	private Handler							mHandler;
	private StatusBarNotification			mNotification;
	private AppSetting						mSetting;
	
	private DataKSequenceManager			mDataKSequenceManager;
	
	private AppEngine()
	{
		mHandler = new Handler();
	}

	public Handler getHandler()
	{
		return mHandler;
	}

	public static AppEngine getInstance()
	{
		return mInstance;
	}

	public Context getContext()
	{
		return mContext;
	}

	public void setContext(final Context c)
	{
		mContext = c;
		mNotification = new StatusBarNotification(mContext);
		mSetting = new AppSetting(c);
		updateExceptionHandler();
	}

	/**
	 * 系统状态栏通知
	 * @param title
	 * @param flashText
	 * @param contentText
	 * @return
	 */
	public boolean showNotification(String title, String flashText,  String contentText)
	{
		mNotification.showNotification(android.R.drawable.ic_menu_info_details, flashText, title, contentText, true, StatusBarNotification.ALARM_NOTIFY_ID++  );
		
		return true;
	}
	
	
	/**
	 * 替换系统默认的异常处理机制。
	 */
	private void updateExceptionHandler()
	{
		mDefaultHandler = Thread.getDefaultUncaughtExceptionHandler();
		Thread.setDefaultUncaughtExceptionHandler(new Thread.UncaughtExceptionHandler()
		{

			@Override
			public void uncaughtException(Thread thread, Throwable ex)
			{
				final Intent intent = new Intent(AppString.SERVICE_ACTION);
				mContext.startService(intent);

				mNotification.showNotification(android.R.drawable.ic_menu_info_details, "", mContext.getString(R.string.crash_notification_title),
						mContext.getString(R.string.crash_notification_content), true, StatusBarNotification.EXCEPTION_NOTIFY_ID);

				saveExceptionInfo(ex);

				mDefaultHandler.uncaughtException(thread, ex);
			}

			/**
			 * 将异常信息存到文件
			 * 
			 * @param ex
			 */
			private void saveExceptionInfo(Throwable ex)
			{
				StringBuilder buf = new StringBuilder();
				buf.append(StringUtil.getCurrentTime());

				File file = new File(FileUtil.getDayDir(""), buf.toString() + ".txt");
				PrintStream fOut = null;

				try
				{
					file.createNewFile();
					fOut = new PrintStream(file);
					ex.printStackTrace(fOut);
				}
				catch (FileNotFoundException e)
				{
					e.printStackTrace();
				}
				catch (IOException e)
				{
					e.printStackTrace();
				}
				finally
				{
					try
					{
						if (fOut != null)
						{
							fOut.close();
						}
					}
					catch (Exception e2)
					{
						e2.printStackTrace();
					}
				}
			}
		});
	}

	public File getRootDir()
	{
		return getContext().getFilesDir();
	}

	public File getCacheDir()
	{
		return getContext().getCacheDir();
	}

	/**
	 * 返回服务是否运行
	 * @param serviceName
	 * @return
	 */
	public boolean isServiceRunning(String serviceName)
	{
		if (serviceName == null)
		{
			return false;
		}

		ActivityManager manager = (ActivityManager) mContext.getSystemService(Context.ACTIVITY_SERVICE);
		for (RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE))
		{
			if (serviceName.equals(service.service.getClassName()))
			{
				return true;
			}
		}
		return false;
	}

	public AppSetting getAppSetting()
	{
		return mSetting;
	}
	

	/**
	 * 返回K线管理器
	 * @return
	 */
	public DataKSequenceManager getDataKManager()
	{
		return mDataKSequenceManager;
	}
	
	/**
	 * 设置K线管理器
	 * @param manager
	 */
	public void setDataKManager(DataKSequenceManager manager)
	{
		mDataKSequenceManager = manager;
	}
}
