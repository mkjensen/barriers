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
 * Torsion angle 1 does not significantly influence the energy value. Sampling
 * using torsion angles 1, 2, and 3, and applying Principal Component Analysis
 * should show that.
 */
public final class DAtomModel extends AbstractAtomModelInstance
{
	private static final long serialVersionUID = -2236729691989545501L;

	@Override
	protected void create()
	{
		setSecondReference(1);
		setThirdReference(REF2ID, REF1ID, 1, Math.toRadians(180));
		addRow(REF3ID, REF2ID, REF1ID, 1, Math.toRadians(180), 0);
		addRow(0, REF3ID, REF2ID, 1, Math.toRadians(135), 0);
		addRow(REF1ID, REF2ID, REF3ID, 1, Math.toRadians(45), Math.toRadians(180));
		addRow(REF2ID, REF3ID, 0, 1, Math.toRadians(135), 0);
	}
}
