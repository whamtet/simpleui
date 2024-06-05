(ns simpleui.render.si-set
  (:require
   [clojure.string :as string]))

;; request side

(defn- update-hx-vals [si-set si-clear si-set-class]
  (fn [hx-vals]
    (if (string? hx-vals)
      hx-vals
      (cond-> hx-vals
        si-set (assoc :si-set (string/join "," si-set))
        si-clear (assoc :si-clear (string/join "," si-clear))
        si-set-class (assoc :si-set-class si-set-class)
        ))))

(defn promote-set [{:keys [si-set si-clear si-set-class] :as m}]
  (if (or si-set si-clear)
    (update m :hx-vals (update-hx-vals si-set si-clear si-set-class))
    m))

;; response side

(defn concat-set-clear [{{:keys [si-set si-clear si-set-class]} :params} body]
  (prn 'si-set si-set)
  body)