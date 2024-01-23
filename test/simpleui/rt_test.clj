(ns simpleui.rt-test
  (:require [clojure.test :refer :all]
            [simpleui.rt :as rt]))

(deftest parsing-test
  (testing "long parse"
           (is (= 1 (rt/parse-long "1 ")))
           (is (= 1 (rt/parse-long 1)))
           (is (nil? (rt/parse-long nil)))
           (is (thrown? NumberFormatException (rt/parse-long ""))))
  (testing "long option parse"
           (is (= 1 (rt/parse-long-option "1 ")))
           (is (= 1 (rt/parse-long 1)))
           (is (nil? (rt/parse-long nil)))
           (is (nil? (rt/parse-long-option ""))))
  (testing "double parse"
           (is (= 1.5 (rt/parse-double "1.5 ")))
           (is (= 1.5 (rt/parse-double 1.5)))
           (is (nil? (rt/parse-double nil)))
           (is (thrown? NumberFormatException (rt/parse-double ""))))
  (testing "double option parse"
           (is (= 1.5 (rt/parse-double-option "1.5 ")))
           (is (= 1.5 (rt/parse-double-option 1.5)))
           (is (nil? (rt/parse-double-option nil)))
           (is (nil? (rt/parse-double-option ""))))
  (testing "longs parse"
           (is (= [1] (rt/parse-longs "1 ")))
           (is (= [1] (rt/parse-longs [1])))
           (is (= [1 2] (rt/parse-longs ["1 " "2 "])))
           (is (= () (rt/parse-longs nil)))
           (is (thrown? NumberFormatException (rt/parse-longs ""))))
  (testing "doubles parse"
           (is (= [1.5] (rt/parse-doubles "1.5 ")))
           (is (= [1.5] (rt/parse-doubles [1.5])))
           (is (= [1.5 2.5] (rt/parse-doubles ["1.5 " "2.5 "])))
           (is (= () (rt/parse-doubles nil)))
           (is (thrown? NumberFormatException (rt/parse-doubles ""))))
  (testing "array parse"
           (is (= ["hi"] (rt/parse-array "hi")))
           (is (= ["hi" "there"] (rt/parse-array ["hi" "there"])))
           (is (nil? (rt/parse-array nil))))
  (testing "boolean parse"
           (is (= true (rt/parse-boolean "true")))
           (is (= true (rt/parse-boolean "on")))
           (is (= true (rt/parse-boolean true)))
           (is (= false (rt/parse-boolean false)))
           (is (= false (rt/parse-boolean nil))))
  (testing "boolean-true parse"
           (is (= true (rt/parse-boolean-true "true")))
           (is (= true (rt/parse-boolean-true "on")))
           (is (= true (rt/parse-boolean-true nil)))
           (is (= true (rt/parse-boolean-true true)))
           (is (= false (rt/parse-boolean-true false)))
           (is (= false (rt/parse-boolean-true "false"))))
  (testing "edn parse"
           (is (= {:a 3} (rt/parse-edn "{:a 3}")))
           (is (= {:a 3} (rt/parse-edn {:a 3})))
           (is (= nil (rt/parse-edn nil)))
           (is (thrown? RuntimeException (rt/parse-edn ""))))
  (testing "nullable parse"
           (is (= nil (rt/parse-nullable "null")))
           (is (= nil (rt/parse-nullable "nil"))))
  (testing "keyword parse"
           (is (= :hi (rt/parse-kw "hi")))
           (is (= :hi (rt/parse-kw :hi)))
           (is (= nil (rt/parse-kw nil)))))

