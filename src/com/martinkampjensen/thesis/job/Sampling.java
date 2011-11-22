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

import com.martinkampjensen.thesis.sampling.Sampler;
import com.martinkampjensen.thesis.util.Util;

/**
 * TODO: Document {@link Sampling}.
 */
@XmlType(propOrder = {})
public final class Sampling extends Group
{
	private static final String DEFAULT_SAMPLER =
		"com.martinkampjensen.thesis.sampling.RandomSampler";
	@XmlElement(defaultValue = DEFAULT_SAMPLER, required = true)
	private String sampler = DEFAULT_SAMPLER;

	private static final int DEFAULT_SAMPLES = 10000;
	@XmlElement(defaultValue = ""+DEFAULT_SAMPLES)
	private int samples = DEFAULT_SAMPLES;

	Sampling()
	{
		super();
	}

	public Sampler sampler()
	{
		// TODO: Create new sampler every time?
		return Util.instantiate(sampler);
	}

	public int samples()
	{
		return samples;
	}

	@Override
	protected void toString(StringBuilder sb)
	{
		add(sb, "sampler", sampler, DEFAULT_SAMPLER);
		add(sb, "samples", samples, DEFAULT_SAMPLES);
	}
}
