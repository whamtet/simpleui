(ns ctmx.response)

(def no-content (clj->js {:statusCode 204 :headers {} :body ""}))
(def hx-refresh (clj->js {:statusCode 200 :headers {"HX-Refresh" "true"} :body ""}))

(defn html-response [body]
  (clj->js
    {:statusCode 200
     :headers {"Content-Type" "text/html"}
     :body body}))

(defn hx-redirect [redirect]
  (clj->js
    {:statusCode 200
     :headers {"HX-Redirect" redirect}
     :body ""}))
