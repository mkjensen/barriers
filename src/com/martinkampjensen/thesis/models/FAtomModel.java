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

import jbcl.data.dict.AtomType;

import com.martinkampjensen.thesis.model.impl.AbstractAtomModelInstance;

/**
 * An example showing the three defining torsion angles in a peptide chain.
 * Torsion angle 2 is omega, torsion angle 3 is phi, and torsion angle 4 is psi.
 */
public final class FAtomModel extends AbstractAtomModelInstance
{
	private static final long serialVersionUID = 2046483948582334890L;

	@Override
	protected void create()
	{
		setFirstReference(AtomType.O);
		setSecondReference(AtomType.C, 1);
		setThirdReference(AtomType.N, REF2ID, REF1ID, 1, Math.toRadians(120));
		addRow(AtomType.H, REF3ID, REF2ID, REF1ID, 1, Math.toRadians(120), Math.toRadians(180));
		addRow(AtomType.C, REF2ID, REF3ID, 0, 1, Math.toRadians(120), Math.toRadians(0));
		addRow(AtomType.C, REF3ID, REF2ID, 1, 1, Math.toRadians(120), Math.toRadians(180));
		addRow(AtomType.C, 2, REF3ID, REF2ID, 1, Math.toRadians(120), Math.toRadians(180));
		addRow(AtomType.N, 3, 2, REF3ID, 1, Math.toRadians(120), Math.toRadians(180));
	}
}
