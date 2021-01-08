/*
 * Copyright (c) 2016, Apptentive, Inc. All Rights Reserved.
 * Please refer to the LICENSE file for the terms and conditions
 * under which redistribution and use of this file is permitted.
 */

package apptentive.com.app.test.rules;

import android.Manifest;
import android.content.Context;
import android.os.Build;
import android.os.IBinder;
import android.util.Log;

import androidx.core.content.PermissionChecker;
import androidx.test.InstrumentationRegistry;
import androidx.test.core.app.ApplicationProvider;

import org.junit.rules.TestRule;
import org.junit.runner.Description;
import org.junit.runners.model.Statement;

import java.lang.reflect.Method;
import java.util.Arrays;

/**
 * https://product.reverb.com/disabling-animations-in-espresso-for-android-testing-de17f7cf236f
 */
public class DisableAnimationsRule implements TestRule {
	private final Method mSetAnimationScalesMethod;
	private final Method mGetAnimationScalesMethod;
	private final Object mWindowManagerObject;

	public DisableAnimationsRule() {
		try {
			Class<?> windowManagerStubClazz = Class.forName("android.view.IWindowManager$Stub");
			Method asInterface = windowManagerStubClazz.getDeclaredMethod("asInterface", IBinder.class);

			Class<?> serviceManagerClazz = Class.forName("android.os.ServiceManager");
			Method getService = serviceManagerClazz.getDeclaredMethod("getService", String.class);

			Class<?> windowManagerClazz = Class.forName("android.view.IWindowManager");

			mSetAnimationScalesMethod = windowManagerClazz.getDeclaredMethod("setAnimationScales", float[].class);
			mGetAnimationScalesMethod = windowManagerClazz.getDeclaredMethod("getAnimationScales");

			IBinder windowManagerBinder = (IBinder) getService.invoke(null, "window");
			mWindowManagerObject = asInterface.invoke(null, windowManagerBinder);
		} catch (Exception e) {
			throw new RuntimeException("Failed to access animation methods", e);
		}
	}

	@Override
	public Statement apply(final Statement statement, Description description) {
		return new Statement() {
			@Override
			public void evaluate() throws Throwable {
				setAnimationScaleFactors(0.0f);
				try {
					statement.evaluate();
				} finally {
					setAnimationScaleFactors(1.0f);
				}
			}
		};
	}

	private void setAnimationScaleFactors(float scaleFactor) throws Exception {
		float[] scaleFactors = (float[]) mGetAnimationScalesMethod.invoke(mWindowManagerObject);
		Arrays.fill(scaleFactors, scaleFactor);

		if (Build.VERSION.SDK_INT >= 23) {
			Context targetContext = ApplicationProvider.getApplicationContext();
			// On Jenkins, we use "adb shell pm grant com.android.test android.permission.SET_ANIMATION_SCALE"
			if (PermissionChecker.checkSelfPermission(targetContext, Manifest.permission.SET_ANIMATION_SCALE) == PermissionChecker.PERMISSION_GRANTED) {
				mSetAnimationScalesMethod.invoke(mWindowManagerObject, scaleFactors);
			}
		}
	}
}