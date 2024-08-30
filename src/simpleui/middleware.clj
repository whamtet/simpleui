(ns simpleui.middleware
  (:require [ring.util.response :refer [get-header]])
  (:import java.net.URL))

;; todo: better parsing
(defn- parse-search [s]
  (into {}
        (for [kv (.split s "&")
              :let [[k v] (.split kv "=")]
              :when v]
          [(keyword k)
           (if (re-find #"^\d" v)
             (Long/parseLong v)
             v)])))

(defn- url->params [^String s]
  (if-let [query (some-> s URL. .getQuery)]
    (parse-search query)
    {}))

(defn wrap-src-params [handler]
  (fn [req]
    (->> (get-header req "hx-current-url")
         url->params
         (assoc req :src-params)
         handler)))
