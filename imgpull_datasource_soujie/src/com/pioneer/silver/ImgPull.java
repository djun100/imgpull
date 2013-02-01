package com.pioneer.silver;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.text.SimpleDateFormat;

import android.app.ProgressDialog;
import android.app.TabActivity;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.ServiceConnection;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TabHost;
import android.widget.TextView;
import android.widget.Toast;

import com.pioneer.constant.AppInt;
import com.pioneer.constant.AppString;
import com.pioneer.engine.AppEngine;
import com.pioneer.k.PatternCompareFactory;
import com.pioneer.setting.AppSetting;
import com.pioneer.silver.service.IDataUpdateService;
import com.pioneer.task.ImageSaveTask;
import com.pioneer.util.FileUtil;
import com.pioneer.util.StringUtil;

public class ImgPull extends TabActivity
{

	private static final String				TAG							= "ImgPull";

	private TextView						mPriceInfo;
	private TextView						mPriceTick;
	private TextView						mTextViewInfo;
	private ImageView						mImageViews[];
	private Button							mBtnPullImage;
	private Button							mBtnPullPrice;
	private boolean							mServiceIsRunning;
	private String							mImgUrls[];
	private String							mTitles[];
	private SimpleDateFormat				mSimpleDateFormat;
	private int								mImgViewId[]				= { R.id.myImageView1, R.id.myImageView2, R.id.myImageView3,
			R.id.myImageView4											};

	private int								mImgIndex					= 0;
	private IDataUpdateService				mService;
	private boolean							mHasBindService				= false;
	final Intent							intent						= new Intent(AppString.SERVICE_ACTION);

	private ProgressDialog					mProgress					= null;
	private SeekBar							mSeekBar;
	private TextView						mProgressText;
	private int[]							mServiceStatusLock			= new int[0];
	private int[]							mPullImageLock				= new int[0];


	private CheckBox						mThreePositive;
	private CheckBox						mThreeNegtive;
	private CheckBox						mFiveNegtive;
	private CheckBox						mFivePositive;
	private CheckBox						mStepIncrease;
	private CheckBox						mStepDecrease;


	private static final int				MENU_ITEM_VOLUME			= Menu.FIRST + 1;
	private static final int				MENU_ITEM_VIBRATE			= Menu.FIRST + 3;
	private static final int				MENU_ITEM_EXIT				= Menu.FIRST + 6;



	// 用来标记服务状态查询是否完成。
	private boolean							mSerciceStatusInitialized	= false;

	private Handler							mHandler					= new UIHandler();

	private ChechBoxCheckedChangedListener	mCheckBoxListener			= new ChechBoxCheckedChangedListener();

	private ServiceConnection				serviceConnection			= new ServiceConnection()
																		{

																			@Override
																			public void onServiceConnected(ComponentName name, IBinder service)
																			{
																				Log.d(TAG, "onServiceConnected");
																				mService = (IDataUpdateService) service;

																				if (mService != null)
																				{
																					mService.setUpdateHandler(mHandler);
																				}
																			}

																			@Override
																			public void onServiceDisconnected(ComponentName name)
																			{
																				Log.d(TAG, "onServiceDisconnected");
																				mService.setUpdateHandler(null);
																				mService = null;
																			}
																		};

	@Override
	public void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		AppEngine.getInstance().setContext(this);
		AppEngine.getInstance().setDataKManager(FileUtil.loadDataKSequenceManager(AppString.FILE_NAME_DATAK_SEQUENCE_MANAGER));

		TabHost tabHost = getTabHost();
		LayoutInflater.from(this).inflate(R.layout.main, tabHost.getTabContentView(), true);

		final CharSequence titlePrice = StringUtil.getString(R.string.tab_host_title_price);
		final CharSequence titlePic = StringUtil.getString(R.string.tab_host_title_pic);
		final CharSequence titleSetting = StringUtil.getString(R.string.tab_host_title_setting);
		tabHost.addTab(tabHost.newTabSpec(titlePrice.toString()).setIndicator(titlePrice).setContent(R.id.view1));
		tabHost.addTab(tabHost.newTabSpec(titlePic.toString()).setIndicator(titlePic).setContent(R.id.view2));
		tabHost.addTab(tabHost.newTabSpec(titleSetting.toString()).setIndicator(titleSetting).setContent(R.id.view3));

		initialUI();

		mBtnPullImage.setOnClickListener(new Button.OnClickListener()
		{
			@Override
			public void onClick(View arg0)
			{
				mProgress = showProgress(mHandler, ImgPull.this, null, StringUtil.getString(R.string.imge_pull_progress), false, true);
				new Thread(new Runnable()
				{
					@Override
					public void run()
					{
						synchronized (mPullImageLock)
						{
							pullSilverImg();
						}

						hideProgress(mProgress);
						mProgress = null;
					}
				}, "pullSilverImg").start();
			}
		});

		mBtnPullPrice.setOnClickListener(new Button.OnClickListener()
		{
			@Override
			public void onClick(View arg0)
			{

				new Thread(new Runnable()
				{
					@Override
					public void run()
					{

						synchronized (mServiceStatusLock)
						{
							if (!mSerciceStatusInitialized)
							{
								// 等待服务状态初始化完成。
								try
								{
									mServiceStatusLock.wait();
								}
								catch (InterruptedException e)
								{
									e.printStackTrace();
								}
							}

							if (mServiceIsRunning)
							{
								// 停止已经运行的服务。
								if (mHasBindService)
									unbindService(serviceConnection);

								mHasBindService = false;
							}
							else
							{
								// 开启服务。
								startServiceByBind();
							}
							mServiceIsRunning = !mServiceIsRunning;

							mHandler.post(new Runnable()
							{
								@Override
								public void run()
								{
									int textResId = mServiceIsRunning ? R.string.stop_service : R.string.start_service;
									mBtnPullPrice.setText(textResId);
								}
							});
						}
					}
				}, "switchService").start();
			}

		});
	}

	/**
	 * 通过连接服务的方式启动service。
	 */
	private void startServiceByBind()
	{
		bindService(intent, serviceConnection, BIND_AUTO_CREATE);
		mHasBindService = true;
	}

	/**
	 * 初始化UI
	 */
	private void initialUI()
	{
		mSimpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		mImgUrls = getResources().getStringArray(R.array.price_urls);
		mTitles = getResources().getStringArray(R.array.price_titles);
		mImageViews = new ImageView[mTitles.length];

		mBtnPullImage = (Button) findViewById(R.id.button_pull_img);
		mTextViewInfo = (TextView) findViewById(R.id.myTextView1);
		mBtnPullPrice = (Button) findViewById(R.id.button_pull_price);
		mPriceInfo = (TextView) findViewById(R.id.TextView_price_info);
		mPriceTick = (TextView) findViewById(R.id.TextView_price_tick);

		// Initialize seekbar
		mSeekBar = (SeekBar) findViewById(R.id.seek);
		mSeekBar.setMax(AppInt.SEEKBAR_MAX);
		mProgressText = (TextView) findViewById(R.id.progress);
		mSeekBar.setOnSeekBarChangeListener(new SeekBarListener());
		mSeekBar.setProgress(AppEngine.getInstance().getAppSetting().getUpdateInterval() / 1000);


		// intialize checkboxes.
		mThreePositive = (CheckBox) findViewById(R.id.pattern_analyses_three_positive);
		mThreeNegtive = (CheckBox) findViewById(R.id.pattern_analyses_three_negtive);
		mFiveNegtive = (CheckBox) findViewById(R.id.pattern_analyses_five_negtive);
		mFivePositive = (CheckBox) findViewById(R.id.pattern_analyses_five_positive);
		mStepIncrease = (CheckBox) findViewById(R.id.pattern_analyses_step_increase);
		mStepDecrease = (CheckBox) findViewById(R.id.pattern_analyses_step_decrease);

		mThreePositive.setOnCheckedChangeListener(mCheckBoxListener);
		mThreePositive.setChecked(AppEngine.getInstance().getAppSetting().getThreePositive());
		mThreeNegtive.setOnCheckedChangeListener(mCheckBoxListener);
		mThreeNegtive.setChecked(AppEngine.getInstance().getAppSetting().getThreeNegtive());
		mFiveNegtive.setOnCheckedChangeListener(mCheckBoxListener);
		mFiveNegtive.setChecked(AppEngine.getInstance().getAppSetting().getFiveNegtive());
		mFivePositive.setOnCheckedChangeListener(mCheckBoxListener);
		mFivePositive.setChecked(AppEngine.getInstance().getAppSetting().getFivePositive());
		mStepDecrease.setOnCheckedChangeListener(mCheckBoxListener);
		mStepDecrease.setChecked(AppEngine.getInstance().getAppSetting()
				.getPatternAnalyseSwitch(PatternCompareFactory.CompareType.STEP_DECREASE.name()));
		mStepIncrease.setOnCheckedChangeListener(mCheckBoxListener);
		mStepIncrease.setChecked(AppEngine.getInstance().getAppSetting()
				.getPatternAnalyseSwitch(PatternCompareFactory.CompareType.STEP_INCREASE.name()));

		initialService();

		int i = 0;
		while (i < mTitles.length)
		{
			mImageViews[i] = (ImageView) findViewById(mImgViewId[i]);
			i++;
		}
	}

	private void initialService()
	{

		new Thread(new Runnable()
		{

			@Override
			public void run()
			{
				final int textRes;
				synchronized (mServiceStatusLock)
				{
					if (mServiceIsRunning = AppEngine.getInstance().isServiceRunning(AppString.SERVICE_NAME))
					{
						textRes = R.string.stop_service;
						stopService(intent);
						startServiceByBind();
					}
					else
					{
						textRes = R.string.start_service;
					}

					mHandler.post(new Runnable()
					{
						@Override
						public void run()
						{
							mBtnPullPrice.setText(textRes);
						}
					});

					mServiceStatusLock.notify();
					mSerciceStatusInitialized = true;
				}

			}
		}, "initialServiceStatus").start();
	}

	/**
	 * 更新服务拉取数据的间隔。
	 * 
	 * @param newInterval
	 */
	private void updateServiceSetting(int newInterval)
	{
		if (newInterval > AppInt.UPDATE_INTERVAL_MIN)
		{
			mService.setUpdateInterval(newInterval);
		}
	}

	@Override
	protected void onDestroy()
	{
		Log.d(TAG, "onDestroy");
		if (mHasBindService)
			unbindService(serviceConnection);

		if (mServiceIsRunning)
		{
			final Intent intent = new Intent(AppString.SERVICE_ACTION);
			startService(intent);
		}
		super.onDestroy();
	}

	private void pullSilverImg()
	{
		try
		{
			String imgUrl = "";
			URL url = null;
			int size = Math.min(mTitles.length, mImgUrls.length);
			for (mImgIndex = 0; mImgIndex < size; mImgIndex++)
			{
				mProgress.setProgress((mImgIndex + 1) * 100 / size);
				imgUrl = mImgUrls[mImgIndex];
				url = new URL(imgUrl);
				HttpURLConnection connection = (HttpURLConnection) url.openConnection();
				connection.connect();
				InputStream is = connection.getInputStream();
				final Bitmap bm = BitmapFactory.decodeStream(is);
				is.close();
				connection.disconnect();
				new ImageSaveTask(bm, getFileName(mTitles[mImgIndex]));
				new UiUpdater(mImageViews[mImgIndex], bm, mTextViewInfo, mSimpleDateFormat.format(new java.util.Date()));

			}
		}
		catch (MalformedURLException e)
		{
			new UiUpdater(mImageViews[mImgIndex], null, mTextViewInfo, "MalformedURLException:" + e.toString());
			e.printStackTrace();
		}
		catch (IOException e)
		{
			new UiUpdater(mImageViews[mImgIndex], null, mTextViewInfo, "IOException:" + e.toString());
			e.printStackTrace();
		}
	}

	class UiUpdater implements Runnable
	{
		private ImageView	mPriceImage;
		private Bitmap		mBitmap;
		private TextView	mTips;
		private String		mText;

		public UiUpdater(ImageView imgCtrl, Bitmap img, TextView tips, String text)
		{
			if (imgCtrl != null && tips != null && text != null)
			{
				mPriceImage = imgCtrl;
				mBitmap = img;
				mTips = tips;
				mText = text;

				new Thread(this, "UiUpdater").start();
			}
		}

		@Override
		public void run()
		{
			mHandler.post(new Runnable()
			{
				// 在主线程更新UI。
				@Override
				public void run()
				{
					mTips.setText(mText);
					mPriceImage.setImageBitmap(mBitmap);
				}
			});
		}
	}

	/**
	 * 拼接文件名称。
	 * 
	 * @return
	 */
	private String getFileName(String name)
	{
		StringBuilder buf = new StringBuilder();
		buf.append(StringUtil.getCurrentTime());
		buf.append("_");

		if (!TextUtils.isEmpty(name))
		{
			buf.append(name);
		}
		buf.append(getString(R.string.file_save_extension));
		return buf.toString();
	}

	private ProgressDialog showProgress(Handler handler, final Context context, final CharSequence title, final CharSequence message,
			final boolean indeterminate, boolean cancelable)
	{
		final ProgressDialog dialog = new ProgressDialog(context);
		dialog.setTitle(title);
		dialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
		dialog.setProgress(100);
		dialog.setMessage(message);
		dialog.setIndeterminate(indeterminate);
		dialog.setCancelable(false);
		dialog.setOnCancelListener(null);

		handler.post(new Runnable()
		{
			public void run()
			{
				dialog.show();
			}
		});

		return dialog;
	}

	private void hideProgress(DialogInterface dialog)
	{
		if (dialog != null)
		{
			dialog.dismiss();
		}
	}


	@Override
	public boolean onCreateOptionsMenu(Menu menu)
	{
		/*
		 * add()方法的四个参数，依次是：
		 * 1、组别，如果不分组的话就写Menu.NONE,
		 * 2、Id，这个很重要，Android根据这个Id来确定不同的菜单
		 * 3、顺序，那个菜单现在在前面由这个参数的大小决定
		 * 4、文本，菜单的显示文本
		 */

		menu.add(Menu.NONE, MENU_ITEM_VOLUME, 0, StringUtil.getString(R.string.menu_volume)).setIcon(R.drawable.ic_lock_silent_mode_off);
		menu.add(Menu.NONE, MENU_ITEM_VIBRATE, 1, StringUtil.getString(R.string.menu_vibrate)).setIcon(android.R.drawable.star_big_on);
		menu.add(Menu.NONE, MENU_ITEM_EXIT, 2, StringUtil.getString(R.string.menu_exit)).setIcon(android.R.drawable.ic_lock_power_off);

		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item)
	{
		final AppSetting appSetting = AppEngine.getInstance().getAppSetting();
		switch (item.getItemId())
		{
			case MENU_ITEM_EXIT:
			{
				finish();
				break;
			}
			case MENU_ITEM_VOLUME:
			{
				appSetting.setEnablePriceSound(!appSetting.getEnablePriceSound());
				Toast.makeText(this, appSetting.getEnablePriceSound() ? R.string.menu_tip_enable_volume : R.string.menu_tip_disable_volume,
						Toast.LENGTH_SHORT).show();
				break;
			}
			case MENU_ITEM_VIBRATE:
			{
				appSetting.setEnableVibrate(!appSetting.getEnableVibrate());
				Toast.makeText(this,
						appSetting.getEnableVibrate() ? R.string.menu_tip_price_vibrate_enable : R.string.menu_tip_price_vibrate_disable,
						Toast.LENGTH_SHORT).show();
				break;
			}
		}

		return true;
	}

	@Override
	public boolean onPrepareOptionsMenu(Menu menu)
	{
		MenuItem item = menu.getItem(0);
		item.setIcon(AppEngine.getInstance().getAppSetting().getEnablePriceSound() ? R.drawable.ic_lock_silent_mode_off
				: R.drawable.ic_lock_silent_mode);

		item = menu.getItem(1);
		item.setIcon(AppEngine.getInstance().getAppSetting().getEnableVibrate() ? R.drawable.star_big_on : R.drawable.star_big_off);

		return true;
	}


	/**
	 * 滑动条监听器
	 * 
	 * @author Denverhan
	 * 
	 */
	private class SeekBarListener implements SeekBar.OnSeekBarChangeListener
	{

		private int	mProgress;

		@Override
		public void onProgressChanged(SeekBar seekBar, int progress, boolean fromTouch)
		{
			StringBuilder builder = new StringBuilder();
			builder.append(StringUtil.getString(R.string.setting_seek_time_interval)).append(progress)
					.append(StringUtil.getString(R.string.setting_seek_time_unit));
			mProgressText.setText(builder.toString());
			mProgress = progress;
		}

		@Override
		public void onStartTrackingTouch(SeekBar arg0)
		{

		}

		@Override
		public void onStopTrackingTouch(SeekBar arg0)
		{
			updateServiceSetting(mProgress * 1000);
		}
	}

	private class UIHandler extends Handler
	{
		@Override
		public void handleMessage(Message msg)
		{

			if (msg == null)
			{
				return;
			}

			switch (msg.what)
			{
				case AppInt.MSG_CODE_UPDATE_PRICE:
				{
					if (null == msg.obj)
					{
						break;
					}

					mPriceInfo.setText(msg.obj.toString());
					break;
				}
				case AppInt.MSG_CODE_UPDATE_TICK:
				{
					if (null == msg.obj)
					{
						break;
					}

					mPriceTick.setText(msg.obj.toString() + "\n" + mPriceTick.getText().toString());

					break;
				}
			}

			super.handleMessage(msg);
		}
	}

	private class ChechBoxCheckedChangedListener implements OnCheckedChangeListener
	{

		@Override
		public void onCheckedChanged(CompoundButton buttonView, boolean isChecked)
		{
			if (buttonView == mThreePositive)
			{
				AppEngine.getInstance().getAppSetting().setThreePositive(isChecked);
			}
			else if (buttonView == mThreeNegtive)
			{
				AppEngine.getInstance().getAppSetting().setThreeNegtive(isChecked);
			}
			else if (buttonView == mFiveNegtive)
			{
				AppEngine.getInstance().getAppSetting().setFiveNegtive(isChecked);
			}
			else if (buttonView == mFivePositive)
			{
				AppEngine.getInstance().getAppSetting().setFivePositive(isChecked);
			}
			else if (buttonView == mStepDecrease)
			{
				AppEngine.getInstance().getAppSetting().setPatternAnalyseSwitch(PatternCompareFactory.CompareType.STEP_DECREASE.name(), isChecked);
			}
			else if (buttonView == mStepIncrease)
			{
				AppEngine.getInstance().getAppSetting().setPatternAnalyseSwitch(PatternCompareFactory.CompareType.STEP_INCREASE.name(), isChecked);
			}

			Log.d(TAG, "Onchecked ischecked=" + isChecked);
		}
	}

}
