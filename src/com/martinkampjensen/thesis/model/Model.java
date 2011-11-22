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

package com.martinkampjensen.thesis.model;

import java.util.List;

import com.martinkampjensen.thesis.Constant;
import com.martinkampjensen.thesis.evaluation.Evaluator;

/**
 * A {@link Model} consists of a number of angles that can be manipulated and a
 * fitness function that returns a fitness value based on the values of those
 * angles.
 * <p>
 * A {@link Model} must have a size no less than <code>1</code>, that is, the
 * {@link #size()} method must return a value greater than or equal to
 * <code>1</code>.
 * <p>
 * The angles in a {@link Model} must be assigned ids from <code>0</code> to
 * <code>size() - 1</code>.
 */
public interface Model extends Comparable<Model>
{
	/**
	 * For two models <code>a</code> and <code>b</code>, <code>a &gt; b</code>
	 * if and only if <code>(a.</code>{@link #evaluate()}
	 * <code>- b.</code>{@link #evaluate()}<code>) &gt;</code>
	 * {@link Constant#DOUBLE_PRECISION}. When
	 * <code>Math.abs(a.</code>{@link #evaluate()} <code> -
	 * b.</code>{@link #evaluate()}<code>) &le;</code>
	 * {@link Constant#DOUBLE_PRECISION}, <code>a</code> and <code>b</code> are
	 * equal.
	 * <p>
	 * {@inheritDoc}
	 * @see Math#abs(double)
	 */
	@Override
	int compareTo(Model other);

	/**
	 * Returns the number of angles in this object.
	 * 
	 * @return the number of angles in this object.
	 */
	int size();

	/**
	 * Returns <code>true</code> if this object has changed since
	 * {@link #evaluate()} was called the last time. If {@link #evaluate()}
	 * has not yet been called, <code>true</code> is returned.
	 * 
	 * @return <code>true</code> if this object has changed.
	 */
	boolean hasChanged();

	/**
	 * Computes and returns the fitness value of this object.
	 * <p>
	 * The implementation should only need to perform calculations if
	 * {@link #hasChanged()} returns <code>true</code> immediately before
	 * calling this method.
	 * 
	 * @return the fitness value of this object.
	 */
	double evaluate();

	/**
	 * Returns the evaluator used when calling {@link #evaluate()}.
	 * 
	 * @return the evaluator or <code>null</code> if none is used.
	 */
	Evaluator getEvaluator();

	/**
	 * Creates and returns a deep copy of this object.
	 * 
	 * @return a deep copy of this object.
	 */
	Model copy();

	/**
	 * Returns the value of an angle.
	 * 
	 * @param id the id of the angle.
	 * @return the value of the angle.
	 */
	double getAngle(int id);

	/**
	 * Fills and returns an array with the values of all angles.
	 * 
	 * @param values the array in which the values will be returned.
	 */
	void getAngles(double[] values);

	/**
	 * Sets the value of an angle.
	 * 
	 * @param id the id of the angle.
	 * @param value the value to assign to the angle.
	 */
	void setAngle(int id, double value);

	/**
	 * Sets the values of all angles.
	 * 
	 * @param values the values to assign to the angles.
	 */
	void setAngles(double[] values);

	/**
	 * Sets the angles of this model to the angle values of another.
	 * 
	 * @param model the model containing the angle values to set.
	 */
	void set(Model model);

	/**
	 * Returns the id assigned to this model (optional operation).
	 * <p>
	 * What id means is up to the implementing class which can also choose not
	 * to support ids (or to only support returning ids).
	 * 
	 * @return the id.
	 * @throws UnsupportedOperationException if this class does not support ids.
	 */
	int getId() throws UnsupportedOperationException;

	/**
	 * Sets the id of this model (optional operation).
	 * <p>
	 * What id means is up to the implementing class which can also choose not
	 * to support ids (or to only support returning ids).
	 * 
	 * @param id the id.
	 * @throws UnsupportedOperationException if this class does not support ids
	 *         or if this class does not support changing an already assigned
	 *         id.
	 */
	void setId(int id) throws UnsupportedOperationException;

	/**
	 * Returns this model in PDB format (optional operation).
	 * 
	 * @return this model in PDB format.
	 * @throws UnsupportedOperationException if this operation is not supported
	 *         by this model. 
	 * @see <a href="http://goo.gl/ZBCXQ">Protein Data Bank (file format)</a>
	 */
	String toPdb() throws UnsupportedOperationException;

	/**
	 * HACK: Try to achieve lower per model evaluation time by evaluating
	 * several models in one method call (optional operation).
	 * <p>
	 * This is used as a convenience call to avoid having to use
	 * {@link #getEvaluator()} and using that directly.
	 * <p>
	 * TODO: Remove this hack.
	 * 
	 * @param models the models to evaluate.
	 * @throws UnsupportedOperationException if this model implementation does
	 *         not support evaluating multiple models at once.
	 */
	void evaluate(List<? extends Model> models)
	throws UnsupportedOperationException;

	/**
	 * HACK: Set the fitness value of this model and mark it has not having
	 * changed (optional operation).
	 * <p>
	 * This is used so that fitness values can be assigned to individual models
	 * after fitness values of many models have been calculated.
	 * <p>
	 * TODO: Remove this hack.
	 * 
	 * @param fitness the fitness value.
	 * @throws UnsupportedOperationException if this model implementation does
	 *         not support manually setting the fitness value.
	 */
	void setFitness(double fitness) throws UnsupportedOperationException;
}
