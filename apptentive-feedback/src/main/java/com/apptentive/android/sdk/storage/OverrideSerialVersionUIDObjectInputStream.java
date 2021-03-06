package com.apptentive.android.sdk.storage;

import static apptentive.com.android.util.LogTags.UTIL;

import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectStreamClass;

import apptentive.com.android.util.Log;


/**
 * Extends ObjectInputStream to be able to read serialized objects after serialVersionUID has been
 * changed during R8 obfuscation.
 */
class OverrideSerialVersionUIDObjectInputStream extends ObjectInputStream {
	OverrideSerialVersionUIDObjectInputStream(InputStream in) throws IOException {
		super(in);
	}

	protected ObjectStreamClass readClassDescriptor() throws IOException, ClassNotFoundException {
		final ObjectStreamClass resultClassDescriptor = super.readClassDescriptor(); // initially streams descriptor
		Class localClass; // the class in the local JVM that this descriptor represents.
		try {
			localClass = Class.forName(resultClassDescriptor.getName());
		} catch (ClassNotFoundException e) {
			Log.w(UTIL, "No local class for: %s ", resultClassDescriptor.getName());
			return resultClassDescriptor;
		}
		ObjectStreamClass localClassDescriptor = ObjectStreamClass.lookup(localClass);
		if (localClassDescriptor != null) { // only if class implements serializable
			final long localSUID = localClassDescriptor.getSerialVersionUID();
			final long streamSUID = resultClassDescriptor.getSerialVersionUID();
			if (streamSUID != localSUID) { // check for serialVersionUID mismatch.
				Log.w(UTIL, "Overriding serialized '%s' version mismatch:\n\tlocal serialVersionUID = %s\n\tstream serialVersionUID = %s", resultClassDescriptor.getName(), localSUID, streamSUID);
				return localClassDescriptor; // Use local class descriptor for deserialization
			}
		}
		return resultClassDescriptor;
	}
}