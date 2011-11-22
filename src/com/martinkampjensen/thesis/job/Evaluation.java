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

import com.martinkampjensen.thesis.evaluation.Evaluator;
import com.martinkampjensen.thesis.util.Util;

/**
 * TODO: Document {@link Evaluation}.
 */
@XmlType(propOrder = {})
public final class Evaluation extends Group
{
	private static final String DEFAULT_EVALUATOR =
		"com.martinkampjensen.thesis.evaluation.LennardJonesEvaluator";
	@XmlElement(defaultValue = DEFAULT_EVALUATOR, required = true)
	private String evaluator = DEFAULT_EVALUATOR;

	Evaluation()
	{
		super();
	}

	public Evaluator evaluator()
	{
		// TODO: Create new evaluator every time?
		return Util.instantiate(evaluator);
	}

	@Override
	protected void toString(StringBuilder sb)
	{
		add(sb, "evaluator", evaluator, DEFAULT_EVALUATOR);
	}
}
