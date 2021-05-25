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

(defn- sheet [href]
  [:link {:rel "stylesheet" :href href}])

(defn page [& args]
  (let [opts (set (butlast args))]
    {:status 200
     :headers {"content-type" "text/html"}
     :body
     (page/html5
      [:head
       (sheet "/css/screen.css")
       (when (:bootstrap opts)
         (sheet "https://cdnjs.cloudflare.com/ajax/libs/twitter-bootstrap/4.5.2/css/bootstrap.min.css"))]
      [:body
       (render/walk-attrs (last args))
       [:script {:src "https://unpkg.com/htmx.org@1.2.0"}]
       [:script {:src "/js/default.js"}]
       (when (:outer opts)
         [:script "htmx.config.defaultSwapStyle = 'outerHTML';"])
       (when (:zero-inner opts)
         [:script "htmx.config.defaultSettleDelay = 0;"])
       (when (:zero-outer opts)
         [:script "htmx.config.defaultSettleDelay = 0; htmx.config.defaultSwapStyle = 'outerHTML';"])
       (when (:hyperscript opts)
         [:script {:src "https://unpkg.com/hyperscript.org@0.0.3"}])
       (when (:sortable opts)
         [:script {:src "https://cdn.jsdelivr.net/npm/sortablejs@latest/Sortable.min.js"}])
       (when (:sortable opts)
         [:script {:src "/js/sortable.js"}])])}))