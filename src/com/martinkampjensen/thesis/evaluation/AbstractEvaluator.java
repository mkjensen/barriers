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

package com.martinkampjensen.thesis.evaluation;

import java.util.List;

import com.martinkampjensen.thesis.model.CartesianModel;
import com.martinkampjensen.thesis.model.Model;

/**
 * This class provides a skeletal implementation of the {@link Evaluator}
 * interface, to minimize the effort required to implement this interface.
 * <p>
 * An implementation only needs to implement one or more of the evaluate
 * methods.
 */
public abstract class AbstractEvaluator implements Evaluator
{
	protected AbstractEvaluator()
	{
	}

	/**
	 * @throws UnsupportedOperationException always.
	 */
	@Override
	public double evaluate(Model model)
	{
		throw new UnsupportedOperationException();
	}

	/**
	 * @throws UnsupportedOperationException always.
	 */
	@Override
	public double evaluate(CartesianModel model)
	{
		throw new UnsupportedOperationException();
	}

	/**
	 * @throws UnsupportedOperationException always.
	 */
	@Override
	public double[] evaluate(List<? extends Model> models)
	{
		throw new UnsupportedOperationException();
	}
}
