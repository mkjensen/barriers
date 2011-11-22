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

import com.martinkampjensen.thesis.model.Model;

/**
 * Strategies for initializing a {@link Model} that will be minimized.
 */
public enum InitializationStrategy
{
	/**
	 * Do not change the angles of the model before minimizing.
	 */
	UNCHANGED("Unchanged"),

	/**
	 * Randomly change the angles of the model before minimizing.
	 */
	RANDOM("Random"),

	/**
	 * Generate a conformer from the model before minimizing.
	 */
	CONFORMER("Conformer");

	private final String _name;

	private InitializationStrategy(String name)
	{
		_name = name;
	}

	@Override
	public String toString()
	{
		return _name;
	}
}
