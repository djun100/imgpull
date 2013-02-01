package com.pioneer.silver.service;

import java.io.File;
import java.util.Date;
import java.util.HashMap;

import android.app.Service;
import android.content.Intent;
import android.media.MediaPlayer;
import android.os.Binder;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.Vibrator;
import android.text.TextUtils;
import android.util.Log;
import android.widget.Toast;

import com.pioneer.StatusBarNotification;
import com.pioneer.constant.AppInt;
import com.pioneer.constant.AppString;
import com.pioneer.engine.AppEngine;
import com.pioneer.pricecenter.LatestPriceCenter;
import com.pioneer.silver.R;
import com.pioneer.sound.Player;
import com.pioneer.task.PricePullTask;
import com.pioneer.task.Task;
import com.pioneer.task.TaskObserver;
import com.pioneer.util.FileUtil;
import com.pioneer.util.StringUtil;

public class DataUpdateService extends Service
{
	private static final float		PRECISION						= 0.001f;
	private static final int		TIME_USED_RIGHT_NOW				= -1;
	private static final String		TAG								= "DataUpdateService";
	private static final int		START_NEW_PULL_TASK_ICBC		= 100;
	private static final int		START_NEW_PULL_TASK_TIANTONG	= 101;

	private static final int		ALARM_CHECK_INTERVAL			= 13;						// 秒

	// 单位毫秒
	private int						mIntervalSec					= 10000;

	// 进行通过时价格变动阈值
	private static final float		PRCIE_DIFFERENCE_THRESHOLD		= 0.015f;

	private static final int		SEQUENCE_CHANGE_INTERVAL		= 65000;

	// 价格拉直线时的变化率。单位：元/秒
	private static final float		PRICE_RATE						= 0.10F;
	private MediaPlayer				mPlayer;

	// 银行买入价
	private String					mBankBuy;

	// 银行卖出价
	private String					mBankSell;

	// 最高中间价
	private String					mMaxMiddle;

	// 最低中间价
	private String					mMinMiddle;

	private String					mCurrentMiddle;
	// 更新时间
	private String					mTime;

	// 价格更新时刻，单位是秒
	private long					mQuoteTime;

	// 出错信息
	private String					mErrorMsg;

	// 状态信息
	private int						mStatus;

	// 上次价格变动时间
	private long					mLastChangeTime					= -1L;

	// 记录上次价格变动方向
	private boolean					mIsLastChangeRaise				= false;
	private DataPullHandler			mHandler						= new DataPullHandler();
	private ServiceBinder			mServiceBinder					= new ServiceBinder();

	// 状态栏通知
	private StatusBarNotification	mNotification;

	// 用于播放价格
	private Player					mPricePlayer;

	private boolean					mHasExited						= false;

	private Handler					mUIHandler;

	public DataUpdateService()
	{
		super();

		mStatus = Task.STATUS_CREATE;
	}

	public class ServiceBinder extends Binder implements IDataUpdateService
	{
		@Override
		public float getBankBuyPrice()
		{
			return DataUpdateService.this.getBankBuyPrice();
		}

		@Override
		public String getUpdateTime()
		{
			return mTime;
		}

		@Override
		public void setUpdateInterval(int interval)
		{
			if (interval > 1000)
			{
				mIntervalSec = interval;
				AppEngine.getInstance().getAppSetting().setUpdateInterval(mIntervalSec);
				Toast.makeText(DataUpdateService.this, (interval / 1000) + "", Toast.LENGTH_SHORT).show();
			}
		}

		@Override
		public int getUpdateInterval()
		{
			return AppEngine.getInstance().getAppSetting().getUpdateInterval();
		}

		@Override
		public String getErrorMsg()
		{
			return mErrorMsg;
		}

		@Override
		public int getServiceStatus()
		{
			return mStatus;
		}

		@Override
		public void setUpdateHandler(Handler handler)
		{
			mUIHandler = handler;
		}
	}

	private class DataPullHandler extends Handler
	{
		private boolean	mCanStartNewTask	= true;

		/**
		 * 恢复允许发起新的网络请求。
		 */
		public void restoreCanstartNewTask()
		{
			mCanStartNewTask = true;
		}

		@Override
		public void handleMessage(Message msg)
		{
			switch (msg.what)
			{
				case START_NEW_PULL_TASK_ICBC:
				{
					if (mCanStartNewTask && !mHasExited)
					{
						mCanStartNewTask = false;
						new PricePullTask(DataUpdateService.this, AppString.AG_URL_ICBC, mIcbcObserver);
					}

					break;
				}
				case START_NEW_PULL_TASK_TIANTONG:
				{
					new PricePullTask(DataUpdateService.this, AppString.AG_URL_TIANTONG, mTiantongObserver);

					break;
				}
			}

			super.handleMessage(msg);
		}
	}

	/**
	 * 将价格存到本地变量
	 * 
	 * @param map
	 */
	private void renewPrice(final HashMap<String, String> map)
	{
		// final String stringPrice = map.get(AppString.KEY_NAME_LAST);
		// mQuoteTime = Long.parseLong(map.get(AppString.KEY_NAME_QUOTE_TIME));
		// mTime = new SimpleDateFormat("HH:mm:ss").format(new Date(mQuoteTime *
		// 1000L));
		// mBankBuy = StringUtil.removeDot(stringPrice);
		// mBankSell = StringUtil.removeDot(stringPrice);
		// mMaxMiddle = StringUtil.removeDot(map.get(AppString.KEY_NAME_HIGH));
		// mMinMiddle = StringUtil.removeDot(map.get(AppString.KEY_NAME_LOW));

		mTime = map.get(AppString.STRING_TIME);
		mQuoteTime = StringUtil.time2Seconds(mTime);
		mBankBuy = map.get(AppString.STRING_BANK_BUY);
		mBankSell = map.get(AppString.STRING_BANK_SELL);
		mMaxMiddle = map.get(AppString.STRING_MIDDLE_MAX);
		mMinMiddle = map.get(AppString.STRING_MIDDLE_MIN);
		mCurrentMiddle = map.get(AppString.STRING_MIDDLE_CURRENT);

		Log.d(TAG, "renewPrice() mTime=" + mTime);
	}

	@Override
	public IBinder onBind(Intent intent)
	{
		Log.d(TAG, "onBind");
		if (AppEngine.getInstance().getContext() == null)
		{
			AppEngine.getInstance().setContext(this);

			AppEngine.getInstance().setDataKManager(FileUtil.loadDataKSequenceManager(AppString.FILE_NAME_DATAK_SEQUENCE_MANAGER));

		}
		mServiceBinder.setUpdateInterval(AppEngine.getInstance().getAppSetting().getUpdateInterval());
		return mServiceBinder;
	}

	@Override
	public void onCreate()
	{
		Log.d(TAG, "onCreate");
		super.onCreate();
		if (AppEngine.getInstance().getContext() == null)
		{
			AppEngine.getInstance().setContext(this);
			AppEngine.getInstance().setDataKManager(FileUtil.loadDataKSequenceManager(AppString.FILE_NAME_DATAK_SEQUENCE_MANAGER));

		}
		Log.d(TAG, "create this=" + this);
		mNotification = new StatusBarNotification(this);
		mPricePlayer = new Player(this);
		mHandler.sendEmptyMessage(START_NEW_PULL_TASK_ICBC);
		mHandler.sendEmptyMessage(START_NEW_PULL_TASK_TIANTONG);
	}

	@Override
	public void onDestroy()
	{
		mHasExited = true;
		mHandler.removeMessages(START_NEW_PULL_TASK_ICBC);
		FileUtil.saveDataKSequence(AppEngine.getInstance().getDataKManager(), new File(FileUtil.getDataKSequenceDir(FileUtil.DATAKSEQUENCE_DIR),
				AppString.FILE_NAME_DATAK_SEQUENCE_MANAGER));

		Log.d(TAG, "Ondestroy");
		Log.d(TAG, "destroy this=" + this);
	}

	@Override
	public void onStart(Intent intent, int startid)
	{
		if (AppEngine.getInstance().getContext() == null)
		{
			AppEngine.getInstance().setContext(this);
			AppEngine.getInstance().setDataKManager(FileUtil.loadDataKSequenceManager(AppString.FILE_NAME_DATAK_SEQUENCE_MANAGER));
		}
		mServiceBinder.setUpdateInterval(AppEngine.getInstance().getAppSetting().getUpdateInterval());
		Log.d(TAG, "onStart");
	}

	/**
	 * 播放特定的声音文件
	 * 
	 * @param mediaRes 资源编号。
	 */
	private void play(int mediaRes)
	{
		try
		{
			if (mPlayer != null)
			{
				mPlayer.stop();
				mPlayer.release();
			}

			mPlayer = MediaPlayer.create(this, mediaRes);
			mPlayer.setLooping(false);

			mPlayer.start();
		}
		catch (IllegalStateException e)
		{
			e.printStackTrace();
		}
	}


	/**
	 * 按照给定的序列震动
	 * 
	 * @param seq
	 */
	private void vibrate(long[] seq)
	{
		if (seq == null)
		{
			return;
		}
		Vibrator mVibrator = null;
		mVibrator = (Vibrator) this.getSystemService(Service.VIBRATOR_SERVICE);

		mVibrator.vibrate(seq, -1);
	}

	private static interface PriceChangeAction
	{
		int	ALARM		= 0;
		int	FREQUENT	= 1;
		int	LOW			= 2;
	}

	private TaskObserver	mTiantongObserver	= new TaskObserver()
												{

													@Override
													public void onTaskComplement(Task task)
													{

														if (task == null || !(task.getData() instanceof String))
														{
															cycle(task.getUsedTime(), START_NEW_PULL_TASK_TIANTONG);
															return;
														}

														String content = task.getData().toString();
														Date date = StringUtil.getSoujieDate(StringUtil.getSoujieTime(content));
														Float price = StringUtil.getSoujiePrice(content);

														// 通知价格中心
														if (date != null && price != null)
														{
															final boolean isNew = LatestPriceCenter.getInstance().updatePrice(price.floatValue(), date.getTime() / 1000);
															
															if (mUIHandler != null && isNew)
															{
																mUIHandler.sendMessage(mUIHandler.obtainMessage(AppInt.MSG_CODE_UPDATE_TICK, date.toLocaleString() + " " + price.toString()));
															}
														}

														cycle(task.getUsedTime(), START_NEW_PULL_TASK_TIANTONG);
													}

													@Override
													public void onTaskFailed(Task task)
													{
														cycle(TIME_USED_RIGHT_NOW, START_NEW_PULL_TASK_TIANTONG);
													}

													@Override
													public void onTaskProgress(Task task)
													{
													}

													@Override
													public void onTaskCreate(Task task)
													{
													}

													@Override
													public void onTaskCancel(Task task)
													{
													}
												};

	private TaskObserver	mIcbcObserver		= new TaskObserver()
												{

													@Override
													public void onTaskProgress(Task task)
													{
														if (task != null)
														{
															mStatus = task.getStatus();
															mErrorMsg = task.getErrorMsg();
														}
														
														if (mUIHandler != null)
														{
															mUIHandler.sendMessage(mUIHandler.obtainMessage(AppInt.MSG_CODE_UPDATE_PRICE, StringUtil.getString(R.string.pulling_price)));
														}
													}

													@Override
													public void onTaskFailed(Task task)
													{
														if (task != null)
														{
															mStatus = task.getStatus();
															mErrorMsg = task.getErrorMsg();
															cycle(task.getUsedTime(), START_NEW_PULL_TASK_ICBC);
														}

														mHandler.restoreCanstartNewTask();
														cycle(TIME_USED_RIGHT_NOW, START_NEW_PULL_TASK_ICBC);
														Log.d(TAG, "onTaskFailed");
														
														if (mUIHandler != null)
														{
															mUIHandler.sendMessage(mUIHandler.obtainMessage(AppInt.MSG_CODE_UPDATE_PRICE, mErrorMsg));
														}
													}

													@Override
													public void onTaskCreate(Task task)
													{
														Log.d(TAG, "onTaskCreate");
														if (task != null)
														{
															mStatus = task.getStatus();
															mErrorMsg = task.getErrorMsg();
														}
														
														if (mUIHandler != null)
														{
															mUIHandler.sendMessage(mUIHandler.obtainMessage(AppInt.MSG_CODE_UPDATE_PRICE, mErrorMsg));
														}
													}

													@Override
													public void onTaskComplement(Task task)
													{
														Log.d(TAG, "onTaskComplement");
														mHandler.restoreCanstartNewTask();

														if (task == null || !(task.getData() instanceof String))
														{
															cycle(task.getUsedTime(), START_NEW_PULL_TASK_ICBC);
															return;
														}

														mStatus = task.getStatus();
														HashMap<String, String> map = StringUtil.getValues(task.getData().toString());
														final String bankBuyPrice = map.get(AppString.STRING_BANK_BUY);
														float oldPrice = 0.0f;

														if (bankBuyPrice == null)
														{
															cycle(task.getUsedTime(), START_NEW_PULL_TASK_ICBC);
															return;
														}

														if (mBankBuy == null)
														{
															mBankBuy = bankBuyPrice;
														}

														float currentPrice = Float.parseFloat(bankBuyPrice);
														oldPrice = Float.parseFloat(mBankBuy);

														renewPrice(map);

														float priceDifference = currentPrice - oldPrice;
														// 上次价格有效,且两次价格不同。
														if (Math.abs(priceDifference) >= PRECISION)
														{
															dispatchPriceChangeAction(bankBuyPrice, priceDifference);
															mLastChangeTime = mQuoteTime;
															mIsLastChangeRaise = priceDifference > PRECISION;
														}

														initialLastChangeTime();

														map.clear();
														map = null;

														cycle(task.getUsedTime(), START_NEW_PULL_TASK_ICBC);
														
														if (mUIHandler != null)
														{
															mUIHandler.sendMessage(mUIHandler.obtainMessage(AppInt.MSG_CODE_UPDATE_PRICE, mTime + " " + mBankBuy));
														}
													}

													/**
													 * 处理价格变动事件。
													 * 
													 * @param bankBuyPrice
													 * @param priceDifference
													 * @param isUp
													 * @param text
													 */
													private void dispatchPriceChangeAction(final String bankBuyPrice, float priceDifference)
													{
														final boolean isUp = priceDifference > PRECISION;
														String text = (isUp ? StringUtil.getString(R.string.price_up) : StringUtil
																.getString(R.string.price_down)) + bankBuyPrice;

														switch (getPriceChangeAction(priceDifference))
														{
															case PriceChangeAction.ALARM:
															{
																mPricePlayer.playPrice(bankBuyPrice, isUp);
																notify(true, isUp, text);
																break;
															}
															case PriceChangeAction.FREQUENT:
															{
																if (AppEngine.getInstance().getAppSetting().getEnablePriceSound())
																	mPricePlayer.playPrice(bankBuyPrice);
																notify(false, isUp, text);
																break;
															}
															case PriceChangeAction.LOW:
															{
																break;
															}
														}
													}

													/**
													 * 初始化上次变动时间
													 */
													private void initialLastChangeTime()
													{
														// 初始化上次变动时间,为-1则表示没有初始化
														if (mLastChangeTime == -1L)
														{
															mLastChangeTime = mQuoteTime;
														}
													}

													/**
													 * 通知价格变动
													 * 
													 * @param alarm
													 * @param isUp
													 * @param text
													 */
													private void notify(boolean alarm, final boolean isUp, String text)
													{
														showStatusNotification(alarm, text);

														// 震动提示涨跌。
														doVibrate(isUp);
													}


													/**
													 * 震动提示涨跌。
													 * 
													 * @param isUp
													 */
													private void doVibrate(final boolean isUp)
													{
														if (AppEngine.getInstance().getAppSetting().getEnableVibrate())
														{
															vibrate(isUp ? AppInt.NUMBER_2 : AppInt.NUMBER_1);
														}
													}

													/**
													 * 系统状态栏给出最近行情变动
													 * 
													 * @param alarm
													 * @param text
													 */
													private void showStatusNotification(boolean alarm, String text)
													{

														StringBuilder content = new StringBuilder();
														content.append(text).append(StringUtil.getString(R.string.max_middle)).append(mMaxMiddle)
																.append(StringUtil.getString(R.string.min_middle)).append(mMinMiddle);

														if (alarm)
														{
															content.append(StringUtil.getString(R.string.update_time)).append(mTime);
														}

														mNotification.clearNotification();
														mNotification.showNotification(android.R.drawable.ic_menu_info_details, text, StringUtil
																.getString(R.string.latest_price), content.toString(), false,
																alarm ? StatusBarNotification.ALARM_NOTIFY_ID++ : StatusBarNotification.NOTIFY_ID);
													}

													@Override
													public void onTaskCancel(Task task)
													{
														mStatus = task.getStatus();
														mErrorMsg = task.getErrorMsg();
														Log.d(TAG, "onTaskCancel");
													}
												};

	/**
	 * 
	 * @param timeUsed
	 *            已经消耗的时间，单位毫秒
	 */
	private void cycle(int timeUsed, int what)
	{
		if (!mHasExited)
		{
			mHandler.removeMessages(what);

			if (TIME_USED_RIGHT_NOW == timeUsed)
			{
				mHandler.sendEmptyMessage(what);
			}
			else
			{

				int delayTime = mIntervalSec;

				if (timeUsed < mIntervalSec)
				{
					delayTime = mIntervalSec - timeUsed;
				}
				else
				{
					delayTime = 0;
				}

				mHandler.sendEmptyMessageDelayed(what, delayTime);
			}
		}
	}

	private int getPriceChangeAction(float priceDifference)
	{

		final long useTime = mQuoteTime - mLastChangeTime;

		final float difference = Math.abs(priceDifference);
		final float thoreshold = PRICE_RATE * useTime;
		final boolean isUp = priceDifference > 0.001f;
		final int validEventTimeSpan = SEQUENCE_CHANGE_INTERVAL + mIntervalSec;
		Log.d(TAG, "shouldAlarm() diff=" + difference);
		Log.d(TAG, "shouldAlarm() useTime=" + useTime);
		Log.d(TAG, "shouldAlarm() thoreshold=" + thoreshold);

		// 加0.01F是为了防止浮点误差导致比较
		if ((isUp && mIsLastChangeRaise || !(isUp || mIsLastChangeRaise)) && System.currentTimeMillis() - mLastChangeTime <= validEventTimeSpan
				|| Math.abs(priceDifference) >= PRCIE_DIFFERENCE_THRESHOLD)
		{
			return PriceChangeAction.ALARM;
		}
		else
		{
			return PriceChangeAction.FREQUENT;
		}

	}
	
	public float getBankBuyPrice()
	{
		if (TextUtils.isEmpty(mBankBuy))
		{
			return 0.0f;
		}
		else
		{
			return Float.valueOf(mBankBuy);
		}
	}
}
