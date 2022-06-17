package hcmute.nhom2.zaloapp.utilities;

import android.content.Context;
import android.content.SharedPreferences;

import java.util.Set;

public class PreferenceManager {
    // Tạo SharedPreferences
    private final SharedPreferences sharedPreferences;

    public PreferenceManager(Context context) {
        // Khởi tạo SharedPreferences
        sharedPreferences = context.getSharedPreferences(Constants.KEY_PREFERENCE_NAME, Context.MODE_PRIVATE);
    }

    // Lưu trữ giá trị Boolean
    public void putBoolean(String key, Boolean value) {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putBoolean(key, value);
        editor.apply();
    }

    // Lấy giá trị Boolean
    public Boolean getBoolean(String key) {
        return sharedPreferences.getBoolean(key, false);
    }

    // Lưu trữ giá trị String
    public void putString(String key, String value) {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString(key, value);
        editor.apply();
    }

    // Lấy giá trị String
    public String getString(String key) {
        return sharedPreferences.getString(key, null);
    }

    // Lưu trữ giá trị Set
    public void putStringSet(String key, Set<String> value) {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putStringSet(key, value);
        editor.apply();
    }

    // Lấy giá trị Set
    public Set<String> getStringSet(String key) {
        return sharedPreferences.getStringSet(key, null);
    }

    public void clear() {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.clear();
        editor.apply();
    }
}
