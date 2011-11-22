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

package com.martinkampjensen.thesis.job;

import javax.xml.bind.annotation.XmlEnum;
import javax.xml.bind.annotation.XmlEnumValue;

/**
 * TODO: Document {@link InputType}.
 */
@XmlEnum 
public enum InputType
{
	@XmlEnumValue("class")
	CLASS("class"),

	@XmlEnumValue("pdb")
	PDB("pdb");

	private final String _inputType;

	private InputType(String inputType)
	{
		_inputType = inputType;
	}

	@Override
	public String toString()
	{
		return _inputType;
	}
}
