package com.ekdorn.stealapeak.database;

import android.arch.persistence.room.ColumnInfo;
import android.arch.persistence.room.Entity;
import android.arch.persistence.room.ForeignKey;
import android.arch.persistence.room.Index;
import android.arch.persistence.room.PrimaryKey;

import org.jetbrains.annotations.NotNull;

@Entity(tableName = "message",
        foreignKeys = @ForeignKey(entity = Contact.class,
                parentColumns = "phone",
                childColumns = "sender",
                onDelete = ForeignKey.CASCADE),
        indices = {@Index("time"), @Index("sender")})
public class Message {
    /*@StringDef(value = {
                    DATA_FLAG,
                    SERVICE_FLAG
            })
    @Retention(RetentionPolicy.SOURCE)
    public @interface Type {}*/

    @ColumnInfo(name = "sender")
    private String sender;

    @PrimaryKey
    @ColumnInfo(name = "time")
    private long time;

    @ColumnInfo(name = "text")
    private String text;

    public Message(@NotNull String sender, long time, String text) {
        this.sender = sender;
        this.time = time;
        this.text = text;
    }

    @NotNull
    public String getSender() {
        return sender;
    }

    public long getTime() {
        return time;
    }

    public String getText() {
        return text;
    }


}
