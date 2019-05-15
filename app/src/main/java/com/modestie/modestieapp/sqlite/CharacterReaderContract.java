package com.modestie.modestieapp.sqlite;

import android.provider.BaseColumns;

public final class CharacterReaderContract
{
    private CharacterReaderContract() {}

    public static class CharacterUpdateEntry implements BaseColumns
    {
        public static final String TABLE_NAME = "characterupdates";
        public static final String COLUMN_NAME_CHARACTER_ID = "lodestoneid";
        public static final String COLUMN_NAME_LAST_UPDATE = "lastupdate";
    }
}
