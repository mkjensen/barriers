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

/**
 * TODO: Document {@link Analysis}.
 */
@XmlType(propOrder = {})
public final class Analysis extends Group
{
	private static final String DEFAULT_OUTPUT = "results/analysis.pdf";
	@XmlElement(defaultValue = DEFAULT_OUTPUT, required = true)
	private String output = DEFAULT_OUTPUT;

	private static final int DEFAULT_CONFIGS = 10;
	@XmlElement(defaultValue = ""+DEFAULT_CONFIGS)
	private int configs = DEFAULT_CONFIGS;

	private static final int DEFAULT_DELTAS = 20;
	@XmlElement(defaultValue = ""+DEFAULT_DELTAS)
	private int deltas = DEFAULT_DELTAS;

	private static final int DEFAULT_SAMPLES = 10000;
	@XmlElement(defaultValue = ""+DEFAULT_SAMPLES)
	private int samples = DEFAULT_SAMPLES;

	private static final double DEFAULT_DELTA = 0.25;
	@XmlElement(defaultValue = ""+DEFAULT_DELTA)
	private double delta = DEFAULT_DELTA;

	Analysis()
	{
		super();
	}

	public String output()
	{
		return output;
	}

	public int configs()
	{
		return configs;
	}

	public int deltas()
	{
		return deltas;
	}

	public int samples()
	{
		return samples;
	}

	public double delta()
	{
		return delta;
	}

	@Override
	protected void toString(StringBuilder sb)
	{
		add(sb, "output", output, DEFAULT_OUTPUT);
		add(sb, "configs", configs, DEFAULT_CONFIGS);
		add(sb, "deltas", deltas, DEFAULT_DELTAS);
		add(sb, "samples", samples, DEFAULT_SAMPLES);
		add(sb, "delta", delta, DEFAULT_DELTA);
	}
}
