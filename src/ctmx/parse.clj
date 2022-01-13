(ns ctmx.parse
  (:require
    [clojure.walk :as walk]
    [crouton.html :as html]))

(defn- combine-class-id [tag attrs]
  (let [{:keys [class id]} attrs
        id-str (some->> id (str "#"))
        class-str (when class (map #(str "." %) (.split class " ")))]
    [(keyword (apply str tag id-str class-str))
     (dissoc attrs :class :id)]))
(defn- convert-map [m]
  (if (:tag m)
    (let [[tag attrs] (combine-class-id (:tag m) (:attrs m))]
      (vec
        (list* tag attrs (:content m))))
    m))

(defn parse-raw [s]
  (->> s
       html/parse
       (walk/prewalk convert-map)))