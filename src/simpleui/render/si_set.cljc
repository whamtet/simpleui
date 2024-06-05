(ns simpleui.render.si-set
  (:require
   [clojure.string :as string]))

;; request side

(defn- comma-join [s]
  (->> s (map name) (string/join ",")))

(defn- update-hx-vals [si-set si-clear si-set-class]
  (fn [hx-vals]
    (if (string? hx-vals)
      hx-vals
      (cond-> hx-vals
        si-set (assoc :si-set (comma-join si-set))
        si-clear (assoc :si-clear (comma-join si-clear))
        si-set-class (assoc :si-set-class si-set-class)
        ))))

(defn promote-set [{:keys [si-set si-clear si-set-class] :as m}]
  (if (or si-set si-clear)
    (-> m
      (update :hx-vals (update-hx-vals si-set si-clear si-set-class))
      (dissoc :si-set :si-clear :si-set-class))
    m))

;; response side

(defn- safe-cat [a b c]
  (concat a b (if (seq? c) c [c])))
(defn- map-comma [f s]
  (when s
    (map #(f (keyword %)) (.split s ","))))

(defn- set-input [params]
  (fn [k]
    [:input {:type "hidden"
             :hx-swap-oob "true"
             :name k
             :id k
             :class (get params :si-set-class)
             :value (get params k)}]))
(defn- clear-div [k]
  [:div {:id k 
         :hx-swap-oob "true"
         :style "display: none"}])

(defn concat-set-clear [{:keys [params]
                        {:keys [si-set si-clear]} :params} body]
  (if (or si-set si-clear)
    (safe-cat
     (map-comma (set-input params) si-set)
     (map-comma clear-div si-clear)
     body)
    body))