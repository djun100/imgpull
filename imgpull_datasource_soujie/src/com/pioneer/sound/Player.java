package com.pioneer.sound;

import java.util.Collections;
import java.util.Vector;

import android.content.Context;
import android.media.MediaPlayer;
import android.media.MediaPlayer.OnCompletionListener;
import android.util.Log;

import com.pioneer.constant.AppInt;
import com.pioneer.silver.R;

/*
 * Copyright (C) 2005-2010 PIONEER Inc.All Rights Reserved.
 * FileName:Player.java
 * Description:根据价格播放声音文件，
 * History
 * 1.0 Denverhan 2012-10-11 Create
 */

public class Player
{
	private static final String TAG = "Player";
	private Context			mContext;
	private MediaPlayer		mPlayer;
	private Vector<Integer>	mSoundId;
	private int				mCursor;

	public Player(Context c)
	{
		mSoundId = new Vector<Integer>();
		mContext = c;
	}

	/**
	 * 根据价格产生声音序列。
	 * @param value
	 */
	private void generateSoundSequence(float value)
	{
		mSoundId.clear();
		int multiply = (int) (value * 100);

		int quotient = multiply % 10;
		multiply = multiply / 10;
		while (multiply != 0)
		{
			mSoundId.add(Integer.valueOf(quotient));
			quotient = multiply % 10;
			multiply /= 10;
		}
		
		Collections.reverse(mSoundId);
	}
	
	/**
	 * 播放价格
	 * @param price被播放的价格
	 */
	public void playPrice(String price)
	{
		Log.d(TAG, "playPrice price=" + price);
		Log.d(TAG, "playPrice=" + this);
		try
		{
			float p = Float.parseFloat(price);
			generateSoundSequence(p);
			playSoundSequence();
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
	}
	
	/**
	 * 先播放涨跌，然后播放价格.
	 * @param price被播放的价格
	 */
	public void playPrice(String price, boolean isRaised)
	{
		try
		{
			float p = Float.parseFloat(price);
			generateSoundSequence(p);
			
			if (isRaised)
			{
				mSoundId.add(0, Integer.valueOf(R.raw.frequent_change_up));
			}
			else
			{
				mSoundId.add(0, Integer.valueOf(R.raw.frequent_change_down));
			}
			
			playSoundSequence();
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
	}

	/**
	 * 播放数字序列。
	 */
	private void playSoundSequence()
	{
		mCursor = 0;
		int resId = getDiDa(mSoundId.get(mCursor++));
		
		try
		{
			if (mPlayer != null)
			{
				mPlayer.release();
			}

			mPlayer = MediaPlayer.create(mContext, resId);
			mPlayer.setLooping(false);
			mPlayer.setOnCompletionListener(new OnCompletionListener()
			{
				
				@Override
				public void onCompletion(MediaPlayer mp)
				{
					if (mp != null && mCursor < mSoundId.size())
					{
						int resId = getDiDa(mSoundId.get(mCursor++));
						mp.reset();
						mp = MediaPlayer.create(mContext, resId);
						mp.setLooping(false);
						mp.setOnCompletionListener(this);
						mp.start();
					}
					
				}
			});
			mPlayer.start();
		}
		catch (IllegalStateException e)
		{
			e.printStackTrace();
		}
	}
	
	private int getDiDa(int n)
	{
		if (n < 0 || n > 9)
		{
			return n;

		}

		return AppInt.NUMBERS[n];
	}
}
