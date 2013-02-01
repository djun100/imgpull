package com.pioneer.k;

import java.io.Serializable;
import java.util.List;

/*
 * Copyright (C) 2005-2010 PIONEER Inc.All Rights Reserved.
 * FileName：PatterenComparable.java
 * Description：K线组合分析方法接口。
 * History：
 * 1.0 Denverhan 2013-1-18 Create
 */

public interface PatternComparable<T extends Number & Comparable<T> & Serializable>
{
	PatternAnalyseResult analyseDataKSequence(List<DataK<T>> list);
}
