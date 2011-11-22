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

package com.martinkampjensen.thesis.util.openbabel;

/**
 * TODO: Document {@link OptimizationAlgorithm}.
 */
public enum OptimizationAlgorithm
{
	/**
	 * Steepest descent.
	 */
	STEEPEST_DESCENT("Steepest descent"),

	/**
	 * Conjugate gradient.
	 */
	CONJUGATE_GRADIENTS("Conjugate gradient");

	private final String _name;

	private OptimizationAlgorithm(String name)
	{
		_name = name;
	}

	@Override
	public String toString()
	{
		return _name;
	}
}
