/*
 * Copyright (C) 2014 Firtecy
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
package de.firtecy.voiceredirectgoogle;

import java.util.ArrayList;
import java.util.List;

import android.app.Service;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;
import android.os.RemoteCallbackList;
import android.os.RemoteException;
import android.util.Log;
import android.speech.jarvis.IJarvis;
import android.speech.jarvis.IJarvisCallback;

public class ProxyService extends Service {

    public static final int MSG_REGISTER_CLIENT = 1;
    public static final int MSG_UNREGISTER_CLIENT = 2;
    
    private static final String TAG = "JarvisServiceApp";
    
    private static final Intent INTENT_GOOGLE_VOICE_SEARCH;
    
    static {
        INTENT_GOOGLE_VOICE_SEARCH = new Intent();
        INTENT_GOOGLE_VOICE_SEARCH.setClassName("com.google.android.googlequicksearchbox",
            "com.google.android.googlequicksearchbox.VoiceSearchActivity");
        INTENT_GOOGLE_VOICE_SEARCH.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK
                | Intent.FLAG_ACTIVITY_CLEAR_TOP);
    }
    
    private final IJarvis.Stub mBinder = new IJarvis.Stub() {
        public int getTargetedApi() { return 1; }

        public long getLastChange() { return 0; }

        public int getCountWords() { return 0; }

        public String getWordAt(int i) { return null; }

        public boolean isReady() { return true; }

        public boolean handleStrings(List<String> var) {
            if(var.get(1).contains("Jarvis")) {
                startActivity(new Intent(INTENT_GOOGLE_VOICE_SEARCH));
                return true;
            } else return false;
        }

        public boolean registerCallback(IJarvisCallback cb) {
            if (cb != null) {
                mCallbacks.register(cb);
                return true;
            }
            return false;
        }

        public boolean unregisterCallback(IJarvisCallback cb) {
            if (cb != null) {
                mCallbacks.unregister(cb);
                return true;
            }
            return false;
        }
    };

	/**
     * This is a list of callbacks that have been registered with the
     * service.  Note that this is package scoped (instead of private) so
     * that it can be accessed more efficiently from inner classes.
     */
    final RemoteCallbackList<IJarvisCallback> mCallbacks
            = new RemoteCallbackList<IJarvisCallback>();

    @Override
    public IBinder onBind(Intent intent) {
        if(intent.getAction().equals(Intent.ACTION_JARVIS_VOICE_CONTROL)) {
            return mBinder;
        }
        return null;
    }
}
