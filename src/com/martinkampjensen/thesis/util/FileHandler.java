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

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;

import com.martinkampjensen.thesis.Main;
import com.martinkampjensen.thesis.StatusCode;

/**
 * TODO: Document {@link FileHandler}.
 */
public final class FileHandler
{
	private FileHandler()
	{
	}

	public static String read(String fileName)
	{
		return read(new File(fileName));
	}

	public static String read(File file)
	{
		try {
			final StringBuilder sb = new StringBuilder(10240);
			final FileReader fr = new FileReader(file);
			final BufferedReader br = new BufferedReader(fr);

			String line;
			while((line = br.readLine()) != null) {
				sb.append(line);
				sb.append('\n');
			}

			br.close();
			fr.close();

			return sb.toString();
		}
		catch(FileNotFoundException e) {
			Main.errorExit(e, StatusCode.IO);
		}
		catch(IOException e) {
			Main.errorExit(e, StatusCode.IO);
		}

		return null;
	}

	public static void write(String string, String fileName)
	{
		write(string, new File(fileName));
	}

	public static void write(String string, File file)
	{
		// Ensure that parent directories exist before writing the file.
		if(file.getParentFile() != null) file.getParentFile().mkdirs();

		try {
			final FileWriter fw = new FileWriter(file);
			fw.write(string);
			fw.close();
		}
		catch(IOException e) {
			Main.errorExit(e, StatusCode.IO);
		}
	}
}
