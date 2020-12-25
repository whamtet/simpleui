(defproject clj-htmx "0.1.0-SNAPSHOT"
  :description "Backend helpers for htmx"
  :url "https://github.com/whamtet/clj-htmx"
  :license {:name "EPL-2.0 OR GPL-2.0-or-later WITH Classpath-exception-2.0"
            :url "https://www.eclipse.org/legal/epl-2.0/"}
  :dependencies [[org.clojure/clojure "1.10.0"]
                 [hiccup "1.0.5"]]
  :repl-options {:init-ns clj-htmx.core}
  :repositories [["snapshots" {:url "https://repo.clojars.org" :creds :gpg}
                  "releases" {:url "https://repo.clojars.org" :creds :gpg}]])
