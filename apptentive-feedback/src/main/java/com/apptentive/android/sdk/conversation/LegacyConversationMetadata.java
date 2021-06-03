package com.apptentive.android.sdk.conversation;

import com.apptentive.android.sdk.serialization.SerializableObject;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * Represents a modified version of legacy [ConversationMetadata] class (read-only). Used in legacy data migration.
 * See: https://github.com/apptentive/apptentive-android/blob/master/apptentive/src/main/java/com/apptentive/android/sdk/conversation/ConversationMetadata.java
 */
public class LegacyConversationMetadata implements SerializableObject, Iterable<LegacyConversationMetadataItem> {
	private static final byte VERSION = 1;

	private final List<LegacyConversationMetadataItem> items;

	static {
		hackR8();
	}

	public LegacyConversationMetadata() {
		items = new ArrayList<>();
	}

	//region Serialization

	public LegacyConversationMetadata(DataInput in) throws IOException {
		byte version = in.readByte();
		if (version != VERSION) {
			throw new IOException("Expected version " + VERSION + " but was " + version);
		}

		int count = in.readByte();
		items = new ArrayList<>(count);
		for (int i = 0; i < count; ++i) {
			items.add(new LegacyConversationMetadataItem(in));
		}
	}

	@Override
	public void writeExternal(DataOutput out) throws IOException {
		out.writeByte(VERSION);
		out.write(items.size());
		for (int i = 0; i < items.size(); ++i) {
			items.get(i).writeExternal(out);
		}
	}

	//endregion

	//region Items

	LegacyConversationMetadataItem findItem(final ConversationState state) {
		return findItem(new Filter() {
			@Override
			public boolean accept(LegacyConversationMetadataItem item) {
				return state.equals(item.getConversationState());
			}
		});
	}

	LegacyConversationMetadataItem findItem(Filter filter) {
		for (LegacyConversationMetadataItem item : items) {
			if (filter.accept(item)) {
				return item;
			}
		}
		return null;
	}

	//endregion

	//region Iterable

	@Override
	public Iterator<LegacyConversationMetadataItem> iterator() {
		return items.iterator();
	}

	//endregion

	//region Getters/Setters

	public boolean hasItems() {
		return items.size() > 0;
	}

	public List<LegacyConversationMetadataItem> getItems() {
		return items;
	}

	//endregion

	//region Filter

	public interface Filter {
		boolean accept(LegacyConversationMetadataItem item);
	}

	//endregion

	//region R8 hack

	/**
	 * This is a hack-workaround for R8 removing unused code despite
	 * ProGuard configuration telling to keep it
	 */
	private static void hackR8() {
		try {
			// this would never be true but we have to trick the obfuscator
			if (System.currentTimeMillis() < 10000L) {
				DataInput stream = null;
				// touch the constructor and "use" the reference
				LegacyConversationMetadata c = new LegacyConversationMetadata(stream);
				System.out.println(c);
			}
		} catch (Exception ignored) {
		}
	}

	//endregion

	@Override
	public String toString() {
		return "ConversationMetadata{" +
			       "items=" + items +
			       '}';
	}
}
