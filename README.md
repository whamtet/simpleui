# ctmx

Clojure backend for for [htmx](https://htmx.org/).

## Rationale

[htmx](https://htmx.org/) enables web developers to create powerful webapps without writing any javascript.  Whenever `hx-*` attributes are included in html the library will update the dom in response to user events.  The architecture is simpler and pages load more quickly than in javascript oriented webapps.

ctmx is a backend accompaniment which makes htmx even easier to use.  It works in conjunction with [hiccup](https://weavejester.github.io/hiccup/) for rendering and [reitit](https://cljdoc.org/d/metosin/reitit/0.5.10/doc/introduction) for routing.

## Demo

[ctmx-demo.herokuapp.com](https://ctmx-demo.herokuapp.com/), source [here](https://github.com/whamtet/ctmx-demo).  First request wakes up Heroku so a bit slow, after that you get an accurate idea of performance.

## Usage

First require the library

```clojure
(require '[ctmx.core :refer :all])
```

The core of ctmx is the `defcomponent` macro.

```clojure
(defcomponent ^:endpoint hello [req my-name]
  [:div#hello "Hello " my-name])
```

This defines an ordinary function which also expands to an endpoint `/hello`.

To use our endpoint we call `make-routes`

```clojure
(make-routes
  "/demo"
  (fn [req]
    (render/html5-response
      [:div {:style "padding: 10px"}
        [:label {:style "margin-right: 10px"}
          "What is your name?"]
        [:input {:type "text"
                 :name "my-name"
                 :hx-patch "hello"
                 :hx-target "#hello"
                 :hx-swap "outerHTML"}]
         (hello req "")])))
```

![](screenshot.png)

Here the only active element is the text input.  On the input's default action (blur) it will request to `/hello` and replace `#hello` with the server response.  We are using `hello` both as a function and an endpoint.  When called as an endpoint arguments are set based on the html `name` attribute.

**The first argument to defcomponent is always the req object**

### ids and values

In the above example we use a fixed id `#hello`.  In general we should not hardcode ids because a component can exist multiple times in the dom.  To resolve this `id` is set automatically based on the call path of nested components.

```clojure
[:div.my-component {:id id} ...]
```

In addition we need to set names and values

```clojure
[:input {:type "text" :name (path "first-name") :value (value "first-name")}]
[:input {:type "text" :name (path "last-name") :value (value "last-name")}]
```

`path` is guaranteed to be unique and `value` will autopopulate based on the request parameters.  This makes it very easy to dynamically generate content without thinking about data flow.

### hx-target

When we first load the page ctmx generates the full dom tree.  Subsequent updates only render a branch on the tree.  To ensure `path` and `value` are set consistently we must always set `hx-target`.

```clojure
(my-component req)
;; clicking updates my-component
[:button
  {:hx-get "my-component"
   :hx-target (hash "my-component")}
   "Click Me!"]
```

`my-component` must also have id set correctly

```clojure
(defcomponent ^:endpoint my-component [req]
  [:div {:id id} ...])
```

ctmx uses the id of the component being updated to set `path` consistently.

### Component Arrays

`path` also includes array indices.  Instead of using `clojure.core/map` use `ctmx.core/map-indexed`.


```clojure
(defcomponent table-row [req i row-datum]
  [:tr ...])

...

[:table
  (map-indexed table-row req table-data)]
```

### Lazy Evaluation

Another `path` related issue.  It is important to expand all arrays in place, no lazy evaluation.  Instead of `clojure.core/for` use `ctmx.core/forall`.

```clojure
(forall [customer customers]
  [:div.customer ...])
```

### relative paths

`path` and `value` are set based on the call path to each component.  To reference paths and values of other components use relative paths.

```clojure
(value "subcomponent/parameter") ;; naughty naughty!
(value "../sibling-component/parameter")
````

Be careful when using `ctmx.core/map-indexed`

```clojure
;; called from within array component
(value "../../sibling-component/parameter")
```

We need to ascend two levels in the call path because the array index counts as one level.  We can also use 'absolute' paths for simple parameters

```clojure
(when (= (value "/parameter-without-path") "do")
  ...)
```

### Parameter Casting

htmx submits all parameters as strings.  It can be convenient to cast parameters to the required type

```clojure
(defcomponent my-component [req ^:int int-argument ^:boolean boolean-argument] ...)
```

You may also cast within the body of `defcomponent`

```clojure
[:div
  (if ^:boolean (value "grumpy")
    "Cheer up!"
    "How are you?")]
```

### Transforming parameters to JSON

htmx submits all parameters as a flat map, however we can use the above naming scheme to transform it into nested json for database access etc.  Simply call `ctmx.form/json-params`


```clojure
(json-params
  {:store-name "My Store"
   :customers_0_customer_first-name "Joe"
   :customers_0_customer_last-name "Smith"
   :customers_1_customer_first-name "Jane"
   :customers_1_customer_last-name "Doe"})

;; {:store-name "My Store"
;;  :customers [{:customer {:first-name "Joe" :last-name "Smith"}}
;;              {:customer {:first-name "Jane" :last-name "Doe"}}]}
```

`ctmx.form/flatten-json` reflattens the above structure.

### Updating Parameters

A typical use case is that we wish to retrieve parameters from storage on a `GET` request and use client parameters on subsequent `PATCH` or `POST` requests.  Parameters are dynamically bound, to update them use the `ctmx.core/update-params` macro

```clojure
(defcomponent my-component [req]
  (update-params
    #(if (-> req :request-method (= :get)) (get-from-db %) %)
    [:div ...]))

### Additional Parameters

In most cases htmx will supply all required parameters.  If you need to include extra ones, set the `hx-vals` attribute.  `hx-vals` takes json, use `clojure.data.json/write-str` for convenience.

```clojure
[:button.delete
  {:hx-delete "trash-can"
   :hx-vals (json/write-str {:hard-delete true})}
   "Delete"]
```

### Action at a distance (hx-swap-oob)

Best to avoid, but sometimes too convenient to resist.  htmx provides the `hx-swap-oob` attribute for updating multiple dom elements within a single response.  In ctmx we must only provide the additional elements when htmx is updating, not in the initial render

```clojure
(defcomponent my-component [req]
  (list
    (when hx-request?
      [:div.side-element
       {:id (path "path/to/side-element)
        :hx-swap-oob "true"}
        ...])
    [:div.main-element {:id id} ...]))
```

Be very careful to only include `hx-swap-oob` elements when `hx-request?` is true.

### Responses

By default ctmx expects components to return hiccup vectors which are rendered into html.
`nil` returns http **204 - No Content** and htmx will not update the dom.
You may also return an explicit ring map if you wish.  A common use case is to refresh the page after an operation is complete

```clojure
(defcomponent my-component [req]
  (case (:request-method req)
    :post
    (do
      (save-to-db ...)
      ctmx.response/hx-refresh)
    :get ...))
```

`ctmx.response/hx-refresh` sets the "HX-Refresh" header to "true" and htmx will refresh the page.

## License

Copyright © 2020 Matthew Molloy

This program and the accompanying materials are made available under the
terms of the Eclipse Public License 2.0 which is available at
http://www.eclipse.org/legal/epl-2.0.

This Source Code may also be made available under the following Secondary
Licenses when the conditions for such availability set forth in the Eclipse
Public License, v. 2.0 are satisfied: GNU General Public License as published by
the Free Software Foundation, either version 2 of the License, or (at your
option) any later version, with the GNU Classpath Exception which is available
at https://www.gnu.org/software/classpath/license.html.
