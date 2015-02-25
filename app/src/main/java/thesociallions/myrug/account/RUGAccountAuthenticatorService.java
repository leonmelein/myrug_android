package thesociallions.myrug.account;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;

public class RUGAccountAuthenticatorService extends Service {
    public RUGAccountAuthenticatorService() {
    }

    @Override
    public IBinder onBind(Intent intent) {
        // TODO Auto-generated method stub
        return new RUGAccountAuthenticator(this).getIBinder();
    }

}
