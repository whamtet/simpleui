(ns demo.env
  (:require [clojure.tools.logging :as log]))

(def defaults
  {:init
   (fn []
     (log/info "\n-=[demo started successfully]=-"))
   :stop
   (fn []
     (log/info "\n-=[demo has shut down successfully]=-"))
   :middleware identity})
