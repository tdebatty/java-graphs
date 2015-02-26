/*
 * The MIT License
 *
 * Copyright 2015 Thibault Debatty.
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */

package info.debatty.java.graphs.build;

import info.debatty.java.graphs.Node;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

/**
 * Bag-Of-Word Strings Graph Builder.
 * This partitioning graph builder relies on the Bag-Of-Word model:
 * In the first phase it builds a dictionary of all keywords available in the
 * dataset, using tokenization and stemming.
 * In the second phase, each input string is represented as a set of keywords
 * 
 * @author Thibaut Debatty
 */
public class StringBOW extends PartitioningGraphBuilder<String>{

    @Override
    protected List<Node<String>>[][] _partition(List<Node<String>> nodes) {
        
        // Build dictionary of keywords
        HashMap<String, Integer> full_dictionary = new HashMap<String, Integer>();
            
        for (Node node: nodes) {
            for (String token : tokenize((String) node.value)) {    
                //System.out.println(keyword);
                
                if (!full_dictionary.containsKey(token)) {
                    full_dictionary.put(token, 1);
                } else {
                    full_dictionary.put(token, full_dictionary.get(token) + 1);
                }
            }
        }
        
        System.out.println("Found  " + full_dictionary.size() + " keywords");
        
        // Remove keywords that appear only once
        // and keywords that appear to often!
        ArrayList<String> dictionary = new ArrayList<String>();
        for (Map.Entry<String, Integer> e : full_dictionary.entrySet()) {
            
            // Eliminate keywords that appear in only a few strings
            if (e.getValue() <= 2) {
                continue;
            }
            
            // Eliminate stop-words
            if (e.getValue() > (full_dictionary.size() / 10) ) {
                continue;
            }
            
            dictionary.add(e.getKey());
        }
        
        //Collections.shuffle(dictionary);
        System.out.println("After filtering, kept " + dictionary.size() + " keywords");
        //System.out.println(dictionary.toString());
        
        ArrayList<Node<String>>[][] partitions = new ArrayList[n_stages][n_partitions];
        
        // Choose seeds for hashing
        Random r = new Random();
        int[] seeds = new int[n_stages];
        for (int i = 0; i < n_stages; i++) {
            seeds[i] = r.nextInt();
        }
        
        for (Node node : nodes) {
            ArrayList<String> tokens = tokenize((String) node.value);
            
            // Compute the boolean vector representation
            boolean[] string_as_boolean = new boolean[dictionary.size()];
            for (int j = 0; j < dictionary.size(); j++) {
                string_as_boolean[j] = tokens.contains(dictionary.get(j));
            }
            
            // hash
            int[] string_partitions = new int[n_stages];
            for (int s = 0; s < n_stages; s++) {
                // hash a piece of the boolean array    
                int hash = seeds[s] + java.util.Arrays.hashCode(string_as_boolean);
                if (hash < 0) {
                    hash += Integer.MAX_VALUE;
                }
                
                string_partitions[s] = hash % n_partitions;
            }
            
            for (int i = 0; i < n_stages; i++) {
                int bucket = string_partitions[i];
                if (partitions[i][bucket] == null) {
                    partitions[i][bucket] = new ArrayList<Node<String>>();
                }
                
                partitions[i][bucket].add(node);
            }
        }
        
        return partitions;
    }
    
    public static ArrayList<String> tokenize(String s) {
        ArrayList<Character> punct = new ArrayList<Character>();
        punct.add('-');
        punct.add('.');
        punct.add(',');
        punct.add('!');
        punct.add('?');
        punct.add('@');
        punct.add('"');
        punct.add('\\');
        punct.add('~');
        
        Porter stemmer = new Porter();
        
        ArrayList<String> r = new ArrayList<String>();

        // Remove punctuation
        for (char p : punct) {
            s = s.replace(p, ' ');
        }
        s = s.trim();

        for (String token : s.split(" ")) {

            String keyword = token.toLowerCase().trim();
            if (keyword.equals("")) {
                continue;
            }
            
            keyword = stemmer.stripAffixes(keyword);
            

            if (keyword.equals("")) {
                continue;
            }
            
            r.add(keyword);
        }
        
        return r;
    }
    
}


class NewString {
  public String str;

  NewString() {
     str = "";
  }
}

class Porter {

  private String Clean( String str ) {
     int last = str.length();
     
     Character ch = new Character( str.charAt(0) );
     String temp = "";

     for ( int i=0; i < last; i++ ) {
         if ( ch.isLetterOrDigit( str.charAt(i) ) )
            temp += str.charAt(i);
     }
   
     return temp;
  } //clean
 
  private boolean hasSuffix( String word, String suffix, NewString stem ) {

     String tmp = "";

     if ( word.length() <= suffix.length() )
        return false;
     if (suffix.length() > 1) 
        if ( word.charAt( word.length()-2 ) != suffix.charAt( suffix.length()-2 ) )
           return false;
  
     stem.str = "";

     for ( int i=0; i<word.length()-suffix.length(); i++ )
         stem.str += word.charAt( i );
     tmp = stem.str;

     for ( int i=0; i<suffix.length(); i++ )
         tmp += suffix.charAt( i );

     if ( tmp.compareTo( word ) == 0 )
        return true;
     else
        return false;
  }

  private boolean vowel( char ch, char prev ) {
     switch ( ch ) {
        case 'a': case 'e': case 'i': case 'o': case 'u': 
          return true;
        case 'y': {

          switch ( prev ) {
            case 'a': case 'e': case 'i': case 'o': case 'u': 
              return false;

            default: 
              return true;
          }
        }
        
        default : 
          return false;
     }
  }

  private int measure( String stem ) {
    
    int i=0, count = 0;
    int length = stem.length();

    while ( i < length ) {
       for ( ; i < length ; i++ ) {
           if ( i > 0 ) {
              if ( vowel(stem.charAt(i),stem.charAt(i-1)) )
                 break;
           }
           else {  
              if ( vowel(stem.charAt(i),'a') )
                break; 
           }
       }

       for ( i++ ; i < length ; i++ ) {
           if ( i > 0 ) {
              if ( !vowel(stem.charAt(i),stem.charAt(i-1)) )
                  break;
              }
           else {  
              if ( !vowel(stem.charAt(i),'?') )
                 break;
           }
       } 
      if ( i < length ) {
         count++;
         i++;
      }
    } //while
    
    return(count);
  }

  private boolean containsVowel( String word ) {

     for (int i=0 ; i < word.length(); i++ )
         if ( i > 0 ) {
            if ( vowel(word.charAt(i),word.charAt(i-1)) )
               return true;
         }
         else {  
            if ( vowel(word.charAt(0),'a') )
               return true;
         }
        
     return false;
  }

  private boolean cvc( String str ) {
     int length=str.length();

     if ( length < 3 )
        return false;
    
     if ( (!vowel(str.charAt(length-1),str.charAt(length-2)) )
        && (str.charAt(length-1) != 'w') && (str.charAt(length-1) != 'x') && (str.charAt(length-1) != 'y')
        && (vowel(str.charAt(length-2),str.charAt(length-3))) ) {

        if (length == 3) {
           if (!vowel(str.charAt(0),'?')) 
              return true;
           else
              return false;
        }
        else {
           if (!vowel(str.charAt(length-3),str.charAt(length-4)) ) 
              return true; 
           else
              return false;
        } 
     }   
  
     return false;
  }

  private String step1( String str ) {
 
     NewString stem = new NewString();

     if ( str.charAt( str.length()-1 ) == 's' ) {
        if ( (hasSuffix( str, "sses", stem )) || (hasSuffix( str, "ies", stem)) ){
           String tmp = "";
           for (int i=0; i<str.length()-2; i++)
               tmp += str.charAt(i);
           str = tmp;
        }
        else {
           if ( ( str.length() == 1 ) && ( str.charAt(str.length()-1) == 's' ) ) {
              str = "";
              return str;
           }
           if ( str.charAt( str.length()-2 ) != 's' ) {
              String tmp = "";
              for (int i=0; i<str.length()-1; i++)
                  tmp += str.charAt(i);
              str = tmp;
           }
        }  
     }

     if ( hasSuffix( str,"eed",stem ) ) {
           if ( measure( stem.str ) > 0 ) {
              String tmp = "";
              for (int i=0; i<str.length()-1; i++)
                  tmp += str.charAt( i );
              str = tmp;
           }
     }
     else {  
        if (  (hasSuffix( str,"ed",stem )) || (hasSuffix( str,"ing",stem )) ) { 
           if (containsVowel( stem.str ))  {

              String tmp = "";
              for ( int i = 0; i < stem.str.length(); i++)
                  tmp += str.charAt( i );
              str = tmp;
              if ( str.length() == 1 )
                 return str;

              if ( ( hasSuffix( str,"at",stem) ) || ( hasSuffix( str,"bl",stem ) ) || ( hasSuffix( str,"iz",stem) ) ) {
                 str += "e";
           
              }
              else {   
                 int length = str.length(); 
                 if ( (str.charAt(length-1) == str.charAt(length-2)) 
                    && (str.charAt(length-1) != 'l') && (str.charAt(length-1) != 's') && (str.charAt(length-1) != 'z') ) {
                     
                    tmp = "";
                    for (int i=0; i<str.length()-1; i++)
                        tmp += str.charAt(i);
                    str = tmp;
                 }
                 else
                    if ( measure( str ) == 1 ) {
                       if ( cvc(str) ) 
                          str += "e";
                    }
              }
           }
        }
     }

     if ( hasSuffix(str,"y",stem) ) 
        if ( containsVowel( stem.str ) ) {
           String tmp = "";
           for (int i=0; i<str.length()-1; i++ )
               tmp += str.charAt(i);
           str = tmp + "i";
        }
     return str;  
  }

  private String step2( String str ) {

     String[][] suffixes = { { "ational", "ate" },
                                    { "tional",  "tion" },
                                    { "enci",    "ence" },
                                    { "anci",    "ance" },
                                    { "izer",    "ize" },
                                    { "iser",    "ize" },
                                    { "abli",    "able" },
                                    { "alli",    "al" },
                                    { "entli",   "ent" },
                                    { "eli",     "e" },
                                    { "ousli",   "ous" },
                                    { "ization", "ize" },
                                    { "isation", "ize" },
                                    { "ation",   "ate" },
                                    { "ator",    "ate" },
                                    { "alism",   "al" },
                                    { "iveness", "ive" },
                                    { "fulness", "ful" },
                                    { "ousness", "ous" },
                                    { "aliti",   "al" },
                                    { "iviti",   "ive" },
                                    { "biliti",  "ble" }};
     NewString stem = new NewString();

     
     for ( int index = 0 ; index < suffixes.length; index++ ) {
         if ( hasSuffix ( str, suffixes[index][0], stem ) ) {
            if ( measure ( stem.str ) > 0 ) {
               str = stem.str + suffixes[index][1];
               return str;
            }
         }
     }

     return str;
  }

  private String step3( String str ) {

        String[][] suffixes = { { "icate", "ic" },
                                       { "ative", "" },
                                       { "alize", "al" },
                                       { "alise", "al" },
                                       { "iciti", "ic" },
                                       { "ical",  "ic" },
                                       { "ful",   "" },
                                       { "ness",  "" }};
        NewString stem = new NewString();

        for ( int index = 0 ; index<suffixes.length; index++ ) {
            if ( hasSuffix ( str, suffixes[index][0], stem ))
               if ( measure ( stem.str ) > 0 ) {
                  str = stem.str + suffixes[index][1];
                  return str;
               }
        }
        return str;
  }

  private String step4( String str ) {
        
     String[] suffixes = { "al", "ance", "ence", "er", "ic", "able", "ible", "ant", "ement", "ment", "ent", "sion", "tion",
                           "ou", "ism", "ate", "iti", "ous", "ive", "ize", "ise"};
     
     NewString stem = new NewString();
        
     for ( int index = 0 ; index<suffixes.length; index++ ) {
         if ( hasSuffix ( str, suffixes[index], stem ) ) {
           
            if ( measure ( stem.str ) > 1 ) {
               str = stem.str;
               return str;
            }
         }
     }
     return str;
  }

  private String step5( String str ) {

     if ( str.charAt(str.length()-1) == 'e' ) { 
        if ( measure(str) > 1 ) {/* measure(str)==measure(stem) if ends in vowel */
           String tmp = "";
           for ( int i=0; i<str.length()-1; i++ ) 
               tmp += str.charAt( i );
           str = tmp;
        }
        else
           if ( measure(str) == 1 ) {
              String stem = "";
              for ( int i=0; i<str.length()-1; i++ ) 
                  stem += str.charAt( i );

              if ( !cvc(stem) )
                 str = stem;
           }
     }
     
     if ( str.length() == 1 )
        return str;
     if ( (str.charAt(str.length()-1) == 'l') && (str.charAt(str.length()-2) == 'l') && (measure(str) > 1) )
        if ( measure(str) > 1 ) {/* measure(str)==measure(stem) if ends in vowel */
           String tmp = "";
           for ( int i=0; i<str.length()-1; i++ ) 
               tmp += str.charAt( i );
           str = tmp;
        } 
     return str;
  }

  private String stripPrefixes ( String str) {

     String[] prefixes = { "kilo", "micro", "milli", "intra", "ultra", "mega", "nano", "pico", "pseudo"};

     int last = prefixes.length;
     for ( int i=0 ; i<last; i++ ) {
         if ( str.startsWith( prefixes[i] ) ) {
            String temp = "";
            for ( int j=0 ; j< str.length()-prefixes[i].length(); j++ )
                temp += str.charAt( j+prefixes[i].length() );
            return temp;
         }
     }
     
     return str;
  }


  private String stripSuffixes( String str ) {

     str = step1( str );
     if ( str.length() >= 1 )
        str = step2( str );
     if ( str.length() >= 1 )
        str = step3( str );
     if ( str.length() >= 1 )
        str = step4( str );
     if ( str.length() >= 1 )
        str = step5( str );
 
     return str; 
  }


  public String stripAffixes( String str ) {

    str = str.toLowerCase();
    str = Clean(str);
  
    if (( str != "" ) && (str.length() > 2)) {
       str = stripPrefixes(str);

       if (str != "" ) 
          str = stripSuffixes(str);

    }   

    return str;
    } //stripAffixes

}