(ns ctmx.config)

(def default-param-method :simple)
(defn set-param-method! [method]
  {:pre [(contains? #{:simple :path} method)]}
  #?(:clj (alter-var-root #'default-param-method (constantly method))
     :cljs (set! default-param-method method)))

(def render-style? true)
(def render-hs? true)
(def render-vals? true)

(defn set-render-style! [s]
  #?(:clj (alter-var-root #'render-style? (constantly s))
     :cljs (set! render-style? s)))
(defn set-render-hs! [s]
  #?(:clj (alter-var-root #'render-hs? (constantly s))
     :cljs (set! render-hs? s)))
(defn set-render-vals! [s]
  #?(:clj (alter-var-root #'render-vals? (constantly s))
     :cljs (set! render-vals? s)))
