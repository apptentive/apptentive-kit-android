package com.apptentive.android.sdk.serialization;

import com.apptentive.android.sdk.Encryption;
import com.apptentive.android.sdk.util.Util;

import java.io.ByteArrayInputStream;
import java.io.DataInput;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.lang.reflect.Constructor;

/**
 * Helper class for a binary file-based object serialization.
 */
public class ObjectSerialization {

	/**
	 * Reads an object from a file
	 */
	public static <T extends SerializableObject> T deserialize(File file, Class<T> cls) throws IOException {
		FileInputStream stream = null;
		try {
			stream = new FileInputStream(file);
			DataInputStream in = new DataInputStream(stream);

			try {
				Constructor<T> constructor = cls.getDeclaredConstructor(DataInput.class);
				constructor.setAccessible(true);
				return constructor.newInstance(in);
			} catch (Exception e) {
				throw new IOException("Unable to instantiate class: " + cls, e);
			}
		} finally {
			Util.ensureClosed(stream);
		}
	}

	public static <T extends SerializableObject> T deserialize(File file, Class<T> cls, Encryption encryption) throws IOException {
		try {
			final byte[] encryptedBytes = Util.readBytes(file);
			final byte[] unencryptedBytes = encryption.decrypt(encryptedBytes);

			ByteArrayInputStream stream = null;
			try {
				stream = new ByteArrayInputStream(unencryptedBytes);
				DataInputStream in = new DataInputStream(stream);
				Constructor<T> constructor = cls.getDeclaredConstructor(DataInput.class);
				constructor.setAccessible(true);
				return constructor.newInstance(in);
			} finally {
				Util.ensureClosed(stream);
			}
		} catch (Exception e) {
			throw new IOException("Unable to instantiate class: " + cls, e);
		}
	}
}
