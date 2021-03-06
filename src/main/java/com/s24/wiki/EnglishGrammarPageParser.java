package com.s24.wiki;

import java.util.HashSet;
import java.util.Locale;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang3.StringUtils;

import com.beust.jcommander.internal.Lists;
import com.google.common.base.CharMatcher;
import com.google.common.base.Splitter;

import edu.jhu.nlp.wikipedia.WikiPage;

public class EnglishGrammarPageParser extends PageParser {

   private final static Pattern pattern = Pattern.compile("\\{\\{en-noun\\|(.*)\\}\\}");
   private final static CharMatcher regularStems = CharMatcher.anyOf("|~-?!");
   private final static CharMatcher illegalCharacters = CharMatcher.anyOf("'%&/.´`*$@{[]");
   private final boolean includeRegularStems;

   public EnglishGrammarPageParser(PageParserCallback cb, boolean includeRegularStems) {
      super(cb);
      this.includeRegularStems = includeRegularStems;
   }

   @Override
   public void parse(WikiPage page) {
      Matcher matcher = pattern.matcher(page.getWikiText());

      Set<String> left = new HashSet<>();
      Set<String> right = new HashSet<>();
      String word = page.getTitle().trim().toLowerCase(Locale.US);
      left.add(word);
      right.add(word);

      // legal words ony
      if (!illegalCharacters.matchesAnyOf(word)
            && !CharMatcher.DIGIT.matchesAnyOf(word)) {

         // explcit plural given
         if (matcher.find()) {
            String stem = matcher.group(1).toLowerCase(Locale.US);

            // no illegal stems (identical, nostem)
            for (String s : Splitter.on('|').trimResults().split(stem)) {
               if (!regularStems.matchesAllOf(s)
                     && !illegalCharacters.matchesAnyOf(s)
                     && !CharMatcher.DIGIT.matchesAnyOf(s)) {
                  if ("s".equalsIgnoreCase(s) || "es".equalsIgnoreCase(s)) {
                     if (includeRegularStems) {
                        left.add(word + s);
                     }
                  } else if (s.length() > 2) {
                     left.add(s);
                  }
               } else if (includeRegularStems && "-".equals(s)) {
                  left.add(word + "s");
               } else if (includeRegularStems && "~".equals(s)) {
                  left.add(word + "s");
               }
            }
         } else if (includeRegularStems) {
            left.add(word + "s");
         }
      }

//      System.out.println("Found: " + StringUtils.join(left, ",") + " => " + right.iterator().next());
      callback.callback(Lists.newArrayList(left), Lists.newArrayList(right));
   }

   @Override
   protected String getName() {
      return "Übersicht";
   }

}
