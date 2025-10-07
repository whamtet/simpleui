(ns demo.middleware.formats
  (:require
    [simpleui.render :as render]
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

(defn page-response [opts & content]
  {:status 200
   :headers {"Content-Type" "text/html"}
   :body (page/html5 opts content)})

(defn page [& args]
  (let [opts (set (butlast args))]
    (page-response
      [:head
       (sheet "/css/screen.css")
       (when (:bootstrap opts)
         (sheet "https://cdnjs.cloudflare.com/ajax/libs/twitter-bootstrap/4.5.2/css/bootstrap.min.css"))]
      [:body
       (render/walk-attrs (last args))
       [:script {:src "/js/htmx.min.js"}]
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
         [:script {:src "/js/sortable.js"}])])))

(defn page-datastar [& body]
  (page-response
   [:head
    [:meta {:charset "UTF-8"}]
    [:title "Datastar + Kit"]
    [:script {:src "https://cdn.jsdelivr.net/gh/starfederation/datastar@v1.0.0-RC.5/bundles/datastar.js" :type "module"}]]
   [:body (render/walk-attrs body)]))
