(ns simpleui.core-test
  (:require [clojure.test :refer :all]
            [simpleui.core :as simpleui :refer [defcomponent with-commands]]
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

(defcomponent ^:endpoint prefix-component [req]
  [:div {:hx-get "prefix-component"} (-> req :src-params :hi)])

(def handler (simpleui/make-routes
              "/base"
              (fn [req]
                (b req))))

(def handler-simple (simpleui/make-routes-simple "http://prefix/" [] prefix-component))
(def prefix-get (assoc-in (mock/request :get "/prefix-component")
                          [:headers "hx-current-url"] "https://www.example.com?hi=there"))

(deftest simple-handler-test
  (testing "simple handler works"
    (is
     (ok?
      "<div hx-get=\"http://prefix/prefix-component\">there</div>"
      (test-req
       handler-simple
       prefix-get)))))

(def unslash-request (mock/request :get "/base"))

(deftest component-test
  (testing "redirects to slash ending without server-name"
           (is
            (redirect?
             "/base/?"
             (test-req
              handler
              unslash-request))))
  (testing "redirects to slash ending with server-name"
           (is
             (redirect?
              "https://example.com/base/?"
              (test-req
               handler
               (assoc unslash-request :server-name "example.com")))))
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

(deftest with-commands-test
  (testing "with-commands"
    (let [req (mock/request :get "/")
          command "make"]
      (is (= [true false true false]
             (with-commands req
               [:div
                [:div {:hx-get "bike:make"}]
                [:div {:get-post "bike:break"}]]
               [get? post? make? break?]))))))
