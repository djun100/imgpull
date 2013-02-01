package com.pioneer.constant;

import com.pioneer.silver.R;

/*
 * Copyright (C) 2005-2010 PIONEER Inc.All Rights Reserved.
 * FileName:AppInt.java
 * Description:
 * History:
 * 1.0 Denverhan 2012-10-30 Create
 */

public interface AppInt
{
	final int		UPDATE_INTERVAL			= 10000;
	final int		UPDATE_INTERVAL_MIN		= 2000;
	final int		UPDATE_INTERVAL_STEP	= 5000;

	final long		DI						= 200;
	final long		DA						= 400;
	final long		PAUSE					= 200;

	/** k线容器容纳最大K线数目 */
	final int		DATAK_SIZE				= 200;

	/** K线容器一次释放的k线数目 */
	final int		DATAK_CLEAR_COUNT		= 50;

	/** 一天的秒数 */
	final long		DAY_SECONDS				= 86400L;

	final long[]	NUMBER_0				= new long[] { PAUSE, DA, PAUSE, DA, PAUSE, DA, PAUSE, DA, PAUSE, DA };
	final long[]	NUMBER_1				= new long[] { PAUSE, DI, PAUSE, DA, PAUSE, DA, PAUSE, DA, PAUSE, DA };
	final long[]	NUMBER_2				= new long[] { PAUSE, DI, PAUSE, DI, PAUSE, DA, PAUSE, DA, PAUSE, DA };
	final long[]	NUMBER_3				= new long[] { PAUSE, DI, PAUSE, DI, PAUSE, DI, PAUSE, DA, PAUSE, DA };
	final long[]	NUMBER_4				= new long[] { PAUSE, DI, PAUSE, DI, PAUSE, DI, PAUSE, DI, PAUSE, DA };
	final long[]	NUMBER_5				= new long[] { PAUSE, DI, PAUSE, DI, PAUSE, DI, PAUSE, DI, PAUSE, DI };
	final long[]	NUMBER_6				= new long[] { PAUSE, DA, PAUSE, DI, PAUSE, DI, PAUSE, DI, PAUSE, DI };
	final long[]	NUMBER_7				= new long[] { PAUSE, DA, PAUSE, DA, PAUSE, DI, PAUSE, DI, PAUSE, DI };
	final long[]	NUMBER_8				= new long[] { PAUSE, DA, PAUSE, DA, PAUSE, DA, PAUSE, DI, PAUSE, DI };
	final long[]	NUMBER_9				= new long[] { PAUSE, DA, PAUSE, DA, PAUSE, DA, PAUSE, DA, PAUSE, DI };


	final int[]		NUMBERS					= new int[] { R.raw.number_0, R.raw.number_1, R.raw.number_2, R.raw.number_3, R.raw.number_4,
			R.raw.number_5, R.raw.number_6, R.raw.number_7, R.raw.number_8, R.raw.number_9 };

	// 滑动块的最大值
	final int		SEEKBAR_MAX				= 180;


	final int		MSG_CODE_UPDATE_PRICE	= 1000;
	final int		MSG_CODE_UPDATE_TICK	= 1001;
}
