(ns tggreene.inertia-cljs.impl.react17
  (:require ["react-dom" :as react-dom]))

(defn renderer
  [{:keys [root-component root-props]
    {:keys [App el props]} :inertia-props}]
  (let [component
        (cond->> (react/createElement App props)
          root-component (react/createElement
                          root-component
                          (clj->js root-props)))]
    (react-dom/render component el)))
