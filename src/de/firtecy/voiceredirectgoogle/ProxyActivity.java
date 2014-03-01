package de.firtecy.voiceredirectgoogle;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.WindowManager;

public class ProxyActivity extends Activity {
    private static final Intent INTENT_GOOGLE_VOICE_SEARCH;
    
    static {
        INTENT_GOOGLE_VOICE_SEARCH = new Intent();
        INTENT_GOOGLE_VOICE_SEARCH.setClassName("com.google.android.googlequicksearchbox",
            "com.google.android.googlequicksearchbox.VoiceSearchActivity");
        INTENT_GOOGLE_VOICE_SEARCH.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
    }
    
    @Override
    protected void onCreate(Bundle b) {
        super.onCreate(b);
        
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD);
        
        startActivityForResult(INTENT_GOOGLE_VOICE_SEARCH, 1);
        finish();
    }
    
    @Override
    protected void onActivityResult(int requestCode, int resultCode,
            Intent data) {
        if (requestCode == 1) {
            if(ProxyService.sService != null) {
                ProxyService.sService.gotRequestFromGoogle();
            }
            finish();
        }
    }
}
