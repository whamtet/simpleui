(ns clj-htmx.render
  (:require
    [hiccup.core :as hiccup]))

(def no-content {:status 204, :headers {}, :body ""})

(defn html-response [body]
  {:status 200
   :headers {"Content-Type" "text/html"}
   :body body})

(defn snippet-response [body]
  (if body
    (-> body hiccup/html html-response)
    no-content))
