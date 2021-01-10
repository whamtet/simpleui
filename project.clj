(defproject ctmx "0.1.0-SNAPSHOT"
  :description "Backend helpers for htmx"
  :url "https://github.com/whamtet/ctmx"
  :license {:name "EPL-2.0 OR GPL-2.0-or-later WITH Classpath-exception-2.0"
            :url "https://www.eclipse.org/legal/epl-2.0/"}
  :dependencies [[org.clojure/clojure "1.10.0"]
                 [hiccup "1.0.5"]
                 [org.clojure/clojurescript "1.10.773"]
                 [hiccups "0.4.0-SNAPSHOT"]
                 ;; TODO reitit-ring
                 [metosin/reitit "0.5.11"]]
  :repl-options {:init-ns ctmx.core}
  :repositories [["snapshots" {:url "https://repo.clojars.org" :creds :gpg}
                  "releases" {:url "https://repo.clojars.org" :creds :gpg}]])
