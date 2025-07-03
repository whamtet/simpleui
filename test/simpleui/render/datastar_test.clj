(ns simpleui.render.datastar-test
  (:require
    [clojure.string :as string]
    [clojure.test :refer :all]
    [simpleui.render.datastar :as datastar]
    [simpleui.response :as response]))

(defn snippet-response [x]
  (->> x
       (datastar/snippet-response-datastar "prefix")
       :body))

(deftest datastar-test
  (testing "empty body"
           (is (= response/no-content
                  (datastar/snippet-response-datastar "prefix" nil))))
  (testing "plain render"
           (is
            (=
             "event: datastar-merge-fragments\ndata: fragments <div>hi</div>"
             (snippet-response [:div "hi"]))))
  (testing "seq render"
           (is
            (=
             (snippet-response
              (list
               [:div {:a "b" :mergeMode :append :useViewTransition true} "hi"]
               [:merge-signals {:foo 1}]
               [:merge-signals true {:foo 1}]
               [:remove-fragments "#hi" "#there"]
               [:remove-signals "foo.bar" "baz"]
               [:script {:autoRemove true :type "module" :defer true} "console.log('hi')\nconsole.log('there')" "console.log('old')\nconsole.log('man')"]))
             (string/join "\n"
                          [
                            "event: datastar-merge-fragments\ndata: mergeMode append\ndata: useViewTransition true\ndata: fragments <div a=\"b\">hi</div>"
                            "event: datastar-merge-signals\ndata: signals {\"foo\":1}"
                            "event: datastar-merge-signals\ndata: onlyIfMissing true\ndata: signals {\"foo\":1}"
                            "event: datastar-remove-fragments\ndata: selector #hi\ndata: selector #there"
                            "event: datastar-remove-signals\ndata: paths foo.bar\ndata: paths baz"
                            "event: datastar-execute-script\ndata: autoRemove true\ndata: attributes :type module\ndata: attributes :defer true\ndata: script console.log('hi')\ndata: script console.log('there')\ndata: script console.log('old')\ndata: script console.log('man')"
                           ])))))
