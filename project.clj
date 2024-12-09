(defproject io.simpleui/simpleui "1.6.1"
  :description "Backend helpers for htmx"
  :url "https://github.com/whamtet/simpleui"
  :license {:name "EPL-2.0 OR GPL-2.0-or-later WITH Classpath-exception-2.0"
            :url "https://www.eclipse.org/legal/epl-2.0/"}
  :dependencies [[hiccup "2.0.0-alpha2"]
                 [org.clojure/clojure "1.12.0"]
                 [org.clojure/data.json "2.4.0"]
                 [metosin/reitit "0.5.11"]]
  :repositories [["clojars" {:url "https://clojars.org/repo/"
                             :sign-releases false}]]
  :resource-paths ["src/resources"]
  :plugins [[lein-auto "0.1.3"]]
  :repl-options {:init-ns simpleui.core}
  :profiles {:test {:dependencies [[ring/ring-mock "0.4.0"]]}
             :test-repl [:test :leiningen/default]})
