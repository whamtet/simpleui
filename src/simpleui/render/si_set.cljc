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

(defmacro whenm [sym body]
  `(when-let [~sym (~(keyword sym) ~'m)] ~body))
(defn promote-set-form [[tag m & rest :as v]]
  (if (and
       (keyword? tag)
       (-> tag name (.startsWith "form"))
       (map? m)
       (or (:si-set m) (:si-clear m)))
    [tag
     (dissoc m :si-set :si-clear :si-set-class)
     (whenm si-set [:input {:type "hidden" :name "si-set" :value (comma-join si-set)}])
     (whenm si-clear [:input {:type "hidden" :name "si-clear" :value (comma-join si-clear)}])
     (whenm si-set-class [:input {:type "hidden" :name "si-set-class" :value si-set-class}])
     rest]
    v))

;; response side

(defn- safe-cat [a b c]
  (concat a b (if (seq? c) c [c])))
(defn- map-comma [f s]
  (when s
    (map #(f (keyword %)) (.split s ","))))

(defn- set-input [params]
  (fn [k]
    (if-let [class (get params :si-set-class)]
      [:input {:type "hidden"
               :hx-swap-oob "true"
               :name k
               :id k
               :class class
               :value (get params k)}]
      (throw (Exception. "missing si-set-class")))))
(defn- clear-div [k]
  [:div {:id k
         :hx-swap-oob "true"
         :style "display: none"}])

(defn concat-set-clear [{:keys [params]
                        {:keys [si-set si-clear]} :params} body]
  (if (or si-set si-clear)
    ;; si-set, si-clear are comma seperated
    (safe-cat
     (map-comma (set-input params) si-set)
     (map-comma clear-div si-clear)
     body)
    body))
