(ns instaparse.abnf-macros
  (:require [clojure.walk :as walk]
            [instaparse.cfg :as cfg]
            [instaparse.reduction :as red]
            [instaparse.combinators-source :refer [regexp]]))

(def abnf-grammar-common
  "
  <rulelist> = <opt-whitespace> (rule | hide-tag-rule)+;
  rule = rulename-left <defined-as> alternation <opt-whitespace>;
  hide-tag-rule = hide-tag <defined-as> alternation <opt-whitespace>;
  rulename-left = rulename;
  rulename-right = rulename;
  <hide-tag> = <'<' opt-whitespace> rulename-left <opt-whitespace '>'>;
  defined-as = <opt-whitespace> ('=' | '=') <opt-whitespace>;
  alternation = concatenation (<opt-whitespace '/' opt-whitespace> concatenation)*;
  concatenation = repetition (<whitespace> repetition)*;
  repetition = [repeat] <opt-whitespace> element;
  repeat = NUM | (NUM? '*' NUM?);
  <element> = rulename-right | group | hide | option | char-val | num-val
          | look | neg | regexp;
  look = <'&' opt-whitespace> element;
  neg = <'!' opt-whitespace> element;
  <group> = <'(' opt-whitespace> alternation <opt-whitespace ')'>;
  option = <'[' opt-whitespace> alternation <opt-whitespace ']'>;
  hide = <'<' opt-whitespace> alternation <opt-whitespace '>'>;
  char-val = <'\\u0022'> #'[\\u0020-\\u0021\\u0023-\\u007E]'* <'\\u0022'> (* double-quoted strings *)
         | <'\\u0027'> #'[\\u0020-\\u0026\u0028-\u007E]'* <'\\u0027'>;  (* single-quoted strings *)
  <num-val> = <'%'> (bin-val | dec-val | hex-val);
  bin-val = <'b'> bin-char
          [ (<'.'> bin-char)+ | ('-' bin-char) ];
  bin-char = ('0' | '1')+;
  dec-val = <'d'> dec-char
          [ (<'.'> dec-char)+ | ('-' dec-char) ];
  dec-char = DIGIT+;
  hex-val = <'x'> hex-char
          [ (<'.'> hex-char)+ | ('-' hex-char) ];
  hex-char = HEXDIG+;
  NUM = DIGIT+;
  <DIGIT> = #'[0-9]';
  <HEXDIG> = #'[0-9a-fA-F]';


  (* extra entrypoint to be used by the abnf combinator *)
  <rules-or-parser> = rulelist | alternation;
  ")

(def abnf-grammar-clj-only
  "
  <rulename> = #'[a-zA-Z][-a-zA-Z0-9]*(?x) #identifier';
  opt-whitespace = #'\\s*(?:;.*?(?:\\u000D?\\u000A\\s*|$))*(?x) # optional whitespace or comments';
  whitespace = #'\\s+(?:;.*?\\u000D?\\u000A\\s*)*(?x) # whitespace or comments';
  regexp = #\"#'[^'\\\\]*(?:\\\\.[^'\\\\]*)*'(?x) #Single-quoted regexp\"
       | #\"#\\\"[^\\\"\\\\]*(?:\\\\.[^\\\"\\\\]*)*\\\"(?x) #Double-quoted regexp\"
  ")

(def abnf-grammar-cljs-only
  "
  <rulename> = #'[a-zA-Z][-a-zA-Z0-9]*';
  opt-whitespace = #'\\s*(?:;.*?(?:\\u000D?\\u000A\\s*|$))*';
  whitespace = #'\\s+(?:;.*?\\u000D?\\u000A\\s*)*';
  regexp = #\"#'[^'\\\\]*(?:\\\\.[^'\\\\]*)*'\"
       | #\"#\\\"[^\\\"\\\\]*(?:\\\\.[^\\\"\\\\]*)*\\\"\"
  ")


(defmacro precompile-cljs-grammar
  []
  (let [combinators (red/apply-standard-reductions 
                     :hiccup (cfg/ebnf (str abnf-grammar-common
                                            abnf-grammar-cljs-only)))]
    (walk/postwalk
     (fn [form]
       (cond
         ;; Lists cannot be evaluated verbatim
         (seq? form)
         (list* 'list form)

         ;; Regexp terminals are handled differently in cljs
         (= :regexp (:tag form))
         `(merge (regexp ~(str (:regexp form)))
                 ~(dissoc form :tag :regexp))

         :else form))
     combinators)))
