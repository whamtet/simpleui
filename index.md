ctmx is an app development tool for fast product development and even faster page load times.  It uses [htmx](https://htmx.org/) on the frontend.

## Basic example

```clojure
(defcomponent ^:endpoint hello [req my-name]
  [:div#hello "Hello " my-name])

(make-routes
  "/demo"
  (fn [req]
    ;; page renders the hiccup and returns a ring response
    (page
      [:div
       [:label "What is your name?"]
       [:input {:name "my-name" :hx-patch "hello" :hx-target "#hello"}]
       (hello req "")])))

```
{% include examples/demo.html %}
---
Try inspecting the above text field.  You should see something like this.

![](inspect.png)

Now try editing the text.  When the input looses focus it submits a request to `/hello` and updates the contents of `#hello`.

The core of ctmx is the `defcomponent` macro which expands to both:

- An ordinary function
- A rest endpoint.  Arguments are bound based on the html `name` attribute.

`defcomponent` enables developers to quickly build rich user interfaces with _no_ javascript.  All code is on the server backend and yet it feels the same as frontend code.

## Getting started

Getting started is easy with clojure tools and the excellent [kit](https://kit-clj.github.io) framework.

```bash
clojure -Ttools install com.github.seancorfield/clj-new '{:git/tag "v1.2.381"}' :as new
clojure -Tnew create :template io.github.kit-clj :name yourname/guestbook
```

Change the url in `kit.edn` to `"https://github.com/whamtet/modules.git"` then run `make repl`

```clojure
(kit/sync-modules)
(kit/install-module :kit/ctmx)
```

Quit the process, `make repl` then

```clojure
(go)
```

Visit [localhost:3000](http://localhost:3000).  To reload changes

```clojure
(reset)
```

## Architecture

{% include architecture.html %}

Ctmx uses Hypermedia as the Engine of Application State ([HATEOAS](https://en.wikipedia.org/wiki/HATEOAS)), the web as it was originally supposed to be.  Application state is implicitly stored in the html itself, not in a separate javascript layer.  By extending the original html model instead of building a javascript layer over top, we get simplicity and **much** faster page load times.

## Handling data flow

{% include examples/data_flow.html %}

```clojure
(defcomponent ^:endpoint form [req ^:path first-name ^:path last-name]
  [:form {:id id :hx-post "form"}
    [:input {:type "text" :name (path "first-name") :value first-name}] [:br]
    [:input {:type "text" :name (path "last-name") :value last-name}] [:br]
    (when (= ["Barry" "Crump"] [first-name last-name])
      [:div "A good keen man!"])
    [:input {:type "submit"}]])

(make-routes
  "/data-flow"
  (fn [req]
    (page (form req "Barry" ""))))
```

ctmx maintains a call stack of nested components.  This makes it easy to label data without name clashes.  Try submitting the above form and then inspecting the browser network tab.

![](network.png)

`(path "first-name")` and `(path "last-name")` macroexpand to unique values which are automatically mapped back to the function arguments.  We can use the `form` component multiple times on the page without worrying about a name clash.

### Components in array

To expand `(path "first-name")` and `(path "last-name")` consistently we must be careful with components inside arrays.  Use `ctmx.rt/map-indexed` to map values across an array.

{% include examples/nesting_components.html %}

```clojure
(def data
  [{:first-name "Matthew" :last-name "Molloy"}
   {:first-name "Chad" :last-name "Thomson"}])

(defcomponent table-row [req first-name last-name]
  [:tr
    [:td first-name] [:td last-name]])

(defcomponent table [req]
  [:table
    (ctmx.rt/map-indexed table-row req data)])

(make-routes
  "/nesting-components"
  (fn [req]
    (page (table req))))
```

## Transforming parameters to JSON

The UI provides a natural structure to nest our data.  This corresponds closely to the database schema and provides a natural connection between the two.  Try adding customers using the form below.

{% include examples/transforming.html %}
---
```clojure
(defn add-customer [{:keys [first-name last-name customer]}]
  {:customer
   (conj (or customer []) {:first-name first-name :last-name last-name})})

(defn- text [name value]
  [:input {:type "text" :name name :value value :required true}])

(defcomponent customer [req first-name last-name]
  [:div
    ;; other display here.
    [:input {:type "hidden" :name (path "first-name") :value first-name}]
    [:input {:type "hidden" :name (path "last-name") :value last-name}]])

(defcomponent ^:endpoint ^{:params-stack add-customer} customer-list
  [req first-name last-name ^:json-stack customer]
  [:form {:id id :hx-post "customer-list"}
    ;; display the nested params
    [:pre (-> req :params ctmx.form/json-params util/pprint)]
    [:br]

    (ctmx.rt/map-indexed serverless.functions.core/customer req customer)
    (text (path "first-name") first-name)
    (text (path "last-name") last-name)
    [:input {:type "submit" :value "Add Customer"}]])

(make-routes
  "/transforming"
  (fn [req]
    (page (customer-list req "Joe" "Stewart" []))))
```

As we add customers the JSON builds up to match the UI.  We would lightly transform the data before persisting it, however it is often already close to what we want it to be.

This example uses the `add-customer` middleware to transform parameters before they are displayed.

## Casting parameters

{% include examples/parameter_casting.html %}
---
```clojure
(defcomponent ^:endpoint click-div [req ^:long num-clicks]
  [:form {:id id :hx-get "click-div" :hx-trigger "click"}
    [:input {:type "hidden" :name "num-clicks" :value (inc num-clicks)}]
    "You have clicked me " num-clicks " times!"])

(make-routes
  "/parameter-casting"
  (fn [req]
    (page (click-div req 0))))
```

Ctmx uses native html forms, so data is submitted as strings.  We can cast it as necessary.  Supported casts include **^:long**, **^:boolean** and **^:double**. See [documentation](https://github.com/whamtet/ctmx#parameter-casting) for details.

We may also cast within the body of `defcomponent`.

```clojure
[:div
  (if ^:boolean (value "grumpy")
    "Cheer up!"
    "How are you?")]
```

## Further reading

Please see the [examples](examples).

{% include footer.html %}
{% include zero_outer.html %}
