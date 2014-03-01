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
import java.util.Timer;
import java.util.TimerTask;

import android.app.ActivityManager;
import android.app.Service;
import android.content.ComponentName;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;
import android.os.RemoteCallbackList;
import android.os.RemoteException;
import android.util.Log;
import android.speech.jarvis.IWakeUpService;
import android.speech.jarvis.IWakeUpServiceCallback;

public class ProxyService extends Service {

    public static ProxyService sService;
    
    public static final int MSG_REGISTER_CLIENT = 1;
    public static final int MSG_UNREGISTER_CLIENT = 2;
    
    private static final String TAG = "JarvisServiceApp";
    
    private final IWakeUpService.Stub mBinder = new IWakeUpService.Stub() {

        public boolean onReceivedWakeUp(String input) {
            sService = ProxyService.this;
            if(input.contains("Jarvis")) {
                startActivity(new Intent(ProxyService.this, ProxyActivity.class)
                    .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK));
                
                if(mTimer != null)
                    mTimer.cancel();
                
                mTimer = new Timer();
                mTimer.scheduleAtFixedRate(new TimerTask() {
                    @Override
                    public void run() {
                        ComponentName component = getActiveActivity();
                        if(!isGoogleSearch(component)) {
                            releaseBlock();
                            mTimer.cancel();
                            mTimer = null;
                        }
                    }
                }, 7000, 1000);
                
                return true;
            } else return false;
        }

        public boolean registerCallback(IWakeUpServiceCallback cb) {
            if (cb != null) {
                mCallbacks.register(cb);
                return true;
            }
            return false;
        }

        public boolean unregisterCallback(IWakeUpServiceCallback cb) {
            if (cb != null) {
                mCallbacks.unregister(cb);
                return true;
            }
            return false;
        }
    };
    
    private Timer mTimer;

	/**
     * This is a list of callbacks that have been registered with the
     * service.  Note that this is package scoped (instead of private) so
     * that it can be accessed more efficiently from inner classes.
     */
    final RemoteCallbackList<IWakeUpServiceCallback> mCallbacks
            = new RemoteCallbackList<IWakeUpServiceCallback>();

    public void acquireBlock() {
        // Broadcast to all clients the new value.
        final int N = mCallbacks.beginBroadcast();
        for (int i=0; i<N; i++) {
            try {
                mCallbacks.getBroadcastItem(i).acquireBlock();
            } catch (RemoteException e) {
                // The RemoteCallbackList will take care of removing
                // the dead object for us.
            }
        }
        mCallbacks.finishBroadcast();
    }
    
    public void releaseBlock() {
        // Broadcast to all clients the new value.
        final int N = mCallbacks.beginBroadcast();
        for (int i=0; i<N; i++) {
            try {
                mCallbacks.getBroadcastItem(i).releaseBlock();
            } catch (RemoteException e) {
                // The RemoteCallbackList will take care of removing
                // the dead object for us.
            }
        }
        mCallbacks.finishBroadcast();
    }
    
    public boolean isGoogleSearch(ComponentName na) {
        String pa = na.getPackageName();
        String cl = na.getClassName();
        
        if(!pa.equals("com.google.android.googlequicksearchbox"))
            return false;
        
        if(!cl.equals("com.google.android.googlequicksearchbox.VoiceSearchActivity")
                && !cl.equals("com.google.android.voicesearch.SendSmsActivity")
                && !cl.equals("com.google.android.velvet.ui.InAppWebPageActivity")
                && !cl.equals("com.google.android.sidekick.main.secondscreen.SecondScreenActivity")
                && !cl.endsWith(".SearchActivity")
                && !cl.equals("com.google.android.velvet.ui.VelvetActivity")
                && !cl.endsWith(".VoiceSearchActivity")
                && !cl.equals("com.google.android.sidekick.main.RemindersListActivity")
                && !cl.equals("com.google.android.velvet.ui.VelvetIntentDispatcher")
                && !cl.equals("com.google.android.search.core.google.GoogleSearch"))
            return false;
        return true;
    }
    
    @Override
    public IBinder onBind(Intent intent) {
        if(intent.getAction().equals(Intent.ACTION_JARVIS_VOICE_CONTROL)) {
            return mBinder;
        }
        return null;
    }
    
    public void gotRequestFromGoogle() {
        Log.i(TAG, "Google activity finished");
    }
    
    public ComponentName getActiveActivity() {
        ActivityManager am = (ActivityManager)getSystemService(ACTIVITY_SERVICE);
        List<ActivityManager.RunningTaskInfo> taskInfo = am.getRunningTasks(1); 
        
        return taskInfo.get(0).topActivity;
    }
}
