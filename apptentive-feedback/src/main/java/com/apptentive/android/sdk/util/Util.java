/*
 * Copyright (c) 2016, Apptentive, Inc. All Rights Reserved.
 * Please refer to the LICENSE file for the terms and conditions
 * under which redistribution and use of this file is permitted.
 */

package com.apptentive.android.sdk.util;

import androidx.annotation.Nullable;

import java.io.ByteArrayOutputStream;
import java.io.Closeable;
import java.io.DataInput;
import java.io.DataOutput;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.TimeZone;

// TODO: this class does too much - split into smaller classes and clean up
public class Util {
	private static final String ENCRYPTED_FILENAME_SUFFIX = ".encrypted";


	public static void ensureClosed(Closeable stream) {
		if (stream != null) {
			try {
				stream.close();
			} catch (IOException e) {

			}
		}
	}

	public static int getUtcOffset() {
		TimeZone timezone = TimeZone.getDefault();
		return timezone.getOffset(System.currentTimeMillis()) / 1000;
	}


	public static byte[] readBytes(File file) throws IOException {
		ByteArrayOutputStream output = null;
		try {
			output = new ByteArrayOutputStream();
			appendFileToStream(file, output);
			return output.toByteArray();
		} finally {
			ensureClosed(output);
		}
	}

	public static void appendFileToStream(File file, OutputStream outputStream) throws IOException {
		if (file == null) {
			throw new IllegalArgumentException("'file' is null");
		}

		if (!file.exists()) {
			throw new FileNotFoundException("File does not exist: " + file);
		}

		if (file.isDirectory()) {
			throw new FileNotFoundException("File is directory: " + file);
		}

		FileInputStream input = null;
		try {
			input = new FileInputStream(file);
			copy(input, outputStream);
		} finally {
			ensureClosed(input);
		}
	}

	private static void copy(InputStream input, OutputStream output) throws IOException {
		byte[] buffer = new byte[4096];
		int bytesRead;
		while ((bytesRead = input.read(buffer)) > 0) {
			output.write(buffer, 0, bytesRead);
		}
	}

	public static void writeNullableUTF(DataOutput out, @Nullable String value) throws IOException {
		out.writeBoolean(value != null);
		if (value != null) {
			out.writeUTF(value);
		}
	}

	public static String readNullableUTF(DataInput in) throws IOException {
		boolean notNull = in.readBoolean();
		return notNull ? in.readUTF() : null;
	}


	public static File getEncryptedFilename(File file) {
		String filename = file.getName();
		return filename.endsWith(ENCRYPTED_FILENAME_SUFFIX) ? file : new File(file.getParent(), filename + ENCRYPTED_FILENAME_SUFFIX);
	}

}
