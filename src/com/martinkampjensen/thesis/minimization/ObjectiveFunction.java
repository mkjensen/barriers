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

package com.martinkampjensen.thesis.minimization;

import org.apache.commons.math.analysis.MultivariateRealFunction;

import com.martinkampjensen.thesis.model.Model;

/**
 * An implementation of the {@link MultivariateRealFunction} interface of the
 * Apache Commons Math package.
 * <p>
 * This implementation enables the use of {@link Model}s for optimization.
 * 
 * @see <a href="http://commons.apache.org/math/">Apache Commons Math</a>
 */
public final class ObjectiveFunction implements MultivariateRealFunction
{
	private final Model _model;

	/**
	 * Constructs a new {@link MultivariateRealFunction} for use with the
	 * optimization classes of the Apache Commons Math package.
	 * 
	 * @param model the model being optimized.
	 * @throws NullPointerException if <code>model</code> is <code>null</code>.
	 * @see <a href="http://commons.apache.org/math/">Apache Commons Math</a>
	 */
	public ObjectiveFunction(Model model)
	{
		if(model == null) {
			throw new NullPointerException("model == null");
		}

		_model = model.copy();
	}

	/**
	 * {@inheritDoc}
	 * <p>
	 * This implementation does not throw an {@link IllegalArgumentException} if
	 * <code>point.length != model.size()</code> where <code>model</code> is
	 * the {@link Model} used to construct this instance of
	 * {@link ObjectiveFunction}. Instead, an ArrayIndexOutOfBoundsException is
	 * thrown if <code>point.length &lt; model.size()</code>. This is to avoid
	 * the overhead of checking <code>point.length</code> at each method
	 * invocation.
	 * 
	 * @throws NullPointerException if <code>point == null</code>.
	 * @throws ArrayIndexOutOfBoundsException if
	 * <code>point.length &lt; model.size()</code> where <code>model</code> is
	 * the {@link Model} used to construct this instance of
	 * {@link ObjectiveFunction}.
	 */
	@Override
	public double value(double[] point)
	{
		_model.setAngles(point);
		return _model.evaluate();
	}
}
