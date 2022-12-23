```clojure
(defcomponent ^:endpoint form-edit [req first-name last-name email]
  [:form {:id id :hx-put "form-ro" :hx-target "this"}
   [:div
    [:label.mr "First Name"]
    (text "first-name" first-name)]
   [:div.form-group
    [:label.mr "Last Name"]
    (text "last-name" last-name)]
   [:div.form-group
    [:label.mr "Email Address"]
    (emaili "email" email)]
   [:button.btn.margin "Save"]
   [:button.btn.margin {:hx-get "form-ro"} "Cancel"]])

(defcomponent ^:endpoint form-ro [req first-name last-name email]
  ;; make sure form-edit is included in endpoints
  form-edit
  [:form {:id id :hx-target "this"}
    (hidden "first-name" first-name)
    [:div [:label "First Name"] ": " first-name]
    (hidden "last-name" last-name)
    [:div [:label "Last Name"] ": " last-name]
    (hidden "email" email)
    [:div [:label "Email"] ": " email]
    [:button.btn.margin
     {:hx-put "form-edit"}
      "Click To Edit"]])

(def ring-handler
  (fn [req]
    ;; page renders initial html
    (page
      (form-ro req "Joe" "Blow" "joe@blow.com"))))
```