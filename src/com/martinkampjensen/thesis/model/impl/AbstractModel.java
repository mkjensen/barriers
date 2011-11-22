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

import com.martinkampjensen.thesis.Main;
import com.martinkampjensen.thesis.StatusCode;
import com.martinkampjensen.thesis.model.Model;

/**
 * This class provides a skeletal implementation of the {@link Model} interface,
 * to minimize the effort required to implement this interface.
 * <p>
 * An implementation only needs to implement {@link #calculateFitness()} method.
 */
public abstract class AbstractModel extends AbstractComparableModel
{
	private final double[] _angles;
	private boolean _hasChanged;
	private double _fitness;

	/**
	 * Constructs a new {@link Model} with a fixed number of angles.
	 * 
	 * @param size the number of angles in the model.
	 * @throws IllegalArgumentException if <code>size &lt; 1</code>.
	 */
	protected AbstractModel(int size)
	{
		if(size < 1) {
			throw new IllegalArgumentException("size < 1");
		}

		_angles = new double[size];
		_hasChanged = true;
	}

	@Override
	public final String toString()
	{
		final StringBuilder sb = new StringBuilder();
		final int size = this.size();

		sb.append(size);
		sb.append('\n');

		for(int i = 0; i < size; i++) {
			sb.append(i);
			sb.append('\t');
			sb.append(this.getAngle(i));
			sb.append('\n');
		}

		return sb.toString();
	}

	@Override
	public final int size()
	{
		return _angles.length;
	}

	@Override
	public final boolean hasChanged()
	{
		return _hasChanged;
	}

	/**
	 * {@inheritDoc}
	 * <p>
	 * This implementation returns the value returned by
	 * {@link #calculateFitness()} if {@link #hasChanged()} returns
	 * <code>true</code>. Otherwise, it returns a cached value. 
	 */
	@Override
	public final double evaluate()
	{
		if(_hasChanged) {
			_fitness = calculateFitness();
			_hasChanged = false;
		}

		return _fitness;
	}

	/**
	 * {@inheritDoc}
	 * <p>
	 * This implementation instantiates and returns a copy of this object (which
	 * is a subclass of <code>AbstractModel</code>) using reflection. The class
	 * is instantiated as if by a new expression with an empty argument list. If
	 * this is not acceptable, then subclasses must override this method.
	 */
	@Override
	public Model copy()
	{
		final Class<? extends Model> modelClass = this.getClass();
		Model copy = null;

		try {
			copy = modelClass.newInstance();
		}
		catch(Exception e) {
			Main.errorExit(e, StatusCode.REFLECTION);
		}

		final int size = size();
		for(int i = 0; i < size; i++) {
			copy.setAngle(i, this.getAngle(i));
		}

		return copy;
	}

	@Override
	public final double getAngle(int id)
	{
		return _angles[id];
	}

	@Override
	public final void setAngle(int id, double value)
	{
		_angles[id] = value;
		_hasChanged = true;
	}

	/**
	 * Computes and returns the fitness value of this object.
	 * <p>
	 * The implementation should always perform the calculations. Whether or not
	 * the method is called from {@link #evaluate()} depends on
	 * {@link #hasChanged()}. 
	 * 
	 * @return the fitness value of this object.
	 */
	protected abstract double calculateFitness();
}
