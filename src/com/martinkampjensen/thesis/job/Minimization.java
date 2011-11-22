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

import com.martinkampjensen.thesis.minimization.Minimizer;
import com.martinkampjensen.thesis.util.Util;

/**
 * TODO: Document {@link Minimization}.
 */
@XmlType(propOrder = {})
public final class Minimization extends Group
{
	private static final String DEFAULT_MINIMIZER =
		"com.martinkampjensen.thesis.minimization.NelderMeadMinimizer";
	@XmlElement(defaultValue = DEFAULT_MINIMIZER, required = true)
	private String minimizer = DEFAULT_MINIMIZER;

	private static final int DEFAULT_MINIMA = 10;
	@XmlElement(defaultValue = ""+DEFAULT_MINIMA)
	private int minima = DEFAULT_MINIMA;

	Minimization()
	{
		super();
	}

	public Minimizer minimizer()
	{
		// TODO: Create new minimizer every time?
		return Util.instantiate(minimizer);
	}

	public int minima()
	{
		return minima;
	}

	@Override
	protected void toString(StringBuilder sb)
	{
		add(sb, "minimizer", minimizer, DEFAULT_MINIMIZER);
		add(sb, "minima", minima, DEFAULT_MINIMA);
	}
}
