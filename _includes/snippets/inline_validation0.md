```clojure
(defcomponent ^:endpoint email [req email]
  (let [valid? (contains? #{"" "test@test.com"} email)]
    [:div {:hx-target "this"}
     [:label.mr "Email Address"]
     [:input {:name "email" :value email :hx-get "email" :class (if valid? "mr" "mr error")}]
     [:img.htmx-indicator {:src "../../bars.svg"}]
     (when-not valid?
       [:div.error-message "That email is already taken.  Please enter another email."])]))

(defn- input-group [label name]
  [:div.form-group
   [:label.mr label] [:input {:type "text" :name name}]])

(def ring-handler
  (fn [req]
    ;; page renders initial html
    (page
      [:div
       [:h3 "Signup Form"]
       [:form
        (email req "")
        (input-group "First Name" "first-name")
        (input-group "Last Name" "last-name")]])))
```