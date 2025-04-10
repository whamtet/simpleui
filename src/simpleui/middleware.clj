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

(defn- clean-chrome-protocol [s]
  (if (.startsWith s "chrome-extension")
    (str "http://" (.substring s 16))
    s))

(defn- url->params [^String s]
  (if-let [query (some-> s clean-chrome-protocol URL. .getQuery)]
    (parse-search query)
    {}))

(defn wrap-src-params
  "Middleware to put query params of calling page in :src-params.
  Useful when the source page is on a cdn."
  [handler]
  (fn [req]
    (->> (get-header req "hx-current-url")
         url->params
         (assoc req :src-params)
         handler)))
