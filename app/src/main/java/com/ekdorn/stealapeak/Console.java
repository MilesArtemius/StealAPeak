package com.ekdorn.stealapeak;

import android.support.annotation.NonNull;

import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.functions.FirebaseFunctions;
import com.google.firebase.functions.HttpsCallableResult;

import java.util.HashMap;
import java.util.Map;

public class Console {
    private static final String FUNC_NAME    = "getUserByPhone";
    private static final String PHONE_FIELD  = "phone";
    private static final String NAME_FIELD   = "name";
    private static final String TOKEN_FIELD  = "token";

    public static void getTokenByPhone(final String phone, final OnLoaded loaded) {
        Map<String, String> data = new HashMap<>();
        data.put(PHONE_FIELD, phone);

        FirebaseFunctions.getInstance()
                .getHttpsCallable(FUNC_NAME)
                .call(data)
                .continueWith(new Continuation<HttpsCallableResult, Map<String, String>>() {
                    @Override
                    public Map<String, String> then(@NonNull Task<HttpsCallableResult> task) throws Exception {
                        return null;
                    }
                }).addOnCompleteListener(new OnCompleteListener<Map<String, String>>() {
                    @Override
                    public void onComplete(@NonNull Task<Map<String, String>> task) {
                        if (task.isSuccessful()) {
                            String name = task.getResult().get(NAME_FIELD);
                            loaded.onGot(new User((name == null) ? phone : name, task.getResult().get(TOKEN_FIELD)));
                        } else {
                            loaded.onGot(null);
                        }
                    }
                });
    }

    public interface OnLoaded {
        void onGot(User user);
    }
}
