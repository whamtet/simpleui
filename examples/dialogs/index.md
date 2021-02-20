## Dialogs

Dialogs can be triggered with the [hx-prompt](https://htmx.org/attributes/hx-prompt) 
and [hx-confirm](https://htmx.org/attributes/hx-confirm) attributes. 
These are triggered by the user interaction that would trigger the AJAX request, 
but the request is only sent if the dialog is accepted.
---
{% include serverless/examples/dialogs/demo.html %}

```clojure
(defcomponent ^:endpoint reply [{:keys [headers]}]
  [:div#response.mmargin "You entered " (headers "hx-prompt")])

(make-routes
  "/demo"
  (fn [req]
    [:div
      [:button.btn.mb
        {:hx-post "reply"
         :hx-prompt "Enter a string"
         :hx-confirm "Are you sure?"
         :hx-target "#response"}
        "Prompt Submission"]
      [:div#response]]))
```

{% include footer.html %}
{% include zero_outer.html %}