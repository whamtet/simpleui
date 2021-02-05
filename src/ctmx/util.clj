(ns ctmx.util)

;; ignore rejections for now
(defn- some-step [g step]
  `(cond
     (nil? ~g) nil
     (.-then ~g) (.then ~g (fn [result#]
                             (if (nil? result#)
                               nil
                               (->> result# ~step))))
     :else (->> ~g ~step)))

(defmacro thread->>
  "When expr is not nil, threads it into the first form (via ->>),
  and when that result is not nil, through the next etc"
  {:added "1.5"}
  [expr & forms]
  (let [g (gensym)
        steps (map #(some-step g %) forms)]
    `(let [~g ~expr
           ~@(interleave (repeat g) (butlast steps))]
       ~(if (empty? steps)
          g
          (last steps)))))
