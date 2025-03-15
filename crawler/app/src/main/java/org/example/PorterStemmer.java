package org.example;

public class PorterStemmer {

    private static StringBuilder word = new StringBuilder();

    public static String stem(String s) {
        word.setLength(0);
        word.append(s);
        if (word.length() < 3)
            return s;

        step1();
        step2();
        step3();
        step4();
        step5();

        return word.toString();
    }

    private static void step1() {
        if (word.toString().endsWith("sses")) {
            word.setLength(word.length() - 2);
        } else if (word.toString().endsWith("ies")) {
            word.setLength(word.length() - 2);
        } else if (word.toString().endsWith("s") && !word.toString().endsWith("ss")) {
            word.setLength(word.length() - 1);
        }
    }

    private static void step2() {
        if (word.toString().endsWith("ed") && containsVowel(word.toString())) {
            word.setLength(word.length() - 2);
        } else if (word.toString().endsWith("ing") && containsVowel(word.toString())) {
            word.setLength(word.length() - 3);
        }
    }

    private static void step3() {
        if (word.toString().endsWith("ational")) {
            replace("ational", "ate");
        } else if (word.toString().endsWith("izer")) {
            replace("izer", "ize");
        }
    }

    private static void step4() {
        if (word.toString().endsWith("al")) {
            word.setLength(word.length() - 2);
        } else if (word.toString().endsWith("ful")) {
            word.setLength(word.length() - 3);
        }
    }

    private static void step5() {
        if (word.toString().endsWith("e") && word.length() > 1) {
            word.setLength(word.length() - 1);
        }
    }

    private static boolean containsVowel(String s) {
        return s.matches(".*[aeiou].*");
    }

    private static void replace(String suffix, String replacement) {
        if (word.toString().endsWith(suffix)) {
            word.replace(word.length() - suffix.length(), word.length(), replacement);
        }
    }
}
