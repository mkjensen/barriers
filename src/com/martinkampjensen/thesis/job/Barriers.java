/**
 * Copyright 2010-2011 Martin Kamp Jensen
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *     http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.martinkampjensen.thesis.job;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;

import com.martinkampjensen.thesis.barriers.Constructor;
import com.martinkampjensen.thesis.barriers.coloring.Colorer;
import com.martinkampjensen.thesis.barriers.structuring.Structurer;
import com.martinkampjensen.thesis.util.Util;

/**
 * TODO: Document {@link Barriers}.
 */
@XmlType(propOrder = {})
public final class Barriers extends Group
{
	private static final String DEFAULT_CONSTRUCTOR =
		"com.martinkampjensen.thesis.barriers.AllMinimaPairsConstructor";
	@XmlElement(defaultValue = DEFAULT_CONSTRUCTOR, required = true)
	private String constructor = DEFAULT_CONSTRUCTOR;

	private static final double DEFAULT_THRESHOLD = Double.MAX_VALUE;
	@XmlElement(defaultValue = ""+DEFAULT_THRESHOLD)
	private double threshold = DEFAULT_THRESHOLD;

	private static final String DEFAULT_STRUCTURER =
		"com.martinkampjensen.thesis.barriers.structuring.WeightStructurer";
	@XmlElement(defaultValue = DEFAULT_STRUCTURER)
	private String structurer = DEFAULT_STRUCTURER;

	private static final String DEFAULT_COLORER =
		"com.martinkampjensen.thesis.barriers.coloring.AngleDifferenceColorer";
	@XmlElement(defaultValue = DEFAULT_COLORER)
	private String colorer = DEFAULT_COLORER;

	Barriers()
	{
		super();
	}

	public Constructor constructor()
	{
		// TODO: Create new constructor every time?
		return Util.instantiate(constructor);
	}

	public double threshold()
	{
		return threshold;
	}

	public Structurer structurer()
	{
		// TODO: Create new structurer every time?
		return Util.instantiate(structurer);
	}

	public Colorer colorer()
	{
		// TODO: Create new colorer every time?
		return Util.instantiate(colorer);
	}

	@Override
	protected void toString(StringBuilder sb)
	{
		add(sb, "constructor", constructor, DEFAULT_CONSTRUCTOR);
		add(sb, "threshold", threshold, DEFAULT_THRESHOLD);
		add(sb, "structurer", structurer, DEFAULT_STRUCTURER);
		add(sb, "colorer", colorer, DEFAULT_COLORER);
	}
}
