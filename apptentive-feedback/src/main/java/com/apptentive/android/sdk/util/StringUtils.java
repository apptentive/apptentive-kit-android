//
//  StringUtils.java
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

import java.util.Map;

import apptentive.com.android.util.Log;

import static apptentive.com.android.feedback.LogTags.UTIL;

/**
 * A collection of useful string-related functions
 */
public final class StringUtils {

	/**
	 * Safe <code>String.format</code>
	 */
	public static String format(String format, Object... args) {
		if (format != null && args != null && args.length > 0) {
			try {
				return String.format(format, args);
			} catch (Exception e) {
				Log.e(UTIL, "Error while formatting String: " + e.getMessage(), e);
			}
		}

		return format;
	}

	/**
	 * Safe <code>Object.toString()</code>
	 */
	public static String toString(Object value) {
		return value != null ? value.toString() : "null";
	}

	/**
	 * Transforms dictionary to string
	 */
	public static String toString(Map<?, ?> map) {
		if (map == null) return null;

		StringBuilder result = new StringBuilder();
		for (Map.Entry<?, ?> entry : map.entrySet()) {
			if (result.length() > 0) result.append(", ");
			result.append("'");
			result.append(entry.getKey());
			result.append("':'");
			result.append(entry.getValue());
			result.append("'");
		}

		return result.toString();
	}

	/**
	 * Checks is string is null or empty
	 */
	public static boolean isNullOrEmpty(String str) {
		return str == null || str.length() == 0;
	}

	/**
	 * Safely checks if two strings are equal (any argument can be null)
	 */
	public static boolean equal(String str1, String str2) {
		return str1 != null && str2 != null && str1.equals(str2);
	}

	/**
	 * Converts a hex String to a byte array.
	 */
	public static byte[] hexToBytes(String hex) {
		int length = hex.length();
		byte[] ret = new byte[length / 2];
		for (int i = 0; i < length; i += 2) {
			ret[i / 2] = (byte) ((Character.digit(hex.charAt(i), 16) << 4) + Character.digit(hex.charAt(i + 1), 16));
		}
		return ret;
	}

	//region Pretty print

	public static String table(Object[][] rows) {
		return table(rows, null);
	}

	public static String table(Object[][] rows, String title) {
		int[] columnSizes = new int[rows[0].length];
		for (Object[] row : rows) {
			for (int i = 0; i < row.length; ++i) {
				columnSizes[i] = Math.max(columnSizes[i], toString(row[i]).length());
			}
		}

		StringBuilder line = new StringBuilder();
		int totalSize = 0;
		for (int i = 0; i < columnSizes.length; ++i) {
			totalSize += columnSizes[i];
		}
		totalSize += columnSizes.length > 0 ? (columnSizes.length - 1) * " | ".length() : 0;
		while (totalSize-- > 0) {
			line.append('-');
		}

		StringBuilder result = new StringBuilder(line);

		for (Object[] row : rows) {
			result.append("\n");

			for (int i = 0; i < row.length; ++i) {
				if (i > 0) {
					result.append(" | ");
				}

				result.append(String.format("%-" + columnSizes[i] + "s", row[i]));
			}
		}

		result.append("\n").append(line);
		return result.toString();
	}

	//endregion
}
