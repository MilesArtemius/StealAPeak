package com.ekdorn.stealapeak;

import android.content.Context;
import android.net.Uri;
import android.support.annotation.NonNull;

import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.UserProfileChangeRequest;
import com.google.firebase.functions.FirebaseFunctions;
import com.google.firebase.functions.HttpsCallableResult;

import java.util.Map;

public class Console {
    private static final String FUNC_NAME    = "getUserByPhone";
    private static final String NAME_FIELD   = "name";
    private static final String TOKEN_FIELD  = "token";

    /**
     * const functions = require('firebase-functions');
     *
     * const admin = require('firebase-admin');
     * admin.initializeApp();
     *
     *
     *
     * exports.getUserByPhone = functions.https.onCall((data, context) => {
     *     const phoneNumber = data.phone;
     *
     *     if (context.auth != null) {
     *         return admin.auth().getUserByPhoneNumber(phoneNumber).then(function (userRecord) {
     *             console.log("User data shared of: ", phoneNumber);
     *             return {"name": userRecord.displayName, "token": userRecord.photoURL};
     *
     *         }).catch(function (error) {
     *             console.log("Error fetching user data:", error);
     *             throw new functions.https.HttpsError('invalid-argument', 'No user with given email found.');
     *         });
     *     }
     * });
     */
    @SuppressWarnings("unchecked")
    public static void getUserByPhone(final String phone, final OnLoaded loaded) {
        FirebaseFunctions.getInstance()
                .getHttpsCallable(FUNC_NAME)
                .call(phone)
                .continueWith(new Continuation<HttpsCallableResult, Map<String, String>>() {
                    @Override
                    public Map<String, String> then(@NonNull Task<HttpsCallableResult> task) throws Exception {
                        return (Map<String, String>) task.getResult().getData();
                    }
                }).addOnCompleteListener(new OnCompleteListener<Map<String, String>>() {
                    @Override
                    public void onComplete(@NonNull Task<Map<String, String>> task) {
                        if (task.isSuccessful()) {
                            String name = task.getResult().get(NAME_FIELD);
                            String token = task.getResult().get(TOKEN_FIELD);
                            loaded.onGot(new User((name == null) ? phone : name, (token == null) ? "token" : name), true);
                        } else {
                            loaded.onGot(null, false);
                        }
                    }
                });
    }

    public static void refreshAllContacts(final Context context) {
        Map<String, User> users = PrefManager.get(context).getAllUsers();

        for (final Map.Entry<String, User> usr: users.entrySet()) {
            getUserByPhone(usr.getKey(), new OnLoaded() {
                @Override
                public void onGot(User user, boolean successful) {
                    if (!successful) {
                        PrefManager.get(context).deleteUser(usr.getKey());
                    } else {
                        PrefManager.get(context).setUser(usr.getKey(), user);
                    }
                }
            });
        }
    }

    public static void reloadToken(String token, final Context context) {
        final String tok = (token == null) ? PrefManager.get(context).getToken(PrefManager.MY_TOKEN) : token;

        UserProfileChangeRequest profileUpdates = new UserProfileChangeRequest.Builder()
                .setPhotoUri(Uri.parse(tok))
                .build();

        FirebaseAuth.getInstance().getCurrentUser().updateProfile(profileUpdates)
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (!task.isSuccessful()) {
                            Console.reloadToken(tok, context);
                        }
                    }
                });
    }



    public interface OnLoaded {
        void onGot(User user, boolean successful);
    }
}
