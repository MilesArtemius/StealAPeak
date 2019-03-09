package com.ekdorn.stealapeak.database;

import android.arch.persistence.room.Database;
import android.arch.persistence.room.Room;
import android.arch.persistence.room.RoomDatabase;
import android.content.Context;

@Database(entities = {Contact.class, Message.class}, version = 3, exportSchema = false)
public abstract class AppDatabase extends RoomDatabase {
    private static AppDatabase INSTANCE;
    private static final String DB_NAME = "data.db";

    public static AppDatabase getDatabase(final Context context) {
        if (INSTANCE == null) {
            synchronized (AppDatabase.class) {
                if (INSTANCE == null) {
                    INSTANCE = Room.databaseBuilder(context.getApplicationContext(), AppDatabase.class, DB_NAME)
                            .fallbackToDestructiveMigration()
                            .allowMainThreadQueries() // SHOULD NOT BE USED IN PRODUCTION !!!
                            .build();
                }
            }
        }
        return INSTANCE;
    }
    public void clearDb() {
        if (INSTANCE != null) {
            INSTANCE.contactDao().deleteAllContacts();
            INSTANCE.messageDao().deleteAllMessages();
        }
    }
    public abstract ContactDao contactDao();
    public abstract MessageDao messageDao();
}
