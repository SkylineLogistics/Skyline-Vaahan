package online.skylinelogistics.vaahan.firebase;

import android.content.Intent;
import android.support.v4.content.LocalBroadcastManager;
import android.widget.Toast;

import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.iid.FirebaseInstanceIdService;

/**
 * Created by Apollyon on 29-03-2018.
 */

public class MessagingInstanceID extends FirebaseInstanceIdService {

    @Override
    public void onTokenRefresh()
    {
        String refreshedToken = FirebaseInstanceId.getInstance().getToken();

        Intent registrationComplete = new Intent("registrationComplete");
        registrationComplete.putExtra("token", refreshedToken);
        LocalBroadcastManager.getInstance(this).sendBroadcast(registrationComplete);
    }

}
