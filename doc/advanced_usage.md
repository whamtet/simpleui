## Advanced usage

The following covers more experimental use of SimpleUI in dynamic forms.  Dynamic forms are those which grow as you fill them out, a good example is the profile builder on LinkedIn.

### relative paths

`path` and `value` are set based on the position of each component on the stack.  It is sometimes useful to reference other components

```clojure
(value "subcomponent/parameter") ;; naughty naughty!
(value "../sibling-component/parameter")
````

Be careful when using `simpleui.rt/map-indexed`

```clojure
;; called from within array component
(value "../../sibling-component/parameter")
```

We need to ascend two levels in the stack because the array index counts as one level.  We can also use absolute paths for simple parameters not in the component stack.

```clojure
(when (= (value "/parameter-without-path") "do")
  ...)
```

### Transforming parameters to JSON

htmx submits all parameters as a flat map, however we can use the `path` components stack to transform it into nested json for database access etc.  Simply call `simpleui.form/json-params`

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

`simpleui.form/flatten-json` reflattens the nested structure.

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

### si-set, si-clear

SimpleUI contains complex state in forms.  On wizards and multistep forms these forms may disappear when we wish to retain the state.
To handle this situation create a 'stack' of hidden elements on initial page render

```clojure
[:input#first-name {:type "hidden"}]
[:input#second-name {:type "hidden"}]
...
```

When you proceed from one form to the next you may push onto the stack

```clojure
[:button {:hx-post "next-step"
          :si-set [:first-name :second-name]
          :si-set-class "current-step"}]
```

`first-name` and `second-name` exist in the current step but not the next, we wish to retain the state.
`si-set` will [oob-swap](https://htmx.org/attributes/hx-swap-oob/) `first-name` and `second-name` into the hidden `#first-name` and `#second-name` inputs
and set their class to `current-step`.  If we wish to return to this step in the wizard pop `first-name` and `second-name` back off the stack.

```clojure
[:button {:hx-post "previous-step"
          :hx-include ".current-step"
          :si-clear [:first-name :second-name]}]
```

`hx-include` class selects the `first-name` and `second-name` fields when rendering `previous-step` and `si-clear` clears the stack.
