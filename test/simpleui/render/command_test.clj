(ns simpleui.render.command-test
  (:require
    [clojure.string :as string]
    [clojure.test :refer :all]
    [simpleui.render.command :as command]
    [simpleui.response :as response]))

(defn- get-command [x]
  (-> {:hx-get x}
      command/assoc-commands
      :hx-vals
      :command))

(deftest command-test
  (testing "relative urls"
           (is
            (= "hi" (get-command "hi:hi")))
           (is
            (nil? (get-command "hi"))))
  (testing "absolute urls"
           (is
            (= "hi" (get-command "/hi:hi")))
           (is
            (nil? (get-command "/hi"))))
  (testing "full urls"
           (is
            (= "hi" (get-command "https://www.google.com/hi:hi"))
            (nil? (get-command "https://www.google.com/hi")))))
