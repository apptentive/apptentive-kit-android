//
//  ObjectUtils.java
//
//  Lunar Unity Mobile Console
//  https://github.com/SpaceMadness/lunar-unity-console
//
//  Copyright 2017 Alex Lementuev, SpaceMadness.
//
//  Licensed under the Apache License, Version 2.0 (the "License");
//  you may not use this file except in compliance with the License.
//  You may obtain a copy of the License at
//
//      http://www.apache.org/licenses/LICENSE-2.0
//
//  Unless required by applicable law or agreed to in writing, software
//  distributed under the License is distributed on an "AS IS" BASIS,
//  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
//  See the License for the specific language governing permissions and
//  limitations under the License.
//

package com.apptentive.android.sdk.util;

import androidx.annotation.Nullable;

/**
 * A collection of useful object-related functions
 */
public final class ObjectUtils {
	/**
	 * Attempts to cast <code>object</code> to class <code>cls</code>.
	 * Returns <code>null</code> if cast is impossible.
	 */
	@SuppressWarnings("unchecked")
	public static @Nullable <T> T as(Object object, Class<T> cls) {
		return cls.isInstance(object) ? (T) object : null;
	}

	public static boolean isNullOrEmpty(byte[] array) {
		return array == null || array.length == 0;
	}
}
