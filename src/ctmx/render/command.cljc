(ns ctmx.render.command)

;; when the hx-* endpoint is of the form endpoint:command
;; we shall split on the colon and add command to hx-vals

(defn- assoc-command [m verb]
  (if-let [endpoint (m verb)]
    (let [[endpoint command] (.split endpoint ":")]
      (if command
        (-> m
            (update :hx-vals #(if (string? %) % (assoc % :command command)))
            (assoc verb endpoint))
        m))
    m))

(defn assoc-commands [m]
  (reduce
    assoc-command
    m
    [:hx-get
     :hx-post
     :hx-put
     :hx-patch
     :hx-delete]))
