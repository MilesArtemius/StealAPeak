const functions = require('firebase-functions');

const admin = require('firebase-admin');
admin.initializeApp();



/** It states for:
 * INPUT:
 * data instance of string, represents user phone number
 * OUTPUT:
 * return instanceof map<string, string>, where:
 * * name represents user name
 * * key represents user public key
 **/
exports.getUserByPhone = functions.https.onCall((data, context) => {
    if (context.auth != null) {
        return admin.auth().getUserByPhoneNumber(data.toString()).then(function (userRecord) {
            console.log("User data shared of: ", data.toString() + " of " + userRecord.displayName + " and " + userRecord.photoURL);
            const ret = userRecord.photoURL.split(":");
            return {"name": ret[0], "key": ret[1]};

        }).catch(function (error) {
            console.log("Error fetching user data:", error);
            return  false;
        });
    } else {
        return false;
    }
});



/** It states for:
 * INPUT:
 * data instance of map<string, string>, where:
 * * phone represents user phone number
 * * type represents message type
 * * text represents message text
 * OUTPUT:
 * return instanceof boolean, represents if message was sent
 **/
exports.sendMessage = functions.https.onCall((data, context) => {
    if (context.auth != null) {
        return admin.auth().getUserByPhoneNumber(data.phone).then(function (userRecord) {
            const message = {
                "token": userRecord.displayName,
                "data": {
                    "sender": context.auth.token.phone_number,
                    "type": data.type,
                    "text": data.text
                }
            };

            console.log(userRecord);
            return admin.messaging().send(message).then(function () {
                console.log("Message sent");
                return true;
            }).catch(function (error) {
                console.log("Error sending message: ", error);
                return false;
            });
        }).catch(function (error) {
            console.log("Error fetching user data: ", error);
            return false;
        });
    } else {
        return false;
    }
});



/** It states for:
 * INPUT:
 * data instance of map<string, string>, where:
 * * phone represents user phone number
 * * image represents user profile image
 * OUTPUT:
 * return instanceof boolean, represents if user profile image was updated
 **/
exports.uploadProfilePic = functions.https.onCall((data, context) => {
    if (context.auth != null) {
        const uploadTask = admin.storage().ref().child(data.phone).put(data.image);

        return uploadTask.on('state_changed', null, function(error) {
            console.log("UploadingImage: ", error);
            return false;
        }, function() {
            return uploadTask.snapshot.ref.getDownloadURL().then(function(downloadURL) {
                console.log('File available at', downloadURL);
                return true;
            });
        });
    } else {
        return false;
    }
});



/** It states for:
 * INPUT:
 * data instance of string, represents user phone number
 * OUTPUT:
 * return instanceof string, represents user image download URL
 **/
exports.downlodProfilePic = functions.https.onCall((data, context) => {
    if (context.auth != null) {
        return admin.storage().ref().child(data.toString()).getDownloadURL().then(function(downloadUrl) {
            return downloadUrl;
        }).catch(function(error) {
            console.log("Downloading image: ", error);
            return '';
        });
    } else {
        return '';
    }
});



/** It states for:
 * INPUT:
 * data instance of string, represents user phone number
 * OUTPUT:
 * return instanceof boolean, represents if user profile image was deleted
 **/
exports.deleteProfilePic = functions.https.onCall((data, context) => {
    if (context.auth != null) {
        return admin.storage().ref().child(data.toString()).delete().then(function() {
            return true;
        }).catch(function(error) {
            console.log("Deleting image: ", error);
            return false;
        });
    } else {
        return false;
    }
});
