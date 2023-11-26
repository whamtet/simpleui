(ns demo.routes.inline-validation
  (:require
    [simpleui.core :as simpleui :refer [defcomponent make-routes defn-parse]]
    [demo.middleware.formats :refer [page]]))

(defcomponent ^:endpoint email [req email]
  (let [valid? (contains? #{"" "test@test.com"} email)]
    [:div {:hx-target "this"}
     [:label.mr "Email Address"]
     [:input {:name "email" :value email :hx-get "email" :class (when-not valid? "error")}]
     [:img.htmx-indicator {:src "/img/bars.svg"}]
     (when-not valid?
       [:div.error-message "That email is already taken.  Please enter another email."])]))

(defn- input-group [label name]
  [:div.form-group
   [:label.mr label] [:input {:type "text" :name name}]])

(defn routes []
  (make-routes
    "/inline-validation"
    (fn [req]
      (page
        :zero-outer
        [:div
          [:h3 "Signup Form"]
            [:form
             (email req "")
             (input-group "First Name" "first-name")
             (input-group "Last Name" "last-name")]]))))
