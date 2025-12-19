package com.ortecfinance.tasklist.Utils;

import java.util.ArrayList;
import java.util.List;

public class Util {

  public static String normalize(String s) {
    return s == null ? "" : s.trim().replaceAll("\\s+", " ");
  }

  public static String[] split2(String s) {
    String n = normalize(s);
    if (n.isEmpty()) {
      return new String[]{""};
    }
    return n.split(" ", 2);
  }

  public static String stripOuterQuotes(String s) {
    if (s == null) {
      return null;
    }
    s = s.trim();
    if (s.length() >= 2) {
      char first = s.charAt(0);
      char last = s.charAt(s.length() - 1);
      if ((first == '"' && last == '"') || (first == '\'' && last == '\'')) {
        return s.substring(1, s.length() - 1);
      }
    }
    return s;
  }

  public static Integer parseIntOrNull(String s) {
    try {
      return Integer.parseInt(s);
    } catch (Exception e) {
      return null;
    }
  }

  public static List<String> splitArgsRespectingQuotes(String input) {
    List<String> result = new ArrayList<>();
    if (input == null || input.isBlank()) {
      return result;
    }

    boolean inQuotes = false;
    char quoteChar = 0;
    StringBuilder current = new StringBuilder();

    for (int i = 0; i < input.length(); i++) {
      char c = input.charAt(i);

      if (c == '"' || c == '\'') {
        if (inQuotes && c == quoteChar) {
          inQuotes = false;
        } else if (!inQuotes) {
          inQuotes = true;
          quoteChar = c;
        } else {
          current.append(c);
        }
      } else if (c == ' ' && !inQuotes) {
        if (current.length() > 0) {
          result.add(current.toString());
          current.setLength(0);
        }
      } else {
        current.append(c);
      }
    }

    if (current.length() > 0) {
      result.add(current.toString());
    }

    return result;
  }

}
