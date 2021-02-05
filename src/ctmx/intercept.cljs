(ns ctmx.intercept
  (:require
    [clojure.string :as string]
    [clojure.walk :as walk]
    hiccups.runtime)
  (:require-macros
    [hiccups.core :as hiccups]))

(defn query-string [args]
  (if (-> args count pos?)
    (->> args
         (map (fn [[k v]] (str k "=" v)))
         (string/join "&")
         (str "?"))
    ""))

(def request-config)
(def to-swap)

(def mock-xhr
  #js {:status 200
       :getAllResponseHeaders (fn [] "")})

(defn log-request []
  (js/console.log
    (-> request-config .-verb .toUpperCase)
    (str "/" (.-path request-config) (-> request-config .-parameters js->clj query-string))))

(defn- lowercaseize [headers]
  (zipmap
    (map #(-> % name .toLowerCase) (keys headers))
    (vals headers)))

(defn get-in-js [s]
  (reduce (fn [o s] (aget o s)) js/window (.split s ".")))
(defn- eval-map [m]
  (zipmap
    (keys m)
    (map get-in-js (vals m))))

(defn coerce-static [{:keys [headers
                             parameters
                             verb]}]
  {:headers (lowercaseize headers)
   :params parameters
   :request-method (keyword verb)})

(defn wrap-response [req f]
  (some->> (or req {})
           js->clj
           walk/keywordize-keys
           coerce-static
           f
           hiccups/html))

(def responses)

(defn set-responses! [metas]
  (->> metas
       (map :deps)
       (apply merge)
       eval-map
       (set! responses))

  (js/htmx.defineExtension
    "intercept"
    #js {:onEvent (fn [name evt]
                    (case name
                      "htmx:beforeRequest"
                      (let [xhr (-> evt .-detail .-xhr)]
                        (set! (-> evt .-detail .-xhr) mock-xhr)
                        (set! request-config (-> evt .-detail .-requestConfig))
                        (log-request)
                        (set! (.-send xhr) (.-onload xhr)))
                      "htmx:beforeSwap"
                      (if-let [f (-> request-config .-path responses)]
                        (if-let [swap (wrap-response request-config f)]
                          (set! to-swap swap)
                          false)
                        false)
                      nil))
         :transformResponse (fn [_ _ _] to-swap)}))
