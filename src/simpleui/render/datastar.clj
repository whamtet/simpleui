(ns simpleui.render.datastar
  (:require
    [clojure.data.json :as json]
    [clojure.string :as string]
    [simpleui.response :as response]
    [simpleui.render :as render]))

(defn- join-lines [& lines]
  (string/join "\n" (filter identity lines)))
(defn- join-lines2 [lines]
  (string/join "\n" (filter identity lines)))

(defn- select-attrs [[_ m]]
  (and (map? m) m))
(defn- dissoc-attrs [[_ m :as v]]
  (if (map? m)
    (update v 1 dissoc :mergeMode :useViewTransition)
    v))

(defmulti render-sse (fn [_ v] (first v)))

(defmethod render-sse :default [prefix v]
  (let [{:keys [mergeMode useViewTransition]} (select-attrs v)]
    (join-lines
      "event: datastar-merge-fragments"
     (when mergeMode
       (str "data: mergeMode " (name mergeMode)))
     (when useViewTransition
       "data: useViewTransition true")
     (->> v dissoc-attrs (render/html prefix) (str "data: fragments ")))))

(defmethod render-sse :merge-signals [_ [_ b c]]
  (join-lines
   "event: datastar-merge-signals"
   (if c
     (str "data: onlyIfMissing " b)
     (str "data: signals " (json/write-str b)))
   (when c
     (str "data: signals " (json/write-str c)))))

(defmethod render-sse :remove-fragments [_ [_ & selectors]]
  (join-lines2
   (conj
    (for [selector selectors]
      (str "data: selector " selector))
    "event: datastar-remove-fragments")))

(defmethod render-sse :remove-signals [_ [_ & paths]]
  (join-lines2
   (conj
    (for [path paths]
      (str "data: paths " path))
    "event: datastar-remove-signals")))

(defmethod render-sse :script [_ [_ m & scripts]]
  (join-lines2
   (concat
     ["event: datastar-execute-script"]
    (when (map? m)
      (for [[k v] m]
        (if (= :autoRemove k)
          (str "data: autoRemove " v)
          (str "data: attributes " k " " v))))
    (for [script (if (map? m)
                   scripts
                   (conj scripts m))
          line (.split script "\n")]
      (str "data: script " line)))))

(defn- sse-response [prefix body]
  (->> body
       (map #(render-sse prefix %))
       (string/join "\n")
       response/html-response-datastar))

(defn snippet-response-datastar
  "Converts SimpleUI component response into datastar ring map."
  [prefix body]
  (cond
    (nil? body) response/no-content
    (seq? body) (sse-response prefix body)
    :else (sse-response prefix (list body))))
