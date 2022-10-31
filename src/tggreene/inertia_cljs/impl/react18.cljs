(ns tggreene.inertia-cljs.impl.react18
  (:require
   ["react" :as react]
   ["react-dom/client" :as react-dom]))

(def root nil)

(defn renderer
  [{:keys [root-component root-props]
    {:keys [App el props]} :inertia-props}]
  (when-not root
    (set! root (react-dom/createRoot el)))
  (let [component
        (cond->> (react/createElement
                  App
                  props)
          root-component (react/createElement
                          root-component
                          (clj->js root-props)))]
    (.render root component)))
