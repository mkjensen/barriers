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

package com.martinkampjensen.thesis.models;

import com.martinkampjensen.thesis.model.impl.AbstractModel;

/**
 * Angle 0 is used as input to the sine function, angle 1 is ignored.
 */
public final class AModel extends AbstractModel
{
	public AModel()
	{
		super(2);
	}

	@Override
	protected double calculateFitness()
	{
		return Math.sin(getAngle(0));
	}
}
