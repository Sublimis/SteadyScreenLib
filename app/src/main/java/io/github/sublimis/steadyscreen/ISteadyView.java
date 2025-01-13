/*
 * Copyright 2025 Sublimis
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.github.sublimis.steadyscreen;

import android.content.Intent;

import io.github.sublimis.steadyscreen.SteadyScreen.MetaInfo;

/**
 * Interface to the <a href="https://play.google.com/store/apps/details?id=com.sublimis.steadyscreen">SteadyScreen</a> service
 * which allows compatible applications to easily counteract small screen movements within their GUI.
 * <p>
 * Find out more at <a href="https://github.com/Sublimis/SteadyScreenLib/">https://github.com/Sublimis/SteadyScreenLib/</a>.
 *
 * @author Sublimis
 * @version 2.0 (2025-01)
 */
public interface ISteadyView
{
	/**
	 * Receive the coordinates of the steady move action.
	 *
	 * @param x X-coordinate, or {@link SteadyScreen#InvalidCoord} if unavailable.
	 * @param y Y-coordinate, or {@link SteadyScreen#InvalidCoord} if unavailable.
	 */
	default void steadyViewAction(final double x, final double y)
	{
	}

	/**
	 * Receive the meta information from the SteadyScreen service.
	 * Use {@link MetaInfo#getInstance(Intent)} to extract the meta information as a {@link MetaInfo} object.
	 *
	 * @param intent The Intent containing meta information, like the service app name and version. Use {@link MetaInfo#getInstance(Intent)} to extract the meta information.
	 * @see SteadyScreen#ParamServiceAppName
	 */
	default void steadyScreenMetaInfo(final Intent intent)
	{
	}
}
