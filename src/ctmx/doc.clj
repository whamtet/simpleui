(ns ctmx.doc
  (:require
    [clojure.string :as string]))

;; generate README.md from gh-pages branch

(defn extract [lines start? end?]
  (loop [[line & todo] lines
         record? true
         done []]
    (cond
      (not line) done
      (start? line) (recur todo false done)
      (end? line) (recur todo true done)
      record? (recur todo record? (conj done line))
      :else (recur todo record? done))))

(defn -main [& args]
  (-> "ctmx-doc/index.md"
      slurp
      (.split "\n")
      (extract #(.startsWith % "```clojure") #(.startsWith % "```"))
      (->>
       (remove #(.startsWith % "{% include"))
       (string/join "\n")
       (spit "README.test.md"))))
