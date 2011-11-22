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
 * TODO: Document {@link ForceField}.
 */
public enum ForceField
{
	/**
	 * Gaff force field.
	 */
	GAFF("Gaff"),

	/**
	 * Ghemical force field.
	 * 
	 * @see <a href="http://openbabel.org/wiki/OBForceFieldGhemical">OBForceFieldGhemical</a>
	 */
	GHEMICAL("Ghemical"),

	/**
	 * MMFF94 force field.
	 * 
	 * @see <a href="http://openbabel.org/wiki/OBForceFieldMMFF94">OBForceFieldMMFF94</a>
	 */
	MMFF94("MMFF94"),

	/**
	 * MMFF94s force field.
	 */
	MMFF94S("MMFF94s"),

	/**
	 * Universal Force Field.
	 * 
	 * @see <a href="http://openbabel.org/wiki/OBForceFieldUFF">OBForceFieldUFF</a>
	 */
	UFF("UFF");

	private final String _id;

	private ForceField(String id)
	{
		_id = id;
	}

	public String getId()
	{
		return _id;
	}
}
