(defproject ctmx "1.4.6"
  :description "Backend helpers for htmx"
  :url "https://github.com/whamtet/ctmx"
  :license {:name "EPL-2.0 OR GPL-2.0-or-later WITH Classpath-exception-2.0"
            :url "https://www.eclipse.org/legal/epl-2.0/"}
  :dependencies [[org.clojure/clojure "1.10.0"]
                 [hiccup "2.0.0-alpha2"]
                 [org.clojure/clojurescript "1.10.773"]
                 [macchiato/hiccups "0.4.1"]
                 ;; TODO reitit-ring
                 [metosin/reitit "0.5.11"]]
  :repositories [["clojars" {:url "https://clojars.org/repo/"
                             :sign-releases false}]]
  :plugins [[lein-auto "0.1.3"]]
  :repl-options {:init-ns ctmx.core})
