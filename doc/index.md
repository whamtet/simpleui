Clojure backend for for [htmx](https://htmx.org/).

## Rationale

[htmx](https://htmx.org/) enables web developers to create powerful webapps without writing any javascript.  Whenever `hx-*` attributes are included in html the library will update the dom in response to user events.  The architecture is simpler and pages load more quickly than in javascript oriented webapps.

ctmx is a backend accompaniment which makes htmx even easier to use.  It works in conjunction with [hiccup](https://weavejester.github.io/hiccup/) for rendering and [reitit](https://cljdoc.org/d/metosin/reitit/0.5.10/doc/introduction) for routing.

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
;; make-routes generates a reitit handler with the root page at /demo
;; and all subcomponents on their own routes
(make-routes
  "/demo"
  (fn [req]
    (page ;; page renders the rest of the page, htmx script etc
      [:div
       [:label "What is your name?"]
       [:input {:name "my-name" :hx-patch "hello" :hx-target "#hello"}]
       (hello req "")])))
```

![](../inspect.png)

Here the only active element is the text input.  On the input's default action (blur) it will request to `/hello` and replace `#hello` with the server response.  We are using `hello` both as a function and an endpoint.  When called as an endpoint arguments are set based on the html `name` attribute.

**The first argument to defcomponent is always the req object**

### component stack

ctmx retains a call stack of nested components.  This is used to set ids and values in the sections below.

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

ctmx uses the id of the component being updated to set the component stack consistently.

### Component Arrays

`path` also includes array indices.  Instead of using `clojure.core/map` use `ctmx.rt/map-indexed`.


```clojure
(def data [{:first-name "Fred" :last-name "Smith"}
           {:first-name "Ocean" :last-name "Leader"}])

(defcomponent table-row [req index first-name last-name]
  [:tr ...])

...

[:table
  (rt/map-indexed table-row req data)]
```

### relative paths

`path` and `value` are set based on the call path to each component.  To reference paths and values of other components use relative paths.

```clojure
(value "subcomponent/parameter") ;; naughty naughty!
(value "../sibling-component/parameter")
````

Be careful when using `ctmx.rt/map-indexed`

```clojure
;; called from within array component
(value "../../sibling-component/parameter")
```

We need to ascend two levels in the call path because the array index counts as one level.  We can also use absolute paths for simple parameters not in the component stack.

```clojure
(when (= (value "/parameter-without-path") "do")
  ...)
```

### Parameter Casting

htmx submits all parameters as strings.  It can be convenient to cast parameters to the required type

```clojure
(defcomponent my-component [req ^:long int-argument ^:boolean boolean-argument] ...)
```

You may also cast within the body of `defcomponent`

```clojure
[:div
  (if ^:boolean (value "grumpy")
    "Cheer up!"
    "How are you?")]
```

Casts available include the following

- **^:long** Casts to long
- **^:long-option** Casts to long (ignores empty string)
- **^:double** Casts to double
- **^:double-option** Casts to double (ignores empty string)
- **^:longs** Casts to array of longs
- **^:doubles** Casts to array of doubles
- **^:array** Puts into an array
- **^:boolean** True when `(= argument "true")`
- **^:boolean-true** True when `(not= argument "false")`
- **^:edn** Reads string into edn
- **^:keyword** Casts to keyword

### Transforming parameters to JSON

htmx submits all parameters as a flat map, however we can use the above `path` scheme to transform it into nested json for database access etc.  Simply call `ctmx.form/json-params`

```clojure
(json-params
  {:store-name "My Store"
   :customers_0_first-name "Joe"
   :customers_0_last-name "Smith"
   :customers_1_first-name "Jane"
   :customers_1_last-name "Doe"})

;; {:store-name "My Store"
;;  :customers [{:first-name "Joe" :last-name "Smith"}
;;              {:first-name "Jane" :last-name "Doe"}]}
```

`ctmx.form/flatten-json` reflattens the nested structure.

### Prebind

Prebind is applied to req *before* the arguments are bound on `defcomponent`.  It can be used in the following way

```clojure
(defcomponent ^{:req my-prebind} my-component [req arg1 arg2] ...)
```

Prebind can be applied in different ways

- **^{:req prebind}** prebind is applied to entire req object `(prebind req)`
- **^{:params prebind}** prebind is applied to the **JSON Nested** params.  `(prebind json-params req)`
- **^{:params-stack prebind}** prebind is applied to the **JSON Nested** params at the current point in the component stack.

For components with multiple arguments, prebind will not be applied when the multi-arg version is invoked.

### Additional Parameters

In most cases htmx will supply all required parameters.  If you need to include extra ones, set the `hx-vals` attribute.  To serialize the map as json in initial page renders, you should call `ctmx.render/walk-attrs` on your returned html body ([example](https://github.com/whamtet/ctmx-demo/blob/57f9b3c55c8088dc5136b10f5ce1d66e9f6bd152/src/clj/htmx/render.clj#L32)).

```clojure
[:button.delete
  {:hx-delete "trash-can"
   :hx-vals {:hard-delete true}}
   "Delete"]
```

### Commands

Commands provide a shorthand to indicate custom actions.

```clojure
(defcomponent ^:endpoint component [req command]
  (case command
    "print" (print req)
    "save" (save req))
  [:div
    [:button {:hx-post "component:print"} "Print"]
    [:button {:hx-post "component:save"} "Save"]])
```

`command` will be bound to the value after the colon in any endpoints.

### Action at a distance (hx-swap-oob)

Best to avoid, but sometimes too convenient to resist.  htmx provides the `hx-swap-oob` attribute for updating multiple dom elements within a single response.  In ctmx we must only provide the additional elements when htmx is updating, not in the initial render

```clojure
(defcomponent my-component [req]
  (list
    (when top-level?
      [:div.side-element
       {:id (path "path/to/side-element")
        :hx-swap-oob "true"}
        ...])
    [:div.main-element {:id id} ...]))
```

Be very careful to only include `hx-swap-oob` elements when `top-level?` is true.

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

### Hanging Components

If you don't include components in an initial render, reference them as symbols so they are still available as endpoints.

```clojure
(defcomponent ^:endpoint next-month [req] [:p "next-month"])
(defcomponent ^:endpoint previous-month [req] [:p "previous-month"])

(defcomponent ^:endpoint calendar [req]
              next-month
              previous-month
              [:div#calendar ...])
```

### Extra hints

htmx does not include disabled fields when submitting requests.  If you wish to retain state in this case use the following pattern.

```clojure
[:input {:type "text" :name (path "input") :value (value "input") :disabled disabled?}]
(when disabled?
  [:input {:type "hidden" :name (path "input") :value (value "input")}])
```
## License

Copyright © 2022 Matthew Molloy

This program and the accompanying materials are made available under the
terms of the Eclipse Public License 2.0 which is available at
http://www.eclipse.org/legal/epl-2.0.

This Source Code may also be made available under the following Secondary
Licenses when the conditions for such availability set forth in the Eclipse
Public License, v. 2.0 are satisfied: GNU General Public License as published by
the Free Software Foundation, either version 2 of the License, or (at your
option) any later version, with the GNU Classpath Exception which is available
at https://www.gnu.org/software/classpath/license.html.
