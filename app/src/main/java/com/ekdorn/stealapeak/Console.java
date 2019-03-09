package com.ekdorn.stealapeak;

import android.content.Context;
import android.support.annotation.NonNull;
import android.util.Log;
import android.widget.Toast;

import com.ekdorn.stealapeak.database.Contact;
import com.ekdorn.stealapeak.database.AppDatabase;
import com.ekdorn.stealapeak.database.Message;
import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.UserProfileChangeRequest;
import com.google.firebase.functions.FirebaseFunctions;
import com.google.firebase.functions.HttpsCallableResult;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Console {
    private static final String FUNC_GUBP_NAME    = "getUserByPhone";
    private static final String NAME_FIELD   = "name";
    private static final String TOKEN_FIELD  = "key";

    /**
     * exports.getUserByPhone = functions.https.onCall((data, context) => {
     *     if (context.auth != null) {
     *         const phoneNumber = data.toString();
     *
     *         return admin.auth().getUserByPhoneNumber(phoneNumber).then(function (userRecord) {
     *             console.log("ContactViewer data shared of: ", phoneNumber + " of " + userRecord.displayName + " and " + userRecord.photoURL);
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
                .getHttpsCallable(FUNC_GUBP_NAME)
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
                            loaded.onGot(new Contact(phone, (name == null) ? phone : name, (key == null) ? TOKEN_FIELD : key, false), true);
                        } else {
                            loaded.onGot(null, false);
                        }
                    }
                });
    }

    public static void refreshAllContacts(final Context context) {
        String myPhone = FirebaseAuth.getInstance().getCurrentUser().getPhoneNumber();
        List<Contact> contacts = AppDatabase.getDatabase(context).contactDao().getAllContacts(myPhone).getValue();

        if (contacts != null) {
            for (final Contact contact : contacts) {
                final boolean isNotificationOpened = contact.isActive();
                getUserByPhone(contact.getPhone(), new OnLoaded() {
                    @Override
                    public void onGot(Contact contact, boolean successful) {
                        if (successful) {
                            contact.setActive(isNotificationOpened);
                            AppDatabase.getDatabase(context).contactDao().updateContact(contact);
                        }
                    }
                });
            }
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

    private static final String FUNC_SM_NAME = "sendMessage";
    private static final String PHONE_FIELD  = "phone";
    private static final String TEXT_FIELD   = "text";
    private static final String TYPE_FIELD   = "type";

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
    public static void sendMessage(String phone, String text, String type, final Context context) {
        if (AppDatabase.getDatabase(context).contactDao().isContact(phone))
            AppDatabase.getDatabase(context).messageDao().setMessage(new Message(phone, true, System.currentTimeMillis(), text));

        Map<String, String> data = new HashMap<>();
        data.put(PHONE_FIELD, phone);
        data.put(TEXT_FIELD, text);
        data.put(TYPE_FIELD, type);

        FirebaseFunctions.getInstance()
                .getHttpsCallable(FUNC_SM_NAME)
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
                        Toast.makeText(context, "User does not exist!", Toast.LENGTH_SHORT).show();
                    }
                }
        });
    }



    public interface OnLoaded {
        void onGot(Contact contact, boolean successful);
    }
}
