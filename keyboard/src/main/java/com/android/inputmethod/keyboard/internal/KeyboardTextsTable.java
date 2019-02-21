/*
 * Copyright (C) 2014 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.android.inputmethod.keyboard.internal;

import java.util.HashMap;
import java.util.Locale;

/**
 * !!!!! DO NOT EDIT THIS FILE !!!!!
 * <p>
 * This file is generated by tools/make-keyboard-text. The base template file is
 * tools/make-keyboard-text/res/src/com/android/inputmethod/keyboard/internal/
 * KeyboardTextsTable.tmpl
 * <p>
 * This file must be updated when any text resources in keyboard layout files have been changed.
 * These text resources are referred as "!text/<resource_name>" in keyboard XML definitions,
 * and should be defined in
 * tools/make-keyboard-text/res/values-<locale>/donottranslate-more-keys.xml
 * <p>
 * To update this file, please run the following commands.
 * $ cd $ANDROID_BUILD_TOP
 * $ mmm packages/inputmethods/LatinIME/tools/make-keyboard-text
 * $ make-keyboard-text -java packages/inputmethods/LatinIME/java
 * <p>
 * The updated source file will be generated to the following path (this file).
 * packages/inputmethods/LatinIME/java/src/com/android/inputmethod/keyboard/internal/
 * KeyboardTextsTable.java
 */
public final class KeyboardTextsTable {
    // Name to index map.
    private static final HashMap<String, Integer> sNameToIndexesMap = new HashMap<>();
    // Locale to texts table map.
    private static final HashMap<String, String[]> sLocaleToTextsTableMap = new HashMap<>();
    // TODO: Remove this variable after debugging.
    // Texts table to locale maps.
    private static final HashMap<String[], String> sTextsTableToLocaleMap = new HashMap<>();

    public static String getText(final String name, final String[] textsTable) {
        final Integer indexObj = sNameToIndexesMap.get(name);
        if (indexObj == null) {
            throw new RuntimeException("Unknown text name=" + name + " locale="
                    + sTextsTableToLocaleMap.get(textsTable));
        }
        final int index = indexObj;
        final String text = (index < textsTable.length) ? textsTable[index] : null;
        if (text != null) {
            return text;
        }
        // Sanity check.
        if (index >= 0 && index < TEXTS_DEFAULT.length) {
            return TEXTS_DEFAULT[index];
        }
        // Throw exception for debugging purpose.
        throw new RuntimeException("Illegal index=" + index + " for name=" + name
                + " locale=" + sTextsTableToLocaleMap.get(textsTable));
    }

    public static String[] getTextsTable(final Locale locale) {
        final String localeKey = locale.toString();
        if (sLocaleToTextsTableMap.containsKey(localeKey)) {
            return sLocaleToTextsTableMap.get(localeKey);
        }
        final String languageKey = locale.getLanguage();
        if (sLocaleToTextsTableMap.containsKey(languageKey)) {
            return sLocaleToTextsTableMap.get(languageKey);
        }
        return TEXTS_DEFAULT;
    }

    private static final String[] NAMES = {
            //  /* index:histogram */ "name",
            /*   0:12 */ "keylabel_to_alpha",
            /*   1:12 */ "additional_morekeys_symbols_1",
            /*   2:12 */ "additional_morekeys_symbols_2",
            /*   3:12 */ "additional_morekeys_symbols_3",
            /*   4:12 */ "additional_morekeys_symbols_4",
            /*   5:12 */ "additional_morekeys_symbols_5",
            /*   6:12 */ "additional_morekeys_symbols_6",
            /*   7:12 */ "additional_morekeys_symbols_7",
            /*   8:12 */ "additional_morekeys_symbols_8",
            /*   9:12 */ "additional_morekeys_symbols_9",
            /*  10:12 */ "additional_morekeys_symbols_0",
            /*  11:11 */ "keyspec_symbols_1",
            /*  12:11 */ "keyspec_symbols_2",
            /*  13:11 */ "keyspec_symbols_3",
            /*  14:11 */ "keyspec_symbols_4",
            /*  15:11 */ "keyspec_symbols_5",
            /*  16:11 */ "keyspec_symbols_6",
            /*  17:11 */ "keyspec_symbols_7",
            /*  18:11 */ "keyspec_symbols_8",
            /*  19:11 */ "keyspec_symbols_9",
            /*  20:11 */ "keyspec_symbols_0",
            /*  21:11 */ "keylabel_to_symbol",
            /*  22: 2 */ "morekeys_a",
            /*  23: 2 */ "morekeys_e",
            /*  24: 2 */ "morekeys_i",
            /*  25: 2 */ "morekeys_o",
            /*  26: 2 */ "morekeys_u",
            /*  27: 2 */ "morekeys_s",
            /*  28: 2 */ "morekeys_n",
            /*  29: 2 */ "morekeys_c",
            /*  30: 1 */ "morekeys_y",
            /*  31: 1 */ "morekeys_d",
            /*  32: 1 */ "morekeys_r",
            /*  33: 1 */ "morekeys_t",
            /*  34: 1 */ "morekeys_z",
            /*  35: 1 */ "morekeys_k",
            /*  36: 1 */ "morekeys_l",
            /*  37: 1 */ "morekeys_g",
            /*  38: 1 */ "morekeys_h",
            /*  39: 1 */ "morekeys_j",
            /*  40: 1 */ "morekeys_w",
            /*  41: 0 */ "morekeys_v",
            /*  42: 0 */ "morekeys_q",
            /*  43: 0 */ "morekeys_x",
            /*  44: 0 */ "keyspec_q",
            /*  45: 0 */ "keyspec_w",
            /*  46: 0 */ "keyspec_y",
            /*  47: 0 */ "keyspec_x",
            /*  48: 0 */ "keyspec_nordic_row1_11",
            /*  49: 0 */ "keyspec_nordic_row2_10",
            /*  50: 0 */ "keyspec_nordic_row2_11",
            /*  51: 0 */ "morekeys_nordic_row2_10",
            /*  52: 0 */ "morekeys_nordic_row2_11",
            /*  53: 0 */ "keyspec_east_slavic_row1_9",
            /*  54: 0 */ "keyspec_east_slavic_row2_2",
            /*  55: 0 */ "keyspec_east_slavic_row2_11",
            /*  56: 0 */ "keyspec_east_slavic_row3_5",
            /*  57: 0 */ "morekeys_east_slavic_row2_2",
            /*  58: 0 */ "morekeys_east_slavic_row2_11",
            /*  59: 0 */ "morekeys_cyrillic_u",
            /*  60: 0 */ "morekeys_cyrillic_ka",
            /*  61: 0 */ "morekeys_cyrillic_en",
            /*  62: 0 */ "morekeys_cyrillic_ghe",
            /*  63: 0 */ "morekeys_cyrillic_a",
            /*  64: 0 */ "morekeys_cyrillic_o",
            /*  65: 0 */ "morekeys_cyrillic_i",
            /*  66: 0 */ "morekeys_cyrillic_ie",
            /*  67: 0 */ "morekeys_cyrillic_soft_sign",
            /*  68: 0 */ "keyspec_south_slavic_row1_6",
            /*  69: 0 */ "keyspec_south_slavic_row2_11",
            /*  70: 0 */ "keyspec_south_slavic_row3_1",
            /*  71: 0 */ "keyspec_south_slavic_row3_8",
            /*  72: 0 */ "keyspec_swiss_row1_11",
            /*  73: 0 */ "keyspec_swiss_row2_10",
            /*  74: 0 */ "keyspec_swiss_row2_11",
            /*  75: 0 */ "morekeys_swiss_row1_11",
            /*  76: 0 */ "morekeys_swiss_row2_10",
            /*  77: 0 */ "morekeys_swiss_row2_11",
            /*  78: 0 */ "single_quotes",
            /*  79: 0 */ "double_quotes",
            /*  80: 0 */ "single_angle_quotes",
            /*  81: 0 */ "double_angle_quotes",
            /*  82: 0 */ "morekeys_currency_dollar",
            /*  83: 0 */ "keylabel_for_currency",
            /*  84: 0 */ "morekeys_currency_generic",
            /*  85: 0 */ "morekeys_punctuation",
            /*  86: 0 */ "morekeys_tablet_punctuation",
            /*  87: 0 */ "keyspec_spanish_row2_10",
            /*  88: 0 */ "morekeys_star",
            /*  89: 0 */ "morekeys_bullet",
            /*  90: 0 */ "morekeys_plus",
            /*  91: 0 */ "morekeys_left_parenthesis",
            /*  92: 0 */ "morekeys_right_parenthesis",
            /*  93: 0 */ "morekeys_less_than",
            /*  94: 0 */ "morekeys_greater_than",
            /*  95: 0 */ "morekeys_arabic_diacritics",
            /*  96: 0 */ "morekeys_symbols_1",
            /*  97: 0 */ "morekeys_symbols_2",
            /*  98: 0 */ "morekeys_symbols_3",
            /*  99: 0 */ "morekeys_symbols_4",
            /* 100: 0 */ "morekeys_symbols_5",
            /* 101: 0 */ "morekeys_symbols_6",
            /* 102: 0 */ "morekeys_symbols_7",
            /* 103: 0 */ "morekeys_symbols_8",
            /* 104: 0 */ "morekeys_symbols_9",
            /* 105: 0 */ "morekeys_symbols_0",
            /* 106: 0 */ "keyspec_left_parenthesis",
            /* 107: 0 */ "keyspec_right_parenthesis",
            /* 108: 0 */ "keyspec_left_square_bracket",
            /* 109: 0 */ "keyspec_right_square_bracket",
            /* 110: 0 */ "keyspec_left_curly_bracket",
            /* 111: 0 */ "keyspec_right_curly_bracket",
            /* 112: 0 */ "keyspec_less_than",
            /* 113: 0 */ "keyspec_greater_than",
            /* 114: 0 */ "keyspec_less_than_equal",
            /* 115: 0 */ "keyspec_greater_than_equal",
            /* 116: 0 */ "keyspec_left_double_angle_quote",
            /* 117: 0 */ "keyspec_right_double_angle_quote",
            /* 118: 0 */ "keyspec_left_single_angle_quote",
            /* 119: 0 */ "keyspec_right_single_angle_quote",
            /* 120: 0 */ "keyspec_comma",
            /* 121: 0 */ "keyspec_tablet_comma",
            /* 122: 0 */ "keyhintlabel_tablet_comma",
            /* 123: 0 */ "morekeys_tablet_comma",
            /* 124: 0 */ "keyspec_period",
            /* 125: 0 */ "keyhintlabel_period",
            /* 126: 0 */ "morekeys_period",
            /* 127: 0 */ "keyspec_tablet_period",
            /* 128: 0 */ "keyhintlabel_tablet_period",
            /* 129: 0 */ "morekeys_tablet_period",
            /* 130: 0 */ "keyspec_symbols_question",
            /* 131: 0 */ "keyspec_symbols_semicolon",
            /* 132: 0 */ "keyspec_symbols_percent",
            /* 133: 0 */ "morekeys_exclamation",
            /* 134: 0 */ "morekeys_question",
            /* 135: 0 */ "morekeys_symbols_semicolon",
            /* 136: 0 */ "morekeys_symbols_percent",
            /* 137: 0 */ "morekeys_am_pm",
            /* 138: 0 */ "keyspec_settings",
            /* 139: 0 */ "keyspec_shortcut",
            /* 140: 0 */ "keyspec_action_next",
            /* 141: 0 */ "keyspec_action_previous",
            /* 142: 0 */ "keylabel_to_more_symbol",
            /* 143: 0 */ "keylabel_tablet_to_more_symbol",
            /* 144: 0 */ "keylabel_to_phone_numeric",
            /* 145: 0 */ "keylabel_to_phone_symbols",
            /* 146: 0 */ "keylabel_time_am",
            /* 147: 0 */ "keylabel_time_pm",
            /* 148: 0 */ "keyspec_popular_domain",
            /* 149: 0 */ "morekeys_popular_domain",
            /* 150: 0 */ "keyspecs_left_parenthesis_more_keys",
            /* 151: 0 */ "keyspecs_right_parenthesis_more_keys",
            /* 152: 0 */ "single_laqm_raqm",
            /* 153: 0 */ "single_raqm_laqm",
            /* 154: 0 */ "double_laqm_raqm",
            /* 155: 0 */ "double_raqm_laqm",
            /* 156: 0 */ "single_lqm_rqm",
            /* 157: 0 */ "single_9qm_lqm",
            /* 158: 0 */ "single_9qm_rqm",
            /* 159: 0 */ "single_rqm_9qm",
            /* 160: 0 */ "double_lqm_rqm",
            /* 161: 0 */ "double_9qm_lqm",
            /* 162: 0 */ "double_9qm_rqm",
            /* 163: 0 */ "double_rqm_9qm",
            /* 164: 0 */ "morekeys_single_quote",
            /* 165: 0 */ "morekeys_double_quote",
            /* 166: 0 */ "morekeys_tablet_double_quote",
            /* 167: 0 */ "keyspec_emoji_action_key",
            /* 168: 0 */ "keyspec_settings_action_key",
            /* 169: 0 */ "keyspec_one_by_four",
            /* 170: 0 */ "keyspec_two_by_four",
            /* 171: 0 */ "keyspec_three_by_four",
    };

    private static final String EMPTY = "";

    /* Default texts */
    private static final String[] TEXTS_DEFAULT = {
            // Label for "switch to alphabetic" key.
            /* keylabel_to_alpha */ "ABC",
            /* additional_morekeys_symbols_1 ~ */
            EMPTY, EMPTY, EMPTY, EMPTY, EMPTY, EMPTY, EMPTY, EMPTY, EMPTY, EMPTY,
            /* ~ additional_morekeys_symbols_0 */
            /* keyspec_symbols_1 */ "1",
            /* keyspec_symbols_2 */ "2",
            /* keyspec_symbols_3 */ "3",
            /* keyspec_symbols_4 */ "4",
            /* keyspec_symbols_5 */ "5",
            /* keyspec_symbols_6 */ "6",
            /* keyspec_symbols_7 */ "7",
            /* keyspec_symbols_8 */ "8",
            /* keyspec_symbols_9 */ "9",
            /* keyspec_symbols_0 */ "0",
            // Label for "switch to symbols" key.
            /* keylabel_to_symbol */ "?123",
            /* morekeys_a ~ */
            EMPTY, EMPTY, EMPTY, EMPTY, EMPTY, EMPTY, EMPTY, EMPTY, EMPTY, EMPTY, EMPTY, EMPTY, EMPTY,
            EMPTY, EMPTY, EMPTY, EMPTY, EMPTY, EMPTY, EMPTY, EMPTY, EMPTY,
            /* ~ morekeys_x */
            /* keyspec_q */ "q",
            /* keyspec_w */ "w",
            /* keyspec_y */ "y",
            /* keyspec_x */ "x",
            /* keyspec_nordic_row1_11 ~ */
            EMPTY, EMPTY, EMPTY, EMPTY, EMPTY, EMPTY, EMPTY, EMPTY, EMPTY, EMPTY, EMPTY, EMPTY, EMPTY,
            EMPTY, EMPTY, EMPTY, EMPTY, EMPTY, EMPTY, EMPTY, EMPTY, EMPTY, EMPTY, EMPTY, EMPTY, EMPTY,
            EMPTY, EMPTY, EMPTY, EMPTY,
            /* ~ morekeys_swiss_row2_11 */
            /* single_quotes */ "!text/single_lqm_rqm",
            /* double_quotes */ "!text/double_lqm_rqm",
            /* single_angle_quotes */ "!text/single_laqm_raqm",
            /* double_angle_quotes */ "!text/double_laqm_raqm",
            // $ - I'm making ₹ as default
            // U+00A2: "¢" CENT SIGN
            // U+00A3: "£" POUND SIGN
            // U+20AC: "€" EURO SIGN
            // U+00A5: "¥" YEN SIGN
            // U+20B1: "₱" PESO SIGN
            /* morekeys_currency_dollar */ "$,\u00A2,\u00A3,\u20AC,\u00A5,\u20B1",
            /* keylabel_for_currency */ "$",
            /* morekeys_currency_generic */ "$,\u00A2,\u20AC,\u00A3,\u00A5,\u20B1",
            /* morekeys_punctuation */ "!autoColumnOrder!8,\\,,?,!,#,!text/keyspec_right_parenthesis,!text/keyspec_left_parenthesis,/,;,',@,:,-,\",+,\\%,&",
            /* morekeys_tablet_punctuation */ "!autoColumnOrder!7,\\,,',#,!text/keyspec_right_parenthesis,!text/keyspec_left_parenthesis,/,;,@,:,-,\",+,\\%,&",
            // U+00F1: "ñ" LATIN SMALL LETTER N WITH TILDE
            /* keyspec_spanish_row2_10 */ "\u00F1",
            // U+2020: "†" DAGGER
            // U+2021: "‡" DOUBLE DAGGER
            // U+2605: "★" BLACK STAR
            /* morekeys_star */ "\u2020,\u2021,\u2605",
            // U+266A: "♪" EIGHTH NOTE
            // U+2665: "♥" BLACK HEART SUIT
            // U+2660: "♠" BLACK SPADE SUIT
            // U+2666: "♦" BLACK DIAMOND SUIT
            // U+2663: "♣" BLACK CLUB SUIT
            /* morekeys_bullet */ "\u266A,\u2665,\u2660,\u2666,\u2663",
            // U+00B1: "±" PLUS-MINUS SIGN
            /* morekeys_plus */ "\u00B1",
            /* morekeys_left_parenthesis */ "!fixedColumnOrder!3,!text/keyspecs_left_parenthesis_more_keys",
            /* morekeys_right_parenthesis */ "!fixedColumnOrder!3,!text/keyspecs_right_parenthesis_more_keys",
            /* morekeys_less_than */ "!fixedColumnOrder!3,!text/keyspec_left_single_angle_quote,!text/keyspec_less_than_equal,!text/keyspec_left_double_angle_quote",
            /* morekeys_greater_than */ "!fixedColumnOrder!3,!text/keyspec_right_single_angle_quote,!text/keyspec_greater_than_equal,!text/keyspec_right_double_angle_quote",
            /* morekeys_arabic_diacritics */ EMPTY,
            // U+00B9: "¹" SUPERSCRIPT ONE
            // U+00BD: "½" VULGAR FRACTION ONE HALF
            // U+2153: "⅓" VULGAR FRACTION ONE THIRD
            // U+00BC: "¼" VULGAR FRACTION ONE QUARTER
            // U+215B: "⅛" VULGAR FRACTION ONE EIGHTH
            /* morekeys_symbols_1 */ "\u00B9,\u00BD,\u2153,\u00BC,\u215B",
            // U+00B2: "²" SUPERSCRIPT TWO
            // U+2154: "⅔" VULGAR FRACTION TWO THIRDS
            /* morekeys_symbols_2 */ "\u00B2,\u2154",
            // U+00B3: "³" SUPERSCRIPT THREE
            // U+00BE: "¾" VULGAR FRACTION THREE QUARTERS
            // U+215C: "⅜" VULGAR FRACTION THREE EIGHTHS
            /* morekeys_symbols_3 */ "\u00B3,\u00BE,\u215C",
            // U+2074: "⁴" SUPERSCRIPT FOUR
            /* morekeys_symbols_4 */ "\u2074",
            // U+215D: "⅝" VULGAR FRACTION FIVE EIGHTHS
            /* morekeys_symbols_5 */ "\u215D",
            /* morekeys_symbols_6 */ EMPTY,
            // U+215E: "⅞" VULGAR FRACTION SEVEN EIGHTHS
            /* morekeys_symbols_7 */ "\u215E",
            /* morekeys_symbols_8 */ EMPTY,
            /* morekeys_symbols_9 */ EMPTY,
            // U+207F: "ⁿ" SUPERSCRIPT LATIN SMALL LETTER N
            // U+2205: "∅" EMPTY SET
            /* morekeys_symbols_0 */ "\u207F,\u2205",
            // The all letters need to be mirrored are found at
            // http://www.unicode.org/Public/6.1.0/ucd/BidiMirroring.txt
            // U+2039: "‹" SINGLE LEFT-POINTING ANGLE QUOTATION MARK
            // U+203A: "›" SINGLE RIGHT-POINTING ANGLE QUOTATION MARK
            // U+2264: "≤" LESS-THAN OR EQUAL TO
            // U+2265: "≥" GREATER-THAN EQUAL TO
            // U+00AB: "«" LEFT-POINTING DOUBLE ANGLE QUOTATION MARK
            // U+00BB: "»" RIGHT-POINTING DOUBLE ANGLE QUOTATION MARK
            /* keyspec_left_parenthesis */ "(",
            /* keyspec_right_parenthesis */ ")",
            /* keyspec_left_square_bracket */ "[",
            /* keyspec_right_square_bracket */ "]",
            /* keyspec_left_curly_bracket */ "{",
            /* keyspec_right_curly_bracket */ "}",
            /* keyspec_less_than */ "<",
            /* keyspec_greater_than */ ">",
            /* keyspec_less_than_equal */ "\u2264",
            /* keyspec_greater_than_equal */ "\u2265",
            /* keyspec_left_double_angle_quote */ "\u00AB",
            /* keyspec_right_double_angle_quote */ "\u00BB",
            /* keyspec_left_single_angle_quote */ "\u2039",
            /* keyspec_right_single_angle_quote */ "\u203A",
            // Comma key
            /* keyspec_comma */ ",",
            /* keyspec_tablet_comma */ ",",
            /* keyhintlabel_tablet_comma */ EMPTY,
            /* morekeys_tablet_comma */ EMPTY,
            // Period key
            /* keyspec_period */ ".",
            /* keyhintlabel_period */ EMPTY,
            /* morekeys_period */ "!text/morekeys_punctuation",
            /* keyspec_tablet_period */ ".",
            /* keyhintlabel_tablet_period */ EMPTY,
            /* morekeys_tablet_period */ "!text/morekeys_tablet_punctuation",
            /* keyspec_symbols_question */ "?",
            /* keyspec_symbols_semicolon */ ";",
            /* keyspec_symbols_percent */ "%",
            // U+00A1: "¡" INVERTED EXCLAMATION MARK
            /* morekeys_exclamation */ "\u00A1",
            // U+00BF: "¿" INVERTED QUESTION MARK
            /* morekeys_question */ "\u00BF",
            /* morekeys_symbols_semicolon */ EMPTY,
            // U+2030: "‰" PER MILLE SIGN
            /* morekeys_symbols_percent */ "\u2030",
            /* morekeys_am_pm */ "!fixedColumnOrder!2,!hasLabels!,!text/keylabel_time_am,!text/keylabel_time_pm",
            /* keyspec_settings */ "!icon/settings_key|!code/key_settings",
            /* keyspec_shortcut */ "!icon/shortcut_key|!code/key_shortcut",
            /* keyspec_action_next */ "!hasLabels!,!text/label_next_key|!code/key_action_next",
            /* keyspec_action_previous */ "!hasLabels!,!text/label_previous_key|!code/key_action_previous",
            // Label for "switch to more symbol" modifier key ("= \ <"). Must be short to fit on key!
            /* keylabel_to_more_symbol */ "= \\\\ <",
            // Label for "switch to more symbol" modifier key on tablets.  Must be short to fit on key!
            /* keylabel_tablet_to_more_symbol */ "~ [ <",
            // Label for "switch to phone numeric" key.  Must be short to fit on key!
            /* keylabel_to_phone_numeric */ "123",
            // Label for "switch to phone symbols" key.  Must be short to fit on key!
            // U+FF0A: "＊" FULLWIDTH ASTERISK
            // U+FF03: "＃" FULLWIDTH NUMBER SIGN
            /* keylabel_to_phone_symbols */ "\uFF0A\uFF03",
            // Key label for "ante meridiem"
            /* keylabel_time_am */ "AM",
            // Key label for "post meridiem"
            /* keylabel_time_pm */ "PM",
            /* keyspec_popular_domain */ ".com",
            // popular web domains for the locale - most popular, displayed on the keyboard
            /* morekeys_popular_domain */ "!hasLabels!,.net,.org,.gov,.edu",
            /* keyspecs_left_parenthesis_more_keys */ "!text/keyspec_less_than,!text/keyspec_left_curly_bracket,!text/keyspec_left_square_bracket",
            /* keyspecs_right_parenthesis_more_keys */ "!text/keyspec_greater_than,!text/keyspec_right_curly_bracket,!text/keyspec_right_square_bracket",
            // The following characters don't need BIDI mirroring.
            // U+2018: "‘" LEFT SINGLE QUOTATION MARK
            // U+2019: "’" RIGHT SINGLE QUOTATION MARK
            // U+201A: "‚" SINGLE LOW-9 QUOTATION MARK
            // U+201C: "“" LEFT DOUBLE QUOTATION MARK
            // U+201D: "”" RIGHT DOUBLE QUOTATION MARK
            // U+201E: "„" DOUBLE LOW-9 QUOTATION MARK
            // Abbreviations are:
            // laqm: LEFT-POINTING ANGLE QUOTATION MARK
            // raqm: RIGHT-POINTING ANGLE QUOTATION MARK
            // lqm: LEFT QUOTATION MARK
            // rqm: RIGHT QUOTATION MARK
            // 9qm: LOW-9 QUOTATION MARK
            // The following each quotation mark pair consist of
            // <opening quotation mark>, <closing quotation mark>
            // and is named after (single|double)_<opening quotation mark>_<closing quotation mark>.
            /* single_laqm_raqm */ "!text/keyspec_left_single_angle_quote,!text/keyspec_right_single_angle_quote",
            /* single_raqm_laqm */ "!text/keyspec_right_single_angle_quote,!text/keyspec_left_single_angle_quote",
            /* double_laqm_raqm */ "!text/keyspec_left_double_angle_quote,!text/keyspec_right_double_angle_quote",
            /* double_raqm_laqm */ "!text/keyspec_right_double_angle_quote,!text/keyspec_left_double_angle_quote",
            // The following each quotation mark triplet consists of
            // <another quotation mark>, <opening quotation mark>, <closing quotation mark>
            // and is named after (single|double)_<opening quotation mark>_<closing quotation mark>.
            /* single_lqm_rqm */ "\u201A,\u2018,\u2019",
            /* single_9qm_lqm */ "\u2019,\u201A,\u2018",
            /* single_9qm_rqm */ "\u2018,\u201A,\u2019",
            /* single_rqm_9qm */ "\u2018,\u2019,\u201A",
            /* double_lqm_rqm */ "\u201E,\u201C,\u201D",
            /* double_9qm_lqm */ "\u201D,\u201E,\u201C",
            /* double_9qm_rqm */ "\u201C,\u201E,\u201D",
            /* double_rqm_9qm */ "\u201C,\u201D,\u201E",
            /* morekeys_single_quote */ "!fixedColumnOrder!5,!text/single_quotes,!text/single_angle_quotes",
            /* morekeys_double_quote */ "!fixedColumnOrder!5,!text/double_quotes,!text/double_angle_quotes",
            /* morekeys_tablet_double_quote */ "!fixedColumnOrder!6,!text/double_quotes,!text/single_quotes,!text/double_angle_quotes,!text/single_angle_quotes",
            /* keyspec_emoji_action_key */ "!icon/emoji_action_key|!code/key_emoji",
            /* keyspec_settings_action_key */ "!icon/settings_key|!code/key_settings",
            /* keyspec_one_by_four */ "1/3",
            /* keyspec_two_by_four */ "2/3",
            /* keyspec_three_by_four */ "3/3",
    };

    /* Locale fa: Farsi */
    private static final String[] TEXTS_fa = {
            "ا‌ب‌پ", "1", "2", "3", "4", "5", "6", "7", "8", "9", "0,\u066b,\u066c", "۱", "۲", "۳", "۴", "۵", "۶", "۷", "۸", "۹",
            "۰", "۳۲۱؟", null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null,//next is 40
            null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null,//next is 60
            null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null,//next is 80
            null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null,//next is 100
            null, null, null, null, null, null, "(", ")", null, null, null, null, null, null, null, null, null, null, null, null,
            "،", null, null, null, null, "\u064b", "!fixedColumnOrder!7, \u0655|\u0655, \u0652|\u0652, \u0651|\u0651, \u064c|\u064c, \u064d|\u064d, \u064b|\u064b, \u0654|\u0654, \u0656|\u0656, \u0670|\u0670, \u0653|\u0653, \u064f|\u064f, \u0650|\u0650, \u064e|\u064e,\u0640\u0640\u0640|\u0640", null, null, null,
            "؟", "؛",
    };

    /* Locale hi_IN: Hindi (India) */
    private static final String[] TEXTS_hi_IN = {
            // Label for "switch to alphabetic" key.
            // U+0915: "क" DEVANAGARI LETTER KA
            // U+0916: "ख" DEVANAGARI LETTER KHA
            // U+0917: "ग" DEVANAGARI LETTER GA
            /* keylabel_to_alpha */ "\u0915\u0916\u0917",
            /* additional_morekeys_symbols_1 */ "1",
            /* additional_morekeys_symbols_2 */ "2",
            /* additional_morekeys_symbols_3 */ "3",
            /* additional_morekeys_symbols_4 */ "4",
            /* additional_morekeys_symbols_5 */ "5",
            /* additional_morekeys_symbols_6 */ "6",
            /* additional_morekeys_symbols_7 */ "7",
            /* additional_morekeys_symbols_8 */ "8",
            /* additional_morekeys_symbols_9 */ "9",
            /* additional_morekeys_symbols_0 */ "0",
            // U+0967: "१" DEVANAGARI DIGIT ONE
            /* keyspec_symbols_1 */ "\u0967",
            // U+0968: "२" DEVANAGARI DIGIT TWO
            /* keyspec_symbols_2 */ "\u0968",
            // U+0969: "३" DEVANAGARI DIGIT THREE
            /* keyspec_symbols_3 */ "\u0969",
            // U+096A: "४" DEVANAGARI DIGIT FOUR
            /* keyspec_symbols_4 */ "\u096A",
            // U+096B: "५" DEVANAGARI DIGIT FIVE
            /* keyspec_symbols_5 */ "\u096B",
            // U+096C: "६" DEVANAGARI DIGIT SIX
            /* keyspec_symbols_6 */ "\u096C",
            // U+096D: "७" DEVANAGARI DIGIT SEVEN
            /* keyspec_symbols_7 */ "\u096D",
            // U+096E: "८" DEVANAGARI DIGIT EIGHT
            /* keyspec_symbols_8 */ "\u096E",
            // U+096F: "९" DEVANAGARI DIGIT NINE
            /* keyspec_symbols_9 */ "\u096F",
            // U+0966: "०" DEVANAGARI DIGIT ZERO
            /* keyspec_symbols_0 */ "\u0966",
            // Label for "switch to symbols" key.
            /* keylabel_to_symbol */ "?\u0967\u0968\u0969",
            /* morekeys_a ~ */
            EMPTY, EMPTY, EMPTY, EMPTY, EMPTY, EMPTY, EMPTY, EMPTY, EMPTY, EMPTY, EMPTY, EMPTY, EMPTY,
            EMPTY, EMPTY, EMPTY, EMPTY, EMPTY, EMPTY, EMPTY, EMPTY, EMPTY,
            /* ~ morekeys_x */
            /* keyspec_q */ "q",
            /* keyspec_w */ "w",
            /* keyspec_y */ "y",
            /* keyspec_x */ "x",
            /* keyspec_nordic_row1_11 ~ */
            EMPTY, EMPTY, EMPTY, EMPTY, EMPTY, EMPTY, EMPTY, EMPTY, EMPTY, EMPTY, EMPTY, EMPTY, EMPTY,
            EMPTY, EMPTY, EMPTY, EMPTY, EMPTY, EMPTY, EMPTY, EMPTY, EMPTY, EMPTY, EMPTY, EMPTY, EMPTY,
            EMPTY, EMPTY, EMPTY, EMPTY,
            /* ~ morekeys_swiss_row2_11 */
            /* single_quotes */ "!text/single_lqm_rqm",
            /* double_quotes */ "!text/double_lqm_rqm",
            /* single_angle_quotes */ "!text/single_laqm_raqm",
            /* double_angle_quotes */ "!text/double_laqm_raqm",
            // $ - I'm making ₹ as default
            // U+00A2: "¢" CENT SIGN
            // U+00A3: "£" POUND SIGN
            // U+20AC: "€" EURO SIGN
            // U+00A5: "¥" YEN SIGN
            // U+20B1: "₱" PESO SIGN
            /* morekeys_currency_dollar */ "$,\u00A2,\u00A3,\u20AC,\u00A5,\u20B1",
            /* keylabel_for_currency */ "$",
            /* morekeys_currency_generic */ "$,\u00A2,\u20AC,\u00A3,\u00A5,\u20B1",
            /* morekeys_punctuation */ "!autoColumnOrder!8,\\,,?,!,#,!text/keyspec_right_parenthesis,!text/keyspec_left_parenthesis,/,;,',@,:,-,\",+,\\%,&",
            /* morekeys_tablet_punctuation */ "!autoColumnOrder!7,\\,,',#,!text/keyspec_right_parenthesis,!text/keyspec_left_parenthesis,/,;,@,:,-,\",+,\\%,&",
            // U+00F1: "ñ" LATIN SMALL LETTER N WITH TILDE
            /* keyspec_spanish_row2_10 */ "\u00F1",
            // U+2020: "†" DAGGER
            // U+2021: "‡" DOUBLE DAGGER
            // U+2605: "★" BLACK STAR
            /* morekeys_star */ "\u2020,\u2021,\u2605",
            // U+266A: "♪" EIGHTH NOTE
            // U+2665: "♥" BLACK HEART SUIT
            // U+2660: "♠" BLACK SPADE SUIT
            // U+2666: "♦" BLACK DIAMOND SUIT
            // U+2663: "♣" BLACK CLUB SUIT
            /* morekeys_bullet */ "\u266A,\u2665,\u2660,\u2666,\u2663",
            // U+00B1: "±" PLUS-MINUS SIGN
            /* morekeys_plus */ "\u00B1",
            /* morekeys_left_parenthesis */ "!fixedColumnOrder!3,!text/keyspecs_left_parenthesis_more_keys",
            /* morekeys_right_parenthesis */ "!fixedColumnOrder!3,!text/keyspecs_right_parenthesis_more_keys",
            /* morekeys_less_than */ "!fixedColumnOrder!3,!text/keyspec_left_single_angle_quote,!text/keyspec_less_than_equal,!text/keyspec_left_double_angle_quote",
            /* morekeys_greater_than */ "!fixedColumnOrder!3,!text/keyspec_right_single_angle_quote,!text/keyspec_greater_than_equal,!text/keyspec_right_double_angle_quote",
            /* morekeys_arabic_diacritics */ EMPTY,
            // U+00B9: "¹" SUPERSCRIPT ONE
            // U+00BD: "½" VULGAR FRACTION ONE HALF
            // U+2153: "⅓" VULGAR FRACTION ONE THIRD
            // U+00BC: "¼" VULGAR FRACTION ONE QUARTER
            // U+215B: "⅛" VULGAR FRACTION ONE EIGHTH
            /* morekeys_symbols_1 */ "\u00B9,\u00BD,\u2153,\u00BC,\u215B",
            // U+00B2: "²" SUPERSCRIPT TWO
            // U+2154: "⅔" VULGAR FRACTION TWO THIRDS
            /* morekeys_symbols_2 */ "\u00B2,\u2154",
            // U+00B3: "³" SUPERSCRIPT THREE
            // U+00BE: "¾" VULGAR FRACTION THREE QUARTERS
            // U+215C: "⅜" VULGAR FRACTION THREE EIGHTHS
            /* morekeys_symbols_3 */ "\u00B3,\u00BE,\u215C",
            // U+2074: "⁴" SUPERSCRIPT FOUR
            /* morekeys_symbols_4 */ "\u2074",
            // U+215D: "⅝" VULGAR FRACTION FIVE EIGHTHS
            /* morekeys_symbols_5 */ "\u215D",
            /* morekeys_symbols_6 */ EMPTY,
            // U+215E: "⅞" VULGAR FRACTION SEVEN EIGHTHS
            /* morekeys_symbols_7 */ "\u215E",
            /* morekeys_symbols_8 */ EMPTY,
            /* morekeys_symbols_9 */ EMPTY,
            // U+207F: "ⁿ" SUPERSCRIPT LATIN SMALL LETTER N
            // U+2205: "∅" EMPTY SET
            /* morekeys_symbols_0 */ "\u207F,\u2205",
            // The all letters need to be mirrored are found at
            // http://www.unicode.org/Public/6.1.0/ucd/BidiMirroring.txt
            // U+2039: "‹" SINGLE LEFT-POINTING ANGLE QUOTATION MARK
            // U+203A: "›" SINGLE RIGHT-POINTING ANGLE QUOTATION MARK
            // U+2264: "≤" LESS-THAN OR EQUAL TO
            // U+2265: "≥" GREATER-THAN EQUAL TO
            // U+00AB: "«" LEFT-POINTING DOUBLE ANGLE QUOTATION MARK
            // U+00BB: "»" RIGHT-POINTING DOUBLE ANGLE QUOTATION MARK
            /* keyspec_left_parenthesis */ "(",
            /* keyspec_right_parenthesis */ ")",
            /* keyspec_left_square_bracket */ "[",
            /* keyspec_right_square_bracket */ "]",
            /* keyspec_left_curly_bracket */ "{",
            /* keyspec_right_curly_bracket */ "}",
            /* keyspec_less_than */ "<",
            /* keyspec_greater_than */ ">",
            /* keyspec_less_than_equal */ "\u2264",
            /* keyspec_greater_than_equal */ "\u2265",
            /* keyspec_left_double_angle_quote */ "\u00AB",
            /* keyspec_right_double_angle_quote */ "\u00BB",
            /* keyspec_left_single_angle_quote */ "\u2039",
            /* keyspec_right_single_angle_quote */ "\u203A",
            // Comma key
            /* keyspec_comma */ ",",
            /* keyspec_tablet_comma */ ",",
            /* keyhintlabel_tablet_comma */ EMPTY,
            /* morekeys_tablet_comma */ EMPTY,
            // Period key
            /* keyspec_period */ "।",
            /* keyhintlabel_period */ EMPTY,
            /* morekeys_period */ "!text/morekeys_punctuation",
            /* keyspec_tablet_period */ "।",
            /* keyhintlabel_tablet_period */ EMPTY,
            /* morekeys_tablet_period */ "!text/morekeys_tablet_punctuation",
    };

    /* Locale en: English */
    private static final String[] TEXTS_en = {
            /* keylabel_to_alpha ~ */
            null, null, null, null, null, null, null, null, null, null, null, null, null, null, null,
            null, null, null, null, null, null, null,
            /* ~ keylabel_to_symbol */
            // U+00E0: "à" LATIN SMALL LETTER A WITH GRAVE
            // U+00E1: "á" LATIN SMALL LETTER A WITH ACUTE
            // U+00E2: "â" LATIN SMALL LETTER A WITH CIRCUMFLEX
            // U+00E4: "ä" LATIN SMALL LETTER A WITH DIAERESIS
            // U+00E6: "æ" LATIN SMALL LETTER AE
            // U+00E3: "ã" LATIN SMALL LETTER A WITH TILDE
            // U+00E5: "å" LATIN SMALL LETTER A WITH RING ABOVE
            // U+0101: "ā" LATIN SMALL LETTER A WITH MACRON
            /* morekeys_a */ "\u00E0,\u00E1,\u00E2,\u00E4,\u00E6,\u00E3,\u00E5,\u0101",
            // U+00E9: "é" LATIN SMALL LETTER E WITH ACUTE
            // U+00E8: "è" LATIN SMALL LETTER E WITH GRAVE
            // U+00EA: "ê" LATIN SMALL LETTER E WITH CIRCUMFLEX
            // U+00EB: "ë" LATIN SMALL LETTER E WITH DIAERESIS
            // U+0113: "ē" LATIN SMALL LETTER E WITH MACRON
            /* morekeys_e */ "\u00E9,\u00E8,\u00EA,\u00EB,\u0113",
            // U+00ED: "í" LATIN SMALL LETTER I WITH ACUTE
            // U+00EE: "î" LATIN SMALL LETTER I WITH CIRCUMFLEX
            // U+00EF: "ï" LATIN SMALL LETTER I WITH DIAERESIS
            // U+012B: "ī" LATIN SMALL LETTER I WITH MACRON
            // U+00EC: "ì" LATIN SMALL LETTER I WITH GRAVE
            /* morekeys_i */ "\u00ED,\u00EE,\u00EF,\u012B,\u00EC",
            // U+00F3: "ó" LATIN SMALL LETTER O WITH ACUTE
            // U+00F4: "ô" LATIN SMALL LETTER O WITH CIRCUMFLEX
            // U+00F6: "ö" LATIN SMALL LETTER O WITH DIAERESIS
            // U+00F2: "ò" LATIN SMALL LETTER O WITH GRAVE
            // U+0153: "œ" LATIN SMALL LIGATURE OE
            // U+00F8: "ø" LATIN SMALL LETTER O WITH STROKE
            // U+014D: "ō" LATIN SMALL LETTER O WITH MACRON
            // U+00F5: "õ" LATIN SMALL LETTER O WITH TILDE
            /* morekeys_o */ "\u00F3,\u00F4,\u00F6,\u00F2,\u0153,\u00F8,\u014D,\u00F5",
            // U+00FA: "ú" LATIN SMALL LETTER U WITH ACUTE
            // U+00FB: "û" LATIN SMALL LETTER U WITH CIRCUMFLEX
            // U+00FC: "ü" LATIN SMALL LETTER U WITH DIAERESIS
            // U+00F9: "ù" LATIN SMALL LETTER U WITH GRAVE
            // U+016B: "ū" LATIN SMALL LETTER U WITH MACRON
            /* morekeys_u */ "\u00FA,\u00FB,\u00FC,\u00F9,\u016B",
            // U+00DF: "ß" LATIN SMALL LETTER SHARP S
            /* morekeys_s */ "\u00DF",
            // U+00F1: "ñ" LATIN SMALL LETTER N WITH TILDE
            /* morekeys_n */ "\u00F1",
            // U+00E7: "ç" LATIN SMALL LETTER C WITH CEDILLA
            /* morekeys_c */ "\u00E7",
    };

    private static final Object[] LOCALES_AND_TEXTS = {
            // "locale", TEXT_ARRAY,  /* numberOfNonNullText/lengthOf_TEXT_ARRAY localeName */
            "DEFAULT", TEXTS_DEFAULT, /* 168/168 DEFAULT */
            "fa", TEXTS_fa, /*  Farsi */
            "en", TEXTS_en,    /*   8/ 30 English */
            "hi", TEXTS_hi_IN,    /*   8/ 30 English */
    };

    static {
        for (int index = 0; index < NAMES.length; index++) {
            sNameToIndexesMap.put(NAMES[index], index);
        }

        for (int i = 0; i < LOCALES_AND_TEXTS.length; i += 2) {
            final String locale = (String) LOCALES_AND_TEXTS[i];
            final String[] textsTable = (String[]) LOCALES_AND_TEXTS[i + 1];
            sLocaleToTextsTableMap.put(locale, textsTable);
            sTextsTableToLocaleMap.put(textsTable, locale);
        }
    }
}