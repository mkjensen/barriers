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

import com.martinkampjensen.thesis.model.AtomModel;

/**
 * This class provides a skeletal implementation, to minimize the effort
 * required to instantiate specific {@link AtomModel} instances. This class
 * uses {@link ZMatrixImpl} as the {@link AtomModel} implementation.
 * <p>
 * An implementation only needs to implement {@link #create()}.
 * <p>
 * TODO: Document {@link AbstractAtomModelInstance} and think about its name and functionality.
 */
public abstract class AbstractAtomModelInstance extends ZMatrixImpl
{
	private static final long serialVersionUID = -7819472778460857288L;

	/**
	 * Constructs a new {@link ZMatrixImpl} that needs to be set up.
	 */
	protected AbstractAtomModelInstance()
	{
		super();

		create();
	}

	/**
	 * Sets up a {@link ZMatrixImpl}.
	 */
	protected abstract void create();
}
