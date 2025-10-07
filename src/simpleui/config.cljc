(ns simpleui.config)

(def default-param-method :simple)
(defn set-param-method! [method]
  {:pre [(contains? #{:simple :path} method)]}
  #?(:clj (alter-var-root #'default-param-method (constantly method))
     :cljs (set! default-param-method method)))

(def render-style? true)
(def render-kv? true) ;; permanent
(def render-hs? true)
(def render-class? true)
(def render-vals? true)
(def render-signals? true) ;; permanent
(def render-headers? true)
(def render-commands? true)
(def render-si-set? true) ;; currently permanent
(def render-hx-request? true) ;; currently permanent
(def render-oob? true)
(def render-safe? true)

(defn set-render-style!
  "Renders style maps as inline css (default true)."
  [s]
  #?(:clj (alter-var-root #'render-style? (constantly s))
     :cljs (set! render-style? s)))
(defn set-render-hs!
  "Renders hyperscript vectors as a string see https://hyperscript.org (default true)."
  [s]
  #?(:clj (alter-var-root #'render-hs? (constantly s))
     :cljs (set! render-hs? s)))
(defn set-render-class!
  "Renders class vectors as a string (default true)."
  [s]
  #?(:clj (alter-var-root #'render-class? (constantly s))
     :cljs (set! render-class? s)))
(defn set-render-vals!
  "Renders hx-vals maps into json (default true)."
  [s]
  #?(:clj (alter-var-root #'render-vals? (constantly s))
     :cljs (set! render-vals? s)))
(defn set-render-headers!
  "Renders hx-headers maps into json (default true)."
  [s]
  #?(:clj (alter-var-root #'render-headers? (constantly s))
     :cljs (set! render-headers? s)))
(defn set-render-commands!
  "Renders hx-post, hx-get of the form \"endpoint:command\" (default true)."
  [s]
  #?(:clj (alter-var-root #'render-commands? (constantly s))
     :cljs (set! render-commands? s)))
(defn set-render-oob
  "Adds hx-oob-swap=\"true\" to all but the last element of a top level list (default true)."
  [s]
  #?(:clj (alter-var-root #'render-oob? (constantly s))
     :cljs (set! render-oob? s)))
(defn set-render-safe
  "Set false if you wish to include plain html strings inside your hiccup (default true)."
  [s]
  #?(:clj (alter-var-root #'render-safe? (constantly s))
     :cljs (set! render-safe? s)))
