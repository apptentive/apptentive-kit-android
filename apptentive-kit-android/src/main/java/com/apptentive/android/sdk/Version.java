package com.apptentive.android.sdk;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.Serializable;

/**
 * <p>This type represents a <a href="http://semver.org/">semantic version</a>. It can be initialized
 * with a string or a long, and there is no limit to the number of parts your semantic version can
 * contain. The class allows comparison based on semantic version rules.
 * Valid versions (In sorted order):</p>
 * <ul>
 * <li>0</li>
 * <li>0.1</li>
 * <li>1.0.0</li>
 * <li>1.0.9</li>
 * <li>1.0.10</li>
 * <li>1.2.3</li>
 * <li>5</li>
 * </ul>
 * Invalid versions:
 * <ul>
 * <li>zero</li>
 * <li>0.1+2015.10.21</li>
 * <li>1.0.0a</li>
 * <li>1.0-rc2</li>
 * <li>1.0.10-SNAPSHOT</li>
 * <li>5a</li>
 * <li>FF01</li>
 * </ul>
 */
public class Version implements Serializable, Comparable<Version> {
    private static final long serialVersionUID = 1891878408603512644L;
    public static final String TYPE = "version";

    private String version;

    public Version() {
    }

    public Version(JSONObject json) throws JSONException {
        this.version = json.optString(TYPE, null);
    }

    public Version(long version) {
        this.version = Long.toString(version);
    }

    public void setVersion(String version) {
        this.version = version;
    }

    public void setVersion(long version) {
        setVersion(Long.toString(version));
    }

    public String getVersion() {
        return version;
    }

    @Override
    public int compareTo(Version other) {
        String thisVersion = getVersion();
        String thatVersion = other.getVersion();
        String[] thisArray = thisVersion.split("\\.");
        String[] thatArray = thatVersion.split("\\.");

        int maxParts = Math.max(thisArray.length, thatArray.length);
        for (int i = 0; i < maxParts; i++) {
            // If one SemVer has more parts than another, pad out the short one with zeros in each slot.
            long left = 0;
            if (thisArray.length > i) {
                left = Long.parseLong(thisArray[i]);
            }
            long right = 0;
            if (thatArray.length > i) {
                right = Long.parseLong(thatArray[i]);
            }
            if (left < right) {
                return -1;
            } else if (left > right) {
                return 1;
            }
        }
        return 0;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o instanceof Version) {
            return compareTo((Version) o) == 0;
        }
        return false;
    }

    @Override
    public String toString() {
        return getVersion();
    }
}