## Advanced usage

The following covers more experimental use of SimpleUI in dynamic forms.  Dynamic forms are those which grow as you fill them out, a good example is the profile builder on LinkedIn.

### relative paths

`path` and `value` are set based on the position of each component on the stack.  It is *ocassionally* useful to reference other components

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
