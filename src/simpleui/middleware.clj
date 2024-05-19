(ns simpleui.middleware
    (:import
      java.net.URL))

;; todo: better parsing
(defn- parse-search [s]
  (into {}
        (for [kv (.split s "&")]
          (let [[k v] (.split kv "=")]
            [(keyword k)
             (if (re-find #"^\d" v)
               (Long/parseLong v)
               v)]))))

(defn- url->params [^String s]
  (if-let [query (some-> s URL. .getQuery)]
    (parse-search query)
    {}))

(defn wrap-src-params [handler]
  (fn [req]
    (->> (get-in req [:headers "hx-current-url"])
         url->params
         (assoc req :src-params)
         handler)))
