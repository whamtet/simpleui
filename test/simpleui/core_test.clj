(ns simpleui.core-test
  (:require [clojure.test :refer :all]
            [simpleui.core :as simpleui :refer [defcomponent]]
            reitit.ring
            [ring.mock.request :as mock]))

;; helper functions

(defn- test-req [raw-handler req]
  ((-> raw-handler reitit.ring/router reitit.ring/ring-handler) req))

(defn- redirect? [^String redirect-path {:keys [status headers]}]
  (and
   (= 302 status)
   headers
   (-> "Location" headers (= redirect-path))))

(defn- ok? [^String test-body {:keys [status body]}]
  (and
   (= 200 status)
   (= test-body body)))

(defn- no-content? [{:keys [status]}]
  (= 204 status))

;; end helper functions

(defcomponent ^:endpoint c [req])
(defcomponent ^:endpoint a [req]
  [:a])
(defcomponent b [req]
  c
  [:b (a req)])

(def handler (simpleui/make-routes
              "/base"
              (fn [req]
                (b req))))

(def handler-simple (simpleui/make-routes-simple [] a))

(deftest simple-handler-test
  (testing "simple handler works"
    (is
     (ok?
      "<a></a>"
      (test-req
       handler-simple
       (mock/request :get "/a"))))))

(deftest component-test
  (testing "redirects to slash ending"
           (is
            (redirect?
             "/base/?"
             (test-req
              handler
              (mock/request :get "/base")))))
  (testing "initial render works"
           (is
            (= [:b [:a]]
               (test-req
                handler
                (mock/request :get "/base/")))))
  (testing "a endpoint works"
           (is
            (ok?
             "<a></a>"
             (test-req
              handler
              (mock/request :get "/base/a")))))
  (testing "c endpoint works"
           (is
            (no-content?
             (test-req
              handler
              (mock/request :get "/base/c")))))
  (testing "no b endpoint"
           (is
            (nil?
             (test-req
              handler
              (mock/request :get "/base/b"))))))

(defcomponent longer-component [req a b]
  [a b])

(deftest apply-component-test
  (testing "apply-component"
    (is
     (= [1 nil]
        (simpleui/apply-component longer-component
                                  (mock/request :get "/")
                                  1))))
  (testing "apply-component-map"
    (is
     (= [1 2]
        (simpleui/apply-component-map longer-component
                                      {:a 1 :b 2}
                                      (mock/request :get "/"))))
    (is
     (= [2 2]
        (simpleui/apply-component-map longer-component
                                      {:a 1 :b 2}
                                      (mock/request :get "/")
                                      2)))))

(deftest defcheck
  (testing "defcheck"
    (is (simpleui/get? (mock/request :get "/")))
    (is (simpleui/post? (mock/request :post "/")))
    (is (not (simpleui/get? (mock/request :post "/"))))
    (is (not (simpleui/post? (mock/request :get "/"))))))