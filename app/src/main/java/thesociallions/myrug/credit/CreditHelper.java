package thesociallions.myrug.credit;

import android.content.SharedPreferences;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by leon on 30-11-14.
 */
public class CreditHelper {

    public static String matchSaldo(String content) throws Exception{
        String saldo;

        Pattern pattern = Pattern.compile("[0123456789.]*EUR");
        Matcher matcher = pattern.matcher(content);
        if (matcher.find()) {
            saldo = matcher.group(0);
            saldo = saldo.replace("EUR", "");
            saldo = saldo.replace(".", ",");
            return saldo;
        } else {
            throw new Exception("Could not find saldo");
        }
    }

    public static void saveSaldo(SharedPreferences preferences, String saldo){
        // Saldo als laatst bekend opslaan, inclusief datum van opvraag
        SharedPreferences.Editor editor = preferences.edit();
        editor.putString("saldo", saldo);

        SimpleDateFormat sdf = new SimpleDateFormat("dd-MM-yyyy HH:mm");
        Date dt = new Date();
        String now = sdf.format(dt);
        editor.putString("saldo_lastupdate", now);
        editor.commit();
    }
}
