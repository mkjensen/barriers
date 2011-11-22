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

import com.martinkampjensen.thesis.model.ZMatrix;

/**
 * This class provides a skeletal implementation of the {@link ZMatrix}
 * interface, to minimize the effort required to implement this interface.
 */
public abstract class AbstractZMatrix extends AbstractComparableModel
implements ZMatrix
{
	protected AbstractZMatrix()
	{
	}

	@Override
	public final double getAngle(int id)
	{
		return getTorsionAngle(id);
	}

	@Override
	public final void setAngle(int id, double value)
	{
		setTorsionAngle(id, value);
	}

	@Override
	public final int additionalSize()
	{
		return NUMBER_OF_REFERENCE_ROWS;
	}
}
