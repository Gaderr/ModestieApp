package com.modestie.modestieapp.sqlite;

import android.provider.BaseColumns;

public final class FreeCompanyReaderContract
{
    // Prevent instantiation
    private FreeCompanyReaderContract() {}

    public static class FreeCompanyEntry implements BaseColumns
    {
        public static final String TABLE_NAME                       = "freecompanies";
        public static final String COLUMN_NAME_LODESTONEID          = "lodestoneid";
        public static final String COLUMN_NAME_NAME                 = "name";
        public static final String COLUMN_NAME_TAG                  = "tag";
        public static final String COLUMN_NAME_SLOGAN               = "slogan";
        public static final String COLUMN_NAME_CRESTBACKGROUND      = "crestbackground";
        public static final String COLUMN_NAME_CRESTFRAME           = "crestframe";
        public static final String COLUMN_NAME_CRESTLOGO            = "crestlogo";
        public static final String COLUMN_NAME_FORMED               = "formed";
        public static final String COLUMN_NAME_RANK                 = "rank";
        public static final String COLUMN_NAME_GRANDCOMPANYNAME     = "grandcompanyname";
        public static final String COLUMN_NAME_GRANDCOMPANYRANK     = "grandcompanyrank";
        public static final String COLUMN_NAME_GRANDCOMPANYPROGRESS = "grandcompanyprogress";
        public static final String COLUMN_NAME_MONTHLYRANKING       = "monthlyranking";
        public static final String COLUMN_NAME_WEEKLYRANKING        = "weeklyranking";
        public static final String COLUMN_NAME_ESTATENAME           = "estatename";
        public static final String COLUMN_NAME_ESTATEPLOT           = "estateplot";
        public static final String COLUMN_NAME_ACTIVE               = "active";
        public static final String COLUMN_NAME_RECRUITMENT          = "recruitment";
        public static final String COLUMN_NAME_UPDATED              = "updated";
    }

    public static class MemberEntry implements BaseColumns
    {
        public static final String TABLE_NAME               = "members";
        public static final String COLUMN_NAME_LODESTONEID  = "lodestoneid";
        public static final String COLUMN_NAME_NAME         = "name";
        public static final String COLUMN_NAME_AVATAR       = "avatar";
        public static final String COLUMN_NAME_RANK         = "rank";
        public static final String COLUMN_NAME_RANKICON     = "rankicon";
        public static final String COLUMN_NAME_FEASTMATCHES = "feastmatches";
    }

    public static class FocusEntry implements BaseColumns
    {
        public static final String TABLE_NAME           = "focus";
        public static final String COLUMN_NAME_NAME     = "name";
        public static final String COLUMN_NAME_ICON     = "icon";
        public static final String COLUMN_NAME_STATUS   = "status";
    }

    public static class SeekedRoleEntry implements BaseColumns
    {
        public static final String TABLE_NAME           = "seekedroles";
        public static final String COLUMN_NAME_NAME     = "name";
        public static final String COLUMN_NAME_ICON     = "icon";
        public static final String COLUMN_NAME_STATUS   = "status";
    }
}
