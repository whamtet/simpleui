(ns demo.middleware.formats
  (:require
    [hiccup.page :as page]
    [luminus-transit.time :as time]
    [muuntaja.core :as m]))

(def instance
  (m/create
    (-> m/default-options
        (update-in
          [:formats "application/transit+json" :decoder-opts]
          (partial merge time/time-deserialization-handlers))
        (update-in
          [:formats "application/transit+json" :encoder-opts]
          (partial merge time/time-serialization-handlers)))))

(defn page [body]
  {:status 200
   :headers {"content-type" "text/html"}
   :body
   (page/html5
    [:body
     body
     [:script {:src "https://unpkg.com/htmx.org@1.3.3"}]])})