## Inline Validation

This example shows how to do inline field validation, in this case of an email address.

```clojure
(defcomponent ^:endpoint email [req email]
  (let [valid? (contains? #{"" "test@test.com"} email)]
    [:div {:hx-target "this"}
     [:label.mr "Email Address"]
     [:input {:name "email" :value email :hx-get "email" :class (when-not valid? "error")}]
     [:img.htmx-indicator {:src "../../bars.svg"}]
     (when-not valid?
       [:div.error-message "That email is already taken.  Please enter another email."])]))

(defn- input-group [label name]
  [:div.form-group
   [:label.mr label] [:input {:type "text" :name name}]])

(make-routes
  "/demo"
  (fn [req]
    ;; page renders the hiccup and returns a ring response
    (page
      [:div
       [:h3 "Signup Form"]
       [:form
        (email req "")
        (input-group "First Name" "first-name")
        (input-group "Last Name" "last-name")]])))
```

{% include serverless/examples/inline_validation/demo.html %}
---
This form can be lightly styled with this CSS:

    .error-message {
      color:red;
    }
    .error input {
      box-shadow: 0 0 3px #CC0000;
    }
    
    .htmx-request + .htmx-indicator {
      opacity: 1;
    }

{% include footer.html %}
{% include zero_outer.html %}