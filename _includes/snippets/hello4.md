```clojure
(defn add-customer [{:keys [first-name last-name customer]} _]
  {:customer
   (conj (or customer []) {:first-name first-name :last-name last-name})})

(defn- text [name value]
  [:input {:type "text"
           :name name
           :value value
           :required true
           :style "margin-right: 5px"}])

(defcomponent customer [req i {:keys [first-name last-name]}]
  [:div
   [:input {:type "hidden" :name (path "first-name") :value first-name}]
   [:input {:type "hidden" :name (path "last-name") :value last-name}]])

(defcomponent ^:endpoint ^{:params-stack add-customer} customer-list
  [req first-name last-name ^:json-stack customer]
  [:form {:id id :hx-post "customer-list"}
   ;; display the nested params
   [:pre (-> req :params ctmx.form/json-params util/pprint)]
   [:br]

   (ctmx.rt/map-indexed ctmx.github-demo.web.views.hello/customer req customer)
   (text (path "first-name") first-name)
   (text (path "last-name") last-name)
   [:input {:type "submit" :value "Add Customer"}]])

(def ring-handler
 (fn [req]
   ;; page renders initial html
   (page
     (customer-list req "Joe" "Stewart" []))))
```