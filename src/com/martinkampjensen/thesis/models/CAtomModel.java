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

import com.martinkampjensen.thesis.model.impl.AbstractAtomModelInstance;

/**
 * Looks as {@link BAtomModel}, but atom 2 is defined using atoms 1, -2, and -3
 * instead of atoms 1, -2, and -1. Hence, the torsion angle of atom 6 is 0
 * degrees instead of 180 degrees.
 */
public final class CAtomModel extends AbstractAtomModelInstance
{
	private static final long serialVersionUID = 2267384710840356985L;

	@Override
	protected void create()
	{
		// TODO: Certain values for torsion angles 0 and 1 lead to an error when converting to Cartesian coordinates.
		setSecondReference(1);
		setThirdReference(REF2ID, REF1ID, 1, Math.toRadians(120));
		addRow(REF3ID, REF2ID, REF1ID, 1, Math.toRadians(120), 0);
		addRow(REF2ID, REF3ID, 0, 1, Math.toRadians(120), Math.toRadians(180));
		addRow(1, REF2ID, REF1ID, 1, Math.toRadians(120), 0);
	}
}
