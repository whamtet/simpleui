(ns simpleui.render.command)

(def hx-requests
  [:hx-get
   :hx-post
   :hx-put
   :hx-patch
   :hx-delete])

;; when the hx-* endpoint is of the form endpoint:command
;; we shall split on the colon and add command to hx-vals

(defn- clean-scheme [endpoint]
  (-> endpoint
      (.replace "http://" "")
      (.replace "https://" "")))

(defn- assoc-command [m verb]
  (if-let [endpoint (m verb)]
    (let [[endpoint command] (.split (clean-scheme endpoint) ":")]
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
    hx-requests))
