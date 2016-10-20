package com.thinkbiganalytics.nifi.v2.sqoop.enums;

/**
 * @author jagrut sharma
 */
/*
List of Hive Null encoding strategies
 */
public enum HiveNullEncodingStrategy {
    ENCODE_STRING_AND_NONSTRING,
    DO_NOT_ENCODE,
    ENCODE_ONLY_STRING,
    ENCODE_ONLY_NONSTRING;

    @Override
    public String toString() {
        switch (this) {
            case ENCODE_STRING_AND_NONSTRING:
                return "ENCODE_STRING_AND_NONSTRING";
            case DO_NOT_ENCODE:
                return "DO_NOT_ENCODE";
            case ENCODE_ONLY_STRING:
                return "ENCODE_ONLY_STRING";
            case ENCODE_ONLY_NONSTRING:
                return "ENCODE_ONLY_NONSTRING";
        }
        return null;
    }
}