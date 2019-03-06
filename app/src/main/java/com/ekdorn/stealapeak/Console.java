package com.ekdorn.stealapeak;

import android.content.Context;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.util.Log;

import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.UserProfileChangeRequest;
import com.google.firebase.functions.FirebaseFunctions;
import com.google.firebase.functions.HttpsCallableResult;

import java.util.HashMap;
import java.util.Map;

public class Console {
    private static final String FUNC_NAME    = "getUserByPhone";
    private static final String NAME_FIELD   = "name";
    private static final String TOKEN_FIELD  = "key";

    /**
     * exports.getUserByPhone = functions.https.onCall((data, context) => {
     *     if (context.auth != null) {
     *         const phoneNumber = data.toString();
     *
     *         return admin.auth().getUserByPhoneNumber(phoneNumber).then(function (userRecord) {
     *             console.log("User data shared of: ", phoneNumber + " of " + userRecord.displayName + " and " + userRecord.photoURL);
     *             const ret = userRecord.photoURL.split(":");
     *             return {"name": ret[0], "key": ret[1]};
     *
     *         }).catch(function (error) {
     *             console.log("Error fetching user data:", error);
     *             throw new functions.https.HttpsError('invalid-argument', 'No user with given email found.');
     *         });
     *     }
     * });
     *
     * @param phone
     * @param loaded
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
                            String key = task.getResult().get(TOKEN_FIELD);
                            loaded.onGot(new User((name == null) ? phone : name, (key == null) ? "key" : key, false), true);
                        } else {
                            loaded.onGot(null, false);
                        }
                    }
                });
    }

    public static void refreshAllContacts(final Context context) {
        Map<String, User> users = PrefManager.get(context).getAllUsers();

        for (final Map.Entry<String, User> usr: users.entrySet()) {
            final boolean isNotificationOpened = usr.getValue().isNotificationOpened();
            getUserByPhone(usr.getKey(), new OnLoaded() {
                @Override
                public void onGot(User user, boolean successful) {
                    if (successful) {
                        user.setNotificationOpened(isNotificationOpened);
                        PrefManager.get(context).setUser(usr.getKey(), user);
                    }
                }
            });
        }
    }

    public static void reloadToken(final String token) {
        UserProfileChangeRequest profileUpdates = new UserProfileChangeRequest.Builder()
                .setDisplayName(token)
                .build();

        FirebaseAuth.getInstance().getCurrentUser().updateProfile(profileUpdates)
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()) {
                            Log.i("FIREBASE", "Token reloaded!");
                        }
                    }
                });
    }

    /**
     * exports.sendMessage = functions.https.onCall((data, context)  => {
     *     if (context.auth != null) {
     *         const phoneNumber = data.phone;
     *         const text = data.text;
     *         const type = data.type;
     *
     *         return admin.auth().getUserByPhoneNumber(phoneNumber).then(function (userRecord) {
     *             const message = {
     *                 "token": userRecord.displayName,
     *                 "data": {
     *                     "sender": context.auth.token.phone_number,
     *                     "time": admin.database.ServerValue.TIMESTAMP.toString(),
     *                     "type": type,
     *                     "text": text
     *                 }
     *             };
     *
     *             return admin.messaging().send(message).then(function () {
     *                 console.log("Message sent");
     *                 return true;
     *             }).catch(function (error) {
     *                 console.log("Error sending message:", error);
     *                 throw new functions.https.HttpsError('invalid-argument', 'No user with given uuid found.');
     *             });
     *         }).catch(function (error) {
     *             console.log("Error fetching user data:", error);
     *             throw new functions.https.HttpsError('invalid-argument', 'No user with given email found.');
     *         });
     *     }
     * });
     *
     * @param phone
     * @param text
     * @param type
     */
    public static void sendMessage(String phone, String text, String type) {
        Map<String, String> data = new HashMap<>();
        data.put("phone", phone);
        data.put("text", text);
        data.put("type", type);

        FirebaseFunctions.getInstance()
                .getHttpsCallable("sendMessage")
                .call(data)
                .continueWith(new Continuation<HttpsCallableResult, Boolean>() {
                    @Override
                    public Boolean then(@NonNull Task<HttpsCallableResult> task) throws Exception {
                        return (boolean) task.getResult().getData();
                    }
                }).addOnCompleteListener(new OnCompleteListener<Boolean>() {
                @Override
                public void onComplete(@NonNull Task<Boolean> task) {
                    if (!task.isSuccessful()) {
                        Log.e("TAG", "onComplete: failed " + task.getException());
                    }
                }
        });
    }



    public interface OnLoaded {
        void onGot(User user, boolean successful);
    }
}
