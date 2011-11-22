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

package com.martinkampjensen.thesis.visualization.opengl;

import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

import javax.media.opengl.GLProfile;

import com.martinkampjensen.thesis.Main;
import com.martinkampjensen.thesis.StatusCode;

/**
 * A {@link WindowListener} ensures that the JOGL library cleans up after
 * itself after a window is closed and then shuts down the JVM.
 * 
 * @see GLProfile#shutdown()
 * @see System#exit(int)
 */
public final class WindowListener extends WindowAdapter
{
	@Override
	public void windowClosing(WindowEvent e)
	{
		// The shutdown implementation is called via the JVM shutdown hook, if
		// not manually invoked here. Invoke shutdown() manually is recommended,
		// due to the unreliable JVM state within the shutdown hook.
		GLProfile.shutdown();

		Main.exit(StatusCode.SUCCESS);
	}
}
