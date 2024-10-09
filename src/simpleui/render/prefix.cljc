(ns simpleui.render.prefix)

(defn- prefix-verb [prefix]
  (fn [m verb]
    (if-let [endpoint (m verb)]
      (assoc m verb (str prefix endpoint))
      m)))

(defn prefix-verbs
  "Prefix cors host to hx- verbs"
  [prefix m]
  (reduce
   (prefix-verb prefix)
   m
   [:hx-get
    :hx-post
    :hx-put
    :hx-patch
    :hx-delete]))
