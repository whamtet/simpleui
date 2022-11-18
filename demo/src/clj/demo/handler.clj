(ns demo.handler
  (:require
    [demo.middleware :as middleware]
    [demo.layout :refer [error-page]]
    [demo.routes.active-search :as active-search]
    [demo.routes.bulk-update :as bulk-update]
    [demo.routes.click-to-edit :as click-to-edit]
    [demo.routes.click-to-load :as click-to-load]
    [demo.routes.delete-row :as delete-row]
    [demo.routes.edit-row :as edit-row]
    [demo.routes.dialogs :as dialogs]
    [demo.routes.home :refer [home-routes]]
    [demo.routes.infinite-scroll :as infinite-scroll]
    [demo.routes.inline-validation :as inline-validation]
    [demo.routes.modal-bootstrap :as modal-bootstrap]
    [demo.routes.progress-bar :as progress-bar]
    [demo.routes.sortable :as sortable]
    [demo.routes.value-select :as value-select]
    [reitit.ring :as ring]
    [ring.middleware.content-type :refer [wrap-content-type]]
    [ring.middleware.webjars :refer [wrap-webjars]]
    [demo.env :refer [defaults]]
    [mount.core :as mount]))

(mount/defstate init-app
  :start ((or (:init defaults) (fn [])))
  :stop  ((or (:stop defaults) (fn []))))

(mount/defstate app-routes
  :start
  (ring/ring-handler
    (ring/router
      [(home-routes)
       (active-search/routes)
       (bulk-update/routes)
       (click-to-edit/routes)
       (click-to-load/routes)
       (delete-row/routes)
       (edit-row/routes)
       (dialogs/routes)
       (inline-validation/routes)
       (infinite-scroll/routes)
       (modal-bootstrap/routes)
       (progress-bar/routes)
       (sortable/routes)
       (value-select/routes)])
    (ring/routes
      (ring/create-resource-handler
        {:path "/"})
      (wrap-content-type
        (wrap-webjars (constantly nil)))
      (ring/create-default-handler
        {:not-found
         (constantly (error-page {:status 404, :title "404 - Page not found"}))
         :method-not-allowed
         (constantly (error-page {:status 405, :title "405 - Not allowed"}))
         :not-acceptable
         (constantly (error-page {:status 406, :title "406 - Not acceptable"}))}))))

(defn app []
  (middleware/wrap-base #'app-routes))
