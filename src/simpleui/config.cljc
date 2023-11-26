(ns simpleui.config)

(def default-param-method :simple)
(defn set-param-method! [method]
  {:pre [(contains? #{:simple :path} method)]}
  #?(:clj (alter-var-root #'default-param-method (constantly method))
     :cljs (set! default-param-method method)))

(def render-style? true)
(def render-hs? true)
(def render-class? true)
(def render-vals? true)
(def render-commands? true)
(def render-oob? false)
(def render-safe? true)

(defn set-render-style! [s]
  #?(:clj (alter-var-root #'render-style? (constantly s))
     :cljs (set! render-style? s)))
(defn set-render-hs! [s]
  #?(:clj (alter-var-root #'render-hs? (constantly s))
     :cljs (set! render-hs? s)))
(defn set-render-class! [s]
  #?(:clj (alter-var-root #'render-class? (constantly s))
     :cljs (set! render-class? s)))
(defn set-render-vals! [s]
  #?(:clj (alter-var-root #'render-vals? (constantly s))
     :cljs (set! render-vals? s)))
(defn set-render-commands! [s]
  #?(:clj (alter-var-root #'render-commands? (constantly s))
     :cljs (set! render-commands? s)))
(defn set-render-oob [s]
  #?(:clj (alter-var-root #'render-oob? (constantly s))
     :cljs (set! render-oob? s)))
(defn set-render-safe [s]
  #?(:clj (alter-var-root #'render-safe? (constantly s))
     :cljs (set! render-safe? s)))
