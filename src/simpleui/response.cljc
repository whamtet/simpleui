(ns simpleui.response)

(def no-content {:status 204 :headers {} :body ""})
(def hx-refresh {:status 200 :headers {"HX-Refresh" "true"} :body ""})

(defn html-response [body]
  {:status 200
   :headers {"Content-Type" "text/html"}
   :body body})

(defn hx-redirect
  "Like regular http 302 but redirects parent page when update is being made on a single component."
  [redirect]
  {:status 200
   :headers {"HX-Redirect" redirect}
   :body ""})

(defn redirect [location]
  {:status 302, :headers {"Location" location}, :body ""})
