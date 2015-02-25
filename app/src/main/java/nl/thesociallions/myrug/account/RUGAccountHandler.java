package nl.thesociallions.myrug.account;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.content.Context;

/** RUGAccountHandler
 * Class for performing frequent actions with the RUG account credentials. Functions as a wrapper class
 * for AccountManager.
 */
public class RUGAccountHandler {
    AccountManager accountManager;

    /**
     * Creates an instance of the RUGAccountHandler object, that can be used to handle common credential scenarios
     * with a logged in RUG account.
     * @param theCTX    The caller's context.
     */
    public RUGAccountHandler(Context theCTX){
        accountManager = AccountManager.get(theCTX);
    }

    /**
     * Retrieves the currently set account. Throws an exception in case there is no account.
     *
     * @return  The current account as a native Account object
     */
    private Account getAccount() throws Exception{
        Account[] allMyRUG = accountManager.getAccountsByType("thesociallions.myrug.account");
        try {
            return allMyRUG[0];
        } catch (IndexOutOfBoundsException i) {
            throw new Exception("There is no account available");
        }
    }


    /**
     * Retrieves the password for the currently set account.
     *
     * @return        The name as a String
     */
    public String getName() throws Exception {
        return getAccount().name;
    }

    /**
     * Retrieves the password for the currently set account.
     *
     * @return        The password as a String
     */
    public String getPassword() throws Exception {
        return accountManager.getPassword(getAccount());
    }

    /**
     * Retrieves the schedule GUID for the currently set account.
     *
     * @return
     */
    public String getSchedule() throws Exception {
        Account account = getAccount();
        return accountManager.getUserData(account, "schedule_token");
    }
}
