---
layout: bootstrap
---

## Modal Dialogs in Bootstrap

Many CSS toolkits include styles (and Javascript) for creating modal dialog boxes. 
This example shows how to use ctmx to display dynamic dialog using Bootstrap, and how to trigger its animation styles in Javascript.

We start with a button that triggers the dialog, along with a div at the bottom of your markup where the dialog will be loaded:

---

{% include serverless/examples/modal_bootstrap/demo.html %}

```clojure
(defcomponent ^:endpoint modal [req]
  (list
    [:div#modal-backdrop.modal-backdrop.fade {:style "display:block"}]
    [:div#modal.modal.fade {:tabindex -1 :style "display:block"}
      [:div.modal-dialog.modal-dialog-centered
        [:div.modal-content
          [:div.modal-header
            [:h5.modal-title "Modal title"]]
          [:div.modal-body
            [:p "Modal body text goes here."]]
          [:div.modal-footer
            [:button.btn.btn-secondary {:type "button" :onclick "closeModal()"}
              "Close"]]]]]))

(make-routes
  "/demo"
  (fn [req]
    modal ;; need to include modal in list of endpoints
    [:div
      [:button.btn.btn-primary
        {:hx-get "modal"
         :hx-target "#modals-here"
         :_ "on htmx:afterOnLoad wait 10ms then add .show to #modal then add .show to #modal-backdrop"}
        "Open Modal"]
      [:div#modals-here]]))
```

The **Open Modal** button contains a [hyperscript](https://hyperscript.org/) snippet to handle css transitions. 
We also use the following custom code to close the bootsrap.

```javascript
function closeModal() {
	var container = document.getElementById("modals-here")
	var backdrop = document.getElementById("modal-backdrop")
	var modal = document.getElementById("modal")

	modal.classList.remove("show")
	backdrop.classList.remove("show")

	setTimeout(function() {
		container.removeChild(backdrop)
		container.removeChild(modal)
	}, 200)
}
```
{% include closeModal.html %}
{% include footer.html %}
{% include hyperscript.html %}