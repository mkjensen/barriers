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

package com.martinkampjensen.thesis.model.impl;

import java.util.List;

import com.martinkampjensen.thesis.evaluation.Evaluator;
import com.martinkampjensen.thesis.model.Model;
import com.martinkampjensen.thesis.util.Util;

/**
 * This class provides a skeletal implementation of the {@link Model} interface,
 * to minimize the effort required to implement this interface.
 */
public abstract class AbstractComparableModel implements Model
{
	protected AbstractComparableModel()
	{
	}

	@Override
	public String toString()
	{
		final int size = size();
		final StringBuilder sb = new StringBuilder(20 * (3 + size));

		sb.append("--------------------\n");
		sb.append(String.format("%3s %8s %7s\n", "Id", "Value", "% 360"));
		sb.append("--------------------\n");

		for(int i = 0; i < size; i++) {
			final double radians = getAngle(i);
			final double degrees = Math.toDegrees(radians);
			final double degreesMod360 =
				Math.toDegrees(Util.ensureAngleInterval(radians));
			sb.append(String.format("%3d %8.3f %7.3f\n",
					i, degrees, degreesMod360));
		}

		return sb.toString();
	}

	@Override
	public final int compareTo(Model other)
	{
		return Util.compare(this.evaluate(), other.evaluate());
	}

	@Override
	public Evaluator getEvaluator()
	{
		return null;
	}

	@Override
	public void getAngles(double[] values)
	{
		final int size = size();
		for(int i = 0; i < size; i++) {
			values[i] = getAngle(i);
		}
	}

	@Override
	public void setAngles(double[] values)
	{
		final int size = size();
		for(int i = 0; i < size; i++) {
			setAngle(i, values[i]);
		}
	}

	@Override
	public void set(Model model)
	{
		final int size = size();
		for(int i = 0; i < size; i++) {
			setAngle(i, model.getAngle(i));
		}
	}

	/**
	 * @throws UnsupportedOperationException always.
	 */
	@Override
	public int getId()
	{
		throw new UnsupportedOperationException("ids are not supported");
	}

	/**
	 * @throws UnsupportedOperationException always.
	 */
	@Override
	public void setId(int id)
	{
		throw new UnsupportedOperationException("ids are not supported");
	}

	/**
	 * @throws UnsupportedOperationException always.
	 */
	@Override
	public String toPdb()
	{
		throw new UnsupportedOperationException();
	}

	/**
	 * @throws UnsupportedOperationException always.
	 */
	@Override
	public void evaluate(List<? extends Model> models)
	{
		throw new UnsupportedOperationException();
	}	

	/**
	 * @throws UnsupportedOperationException always.
	 */
	@Override
	public void setFitness(double fitness)
	{
		throw new UnsupportedOperationException();
	}
}
