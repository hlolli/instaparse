(defproject instaparse-lumo "1.4.7"
  :description "Instaparse: No grammar left behind"
  :url "https://github.com/Engelberg/instaparse"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}
  :source-paths ["src/" "target/generated/src/clj"]
  :plugins [[lein-cljsbuild "1.1.5"]
            [cljsee "0.1.0"]]
  :target-path "target"
  :scm {:name "git"
        :url "https://github.com/Engelberg/instaparse"})
