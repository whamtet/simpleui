(ns ctmx.core-test
  (:require [clojure.test :refer :all]
            [ctmx.core :as ctmx :refer [defcomponent]]
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

;; end helper functions

(defcomponent ^:endpoint a [req]
  [:a])
(defcomponent ^:endpoint b [req]
  [:b (a req)])

(def handler (ctmx/make-routes
              "/base"
              (fn [req]
                (b req))))

(def true-base-request
  (assoc
   (mock/request :get "") :uri ""))

(deftest component-test
  (let []
    (testing "basic routing works"
             (is
              (redirect?
               "/base/?"
               (test-req
                handler
                (mock/request :get "/base")))))))
