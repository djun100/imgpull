package com.pioneer.k;

import java.io.Serializable;

/*
 * Copyright (C) 2005-2010 PIONEER Inc.All Rights Reserved.
 * FileName：DataKComparesion.java
 * Description：两根K线比较方法接口。
 * History：
 * 1.0 Denverhan 2013-1-17 Create
 */

public interface DataKComparable<T extends Number & Comparable<T> & Serializable>
{
	int compare(DataK<T> left, DataK<T> right);
}
