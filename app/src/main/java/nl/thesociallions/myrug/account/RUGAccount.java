package nl.thesociallions.myrug.account;

/** RUGAccount
 * Class for adding RUG accounts to the Android AccountManager.
 * TODO: MULTIPLE CONSTUCTORS FOR AIDING NEW/EXISTING DIVIDE
 * TODO: DIVISION OF LABOR BETWEEN RUGAccount, RUGAccountHandler and the individual activities
 */

import android.accounts.Account;
import android.accounts.AccountManager;
import android.content.ContentResolver;
import android.content.Context;
import android.os.Bundle;

public class RUGAccount {

    private String user;
    private String password;
    private Bundle extra;
    private Context ctx;

    /**
     * Creates an instance of the RUGAccount object, that can be used to set up a new RUG account in
     * the Android Account Manager
     *
     * @param theUser       The user's student number
     * @param thePassword   The user's password
     * @param theGUID       The user' schedule id
     * @param theCTX        The caller's context
     */
    public RUGAccount(String theUser, String thePassword, String theGUID, Context theCTX){
        // Set context
        ctx = theCTX;

        // Store username and password
        user = theUser;
        password = thePassword;

        // Pack schedule token in Bundle
        extra = new Bundle();
        extra.putString("schedule_token", theGUID);
    }

    /**
     * Adds the RUGAccount instance to the AccountManager, via an intermediate native Account instance.
     * Furthermore, it enables automated synchronization for the newly added account.     *
     */
    public void addToAccountManager(){
        // Add account to AccountManger
        String authority = "thesociallions.myrug.account";
        final Account account = new Account(user, authority);
        AccountManager manager = AccountManager.get(ctx);
        manager.addAccountExplicitly(account, password, extra);

        // Tell ContentResolver that the account is available for syncing
        enableSync(account);
    }

    /**
     * Enables automated synchroization of data, multiple times per day, as governed by the Android
     * SyncManager framework
     *
     * @param account   An Android Account object
     */
    private void enableSync(Account account){
        String provider = "thesociallions.myrug.schedule";
        ContentResolver.setIsSyncable(account, provider, 1);
        ContentResolver.setSyncAutomatically(account, provider, true);
        ContentResolver.addPeriodicSync(account, provider, new Bundle(), 1440);

        Bundle settingsBundle = new Bundle();
        settingsBundle.putBoolean(
                ContentResolver.SYNC_EXTRAS_MANUAL, true);
        settingsBundle.putBoolean(
                ContentResolver.SYNC_EXTRAS_EXPEDITED, true);
        ContentResolver.requestSync(account, "thesociallions.myrug.schedule", settingsBundle);
    }
}