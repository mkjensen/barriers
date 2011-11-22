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
 * Pair with {@link CAtomModel} to see that the same torsion angle defined using
 * different atoms results in shifts in the energy landscape.
 */
public final class BAtomModel extends AbstractAtomModelInstance
{
	private static final long serialVersionUID = 451365394032174262L;

	@Override
	protected void create()
	{
		setSecondReference(1);
		setThirdReference(REF2ID, REF1ID, 1, Math.toRadians(120));
		addRow(REF3ID, REF2ID, REF1ID, 1, Math.toRadians(120), 0);
		addRow(REF2ID, REF3ID, 0, 1, Math.toRadians(120), Math.toRadians(180));
		addRow(1, REF2ID, REF3ID, 1, Math.toRadians(120), Math.toRadians(180));
	}
}
