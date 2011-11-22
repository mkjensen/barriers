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
 * TODO: Document {@link Options}.
 */
@XmlType(propOrder = {})
public final class Options extends Group
{
	private static final long DEFAULT_SEED = 42;
	@XmlElement(defaultValue = ""+DEFAULT_SEED)
	private long seed = DEFAULT_SEED;

	@XmlElement
	private Analysis analysis = new Analysis();

	@XmlElement
	private Barriers barriers = new Barriers();

	@XmlElement
	private Connection connection = new Connection();

	@XmlElement
	private Evaluation evaluation = new Evaluation();

	@XmlElement
	private Minimization minimization = new Minimization();

	@XmlElement
	private Sampling sampling = new Sampling();

	Options()
	{
		super();
	}

	public long seed()
	{
		return seed;
	}

	public Analysis analysis()
	{
		return analysis;
	}

	public Barriers barriers()
	{
		return barriers;
	}

	public Connection connection()
	{
		return connection;
	}

	public Evaluation evaluation()
	{
		return evaluation;
	}

	public Minimization minimization()
	{
		return minimization;
	}

	public Sampling sampling()
	{
		return sampling;
	}

	@Override
	protected void toString(StringBuilder sb)
	{
		add(sb, "seed", seed, DEFAULT_SEED);
		analysis.toString(sb);
		barriers.toString(sb);
		connection.toString(sb);
		evaluation.toString(sb);
		minimization.toString(sb);
		sampling.toString(sb);
	}
}
