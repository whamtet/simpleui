(ns demo.middleware.formats
  (:require
    [ctmx.render :as render]
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

(defn page [style body]
  {:status 200
   :headers {"content-type" "text/html"}
   :body
   (page/html5
    [:head
     [:link {:rel "stylesheet" :href "/css/screen.css"}]]
    [:body
     (render/walk-attrs body)
     [:script {:src "https://unpkg.com/htmx.org@1.3.3"}]
     [:script
      (case style
        :outer "htmx.config.defaultSwapStyle = 'outerHTML';"
        :zero-inner "htmx.config.defaultSettleDelay = 0;"
        :zero-outer "htmx.config.defaultSettleDelay = 0; htmx.config.defaultSwapStyle = 'outerHTML';"
        "")]])})