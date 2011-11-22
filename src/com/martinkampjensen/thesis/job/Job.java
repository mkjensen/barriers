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

import java.io.File;

import javax.xml.XMLConstants;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;

import org.xml.sax.SAXException;

import com.martinkampjensen.thesis.Constant;

/**
 * TODO: Document {@link Job}.
 */
@XmlRootElement(namespace = Job.NAMESPACE)
@XmlType(propOrder = { "name", "description", "input", "action", "options" })
public final class Job extends Group
{
	static final String NAMESPACE =
		"http://martinkampjensen.com/thesis/job";

	private static final String DEFAULT_VERSION = "1.0";
	@XmlAttribute(required = true)
	private String version = DEFAULT_VERSION;

	private static final String DEFAULT_NAME = "Unnamed job";
	@XmlElement(defaultValue = DEFAULT_NAME)
	private String name = DEFAULT_NAME;

	private static final String DEFAULT_DESCRIPTION = "";
	@XmlElement(defaultValue = DEFAULT_DESCRIPTION)
	private String description = DEFAULT_DESCRIPTION;

	@XmlElement(required = true)
	private Input input;

	@XmlElement(required = true)
	private Action action;

	@XmlElement
	private Options options = new Options();

	Job()
	{
		super();
	}

	public static Job parse(File file) throws SAXException, JAXBException
	{
		final String packageName = Job.class.getPackage().getName();
		final JAXBContext context = JAXBContext.newInstance(packageName);

		final Unmarshaller unmarshaller = context.createUnmarshaller();
		unmarshaller.setSchema(loadSchema());

		return (Job)unmarshaller.unmarshal(file);
	}

	@Override
	public String toString()
	{
		final StringBuilder sb = new StringBuilder();
		toString(sb);
		return sb.toString();
	}

	public String version()
	{
		return version;
	}

	public String name()
	{
		return name;
	}

	public String description()
	{
		return description; 
	}

	public Input input()
	{
		return input;
	}

	public Action action()
	{
		return action;
	}

	public Options options()
	{
		return options;
	}

	@Override
	protected void toString(StringBuilder sb)
	{
		add(sb, "version", version, DEFAULT_VERSION);
		add(sb, "name", name, DEFAULT_NAME);
		add(sb, "description", description, DEFAULT_DESCRIPTION);
		input.toString(sb);
		add(sb, "action", action.toString());
		options.toString(sb);
	}

	private static Schema loadSchema() throws SAXException
	{
		final SchemaFactory schemaFactory =
			SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI);
		return schemaFactory.newSchema(Constant.JOB_XML_SCHEMA);
	}
}
