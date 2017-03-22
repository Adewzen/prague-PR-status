package cz.adewzen.prstatus;

import android.app.IntentService;
import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.util.Log;

public class GetDataService extends IntentService {

    public GetDataService() {
        super("GetDataService");
    }

    @Override
    protected void onHandleIntent(@Nullable Intent intent) {
        Log.i("Service","Start of GetData");
        MainActivity.getDataFromUrl();
        Log.i("Service","end of getdata");
        synchronized (MainActivity.sharedLock) {
            MainActivity.sharedLock.notify();
        }
    }
}
