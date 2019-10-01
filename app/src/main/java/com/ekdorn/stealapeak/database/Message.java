package com.ekdorn.stealapeak.database;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.ForeignKey;
import androidx.room.Index;
import androidx.room.PrimaryKey;

import org.jetbrains.annotations.NotNull;

import java.io.Serializable;

@Entity(tableName = "message",
        foreignKeys = @ForeignKey(entity = Contact.class,
                parentColumns = "phone",
                childColumns = "referal",
                onDelete = ForeignKey.CASCADE),
        indices = {@Index("time"), @Index("referal")})
public class Message implements Serializable {
    @ColumnInfo(name = "referal")
    private String referal;

    @ColumnInfo(name = "my_message")
    private boolean myMessage;

    @PrimaryKey
    @ColumnInfo(name = "time")
    private long time;

    @ColumnInfo(name = "text")
    private String text;

    public Message(@NotNull String referal, boolean myMessage, long time, String text) {
        this.myMessage = myMessage;
        this.referal = referal;
        this.time = time;
        this.text = text;
    }

    @NotNull
    public boolean getMyMessage() {
        return myMessage;
    }

    @NotNull
    public String getReferal() {
        return referal;
    }

    public long getTime() {
        return time;
    }

    public String getText() {
        return text;
    }


}
