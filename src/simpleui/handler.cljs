(ns simpleui.handler
  (:require
    [reitit.ring :as ring]))

(defn key-map [f m]
  (zipmap (map f (keys m)) (vals m)))

(defn lowercaseize [m]
  (key-map #(.toLowerCase %) m))
(defn keywordize [m]
  (key-map keyword m))
(defn keywordize-lower [m]
  (key-map #(-> % .toLowerCase keyword) m))

(defn e->ring [event]
  (let [{:keys [headers rawPath rawQueryString requestContext body]} (-> event js->clj keywordize)
        headers-kw (keywordize-lower headers)]
    {:server-port (some-> headers-kw :x-forwarded-port js/Number)
     :server-name (:host headers-kw)
     :remote-addr (:x-forwarded-for headers-kw)
     :uri rawPath
     :query-string rawQueryString
     :scheme (if (-> headers-kw :x-forwarded-proto (= "https"))
               :https :http)
     :request-method (some-> requestContext (get "http") (get "method") .toLowerCase keyword)
     :headers (lowercaseize headers)
     :body body}))

(defn lambda-handler [router]
  (let [handler (-> router ring/router ring/ring-handler)]
    (fn [event]
      (-> event e->ring handler))))
