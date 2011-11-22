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
 * Sample using torsion angle 6 to get a 2d energy landscape shaped like a
 * valley. Sample using torsion angles 2 and 6 to get a 3d energy landscape with
 * a valley.
 */
public final class EAtomModel extends AbstractAtomModelInstance
{
	private static final long serialVersionUID = -7267256371103608735L;

	@Override
	protected void create()
	{
		// Backbone atoms.
		setSecondReference(1);
		setThirdReference(REF2ID, REF1ID, 1, Math.toRadians(180));
		addRow(REF3ID, REF2ID, REF1ID, 1, Math.toRadians(180), Math.toRadians(180));
		addRow(0, REF3ID, REF2ID, 1, Math.toRadians(180), Math.toRadians(180));
		addRow(1, 0, REF3ID, 1, Math.toRadians(180), Math.toRadians(180));
		addRow(2, 1, 0, 1, Math.toRadians(180), Math.toRadians(180));
		addRow(3, 2, 1, 1, Math.toRadians(180), Math.toRadians(180));
		addRow(4, 3, 2, 1, Math.toRadians(180), Math.toRadians(180));

		// Primary atom of interest.
		addRow(1, 0, REF3ID, 1, Math.toRadians(90), Math.toRadians(0));

		// Upper atom ring.
		addRow(2, 3, 4, 1, Math.toRadians(112.5), Math.toRadians(45));
		addRow(2, 3, 4, 1, Math.toRadians(115), Math.toRadians(90));
		addRow(2, 3, 4, 1, Math.toRadians(115), Math.toRadians(135));
		addRow(2, 3, 4, 1, Math.toRadians(115), Math.toRadians(180));
		addRow(2, 3, 4, 1, Math.toRadians(115), Math.toRadians(225));
		addRow(2, 3, 4, 1, Math.toRadians(115), Math.toRadians(270));
		addRow(2, 3, 4, 1, Math.toRadians(112.5), Math.toRadians(315));

		// Lower atom ring.	
		addRow(0, REF3ID, REF2ID, 1, Math.toRadians(245), Math.toRadians(22.5));
		addRow(0, REF3ID, REF2ID, 1, Math.toRadians(245), Math.toRadians(67.5));
		addRow(0, REF3ID, REF2ID, 1, Math.toRadians(246.5), Math.toRadians(112.5));
		addRow(0, REF3ID, REF2ID, 1, Math.toRadians(246.5), Math.toRadians(247.5));
		addRow(0, REF3ID, REF2ID, 1, Math.toRadians(245), Math.toRadians(292.5));
		addRow(0, REF3ID, REF2ID, 1, Math.toRadians(245), Math.toRadians(337.5));
	}
}
