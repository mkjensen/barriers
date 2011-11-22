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

package com.martinkampjensen.thesis.util;

/**
 * TODO: Document {@link Print}.
 */
public final class Print
{
	private Print()
	{
	}

	public static void line(String x)
	{
		System.out.println(x);
	}

	public static void line(double x)
	{
		System.out.println(x);
	}

	public static void line(String format, Object... args)
	{
		System.out.println(String.format(format, args));
	}

	public static void length(char p, char q, double length)
	{
		line("|%c%c|  = %f", p, q, length);
	}

	public static void angle(double angle)
	{
		line(Math.toDegrees(angle));
	}

	public static void angle(char p, char q, char r, double angle)
	{
		line("<%c%c%c  = %f", p, q, r, Math.toDegrees(angle));
	}

	public static void angle(char p, char q, char r, char s, double angle)
	{
		line("<%c%c%c%c = %f", p, q, r, s, Math.toDegrees(angle));
	}
}
