/*
 * Copyright 2012 Google Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.google.android.gcm.imp;

import static com.google.android.gcm.imp.CommonUtilities.TAG;

import java.util.Random;

import android.content.Context;
import android.util.Log;

import com.google.android.gcm.GCMRegistrar;

/**
 * Helper class used to communicate with the demo server.
 */
public final class ServerUtilities {

	private static final int MAX_ATTEMPTS = 5;
	private static final int BACKOFF_MILLI_SECONDS = 2000;
	private static final Random random = new Random();

	/**
	 * Register this account/device pair within the server.
	 * 
	 * @return whether the registration succeeded or not.
	 */
	public static boolean register(final Context context, final String regId) {
		Log.i(TAG, "registering device (regId = " + regId + ")");
		long backoff = BACKOFF_MILLI_SECONDS + random.nextInt(1000);
		// Once GCM returns a registration id, we need to register it in the
		// demo server. As the server might be down, we will retry it a couple
		// times.
		for (int i = 1; i <= MAX_ATTEMPTS; i++) {
			Log.d(TAG, "Attempt #" + i + " to register");
			try {
				if (onRegisterListener != null) {
					onRegisterListener.onRegister(regId);
				}
				GCMRegistrar.setRegisteredOnServer(context, true);
				return true;
			} catch (Exception e) {
				// Here we are simplifying and retrying on any error; in a real
				// application, it should retry only on unrecoverable errors
				// (like HTTP error code 503).
				Log.e(TAG, "Failed to register on attempt " + i, e);
				if (i == MAX_ATTEMPTS) {
					break;
				}
				try {
					Log.d(TAG, "Sleeping for " + backoff + " ms before retry");
					Thread.sleep(backoff);
				} catch (InterruptedException e1) {
					// Activity finished before we complete - exit.
					Log.d(TAG, "Thread interrupted: abort remaining retries!");
					Thread.currentThread().interrupt();
					return false;
				}
				// increase backoff exponentially
				backoff *= 2;
			}
		}
		return false;
	}

	/**
	 * Unregister this account/device pair within the server.
	 */
	public static void unregister(final Context context, final String regId) {
		Log.i(TAG, "unregistering device (regId = " + regId + ")");
		try {
			if (onRegisterListener != null) {
				onRegisterListener.onUnregister(regId);
			}
			GCMRegistrar.setRegisteredOnServer(context, false);
		} catch (Exception e) {
		}
	}

	public static OnRegisterListener onRegisterListener;

	public static void setOnRegisterListener(OnRegisterListener l) {
		ServerUtilities.onRegisterListener = l;
	}

	public interface OnRegisterListener {
		public void onRegister(String id);

		public void onUnregister(String id);
	}
}
