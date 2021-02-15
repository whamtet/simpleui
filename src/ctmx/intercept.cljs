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

(def mock-xhr
  #js {:status 200
       :getAllResponseHeaders (fn [] "")})

(defn log-request [request-config]
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

(defn coerce-static [m]
  (-> m
      (update :headers lowercaseize)
      (assoc :params (:parameters m) :request-method (-> m :verb keyword))))

(defn wrap-response [req f]
  (let [f-result (-> req js->clj walk/keywordize-keys coerce-static f)]
    (cond
      (nil? f-result) nil
      (.-then f-result) (.then f-result hiccups/html)
      :else (hiccups/html f-result))))

(def responses)

(def to-swap)
(def log? false)

(defn set-responses! [metas]
  (->> metas
       (map :deps)
       (apply merge)
       eval-map
       (set! responses))

  (js/htmx.defineExtension
    "intercept"
    #js {:onEvent (fn [name evt]
                    (when log?
                      (js/console.log name evt))
                    (case name
                      "htmx:beforeRequest"
                      (let [xhr (-> evt .-detail .-xhr)
                            request-config (-> evt .-detail .-requestConfig)
                            swap (fn [swap]
                                   (set! to-swap swap)
                                   (.onload xhr))]
                        (set! (-> evt .-detail .-xhr) mock-xhr)
                        (set! (.-send xhr)
                              (fn [] ;;now we're in async land
                                (if-let [f (-> request-config .-path responses)]
                                  (let [r (wrap-response request-config f)]
                                    (if (some-> r .-then)
                                      (.then r swap)
                                      (swap r)))
                                  (swap nil)))))
                      "htmx:beforeSwap"
                      (boolean to-swap)
                      nil))
         :transformResponse (fn [_ _ _] to-swap)}))
