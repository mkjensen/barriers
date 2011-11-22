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

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;

import com.martinkampjensen.thesis.connection.Connector;
import com.martinkampjensen.thesis.util.Util;

/**
 * TODO: Document {@link Connection}.
 */
@XmlType(propOrder = {})
public final class Connection extends Group
{
	private static final String DEFAULT_CONNECTOR =
		"com.martinkampjensen.thesis.connection.DirectConnector";
	@XmlElement(defaultValue = DEFAULT_CONNECTOR, required = true)
	private String connector = DEFAULT_CONNECTOR;

	Connection()
	{
		super();
	}

	public Connector connector()
	{
		// TODO: Create new connector every time?
		return Util.instantiate(connector);
	}

	@Override
	protected void toString(StringBuilder sb)
	{
		add(sb, "connector", connector, DEFAULT_CONNECTOR);
	}
}
